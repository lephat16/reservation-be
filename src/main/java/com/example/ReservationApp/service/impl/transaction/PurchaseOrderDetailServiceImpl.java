package com.example.ReservationApp.service.impl.transaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ReservationApp.dto.ResponseDTO;
import com.example.ReservationApp.dto.response.transaction.PurchaseOrderDetailWithSkuFlatDTO;
import com.example.ReservationApp.dto.response.transaction.PurchasesProcessingOrderWithRemainingQtyFlatDTO;
import com.example.ReservationApp.dto.transaction.PurchaseOrderDTO;
import com.example.ReservationApp.dto.transaction.PurchaseOrderDetailDTO;
import com.example.ReservationApp.entity.product.Product;
import com.example.ReservationApp.entity.transaction.PurchaseOrder;
import com.example.ReservationApp.entity.transaction.PurchaseOrderDetail;
import com.example.ReservationApp.enums.OrderStatus;
import com.example.ReservationApp.exception.InvalidCredentialException;
import com.example.ReservationApp.exception.NotFoundException;
import com.example.ReservationApp.mapper.PurchaseOrderDetailMapper;
import com.example.ReservationApp.repository.product.ProductRepository;
import com.example.ReservationApp.repository.transaction.PurchaseOrderDetailRepository;
import com.example.ReservationApp.repository.transaction.PurchaseOrderRepository;
import com.example.ReservationApp.service.transaction.PurchaseOrderDetailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * PurchaseOrderDetailServiceImpl
 *
 * 購入注文詳細（PurchaseOrderDetail）に関するサービス実装クラス。
 * CRUD操作（追加・更新・削除・取得）と購入注文合計金額更新を担当する。
 *
 * ・PurchaseOrderDetail：各注文の詳細情報
 * ・PurchaseOrder：注文ヘッダー
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
public class PurchaseOrderDetailServiceImpl implements PurchaseOrderDetailService {
        private final PurchaseOrderDetailRepository poDetailRepository;
        private final PurchaseOrderRepository poRepository;
        private final ProductRepository productRepository;
        private final PurchaseOrderDetailMapper poDetailMapper;

        /**
         * 注文詳細を新規追加する
         *
         * @param purchaseOrderId 注文ID（親PurchaseOrderのID）
         * @param poDetailDTO     追加する注文詳細DTO
         * @return 作成されたPurchaseOrderDetailDTOを含むResponseDTO
         */
        @Override
        @Transactional
        public ResponseDTO<PurchaseOrderDetailDTO> addDetail(Long purchaseOrderId, PurchaseOrderDetailDTO poDetailDTO) {
                // 必須項目チェック
                if (poDetailDTO.getProductId() == null
                                || poDetailDTO.getQty() == null
                                || poDetailDTO.getCost() == null) {
                        throw new InvalidCredentialException("商品ID、商品の数量、単価は必須です。");
                }
                if (poDetailDTO.getQty() <= 0) {
                        throw new InvalidCredentialException("数量は0より大きくなければなりません。");
                }
                // PurchaseOrder, Product存在確認
                PurchaseOrder po = poRepository.findById(purchaseOrderId)
                                .orElseThrow(() -> new NotFoundException("この注文書が存在していません。"));
                if (po.getStatus() != OrderStatus.NEW) {
                        throw new IllegalStateException("この注文書は編集できません。");
                }
                Product product = productRepository.findById(poDetailDTO.getProductId())
                                .orElseThrow(() -> new NotFoundException("この商品は存在していません。"));

                Optional<PurchaseOrderDetail> existingProductInDetails = poDetailRepository
                                .findByPurchaseOrderIdAndProductId(
                                                purchaseOrderId,
                                                product.getId());

                PurchaseOrderDetail poDetail;

                if (existingProductInDetails.isPresent()) {
                        poDetail = existingProductInDetails.get();
                        if (poDetail.getCost().compareTo(poDetailDTO.getCost()) != 0) {
                                throw new IllegalStateException("同一商品の単価が一致していません。");
                        }
                        poDetail.setQty(poDetail.getQty() + poDetailDTO.getQty());
                        poDetailRepository.save(poDetail);
                } else {
                        poDetail = new PurchaseOrderDetail();
                        poDetail.setPurchaseOrder(po);
                        poDetail.setProduct(product);
                        poDetail.setQty(poDetailDTO.getQty());
                        poDetail.setCost(poDetailDTO.getCost());
                        poDetailRepository.save(poDetail);
                        po.getDetails().add(poDetail);
                }

                updateTotal(po);

                return ResponseDTO.<PurchaseOrderDetailDTO>builder()
                                .status(HttpStatus.OK.value())
                                .message("購入明細が正常に追加されました。")
                                .data(poDetailMapper.toDTO(poDetail))
                                .build();
        }

        /**
         * 注文詳細を更新する
         *
         * @param detailId    更新するPurchaseOrderDetailのID
         * @param poDetailDTO 更新内容（数量や単価など）
         * @return 更新後のPurchaseOrderDetailDTOを含むResponseDTO
         * 
         */
        @Override
        @Transactional
        public ResponseDTO<PurchaseOrderDetailDTO> updateDetail(Long detailId, PurchaseOrderDetailDTO poDetailDTO) {

                log.info("Updating PurchaseOrderDetail ID = {}", detailId);
                PurchaseOrderDetail updatedPoDetail = poDetailRepository.findById(detailId)
                                .orElseThrow(() -> new NotFoundException("この購入の詳細は存在していません。"));

                if (poDetailDTO.getCost() != null) {
                        updatedPoDetail.setCost(poDetailDTO.getCost());
                }
                if (poDetailDTO.getQty() != null) {
                        updatedPoDetail.setQty(poDetailDTO.getQty());
                }
                poDetailRepository.save(updatedPoDetail);
                updateTotal(updatedPoDetail.getPurchaseOrder());

                return ResponseDTO.<PurchaseOrderDetailDTO>builder()
                                .status(HttpStatus.OK.value())
                                .message("購入の詳細が正常に更新されました。")
                                .data(poDetailMapper.toDTO(updatedPoDetail))
                                .build();
        }

        @Override
        @Transactional
        public ResponseDTO<PurchaseOrderDetailDTO> updateDetailQuantity(Long detailId, int newQty) {

                log.info("Updating PurchaseOrderDetail ID = {}", detailId);
                PurchaseOrderDetail updatedPoDetail = poDetailRepository.findById(detailId)
                                .orElseThrow(() -> new NotFoundException("指定された購入注文の明細は存在しません。"));

                PurchaseOrder po = updatedPoDetail.getPurchaseOrder();
                // ステータスチェック
                if (po.getStatus() != OrderStatus.NEW) {
                        throw new IllegalStateException("購入注文がNEWの状態でないため、数量を変更できません。");
                }
                // 数量更新
                updatedPoDetail.setQty(newQty);
                poDetailRepository.save(updatedPoDetail);

                updateTotal(updatedPoDetail.getPurchaseOrder());

                return ResponseDTO.<PurchaseOrderDetailDTO>builder()
                                .status(HttpStatus.OK.value())
                                .message("購入注文明細の数量が正常に更新されました。")
                                .data(poDetailMapper.toDTO(updatedPoDetail))
                                .build();
        }

        /**
         * 注文詳細を削除する
         *
         * @param detailId 削除するPurchaseOrderDetailのID
         * @return Voidを含むResponseDTO
         */
        @Override
        public ResponseDTO<Void> deleteDetail(Long detailId) {

                PurchaseOrderDetail poDetail = poDetailRepository.findById(detailId)
                                .orElseThrow(() -> new NotFoundException("この購入の詳細は存在していません。"));
                PurchaseOrder po = poDetail.getPurchaseOrder();
                po.getDetails().remove(poDetail);

                poDetailRepository.delete(poDetail);
                updateTotal(po);
                return ResponseDTO.<Void>builder()
                                .status(HttpStatus.OK.value())
                                .message("購入の詳細が正常に削除されました。")
                                .build();
        }

        /**
         * 指定注文書IDの詳細リストを取得
         *
         * @param purchaseOrderId 注文ID
         * @return DetailDTOのリストを含むResponseDTO
         */
        public ResponseDTO<List<PurchaseOrderDetailDTO>> getByPurchaseOrderId(Long purchaseOrderId) {

                PurchaseOrder po = poRepository.findById(purchaseOrderId)
                                .orElseThrow(() -> new NotFoundException("この注文書が存在していません。"));
                List<PurchaseOrderDetail> poDetails = poDetailRepository.findByPurchaseOrderFetchProduct(po);

                return ResponseDTO.<List<PurchaseOrderDetailDTO>>builder()
                                .status(HttpStatus.OK.value())
                                .message("詳細情報の取得に成功しました。")
                                .data(poDetailMapper.toDTOList(poDetails))
                                .build();
        }

        /**
         * 注文IDでDetailDTOリストを取得（Entity取得版）
         */
        @Override
        public List<PurchaseOrderDetailDTO> getDetailEntitysByOrder(Long purchaseOrderId) {

                PurchaseOrder po = poRepository.findById(purchaseOrderId)
                                .orElseThrow(() -> new NotFoundException("この注文書が存在していません。"));
                List<PurchaseOrderDetail> poDetails = poDetailRepository.findByPurchaseOrder(po);

                return poDetailMapper.toDTOList(poDetails);
        }

        /**
         * 複数注文DTOから全てのDetailDTOを取得
         */
        @Override
        public List<PurchaseOrderDetailDTO> getAllDetailEntitys(List<PurchaseOrderDTO> purchaseOrderDTOs) {

                if (purchaseOrderDTOs == null || purchaseOrderDTOs.isEmpty()) {
                        return List.of();
                }
                List<Long> purchaseOrderIds = purchaseOrderDTOs.stream()
                                .map(PurchaseOrderDTO::getId)
                                .toList();
                List<PurchaseOrderDetail> poDetails = poDetailRepository.findByPurchaseOrderIdIn(purchaseOrderIds);

                return poDetailMapper.toDTOList(poDetails);
        }

        /**
         * PurchaseOrderの合計金額更新
         * 書き込みメソッドから呼ばれる
         */
        private void updateTotal(PurchaseOrder po) {
                BigDecimal total = po.getDetails().stream()
                                .map(d -> d.getCost().multiply(BigDecimal.valueOf(d.getQty())))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                po.setTotal(total);
                // poRepository.save(po);
        }

        @Override
        public ResponseDTO<List<PurchaseOrderDetailDTO>> getByPurchaseOrderWithSku(Long purchaseOrderId) {

                List<PurchaseOrderDetailWithSkuFlatDTO> podFlat = poDetailRepository
                                .findDetailsWithSupplierSku(purchaseOrderId);

                List<PurchaseOrderDetailDTO> poDetails = podFlat.stream()
                                .map(row -> PurchaseOrderDetailDTO.builder()
                                                .id(row.getId())
                                                .qty(row.getQty())
                                                .cost(row.getCost())
                                                .productId(row.getProductId())
                                                .purchaseOrderId(row.getPurchaseOrderId())
                                                .status(OrderStatus.valueOf(row.getStatus()))
                                                .sku(row.getSupplierSku())
                                                .build())
                                .toList();
                return ResponseDTO.<List<PurchaseOrderDetailDTO>>builder()
                                .status(HttpStatus.OK.value())
                                .message("詳細情報の取得に成功しました。")
                                .data(poDetails)
                                .build();
        }

        @Override
        public ResponseDTO<List<PurchaseOrderDetailDTO>> getPurchaseProcessingDetailWithRemainingQty(
                        Long purchaseOrderId) {

                List<PurchasesProcessingOrderWithRemainingQtyFlatDTO> podFlat = poDetailRepository
                                .findProcessingDetailWithRemaingQty(purchaseOrderId);

                List<PurchaseOrderDetailDTO> poDetails = podFlat.stream()
                                .map(row -> PurchaseOrderDetailDTO.builder()
                                                .id(row.getDetailId())
                                                .productId(row.getProductId())
                                                .productName(row.getProductName())
                                                .sku(row.getSku())
                                                .orderedQty(row.getOrderedQty())
                                                .receivedQty(row.getReceivedQty())
                                                .remainingQty(row.getRemainingQty())
                                                .build())
                                .toList();
                return ResponseDTO.<List<PurchaseOrderDetailDTO>>builder()
                                .status(HttpStatus.OK.value())
                                .message("詳細情報の取得に成功しました。")
                                .data(poDetails)
                                .build();
        }

}
