package com.example.ReservationApp.service.impl.transaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ReservationApp.dto.ResponseDTO;
import com.example.ReservationApp.dto.transaction.SalesOrderDTO;
import com.example.ReservationApp.dto.transaction.SalesOrderDetailDTO;
import com.example.ReservationApp.entity.supplier.SupplierProduct;
import com.example.ReservationApp.entity.transaction.SalesOrder;
import com.example.ReservationApp.entity.transaction.SalesOrderDetail;
import com.example.ReservationApp.enums.OrderStatus;
import com.example.ReservationApp.exception.InvalidCredentialException;
import com.example.ReservationApp.exception.NotFoundException;
import com.example.ReservationApp.mapper.SalesOrderDetailMapper;
import com.example.ReservationApp.repository.inventory.InventoryStockRepository;
import com.example.ReservationApp.repository.supplier.SupplierProductRepository;
import com.example.ReservationApp.repository.transaction.SalesOrderDetailRepository;
import com.example.ReservationApp.repository.transaction.SalesOrderRepository;
import com.example.ReservationApp.service.transaction.SalesOrderDetailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * SalesOrderDetailServiceImpl
 *
 * 販売注文詳細（SalesOrderDetail）に関するサービス実装クラス。
 * CRUD操作（追加・更新・削除・取得）と販売注文合計金額更新を担当する。
 *
 * ・SalesOrderDetail：各注文の詳細情報
 * ・SalesOrder：注文ヘッダー
 * ・Product：注文商品
 *
 * このクラスは以下の処理を行う：
 * 1. DTOからEntityへの変換
 * 2. DB保存および取得
 * 3. 関連エンティティとの紐付け
 * 4. 合計金額の計算と更新
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true) // デフォルトは read-only、書き込みメソッドでは個別に @Transactional を使用
public class SalesOrderDetailServiceImpl implements SalesOrderDetailService {

        private final SalesOrderDetailRepository soDetailRepository;
        private final SalesOrderRepository soRepository;
        private final SupplierProductRepository supplierProductRepository;
        private final SalesOrderDetailMapper soDetailMapper;
        private final InventoryStockRepository inventoryStockRepository;

        /**
         * SalesOrder に新しい detail を追加する。
         *
         * @param salesOrderId 対象の注文ID
         * @param soDetailDTO  追加する注文詳細DTO
         * @return 作成された SalesOrderDetailDTO
         */
        @Override
        @Transactional
        public ResponseDTO<SalesOrderDetailDTO> addDetail(Long salesOrderId, SalesOrderDetailDTO soDetailDTO) {

                // 商品ID・数量・単価の必須チェック
                if (soDetailDTO.getProductId() == null
                                || soDetailDTO.getQty() == null
                                || soDetailDTO.getPrice() == null) {
                        throw new InvalidCredentialException("商品ID、商品の数量、単価は必須です");
                }

                // 数量は正の値でなければならない
                if (soDetailDTO.getQty() <= 0) {
                        throw new InvalidCredentialException("数量は0より大きくなければなりません");
                }

                SalesOrder so = soRepository.findById(salesOrderId)
                                .orElseThrow(() -> new NotFoundException("この注文書が存在していません"));

                // NEWステータスのみ編集可能
                if (so.getStatus() != OrderStatus.NEW) {
                        throw new IllegalStateException("この注文書は編集できません");
                }

                // Product product = productRepository.findById(soDetailDTO.getProductId())
                //                 .orElseThrow(() -> new NotFoundException("この商品は存在していません"));

                SupplierProduct supplierProduct = supplierProductRepository.findBySupplierSku(soDetailDTO.getSku())
                                .orElseThrow(() -> new NotFoundException("SKUが存在しません: " + soDetailDTO.getSku()));

                // 既存明細があるか確認（同一注文＋同一商品）
                // Optional<SalesOrderDetail> existingProductInDetails = soDetailRepository
                //                 .findBySalesOrderIdAndProductId(
                //                                 salesOrderId,
                //                                 product.getId());

                Optional<SalesOrderDetail> existingDetails = soDetailRepository
                                .findBySalesOrderIdAndSupplierProductId(salesOrderId, supplierProduct.getId());
                SalesOrderDetail soDetail;
                int addedQty = soDetailDTO.getQty();

                if (existingDetails.isPresent()) {
                        // 既存明細がある場合
                        soDetail = existingDetails.get();

                        // 単価不一致チェック
                        if (soDetail.getPrice().compareTo(soDetailDTO.getPrice()) != 0) {
                                throw new IllegalStateException("同一商品の単価が一致していません");
                        }

                        // 数量加算
                        soDetail.setQty(soDetail.getQty() + addedQty);
                        soDetailRepository.save(soDetail);
                } else {
                        // 新規明細作成
                        soDetail = SalesOrderDetail.builder()
                                        .salesOrder(so)
                                        .product(supplierProduct.getProduct())
                                        .supplierProduct(supplierProduct)
                                        .qty(addedQty)
                                        .price(soDetailDTO.getPrice())
                                        .build();
                        soDetailRepository.save(soDetail);
                        so.getDetails().add(soDetail);
                }

                // 在庫チェック（全倉庫合計
                // int available = inventoryStockRepository.getAvailableStock(product.getId());
                int available = inventoryStockRepository.getAvailableStockBySku(supplierProduct.getSupplierSku());
                if (available < addedQty) {
                        throw new InvalidCredentialException(
                                        "在庫が不足しています。SKU=" + supplierProduct.getSupplierSku()
                                                        + ", required=" + addedQty
                                                        + ", available=" + available);
                }

                // 在庫引当（倉庫は特定しない
                inventoryStockRepository.reserveStockBySku(
                                supplierProduct.getSupplierSku(),
                                addedQty);

                // 注文合計金額再計算
                updateTotal(so);
                return ResponseDTO.<SalesOrderDetailDTO>builder()
                                .status(HttpStatus.OK.value())
                                .message("販売の詳細が正常に追加されました")
                                .data(soDetailMapper.toDTO(soDetail))
                                .build();
        }

        /**
         * SalesOrderDetail の更新
         *
         * @param detailId    更新対象の SalesOrderDetail ID
         * @param soDetailDTO 更新内容を含む DTO
         * @return 更新された SalesOrderDetailDTO
         */
        @Override
        @Transactional
        public ResponseDTO<SalesOrderDetailDTO> updateDetail(Long detailId, SalesOrderDetailDTO soDetailDTO) {

                log.info("Updating PurchaseOrderDetail ID = {}", detailId);
                SalesOrderDetail updatedSoDetail = soDetailRepository.findById(detailId)
                                .orElseThrow(() -> new NotFoundException("この販売の詳細は存在していません"));

                if (soDetailDTO.getPrice() != null) {
                        updatedSoDetail.setPrice(soDetailDTO.getPrice());
                }
                if (soDetailDTO.getQty() != null) {
                        updatedSoDetail.setQty(soDetailDTO.getQty());
                }
                soDetailRepository.save(updatedSoDetail);
                updateTotal(updatedSoDetail.getSalesOrder());

                return ResponseDTO.<SalesOrderDetailDTO>builder()
                                .status(HttpStatus.OK.value())
                                .message("販売の詳細が正常に更新されました")
                                .data(soDetailMapper.toDTO(updatedSoDetail))
                                .build();
        }

        @Override
        public ResponseDTO<Void> deleteDetail(Long detailId) {
                SalesOrderDetail soDetail = soDetailRepository.findById(detailId)
                                .orElseThrow(() -> new NotFoundException("この販売の詳細は存在していません"));
                SalesOrder po = soDetail.getSalesOrder();
                po.getDetails().remove(soDetail);

                soDetailRepository.delete(soDetail);
                updateTotal(po);
                return ResponseDTO.<Void>builder()
                                .status(HttpStatus.OK.value())
                                .message("販売の詳細が正常に削除されました")
                                .build();
        }

        @Override
        public ResponseDTO<List<SalesOrderDetailDTO>> getBySalesOrderId(Long salesOrderId) {

                SalesOrder so = soRepository.findById(salesOrderId)
                                .orElseThrow(() -> new NotFoundException("この注文書は存在していません"));
                List<SalesOrderDetail> soDetails = soDetailRepository.findBySalesOrder(so);

                return ResponseDTO.<List<SalesOrderDetailDTO>>builder()
                                .status(HttpStatus.OK.value())
                                .message("詳細情報の取得に成功しました")
                                .data(soDetailMapper.toDTOList(soDetails))
                                .build();
        }

        @Override
        public List<SalesOrderDetailDTO> getDetailEntitysByOrder(Long salesOrderId) {

                SalesOrder so = soRepository.findById(salesOrderId)
                                .orElseThrow(() -> new NotFoundException("この注文書は存在していません"));
                List<SalesOrderDetail> soDetails = soDetailRepository.findBySalesOrder(so);
                return soDetailMapper.toDTOList(soDetails);
        }

        @Override
        public List<SalesOrderDetailDTO> getAllDetailEntitys(List<SalesOrderDTO> salesOrderDTOs) {
                if (salesOrderDTOs == null || salesOrderDTOs.isEmpty()) {
                        return List.of();
                }
                List<Long> salesOrderIds = salesOrderDTOs.stream()
                                .map(SalesOrderDTO::getId)
                                .toList();
                List<SalesOrderDetail> soDetails = soDetailRepository.findBySalesOrderIdIn(salesOrderIds);

                return soDetailMapper.toDTOList(soDetails);
        }

        /**
         * SalesOrderの合計金額更新
         * 書き込みメソッドから呼ばれる
         */
        private void updateTotal(SalesOrder so) {
                BigDecimal total = so.getDetails().stream()
                                .map(d -> d.getPrice().multiply(BigDecimal.valueOf(d.getQty())))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                so.setTotal(total);
                // soRepository.save(so);
        }
}
