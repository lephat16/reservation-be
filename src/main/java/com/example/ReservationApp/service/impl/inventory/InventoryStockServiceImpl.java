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
import com.example.ReservationApp.dto.request.DeliverStockItemDTO;
import com.example.ReservationApp.dto.request.ReceiveStockItemDTO;
import com.example.ReservationApp.dto.request.StockChangeRequest;
import com.example.ReservationApp.dto.response.inventory.DeliverStockResultDTO;
import com.example.ReservationApp.dto.response.inventory.InventoryStockDTO;
import com.example.ReservationApp.dto.response.inventory.ReceiveStockResultDTO;
import com.example.ReservationApp.dto.response.inventory.StockHistoryDTO;
import com.example.ReservationApp.entity.inventory.InventoryStock;
import com.example.ReservationApp.entity.inventory.StockHistory;
import com.example.ReservationApp.entity.inventory.Warehouse;
import com.example.ReservationApp.entity.product.Product;
import com.example.ReservationApp.entity.transaction.PurchaseOrder;
import com.example.ReservationApp.entity.transaction.PurchaseOrderDetail;
import com.example.ReservationApp.entity.transaction.SalesOrder;
import com.example.ReservationApp.entity.transaction.SalesOrderDetail;
import com.example.ReservationApp.enums.OrderStatus;
import com.example.ReservationApp.enums.RefType;
import com.example.ReservationApp.enums.StockChangeType;
import com.example.ReservationApp.exception.InvalidCredentialException;
import com.example.ReservationApp.exception.NotFoundException;
import com.example.ReservationApp.mapper.InventoryStockMapper;
import com.example.ReservationApp.mapper.StockHistoryMapper;
import com.example.ReservationApp.repository.inventory.InventoryStockRepository;
import com.example.ReservationApp.repository.inventory.StockHistoryRepository;
import com.example.ReservationApp.repository.inventory.WarehouseRepository;
import com.example.ReservationApp.repository.product.ProductRepository;
import com.example.ReservationApp.repository.transaction.PurchaseOrderDetailRepository;
import com.example.ReservationApp.repository.transaction.PurchaseOrderRepository;
import com.example.ReservationApp.repository.transaction.SalesOrderDetailRepository;
import com.example.ReservationApp.repository.transaction.SalesOrderRepository;
import com.example.ReservationApp.service.inventory.InventoryStockService;
import com.example.ReservationApp.service.inventory.StockHistoryService;

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
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockHistoryService stockHistoryService;
    private final InventoryStockMapper inventoryStockMapper;
    private final StockHistoryMapper stockHistoryMapper;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final PurchaseOrderDetailRepository poDetailRepository;
    private final SalesOrderDetailRepository salesOrderDetailRepository;
    private final StockHistoryRepository stockHistoryRepository;

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
                .message("在庫一覧の取得に成功しました。")
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
                .orElseThrow(() -> new NotFoundException("inventory stock not found"));
        InventoryStockDTO inventoryStockDTO = inventoryStockMapper.toDTO(inventoryStock);
        List<StockHistoryDTO> stockHistoryDTOs = stockHistoryMapper.toDTOList(inventoryStock.getStockHistories());
        inventoryStockDTO.setStockHistories(stockHistoryDTOs);
        return ResponseDTO.<InventoryStockDTO>builder()
                .status(HttpStatus.OK.value())
                .message("在庫情報の取得に成功しました。")
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
                .message("在庫情報の取得に成功しました。")
                .data(inventoryStockDTOs)
                .build();
    }

    /**
     * 指定した在庫から数量を減少させます。
     * 
     * @param request StockChangeRequest 在庫変更リクエスト情報
     * @return ResponseDTO<InventoryStockDTO> 変更後の在庫情報と履歴
     * @throws NotFoundException          商品または倉庫が存在しない場合
     * @throws InvalidCredentialException 減少数量が不正、または在庫不足の場合
     */
    @Override
    @Transactional
    public ResponseDTO<InventoryStockDTO> increaseStock(StockChangeRequest request) {

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new NotFoundException("商品が存在していません。"));
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new NotFoundException("倉庫が存在していません。"));

        InventoryStock stock = inventoryStockRepository
                .findByProductIdAndWarehouseId(
                        request.getProductId(),
                        request.getWarehouseId())
                .orElseGet(() -> {
                    System.out.println("CREATING NEW STOCK");
                    InventoryStock newStock = new InventoryStock();
                    newStock.setProduct(product);
                    newStock.setWarehouse(warehouse);
                    newStock.setQuantity(0);
                    return inventoryStockRepository.save(newStock);
                });
        if (request.getQty() <= 0) {
            throw new InvalidCredentialException("数量が正しくありません。");
        }

        StockHistoryDTO history = new StockHistoryDTO();
        history.setInventoryStockId(stock.getId());
        history.setChangeQty(request.getQty());
        history.setType(StockChangeType.IN);
        history.setRefType(RefType.PO);
        history.setRefId(request.getRefId());
        history.setNotes(request.getNotes());

        ResponseDTO<StockHistoryDTO> savedHistory = stockHistoryService.createStockHistory(history, stock.getId());

        List<StockHistoryDTO> stockHistoryDTOs = stockHistoryMapper.toDTOList(stock.getStockHistories());
        InventoryStockDTO stockDTO = inventoryStockMapper.toDTO(stock);
        stockDTO.setStockHistories(stockHistoryDTOs);
        if (stock.getStockHistories().isEmpty()) {
            stockDTO.setStockHistories(List.of(savedHistory.getData()));
        }
        return ResponseDTO.<InventoryStockDTO>builder()
                .status(HttpStatus.OK.value())
                .message("在庫の追加に成功しました。")
                .data(stockDTO)
                .build();
    }

    /**
     * 在庫数量を調整します（増減の両方）。
     * 
     * @param request StockChangeRequest 調整情報
     * @return ResponseDTO<InventoryStockDTO> 調整後の在庫情報と履歴
     * @throws NotFoundException          商品または倉庫が存在しない場合
     * @throws InvalidCredentialException 在庫不足または不正な数量の場合
     */
    @Override
    public ResponseDTO<InventoryStockDTO> decreaseStock(StockChangeRequest request) {

        if (!productRepository.existsById(request.getProductId())) {
            throw new NotFoundException("商品が存在していません。");
        }
        if (!warehouseRepository.existsById(request.getWarehouseId())) {
            throw new NotFoundException("倉庫が存在していません。");
        }
        InventoryStock stock = inventoryStockRepository
                .findByProductIdAndWarehouseId(
                        request.getProductId(),
                        request.getWarehouseId())
                .orElseThrow(() -> new NotFoundException("在庫が存在していません。"));
        if (request.getQty() <= 0) {
            throw new InvalidCredentialException("減少数量が正しくありません。");
        }
        if (stock.getQuantity() < request.getQty()) {
            throw new InvalidCredentialException("在庫が不足しています。");
        }

        StockHistoryDTO history = new StockHistoryDTO();
        history.setInventoryStockId(stock.getId());
        history.setChangeQty(-request.getQty());
        history.setType(StockChangeType.OUT);
        history.setRefType(RefType.SO);
        history.setRefId(request.getRefId());
        history.setNotes(request.getNotes());

        stockHistoryService.createStockHistory(history, stock.getId());
        List<StockHistoryDTO> stockHistoryDTOs = stockHistoryMapper.toDTOList(stock.getStockHistories());
        InventoryStockDTO stockDTO = inventoryStockMapper.toDTO(stock);
        stockDTO.setStockHistories(stockHistoryDTOs);

        return ResponseDTO.<InventoryStockDTO>builder()
                .status(HttpStatus.OK.value())
                .message("在庫の減少に成功しました。")
                .data(stockDTO)
                .build();
    }

    @Override
    @Transactional
    public ResponseDTO<InventoryStockDTO> adjustStock(StockChangeRequest request) {

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new NotFoundException("商品が存在していません。"));
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new NotFoundException("倉庫が存在していません。"));

        InventoryStock stock = inventoryStockRepository
                .findByProductIdAndWarehouseId(request.getProductId(), request.getWarehouseId())
                .orElseGet(() -> {
                    if (request.getQty() < 0) {
                        throw new InvalidCredentialException("在庫が存在しないため、減少はできません。");
                    }
                    InventoryStock newStock = new InventoryStock();
                    newStock.setProduct(product);
                    newStock.setWarehouse(warehouse);
                    newStock.setQuantity(0);
                    return inventoryStockRepository.save(newStock);
                });

        int newQty = stock.getQuantity() + request.getQty();
        if (newQty < 0) {
            throw new InvalidCredentialException("在庫が不足しています。");
        }
        stock.setQuantity(newQty);
        inventoryStockRepository.save(stock);

        StockHistoryDTO history = new StockHistoryDTO();
        history.setInventoryStockId(stock.getId());
        history.setChangeQty(request.getQty());
        history.setType(StockChangeType.ADJ);
        history.setRefType(RefType.ADJ);
        history.setRefId(request.getRefId());
        history.setNotes(request.getNotes());

        ResponseDTO<StockHistoryDTO> savedHistory = stockHistoryService.createStockHistory(history, stock.getId());

        InventoryStockDTO stockDTO = inventoryStockMapper.toDTO(stock);
        stockDTO.setStockHistories(stockHistoryMapper.toDTOList(stock.getStockHistories()));
        if (stock.getStockHistories().isEmpty()) {
            stockDTO.setStockHistories(List.of(savedHistory.getData()));
        }
        return ResponseDTO.<InventoryStockDTO>builder()
                .status(HttpStatus.OK.value())
                .message("在庫の調整に成功しました。")
                .data(stockDTO)
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

        // PO を取得。存在しなければ例外
        PurchaseOrder po = purchaseOrderRepository.findById(poId)
                .orElseThrow(() -> new NotFoundException("この注文書は存在していません。"));

        // PO が完了済みなら例外
        if (po.getStatus() == OrderStatus.COMPLETED) {
            throw new IllegalStateException("この注文書は既に完了しています。");
        }

        boolean anyReceived = false; // 今回受領したアイテムがあるか
        boolean allReceived = true; // 全明細が完了しているか

        // PO に含まれる明細IDをセット化
        Set<Long> poDetailIds = po.getDetails().stream()
                .map(PurchaseOrderDetail::getId)
                .collect(Collectors.toSet());

        // すでに受領済み数量を productId ごとに取得
        Map<Long, Integer> receivedQtyMap = new HashMap<>();
        for (Object[] row : stockHistoryRepository.sumReceivedQtyByPoGroupByProduct(poId)) {
            Long productId = (Long) row[0];
            Integer qty = ((Number) row[1]).intValue();
            receivedQtyMap.put(productId, qty);
        }

        // request 内で同じ detailId が複数回出た場合、数量を合計
        Map<Long, Integer> requestQtyMap = new HashMap<>();
        for (ReceiveStockItemDTO item : receivedItems) {
            if (!poDetailIds.contains(item.getDetailId())) {
                throw new IllegalStateException(
                        "注文書に含まれていない明細です。 detailId=" + item.getDetailId());
            }

            if (item.getReceivedQty() <= 0) {
                throw new InvalidCredentialException("受領数量は0より大きくなければなりません。");
            }

            // 同じ明細IDの数量を加算
            requestQtyMap.put(
                    item.getDetailId(),
                    requestQtyMap.getOrDefault(item.getDetailId(), 0) + item.getReceivedQty());

        }

        List<Long> allCompletedDetailIds = po.getDetails().stream()
                .filter(d -> d.getStatus() == OrderStatus.COMPLETED)
                .map(PurchaseOrderDetail::getId)
                .collect(Collectors.toList());
        List<StockHistoryDTO> createdStockHistories = new ArrayList<>();
        // 各明細ごとの受領処理
        for (Map.Entry<Long, Integer> entry : requestQtyMap.entrySet()) {
            Long detailId = entry.getKey();
            int totalReceivedInRequest = entry.getValue();

            PurchaseOrderDetail detail = poDetailRepository.findById(detailId)
                    .orElseThrow(() -> new NotFoundException("注文詳細が存在していません。 ID=" + detailId));

            ReceiveStockItemDTO item = receivedItems.stream()
                    .filter(i -> i.getDetailId().equals(detailId))
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException("注文明細IDに対応する受領アイテムが見つかりません: detailId=" + detailId));

            Long productId = detail.getProduct().getId();
            int receivedSoFar = receivedQtyMap.getOrDefault(productId, 0);
            int totalAfterReceive = receivedSoFar + totalReceivedInRequest;

            if (totalAfterReceive > detail.getQty()) {
                throw new InvalidCredentialException(
                        "受領数量が発注数量を超えています。 (orderedQty = " + detail.getQty() + ")");
            }

            if (totalAfterReceive == detail.getQty()) {
                detail.setStatus(OrderStatus.COMPLETED);
                allCompletedDetailIds.add(detail.getId());

            } else if (totalAfterReceive > 0) {
                detail.setStatus(OrderStatus.PROCESSING);
            }

            poDetailRepository.save(detail);

            receivedQtyMap.put(productId, totalAfterReceive);
            anyReceived = true;

            InventoryStock stock = inventoryStockRepository
                    .findByProductIdAndWarehouseId(productId, receivedItems.stream()
                            .filter(i -> i.getDetailId().equals(detailId))
                            .findFirst()
                            .get()
                            .getWarehouseId())
                    .orElseGet(() -> {
                        InventoryStock newStock = new InventoryStock();
                        newStock.setProduct(detail.getProduct());
                        Warehouse wh = warehouseRepository.findById(
                                receivedItems.stream()
                                        .filter(i -> i.getDetailId().equals(detailId))
                                        .findFirst()
                                        .get()
                                        .getWarehouseId())
                                .orElseThrow(() -> new NotFoundException(
                                        "倉庫が存在していません。, ID=" + detailId));
                        newStock.setWarehouse(wh);
                        newStock.setQuantity(0);
                        return inventoryStockRepository.save(newStock);
                    });

            StockHistory history = new StockHistory();
            history.setInventoryStock(stock);
            history.setChangeQty(totalReceivedInRequest);
            history.setType(StockChangeType.IN);
            history.setRefType(RefType.PO);
            history.setRefId(po.getId());
            history.setNotes(item.getNote() != null ? item.getNote() : "発注書からの受領" + //
                                "");
            stockHistoryRepository.save(history);

            stock.setQuantity(stock.getQuantity() + totalReceivedInRequest);
            inventoryStockRepository.save(stock);
            createdStockHistories.add(stockHistoryMapper.toDTO(history));
        }

        // 全明細が完了しているか判定
        for (PurchaseOrderDetail d : po.getDetails()) {
            int received = receivedQtyMap.getOrDefault(d.getProduct().getId(), 0);
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

        ReceiveStockResultDTO result = ReceiveStockResultDTO.builder()
                .purchaseOrderId(po.getId())
                .poStatus(po.getStatus())
                .completedDetailIds(allCompletedDetailIds)
                .stockHistories(createdStockHistories)
                .build();

        return ResponseDTO.<ReceiveStockResultDTO>builder()
                .status(HttpStatus.OK.value())
                .message("在庫の受領処理が完了しました。")
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
            throw new IllegalStateException("この注文書は既に完了しています。");
        }

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
                throw new InvalidCredentialException("出庫数量は0より大きくなければなりません。");
            }

            // 明細を取得
            SalesOrderDetail detail = salesOrderDetailRepository.findById(itemDTO.getDetailId())
                    .orElseThrow(() -> new NotFoundException("明細が存在しません。ID=" + itemDTO.getDetailId()));

            Product product = detail.getProduct();

            // 出庫対象の在庫を取得
            InventoryStock stock = inventoryStockRepository
                    .findByProductIdAndWarehouseId(
                            product.getId(),
                            itemDTO.getWarehouseId())
                    .orElseThrow(() -> new NotFoundException(
                            "在庫が存在しません。productId=" + product.getId()
                                    + ", warehouseId=" + itemDTO.getWarehouseId()));

            // 予約在庫が不足している場合は例外
            if (stock.getReservedQuantity() < itemDTO.getDeliveredQty()) {
                throw new InvalidCredentialException(
                        "予約在庫が不足しています。productId=" + product.getId()
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

        // 出庫結果をDTOにまとめる
        DeliverStockResultDTO result = DeliverStockResultDTO.builder()
                .salesOrderId(so.getId())
                .soStatus(so.getStatus())
                .completedDetailIds(completedDetailIds)
                .stockHistories(historyDTOs)
                .build();

        return ResponseDTO.<DeliverStockResultDTO>builder()
                .status(HttpStatus.OK.value())
                .message("出庫処理が完了しました。")
                .data(result)
                .build();
    }

    
}
