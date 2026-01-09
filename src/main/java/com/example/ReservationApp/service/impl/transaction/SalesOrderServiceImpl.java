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
import com.example.ReservationApp.dto.response.inventory.InventoryStockDTO;
import com.example.ReservationApp.dto.transaction.SalesOrderDTO;
import com.example.ReservationApp.dto.transaction.SalesOrderDetailDTO;
import com.example.ReservationApp.entity.inventory.InventoryStock;
import com.example.ReservationApp.entity.product.Product;
import com.example.ReservationApp.entity.transaction.SalesOrder;
import com.example.ReservationApp.entity.transaction.SalesOrderDetail;
import com.example.ReservationApp.entity.user.User;
import com.example.ReservationApp.enums.OrderStatus;
import com.example.ReservationApp.enums.UserRole;
import com.example.ReservationApp.exception.InvalidActionException;
import com.example.ReservationApp.exception.InvalidCredentialException;
import com.example.ReservationApp.exception.NotFoundException;
import com.example.ReservationApp.exception.UnauthorizedException;
import com.example.ReservationApp.mapper.InventoryStockMapper;
import com.example.ReservationApp.mapper.SalesOrderDetailMapper;
import com.example.ReservationApp.mapper.SalesOrderMapper;
import com.example.ReservationApp.repository.inventory.InventoryStockRepository;
import com.example.ReservationApp.repository.product.ProductRepository;
import com.example.ReservationApp.repository.transaction.SalesOrderRepository;
import com.example.ReservationApp.service.impl.auth.UserServiceImpl;
import com.example.ReservationApp.service.transaction.SalesOrderDetailService;
import com.example.ReservationApp.service.transaction.SalesOrderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalesOrderServiceImpl implements SalesOrderService {

    private final SalesOrderRepository soRepository;
    private final ProductRepository productRepository;
    private final SalesOrderMapper soMapper;
    private final SalesOrderDetailMapper soDetailMapper;
    private final InventoryStockMapper inventoryStockMapper;
    private final UserServiceImpl userService;
    private final SalesOrderDetailService soDetailService;
    private final InventoryStockRepository inventoryStockRepository;

    /**
     * 新しい SalesOrder を作成する
     *
     * @param salesOrderDTO 作成する注文データ
     * @return 作成された SalesOrderDTO
     */
    @Override
    @Transactional
    public ResponseDTO<SalesOrderDTO> createSalesOrder(SalesOrderDTO salesOrderDTO) {

        // 顧客名の必須チェック
        if (salesOrderDTO.getCustomerName() == null || salesOrderDTO.getCustomerName().isBlank()) {
            throw new InvalidCredentialException("名前は必須です。");
        }

        // 現在ユーザー取得 & SalesOrder Entity作成
        User currentUser = userService.getCurrentUserEntity();
        SalesOrder so = soMapper.toEntity(salesOrderDTO);
        so.setCreatedBy(currentUser);
        so.setStatus(OrderStatus.NEW);

        // 注文詳細から商品IDリスト取得
        List<Long> productIds = salesOrderDTO.getDetails().stream()
                .map(SalesOrderDetailDTO::getProductId)
                .distinct()
                .toList();

        // DBから商品情報 & 在庫情報取得
        Map<Long, Product> productMap = productRepository.findAllById(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        Map<Long, List<InventoryStockDTO>> stockMap = inventoryStockRepository.findByProductIdIn(productIds)
                .stream()
                .map(stock -> inventoryStockMapper.toDTO(stock))
                .collect(Collectors.groupingBy(InventoryStockDTO::getProductId));

        Map<Long, SalesOrderDetail> detailMap = new HashMap<>();
        List<SalesOrderDetail> details = new ArrayList<>();

        // 注文詳細ごとのチェック・合計数量計算
        for (SalesOrderDetailDTO detailDTO : salesOrderDTO.getDetails()) {
            Product product = productMap.get(detailDTO.getProductId());
            if (product == null) {
                throw new NotFoundException("ID=" + detailDTO.getProductId() + "の商品は存在していません。");
            }
            // 同一商品の重複チェックと数量累積
            SalesOrderDetail existing = detailMap.get(product.getId());
            if (existing != null) {
                if (existing.getPrice().compareTo(detailDTO.getPrice()) != 0) {
                    throw new IllegalStateException(
                            "同一商品の単価が一致していません。 productId=" + product.getId());
                }
                existing.setQty(existing.getQty() + detailDTO.getQty());
            } else {
                SalesOrderDetail detail = soDetailMapper.toEntity(detailDTO);
                detail.setProduct(product);
                detail.setSalesOrder(so);
                details.add(detail);
                detailMap.put(product.getId(), detail);
            }

            // 在庫の有無チェック
            List<InventoryStockDTO> stocks = stockMap.get(product.getId());
            if (stocks == null || stocks.isEmpty()) {
                throw new InvalidCredentialException("在庫情報が存在しません。productId=" + product.getId());
            }

            // 在庫数計算（総数-予約済み） & 必要数チェック
            int total = stocks.stream().mapToInt(InventoryStockDTO::getQuantity).sum();
            int reserved = stocks.stream().mapToInt(InventoryStockDTO::getReservedQuantity).sum();
            int available = total - reserved;
            int required = detailMap.get(product.getId()).getQty();

            if (available < required) {
                throw new InvalidCredentialException(
                        "在庫が不足しています。productId=" + product.getId() +
                                ", required=" + required +
                                ", available=" + available);
            }
        }

        // 在庫予約処理
        for (SalesOrderDetail detail : details) {
            int remaining = detail.getQty();
            List<InventoryStockDTO> stocks = stockMap.get(detail.getProduct().getId());

            List<Long> stockIds = stocks.stream().map(InventoryStockDTO::getId).toList();
            List<InventoryStock> stockEntities = inventoryStockRepository.findAllByIdsWithWarehouse(stockIds);

            Map<Long, InventoryStock> stockByIdMap = stockEntities.stream()
                    .collect(Collectors.toMap(InventoryStock::getId, s -> s));
            for (InventoryStockDTO stockDTO : stocks) {
                int availableQty = stockDTO.getQuantity() - stockDTO.getReservedQuantity();
                int reserveQty = Math.min(availableQty, remaining);
                remaining -= reserveQty;

                InventoryStock stockEntity = stockByIdMap.get(stockDTO.getId());
                if (stockEntity == null)
                    throw new NotFoundException(
                            "在庫が見つかりません。stockId=" + stockDTO.getId());

                // 予約数量更新
                stockEntity.setReservedQuantity(stockEntity.getReservedQuantity() + reserveQty);
                inventoryStockRepository.save(stockEntity);

                if (remaining == 0)
                    break;
            }

            if (remaining > 0) {
                throw new InvalidCredentialException(
                        "在庫が不足しています。productId=" + detail.getProduct().getId());
            }
        }

        // SalesOrder に詳細と合計金額セット & 保存
        so.setDetails(details);
        so.setTotal(calcTotal(details));
        soRepository.save(so);

        // DTOに変換してレスポンス返却
        SalesOrderDTO soDTO = soMapper.toDTO(so);
        soDTO.setDetails(soDetailMapper.toDTOList(details));

        return ResponseDTO.<SalesOrderDTO>builder()
                .status(HttpStatus.OK.value())
                .message("販売注文が正常に作成されました。")
                .data(soDTO)
                .build();
    }

    /**
     * すべての販売注文（SalesOrder）を取得する。
     * 
     * @return 販売注文DTOのリストを含むResponseDTO
     */
    @Override
    public ResponseDTO<List<SalesOrderDTO>> getAllSalesOrders() {

        // SalesOrder + details + user を一括取得
        List<SalesOrder> salesOrders = soRepository.findAllWithDetailsAndUser();

        // Entity → DTO 変換、details も DTO に詰め込む
        List<SalesOrderDTO> salesOrderDTOs = salesOrders.stream()
                .map(so -> {
                    SalesOrderDTO soDTO = soMapper.toDTO(so);
                    List<SalesOrderDetailDTO> detailDTOs = soDetailMapper.toDTOList(so.getDetails());
                    soDTO.setDetails(detailDTOs);
                    return soDTO;
                }).toList();
        return ResponseDTO.<List<SalesOrderDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("販売注文書一覧が正常に取得されました。")
                .data(salesOrderDTOs)
                .build();
    }

    /**
     * 指定されたIDの販売注文書を取得する。
     *
     * @param salesOrderId 取得対象の販売注文書ID
     * @return ResponseDTO<SalesOrderDTO> 取得した販売注文書のDTOを含むレスポンス
     * @throws NotFoundException 指定されたIDの販売注文書が存在しない場合
     */
    @Override
    public ResponseDTO<SalesOrderDTO> getSalesOrderById(Long salesOrderId) {
        SalesOrder salesOrder = soRepository.findByIdWithDetails(salesOrderId)
                .orElseThrow(() -> new NotFoundException("この注文書は見つかりません。"));
        SalesOrderDTO salesOrderDTO = soMapper.toDTO(salesOrder);

        // 詳細は別取得
        List<SalesOrderDetailDTO> soDetailDTO = soDetailService.getDetailEntitysByOrder(salesOrderId);
        salesOrderDTO.setDetails(soDetailDTO);
        return ResponseDTO.<SalesOrderDTO>builder()
                .status(HttpStatus.OK.value())
                .message("販売注文書が正常に取得されました。")
                .data(salesOrderDTO)
                .build();
    }

    /**
     * 指定されたIDの販売注文書を更新する。
     * 
     * @param soId          更新対象の販売注文書ID
     * @param salesOrderDTO 更新内容を含むDTO
     * @return ResponseDTO<SalesOrderDTO> 更新後の販売注文書DTOを含むレスポンス
     * @throws NotFoundException 指定されたIDの販売注文書が存在しない場合
     */
    @Override
    @Transactional
    public ResponseDTO<SalesOrderDTO> updateSalesOrder(Long soId, SalesOrderDTO salesOrderDTO) {

        SalesOrder so = soRepository.findById(soId)
                .orElseThrow(() -> new NotFoundException("この販売注文書は存在していません。"));
        if (salesOrderDTO.getStatus() != null) {
            so.setStatus(salesOrderDTO.getStatus());
        }
        if (salesOrderDTO.getDescription() != null && !salesOrderDTO.getDescription().isBlank()) {
            so.setDescription(salesOrderDTO.getDescription());
        }
        soRepository.save(so);
        SalesOrderDTO soDTO = soMapper.toDTO(so);
        soDTO.setDetails(soDetailMapper.toDTOList(so.getDetails()));
        return ResponseDTO.<SalesOrderDTO>builder()
                .status(HttpStatus.OK.value())
                .message("販売注文書が正常に更新されました。")
                .data(soDTO)
                .build();
    }

    /**
     * 指定されたIDの販売注文書を削除する。
     *
     * @param soId 削除対象の販売注文書ID
     * @return ResponseDTO<Void> 削除成功メッセージを含むレスポンス
     * @throws NotFoundException 指定されたIDの販売注文書が存在しない場合
     */

    @Override
    public ResponseDTO<Void> deleteSalesOrder(Long soId) {

        SalesOrder so = soRepository.findById(soId)
                .orElseThrow(() -> new NotFoundException("この注文書は存在していません。"));
        User currentUser = userService.getCurrentUserEntity();

        if (!so.getStatus().equals(OrderStatus.NEW)) {
            throw new InvalidActionException("注文書はすでに処理済みのため削除できません。");
        }
        if (!so.getCreatedBy().equals(currentUser) && !currentUser.getRole().equals(UserRole.ADMIN)) {
            throw new UnauthorizedException("削除する権限がありません。");
        }
        soRepository.delete(so);
        return ResponseDTO.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("販売注文書が正常に削除されました。")
                .build();
    }

    /**
     * 販売注文書の明細リストから合計金額を計算する。
     *
     * @param details 販売注文書の明細リスト
     * @return BigDecimal 計算された合計金額
     */
    private BigDecimal calcTotal(List<SalesOrderDetail> details) {
        return details.stream()
                .map(d -> d.getPrice().multiply(BigDecimal.valueOf(d.getQty())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
