package com.example.ReservationApp.service.impl.transaction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ReservationApp.dto.ResponseDTO;
import com.example.ReservationApp.dto.response.transaction.PurchaseOrderDetailWithSkuFlatDTO;
import com.example.ReservationApp.dto.transaction.PurchaseOrderDTO;
import com.example.ReservationApp.dto.transaction.PurchaseOrderDetailDTO;
import com.example.ReservationApp.entity.product.Product;
import com.example.ReservationApp.entity.supplier.Supplier;
import com.example.ReservationApp.entity.transaction.PurchaseOrder;
import com.example.ReservationApp.entity.transaction.PurchaseOrderDetail;
import com.example.ReservationApp.entity.user.User;
import com.example.ReservationApp.enums.OrderStatus;
import com.example.ReservationApp.enums.UserRole;
import com.example.ReservationApp.exception.InvalidActionException;
import com.example.ReservationApp.exception.NotFoundException;
import com.example.ReservationApp.exception.UnauthorizedException;
import com.example.ReservationApp.mapper.PurchaseOrderDetailMapper;
import com.example.ReservationApp.mapper.PurchaseOrderMapper;
import com.example.ReservationApp.repository.product.ProductRepository;
import com.example.ReservationApp.repository.supplier.SupplierRepository;
import com.example.ReservationApp.repository.transaction.PurchaseOrderDetailRepository;
import com.example.ReservationApp.repository.transaction.PurchaseOrderRepository;
import com.example.ReservationApp.service.impl.auth.UserServiceImpl;
import com.example.ReservationApp.service.transaction.PurchaseOrderDetailService;
import com.example.ReservationApp.service.transaction.PurchaseOrderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 購入注文（Purchase Order）に関するサービス実装クラス。
 * 注文の作成、取得、更新、削除を行う。
 *
 * ・Supplier（仕入れ先）
 * ・User（作成者）
 * ・PurchaseOrderDetail（注文詳細）
 *
 * これらを一括して処理するビジネスロジックを担当する。
 */

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PurchaseOrderServiceImpl implements PurchaseOrderService {
        private final PurchaseOrderRepository purchaseOrderRepository;
        private final PurchaseOrderDetailRepository poDetaiRepository;
        private final SupplierRepository supplierRepository;
        private final UserServiceImpl userService;
        private final PurchaseOrderMapper purchaseOrderMapper;
        private final PurchaseOrderDetailMapper poDetailMapper;
        private final PurchaseOrderDetailService poDetailService;
        private final ProductRepository productRepository;

        /**
         * 購入注文を新規作成する。
         *
         * @param purchaseOrderDTO 注文データ（DTO）
         * @return 作成された注文のレスポンスDTO
         *
         *         処理内容：
         *         - 仕入れ先の存在チェック
         *         - ログインユーザー取得
         *         - PurchaseOrder エンティティ作成
         *         - 注文詳細の追加
         *         - 合計金額の計算
         */

        @Override
        @Transactional
        public ResponseDTO<PurchaseOrderDTO> createPurchaseOrder(PurchaseOrderDTO purchaseOrderDTO) {

                log.info("Creating PurchaseOrder... supplierId = {}", purchaseOrderDTO.getSupplierId());
                // 仕入れ先の取得（商品とカテゴリーも一括ロード）
                Supplier supplier = supplierRepository
                                .findSupplierWithProductsAndCategory(purchaseOrderDTO.getSupplierId())
                                .orElseThrow(() -> new NotFoundException("この仕入先は存在していません"));
                // 仕入れ先に商品が存在するかチェック
                if (supplier.getSupplierProducts() == null || supplier.getSupplierProducts().isEmpty()) {
                        throw new NotFoundException("この仕入先には商品がありません");
                }
                // ログインユーザー取得
                User currentUser = userService.getCurrentUserEntity();
                // DTO → Entity 変換
                PurchaseOrder po = purchaseOrderMapper.toEntity(purchaseOrderDTO);
                po.setSupplier(supplier);
                po.setCreatedBy(currentUser);
                po.setStatus(OrderStatus.NEW);

                Map<Long, PurchaseOrderDetail> detailMap = new HashMap<>();
                List<PurchaseOrderDetail> details = new ArrayList<>();
                for (PurchaseOrderDetailDTO detailDTO : purchaseOrderDTO.getDetails()) {
                        Product product = productRepository.findById(detailDTO.getProductId())
                                        .orElseThrow(() -> new NotFoundException("この商品は存在していません"));
                        PurchaseOrderDetail existing = detailMap.get(product.getId());
                        if (existing != null) {
                                if (existing.getCost().compareTo(detailDTO.getCost()) != 0) {
                                        throw new IllegalStateException(
                                                        "同一商品の単価が一致していません。 productId=" + product.getId());
                                }
                                existing.setQty(existing.getQty() + detailDTO.getQty());
                        } else {
                                PurchaseOrderDetail detail = poDetailMapper.toEntity(detailDTO);
                                detail.setProduct(product);
                                detail.setPurchaseOrder(po);
                                details.add(detail);
                                detailMap.put(product.getId(), detail);
                        }
                }

                po.setDetails(details);
                // 合計金額を計算して設定
                po.setTotal(calcTotal(details));
                purchaseOrderRepository.save(po);
                // DTO に変換
                PurchaseOrderDTO poDTO = purchaseOrderMapper.toDTO(po);
                poDTO.setDetails(poDetailMapper.toDTOList(details));
                return ResponseDTO.<PurchaseOrderDTO>builder()
                                .status(HttpStatus.OK.value())
                                .message("注文書が正常に作成されました")
                                .data(poDTO)
                                .build();

        }

        /**
         * すべての購入注文を取得する。
         * JOIN FETCH を使用して N+1 問題を回避し、
         * ユーザー・仕入先・詳細を一括ロードする。
         *
         * @return 購入注文DTOのリスト
         */
        @Override
        public ResponseDTO<List<PurchaseOrderDTO>> getAllPurchaseOrders() {
                // JOIN FETCH で注文 + user + supplier + details を一括取得
                List<PurchaseOrder> purchaseOrders = purchaseOrderRepository.findAllWithDetailsUserAndSupplier();
                // Entity → DTO へ変換し、さらに各注文書の details も DTO に詰め込む
                List<PurchaseOrderDTO> purchaseOrderDTOs = purchaseOrders.stream()
                                .map(po -> {
                                        // PurchaseOrder の基本情報を DTO にマッピング
                                        PurchaseOrderDTO poDTO = purchaseOrderMapper.toDTO(po);
                                        // PurchaseOrder に紐づく Detail List を DTO に変換
                                        List<PurchaseOrderDetailDTO> detailDTOs = poDetailMapper
                                                        .toDTOList(po.getDetails());
                                        poDTO.setDetails(detailDTOs);
                                        return poDTO;
                                }).toList();

                return ResponseDTO.<List<PurchaseOrderDTO>>builder()
                                .status(HttpStatus.OK.value())
                                .message("注文書一覧が正常に取得されました")
                                .data(purchaseOrderDTOs)
                                .build();
        }

        /**
         * IDを指定して購入注文を取得する。
         *
         * @param purchaseOrderId 注文ID
         * @return 購入注文DTO
         */
        @Override
        public ResponseDTO<PurchaseOrderDTO> getPurchaseOrderById(Long purchaseOrderId) {

                PurchaseOrder purchaseOrder = purchaseOrderRepository.findByIdWithDetails(purchaseOrderId)
                                .orElseThrow(() -> new NotFoundException("この注文書は見つかりません"));
                PurchaseOrderDTO purchaseOrderDTO = purchaseOrderMapper.toDTO(purchaseOrder);

                // 詳細は別取得
                List<PurchaseOrderDetailDTO> poDetailDTO = poDetailService.getDetailEntitysByOrder(purchaseOrderId);
                purchaseOrderDTO.setDetails(poDetailDTO);
                return ResponseDTO.<PurchaseOrderDTO>builder()
                                .status(HttpStatus.OK.value())
                                .message("注文書が正常に取得されました")
                                .data(purchaseOrderDTO)
                                .build();
        }

        /**
         * 購入注文の更新（ステータス・説明のみ）。
         *
         * @param poId             注文ID
         * @param purchaseOrderDTO 更新内容
         * @return 更新後のDTO
         */
        @Override
        @Transactional
        public ResponseDTO<PurchaseOrderDTO> updatePurchaseOrder(Long poId,
                        PurchaseOrderDTO purchaseOrderDTO) {

                PurchaseOrder po = purchaseOrderRepository.findById(poId)
                                .orElseThrow(() -> new NotFoundException("この注文書は存在していません"));
                if (purchaseOrderDTO.getStatus() != null) {
                        po.setStatus(purchaseOrderDTO.getStatus());
                }
                if (purchaseOrderDTO.getDescription() != null &&
                                !purchaseOrderDTO.getDescription().isBlank()) {
                        po.setDescription(purchaseOrderDTO.getDescription());
                }
                purchaseOrderRepository.save(po);
                PurchaseOrderDTO poDTO = purchaseOrderMapper.toDTO(po);
                poDTO.setDetails(poDetailMapper.toDTOList(po.getDetails()));
                return ResponseDTO.<PurchaseOrderDTO>builder()
                                .status(HttpStatus.OK.value())
                                .message("購入注文が正常に更新されました")
                                .data(purchaseOrderMapper.toDTO(po))
                                .build();
        }

        @Override
        @Transactional
        public ResponseDTO<PurchaseOrderDTO> updatePurchaseOrderQuantityAndDescription(Long poId,
                        PurchaseOrderDTO purchaseOrderDTO) {

                PurchaseOrder po = purchaseOrderRepository.findById(poId)
                                .orElseThrow(() -> new NotFoundException("この注文書は存在していません"));

                if (po.getStatus() != OrderStatus.NEW) {
                        throw new IllegalStateException("NEWの状態のみ更新可能です");
                }

                if (purchaseOrderDTO.getDescription() != null && !purchaseOrderDTO.getDescription().isBlank()) {
                        po.setDescription(purchaseOrderDTO.getDescription());
                }

                if (purchaseOrderDTO.getDetails() != null) {
                        Map<Long, PurchaseOrderDetail> detailMap = po.getDetails().stream()
                                        .collect(Collectors.toMap(detail -> detail.getProduct().getId(),
                                                        detail -> detail));

                        for (PurchaseOrderDetailDTO detailDTO : purchaseOrderDTO.getDetails()) {
                                PurchaseOrderDetail detail = detailMap.get(detailDTO.getProductId());
                                if (detail != null) {
                                        detail.setQty(detailDTO.getQty());
                                        poDetaiRepository.save(detail);
                                } else {
                                        throw new IllegalStateException(
                                                        "POに存在しない商品は更新できません。 sku=" + detailDTO.getSku());
                                }
                        }
                }
                po.setTotal(calcTotal(po.getDetails()));
                purchaseOrderRepository.save(po);
                PurchaseOrderDTO poDTO = purchaseOrderMapper.toDTO(po);
                poDTO.setDetails(poDetailMapper.toDTOList(po.getDetails()));
                return ResponseDTO.<PurchaseOrderDTO>builder()
                                .status(HttpStatus.OK.value())
                                .message("購入注文が正常に更新されました")
                                .data(poDTO)
                                .build();
        }

        /**
         * 購入注文を削除する。
         *
         * @param poId 注文ID
         * @return Voidレスポンス
         */
        @Override
        public ResponseDTO<Void> deletePurchaseOrder(Long poId) {

                PurchaseOrder po = purchaseOrderRepository.findById(poId)
                                .orElseThrow(() -> new NotFoundException("この注文書は存在していません"));

                User currentUser = userService.getCurrentUserEntity();
                if (!po.getStatus().equals(OrderStatus.NEW)) {
                        throw new InvalidActionException("注文書はすでに処理済みのため削除できません");
                }
                if (!po.getCreatedBy().equals(currentUser) && !currentUser.getRole().equals(UserRole.ADMIN)) {
                        throw new UnauthorizedException("削除する権限がありません");
                }

                purchaseOrderRepository.delete(po);
                return ResponseDTO.<Void>builder()
                                .status(HttpStatus.OK.value())
                                .message("購入注文が正常に削除されました")
                                .build();
        }

        private BigDecimal calcTotal(List<PurchaseOrderDetail> details) {
                return details.stream()
                                .map(d -> d.getCost().multiply(BigDecimal.valueOf(d.getQty())))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        @Override
        public ResponseDTO<PurchaseOrderDTO> getPurchaseOrderDetailsByPOIdWithSku(Long purchaseOrderId) {

                PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(purchaseOrderId)
                                .orElseThrow(() -> new NotFoundException("この注文書は存在していません"));
                List<PurchaseOrderDetailWithSkuFlatDTO> podFlat = poDetaiRepository
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
                                                .productName(row.getProductName())
                                                .build())
                                .toList();
                PurchaseOrderDTO purchaseOrderDTO = purchaseOrderMapper.toDTO(purchaseOrder);
                purchaseOrderDTO.setDetails(poDetails);
                return ResponseDTO.<PurchaseOrderDTO>builder()
                                .status(HttpStatus.OK.value())
                                .message("注文書が正常に取得されました")
                                .data(purchaseOrderDTO)
                                .build();
        }

        @Override
        public ResponseDTO<PurchaseOrderDTO> placeOrder(Long purchaseOrderId) {

                PurchaseOrder po = purchaseOrderRepository.findById(purchaseOrderId)
                                .orElseThrow(() -> new NotFoundException("この注文書は存在していません"));

                if (po.getStatus() != OrderStatus.NEW) {
                        throw new IllegalStateException("この注文書はすでに処理中です");
                }

                if (po.getDetails() == null || po.getDetails().isEmpty()) {
                        throw new IllegalStateException("この注文書には商品が含まれていません");
                }

                for (PurchaseOrderDetail detail : po.getDetails()) {
                        if (detail.getQty() == null || detail.getQty() <= 0) {
                                throw new IllegalStateException("商品「" + detail.getProduct().getName() + "」の数量が無効です");
                        }
                        if (detail.getCost() == null || detail.getCost().compareTo(BigDecimal.ZERO) <= 0) {
                                throw new IllegalStateException("商品「" + detail.getProduct().getName() + "」の単価が無効です");
                        }
                        detail.setStatus(OrderStatus.PENDING);
                }

                po.setStatus(OrderStatus.PENDING);
                purchaseOrderRepository.save(po);

                PurchaseOrderDTO poDTO = purchaseOrderMapper.toDTO(po);
                return ResponseDTO.<PurchaseOrderDTO>builder()
                                .status(HttpStatus.OK.value())
                                .message("注文書の状態が正常に更新されました")
                                .data(poDTO)
                                .build();

        }

}
