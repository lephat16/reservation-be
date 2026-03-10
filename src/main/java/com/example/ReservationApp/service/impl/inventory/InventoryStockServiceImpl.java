package com.example.ReservationApp.service.impl.inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ReservationApp.dto.ResponseDTO;
import com.example.ReservationApp.dto.notification.NotificationDTO;
import com.example.ReservationApp.dto.request.DeliverStockItemDTO;
import com.example.ReservationApp.dto.request.ReceiveStockItemDTO;
import com.example.ReservationApp.dto.response.inventory.DeliverStockResultDTO;
import com.example.ReservationApp.dto.response.inventory.InventoryStockDTO;
import com.example.ReservationApp.dto.response.inventory.ReceiveStockResultDTO;
import com.example.ReservationApp.dto.response.inventory.StockHistoryDTO;

import com.example.ReservationApp.entity.inventory.InventoryStock;
import com.example.ReservationApp.entity.inventory.StockHistory;
import com.example.ReservationApp.entity.inventory.Warehouse;
import com.example.ReservationApp.entity.supplier.SupplierProduct;
import com.example.ReservationApp.entity.transaction.PurchaseOrder;
import com.example.ReservationApp.entity.transaction.PurchaseOrderDetail;
import com.example.ReservationApp.entity.transaction.SalesOrder;
import com.example.ReservationApp.entity.transaction.SalesOrderDetail;
import com.example.ReservationApp.entity.user.User;
import com.example.ReservationApp.enums.NotificationType;
import com.example.ReservationApp.enums.OrderStatus;
import com.example.ReservationApp.enums.RefType;
import com.example.ReservationApp.enums.StockChangeType;
import com.example.ReservationApp.exception.InvalidCredentialException;
import com.example.ReservationApp.exception.NotFoundException;
import com.example.ReservationApp.mapper.InventoryStockMapper;
import com.example.ReservationApp.mapper.ProductMapper;
import com.example.ReservationApp.mapper.StockHistoryMapper;
import com.example.ReservationApp.mapper.SupplierProductMapper;
import com.example.ReservationApp.repository.inventory.InventoryStockRepository;
import com.example.ReservationApp.repository.inventory.StockHistoryRepository;
import com.example.ReservationApp.repository.inventory.WarehouseRepository;
import com.example.ReservationApp.repository.notification.NotificationRepository;
import com.example.ReservationApp.repository.supplier.SupplierProductRepository;
import com.example.ReservationApp.repository.transaction.PurchaseOrderDetailRepository;
import com.example.ReservationApp.repository.transaction.PurchaseOrderRepository;
import com.example.ReservationApp.repository.transaction.SalesOrderDetailRepository;
import com.example.ReservationApp.repository.transaction.SalesOrderRepository;
import com.example.ReservationApp.repository.user.UserRepository;
import com.example.ReservationApp.service.auth.UserService;
import com.example.ReservationApp.service.inventory.InventoryStockService;
import com.example.ReservationApp.service.notification.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 在庫管理サービスの実装クラスです。
 * 在庫の取得、増減、調整、受領、出庫などの業務ロジックを提供します。
 * StockHistoryServiceやリポジトリを使用してデータベース操作を行います。
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InventoryStockServiceImpl implements InventoryStockService {

    private final InventoryStockRepository inventoryStockRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryStockMapper inventoryStockMapper;
    private final StockHistoryMapper stockHistoryMapper;
    private final ProductMapper productMapper;
    private final SupplierProductMapper supplierProductMapper;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final PurchaseOrderDetailRepository poDetailRepository;
    private final SalesOrderDetailRepository salesOrderDetailRepository;
    private final StockHistoryRepository stockHistoryRepository;
    private final SupplierProductRepository supplierProductRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    /**
     * すべての在庫情報を取得します。
     * 
     * @return ResponseDTO<List<InventoryStockDTO>> 在庫一覧とHTTPステータス、メッセージを含むDTO
     */
    @Override
    public ResponseDTO<List<InventoryStockDTO>> getAllInventoryStocks() {

        List<InventoryStock> inventoryStocks = inventoryStockRepository.findAllWithRelations();
        List<InventoryStockDTO> inventoryStockDTOs = inventoryStockMapper.toDTOList(inventoryStocks);

        IntStream.range(0, inventoryStocks.size())
                .forEach(i -> {
                    inventoryStockDTOs.get(i).setStockHistories(
                            stockHistoryMapper.toDTOList(inventoryStocks.get(i).getStockHistories()));
                });
        return ResponseDTO.<List<InventoryStockDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("在庫一覧の取得に成功しました")
                .data(inventoryStockDTOs)
                .build();
    }

    @Override
    public ResponseDTO<List<InventoryStockDTO>> getAllStockWithSupplierAndProduct() {

        List<InventoryStock> inventoryStocks = inventoryStockRepository.findAllStockWithSupplierAndProduct();
        List<InventoryStockDTO> inventoryStockDTOs = inventoryStocks.stream()
                .map(stock -> {
                    InventoryStockDTO inventoryStockDTO = inventoryStockMapper.toDTO(stock);
                    inventoryStockDTO.setProduct(productMapper.toDTO(stock.getProduct()));
                    inventoryStockDTO.setSupplierProduct(supplierProductMapper.toDTO(stock.getSupplierProduct()));
                    return inventoryStockDTO;
                })
                .collect(Collectors.toList());

        return ResponseDTO.<List<InventoryStockDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("在庫一覧の取得に成功しました")
                .data(inventoryStockDTOs)
                .build();
    }

    /**
     * IDに基づいて単一の在庫情報を取得します。
     * 
     * @param invenStockId 在庫ID
     * @return ResponseDTO<InventoryStockDTO> 対象在庫と関連履歴情報を含むDTO
     * @throws NotFoundException 指定IDの在庫が存在しない場合
     */
    @Override
    public ResponseDTO<InventoryStockDTO> getInventoryStockById(Long invenStockId) {

        InventoryStock inventoryStock = inventoryStockRepository.findById(invenStockId)
                .orElseThrow(() -> new NotFoundException("このストックは存在していません"));
        InventoryStockDTO inventoryStockDTO = inventoryStockMapper.toDTO(inventoryStock);
        List<StockHistoryDTO> stockHistoryDTOs = stockHistoryMapper.toDTOList(inventoryStock.getStockHistories());
        inventoryStockDTO.setStockHistories(stockHistoryDTOs);
        return ResponseDTO.<InventoryStockDTO>builder()
                .status(HttpStatus.OK.value())
                .message("在庫情報の取得に成功しました")
                .data(inventoryStockDTO)
                .build();
    }

    /**
     * 指定した商品IDに関連する在庫情報を取得します。
     * 
     * @param productId 商品ID
     * @return ResponseDTO<List<InventoryStockDTO>> 該当商品の在庫情報と履歴リスト
     */
    @Override
    public ResponseDTO<List<InventoryStockDTO>> getInventoryStockByProduct(Long productId) {

        List<InventoryStock> inventoryStocks = inventoryStockRepository.findByProductId(productId);
        List<InventoryStockDTO> inventoryStockDTOs = inventoryStockMapper.toDTOList(inventoryStocks);

        IntStream.range(0, inventoryStocks.size())
                .forEach(i -> {
                    inventoryStockDTOs.get(i).setStockHistories(
                            stockHistoryMapper.toDTOList(inventoryStocks.get(i).getStockHistories()));
                });

        return ResponseDTO.<List<InventoryStockDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("在庫情報の取得に成功しました")
                .data(inventoryStockDTOs)
                .build();
    }

    @Override
    public ResponseDTO<InventoryStockDTO> getBySupplierProductIdAndWarehouseId(Long supplierProductId,
            Long warehouseId) {

        InventoryStock inventoryStock = inventoryStockRepository
                .findBySupplierProductIdAndWarehouseId(supplierProductId, warehouseId)
                .orElseThrow(() -> new NotFoundException(""));
        InventoryStockDTO inventoryStockDTO = inventoryStockMapper.toDTO(inventoryStock);

        return ResponseDTO.<InventoryStockDTO>builder()
                .status(HttpStatus.OK.value())
                .message("在庫情報の取得に成功しました")
                .data(inventoryStockDTO)
                .build();
    }

    @Override
    public ResponseDTO<List<InventoryStockDTO>> getBySupplierSku(String sku) {

        List<InventoryStock> inventoryStock = inventoryStockRepository
                .findBySupplierProduct_SupplierSku(sku);
        List<InventoryStockDTO> inventoryStockDTO = inventoryStockMapper.toDTOList(inventoryStock);

        return ResponseDTO.<List<InventoryStockDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("在庫情報の取得に成功しました")
                .data(inventoryStockDTO)
                .build();
    }

    /**
     * 発注書 (Purchase Order) に対して在庫受領処理を行います。
     *
     * @param poId          対象の Purchase Order の ID
     * @param receivedItems 受領するアイテム情報のリスト
     * @return 在庫受領処理後の PurchaseOrderDTO を格納した ResponseDTO
     * @throws NotFoundException          PO または明細、倉庫、商品が存在しない場合
     * @throws IllegalStateException      PO が完了している場合、または PO に含まれない明細が送信された場合
     * @throws InvalidCredentialException 受領数量が0以下、または発注数量を超えた場合
     */
    @Override
    @Transactional
    public ResponseDTO<ReceiveStockResultDTO> receiveStock(Long poId, List<ReceiveStockItemDTO> receivedItems) {

        // 明細も含めて取得（存在しない場合は例外）
        PurchaseOrder po = purchaseOrderRepository.findByIdWithDetails(poId)
                .orElseThrow(() -> new NotFoundException("この注文書は存在していません"));
        // 既に完了済みの発注書は受領不可
        if (po.getStatus() == OrderStatus.COMPLETED) {
            throw new IllegalStateException("この注文書は既に完了しています");
        }
        User currentUser = userService.getCurrentUserEntity();

        boolean anyReceived = false; // 今回受領したアイテムがあるか
        boolean allReceived = true; // 全明細が完了しているか

        // 明細情報をMap化（detailId → Detail）
        Map<Long, PurchaseOrderDetail> detailMap = po.getDetails().stream()
                .collect(Collectors.toMap(PurchaseOrderDetail::getId, d -> d));
        // POに含まれる商品ID一覧を取得
        List<Long> productIds = po.getDetails().stream()
                .map(d -> d.getProduct().getId())
                .toList();
        // SupplierProduct を取得
        List<SupplierProduct> sps = supplierProductRepository.findByProductIdInAndSupplierId(
                productIds, po.getSupplier().getId());
        // productId → SupplierProduct のMap
        Map<Long, SupplierProduct> spMap = sps.stream()
                .collect(Collectors.toMap(sp -> sp.getProduct().getId(), sp -> sp));

        // 既存の在庫情報を取得
        Set<Long> spIds = spMap.values().stream().map(SupplierProduct::getId).collect(Collectors.toSet());
        Set<Long> warehouseIds = receivedItems.stream().map(ReceiveStockItemDTO::getWarehouseId)
                .collect(Collectors.toSet());
        List<InventoryStock> allStocks = inventoryStockRepository.findBySupplierProductIdInAndWarehouseIdIn(spIds,
                warehouseIds);

        // key: supplierProductId_warehouseId
        Map<String, InventoryStock> stockMap = allStocks.stream()
                .collect(Collectors.toMap(
                        s -> s.getSupplierProduct().getId() + "_" + s.getWarehouse().getId(),
                        s -> s));

        // 既に受領済み数量を取得（履歴テーブルから集計）
        Map<Long, Integer> receivedQtyMap = new HashMap<>();
        for (Object[] row : stockHistoryRepository.sumReceivedQtyByPoGroupBySupplierProduct(poId)) {
            Long spId = (Long) row[0];
            Integer qty = ((Number) row[1]).intValue();
            receivedQtyMap.put(spId, qty);
        }
        // 既に完了している明細IDリスト
        List<Long> allCompletedDetailIds = po.getDetails().stream()
                .filter(d -> d.getStatus() == OrderStatus.COMPLETED)
                .map(PurchaseOrderDetail::getId)
                .collect(Collectors.toCollection(ArrayList::new));

        List<StockHistoryDTO> createdStockHistories = new ArrayList<>();
        // 受領処理ループ
        for (ReceiveStockItemDTO item : receivedItems) {
            PurchaseOrderDetail detail = detailMap.get(item.getDetailId());
            // 明細存在チェック
            if (detail == null)
                throw new IllegalStateException("注文書に含まれていない明細です。 detailId=" + item.getDetailId());
            // 数量チェック
            if (item.getReceivedQty() <= 0)
                throw new InvalidCredentialException("受領数量は0より大きくなければなりません");
            // SupplierProduct存在チェック
            SupplierProduct sp = spMap.get(detail.getProduct().getId());
            if (sp == null)
                throw new NotFoundException("SupplierProductが存在しません。productId=" + detail.getProduct().getId());
            // 発注数量超過チェック
            int deliveredSoFar = receivedQtyMap.getOrDefault(sp.getId(), 0);
            int totalAfterReceive = deliveredSoFar + item.getReceivedQty();

            if (totalAfterReceive > detail.getQty())
                throw new InvalidCredentialException("受領数量が発注数量を超えています。 (orderedQty = " + detail.getQty() + ")");

            // 明細ステータス更新
            if (totalAfterReceive == detail.getQty()) {
                detail.setStatus(OrderStatus.COMPLETED);
                allCompletedDetailIds.add(detail.getId());
            } else if (totalAfterReceive > 0) {
                detail.setStatus(OrderStatus.PROCESSING);
            }
            poDetailRepository.save(detail);

            receivedQtyMap.put(sp.getId(), totalAfterReceive);
            anyReceived = true;
            // 在庫更新処理
            String stockKey = sp.getId() + "_" + item.getWarehouseId();
            InventoryStock stock = stockMap.get(stockKey);
            // 在庫が存在しない場合は新規作成
            if (stock == null) {
                Warehouse wh = warehouseRepository.findById(item.getWarehouseId())
                        .orElseThrow(() -> new NotFoundException("倉庫が存在していません。, ID=" + item.getWarehouseId()));
                stock = new InventoryStock();
                stock.setProduct(detail.getProduct());
                stock.setSupplierProduct(sp);
                stock.setWarehouse(wh);
                stock.setQuantity(0);
                stock = inventoryStockRepository.save(stock);
                stockMap.put(stockKey, stock);
            }
            // 数量加算
            stock.setQuantity(stock.getQuantity() + item.getReceivedQty());
            inventoryStockRepository.save(stock);
            // 在庫履歴登録
            StockHistory history = new StockHistory();
            history.setInventoryStock(stock);
            history.setChangeQty(item.getReceivedQty());
            history.setType(StockChangeType.IN);
            history.setRefType(RefType.PO);
            history.setRefId(po.getId());
            history.setNotes(item.getNote() != null ? item.getNote() : "発注書からの受領");
            stockHistoryRepository.save(history);

            createdStockHistories.add(stockHistoryMapper.toDTO(history));
        }

        // 全明細が完了しているか判定
        for (PurchaseOrderDetail d : po.getDetails()) {

            SupplierProduct sp = spMap.get(d.getProduct().getId());
            int received = receivedQtyMap.getOrDefault(sp.getId(), 0);
            if (received < d.getQty()) {
                allReceived = false;
                break;
            }
        }

        // PO ステータス更新

        if (!anyReceived) {
            po.setStatus(OrderStatus.PENDING);
        } else if (allReceived) {
            po.setStatus(OrderStatus.COMPLETED);
        } else {
            po.setStatus(OrderStatus.PROCESSING);
        }

        purchaseOrderRepository.save(po);

        notificationService.createNotification(
                NotificationDTO.builder()
                        .userId(currentUser.getId())
                        .title("発注書の入庫処理が行われました")
                        .message("注文ID #" + po.getId() + " の商品が入庫されました")
                        .type(NotificationType.STOCK)
                        .link("/purchase-order/" + po.getId())
                        .build());

        ReceiveStockResultDTO result = ReceiveStockResultDTO.builder()
                .orderId(po.getId())
                .status(po.getStatus())
                .completedDetailIds(allCompletedDetailIds)
                .stockHistories(createdStockHistories)
                .build();

        return ResponseDTO.<ReceiveStockResultDTO>builder()
                .status(HttpStatus.OK.value())
                .message("在庫の受領処理が完了しました")
                .data(result)
                .build();
    }

    /**
     * 注文書 (Sales Order) に対して在庫出庫処理を行います。
     * 
     * @param soId         対象のSales Order ID
     * @param deliverItems 出庫するアイテム情報リスト
     * @return ResponseDTO<DeliverStockResultDTO> 出庫結果、更新後のSOステータス、作成された在庫履歴
     * @throws NotFoundException          SO、明細、在庫が存在しない場合
     * @throws IllegalStateException      SOが完了済み、または不正な明細が送信された場合
     * @throws InvalidCredentialException 出庫数量が0以下、または予約在庫不足の場合
     */
    @Override
    @Transactional
    public ResponseDTO<DeliverStockResultDTO> deliverStock(Long soId, List<DeliverStockItemDTO> deliverItems) {

        // SalesOrder をIDで取得。存在しなければ例外
        SalesOrder so = salesOrderRepository.findById(soId)
                .orElseThrow(() -> new NotFoundException("注文書が存在しません"));

        // 完了済みの注文書なら出庫不可
        if (so.getStatus() == OrderStatus.COMPLETED) {
            throw new IllegalStateException("この注文書は既に完了しています");
        }

        User currentUser = userService.getCurrentUserEntity();

        boolean anyDelivered = false; // 今回出庫したアイテムがあるか
        boolean allDelivered = true; // 全明細が出庫完了か

        // 注文書に含まれる明細IDをセット化
        Set<Long> soDetailIds = so.getDetails().stream()
                .map(SalesOrderDetail::getId)
                .collect(Collectors.toSet());

        List<Long> completedDetailIds = new ArrayList<>(); // 出庫完了した明細IDリスト
        List<StockHistoryDTO> historyDTOs = new ArrayList<>(); // 作成した在庫履歴DTOリスト

        // 各出庫アイテムを処理
        for (DeliverStockItemDTO itemDTO : deliverItems) {

            // 注文書に含まれない明細の場合は例外
            if (!soDetailIds.contains(itemDTO.getDetailId())) {
                throw new IllegalStateException(
                        "注文書に含まれていない明細です。detailId=" + itemDTO.getDetailId());
            }

            // 出庫数量が0以下の場合は例外
            if (itemDTO.getDeliveredQty() <= 0) {
                throw new InvalidCredentialException("出庫数量は0より大きくなければなりません");
            }

            // 明細を取得
            SalesOrderDetail detail = salesOrderDetailRepository.findById(itemDTO.getDetailId())
                    .orElseThrow(() -> new NotFoundException("明細が存在しません。ID=" + itemDTO.getDetailId()));

            SupplierProduct supplierProduct = detail.getSupplierProduct();

            // 出庫対象の在庫を取得
            InventoryStock stock = inventoryStockRepository
                    .findBySupplierProductIdAndWarehouseId(
                            supplierProduct.getId(),
                            itemDTO.getWarehouseId())
                    .orElseThrow(() -> new NotFoundException(
                            "在庫が存在しません。productId=" + supplierProduct.getId()
                                    + ", warehouseId=" + itemDTO.getWarehouseId()));
            log.info("ReservedQuantity: {}", stock.getReservedQuantity());
            log.info("DeliveredQty: {}", itemDTO.getDeliveredQty());
            // 予約在庫が不足している場合は例外
            if (stock.getReservedQuantity() < itemDTO.getDeliveredQty()) {
                throw new InvalidCredentialException(
                        "予約在庫が不足しています。productId=" + supplierProduct.getId()
                                + ", warehouseId=" + itemDTO.getWarehouseId());
            }

            // 在庫数量と予約数量を減算
            stock.setQuantity(stock.getQuantity() - itemDTO.getDeliveredQty());
            stock.setReservedQuantity(stock.getReservedQuantity() - itemDTO.getDeliveredQty());
            inventoryStockRepository.save(stock);

            // 出庫履歴を作成
            StockHistory history = new StockHistory();
            history.setInventoryStock(stock);
            history.setChangeQty(itemDTO.getDeliveredQty());
            history.setType(StockChangeType.OUT);
            history.setRefType(RefType.SO);
            history.setRefId(so.getId());
            history.setNotes("Deliver from SalesOrder");
            stockHistoryRepository.save(history);

            historyDTOs.add(stockHistoryMapper.toDTO(history));

            // 明細の出庫済数量を更新
            int deliveredSofar = detail.getDeliveredQty() + itemDTO.getDeliveredQty();
            detail.setDeliveredQty(deliveredSofar);

            // 明細の出庫完了判定
            if (deliveredSofar == detail.getQty()) {
                detail.setStatus(OrderStatus.COMPLETED);
                completedDetailIds.add(detail.getId());
            } else {
                allDelivered = false;
            }

            salesOrderDetailRepository.save(detail);
            anyDelivered = true;
        }

        // SalesOrderのステータス更新
        if (!anyDelivered) {
            so.setStatus(OrderStatus.NEW);
        } else if (allDelivered) {
            so.setStatus(OrderStatus.COMPLETED);
        } else {
            so.setStatus(OrderStatus.PROCESSING);
        }

        salesOrderRepository.save(so);

        notificationService.createNotification(
                NotificationDTO.builder()
                        .userId(currentUser.getId())
                        .title("注文商品の出庫が完了しました")
                        .message("注文ID #" + so.getId() + "の商品がすべて出庫されました")
                        .type(NotificationType.STOCK)
                        .link("/sales-order/" + so.getId())
                        .build());
        // 出庫結果をDTOにまとめる
        DeliverStockResultDTO result = DeliverStockResultDTO.builder()
                .salesOrderId(so.getId())
                .soStatus(so.getStatus())
                .completedDetailIds(completedDetailIds)
                .stockHistories(historyDTOs)
                .build();

        return ResponseDTO.<DeliverStockResultDTO>builder()
                .status(HttpStatus.OK.value())
                .message("出庫処理が完了しました")
                .data(result)
                .build();
    }

}
