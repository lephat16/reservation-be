package com.example.ReservationApp.service.impl.inventory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.example.ReservationApp.dto.ResponseDTO;
import com.example.ReservationApp.dto.response.inventory.InventoryHistoryByOrderDTO;
import com.example.ReservationApp.dto.response.inventory.InventoryHistoryByPurchaseOrderFlatDTO;
import com.example.ReservationApp.dto.response.inventory.InventoryHistoryBySaleOrderFlatDTO;
import com.example.ReservationApp.dto.response.inventory.StockHistoriesWithDetailDTO;
import com.example.ReservationApp.dto.response.inventory.StockHistoryDTO;
import com.example.ReservationApp.entity.inventory.InventoryStock;
import com.example.ReservationApp.entity.inventory.StockHistory;

import com.example.ReservationApp.exception.NotFoundException;
import com.example.ReservationApp.mapper.StockHistoryMapper;
import com.example.ReservationApp.repository.inventory.InventoryStockRepository;
import com.example.ReservationApp.repository.inventory.StockHistoryRepository;
import com.example.ReservationApp.repository.inventory.WarehouseRepository;
import com.example.ReservationApp.repository.product.ProductRepository;
import com.example.ReservationApp.repository.transaction.SalesOrderRepository;
import com.example.ReservationApp.service.inventory.StockHistoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StockHistoryServiceImpl implements StockHistoryService {

    private final StockHistoryRepository stockHistoryRepository;
    private final InventoryStockRepository inventoryStockRepository;
    private final StockHistoryMapper stockHistoryMapper;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final SalesOrderRepository salesOrderRepository;

    /**
     * 在庫履歴を作成します。数量の増減を反映します。
     *
     * @param stockHistoryDTO  在庫履歴情報
     * @param inventoryStockId 対象の在庫ID
     * @return 作成された在庫履歴DTO
     */
    @Override
    @Transactional
    public ResponseDTO<StockHistoryDTO> createStockHistory(
            StockHistoryDTO stockHistoryDTO,
            @RequestParam Long inventoryStockId) {

        // 変更数量が null または 0 の場合は例外
        if (stockHistoryDTO.getChangeQty() == null || stockHistoryDTO.getChangeQty() == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "数量の変更は0にできません");
        }

        // 対象の在庫データを取得。存在しなければ例外
        InventoryStock inventoryStock = inventoryStockRepository.findById(inventoryStockId)
                .orElseThrow(() -> new NotFoundException("在庫データが存在していません"));

        int newQty = inventoryStock.getQuantity() + stockHistoryDTO.getChangeQty();
        if (newQty < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "在庫が不足しています");
        }

        // StockHistory エンティティに変換し、在庫と紐付けて保存
        StockHistory stockHistory = stockHistoryMapper.toEntity(stockHistoryDTO);
        stockHistory.setInventoryStock(inventoryStock);
        stockHistoryRepository.save(stockHistory);

        // InventoryStock の数量を更新
        inventoryStock.setQuantity(newQty);
        inventoryStockRepository.save(inventoryStock);

        return ResponseDTO.<StockHistoryDTO>builder()
                .status(HttpStatus.OK.value())
                .message("新しい商品の追加に成功しました")
                .data(stockHistoryMapper.toDTO(stockHistory))
                .build();
    }

    /**
     * 全在庫履歴を取得します。
     *
     * @return 在庫履歴DTOのリスト
     */
    @Override
    public ResponseDTO<List<StockHistoryDTO>> getAllStockHistories() {

        // 全在庫履歴を取得（在庫・商品・倉庫情報も取得）
        List<StockHistory> stockHistories = stockHistoryRepository.findAllWithStockProductWarehouse();
        List<StockHistoryDTO> stockHistoryDTOs = stockHistoryMapper.toDTOList(stockHistories);

        // DTOに変換して返す
        return ResponseDTO.<List<StockHistoryDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("在庫履歴の取得に成功しました")
                .data(stockHistoryDTOs)
                .build();
    }

    @Override
    public ResponseDTO<List<StockHistoriesWithDetailDTO>> getAllStockHistoriesWithDetails() {

        // 全在庫履歴を取得（在庫・商品・倉庫情報も取得）
        List<StockHistoriesWithDetailDTO> stockHistoryDTOs = stockHistoryRepository.findAllStockHistoriesWithDetails();
        
        // DTOに変換して返す
        return ResponseDTO.<List<StockHistoriesWithDetailDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("在庫履歴の取得に成功しました")
                .data(stockHistoryDTOs)
                .build();
    }

    /**
     * 指定の在庫IDに紐づく履歴を取得します。
     *
     * @param inventoryStockId 在庫ID
     * @return 在庫履歴DTOのリスト
     */
    @Override
    public ResponseDTO<List<StockHistoryDTO>> getStockHistoryByInventoryId(Long inventoryStockIdLong) {

        // 指定在庫IDに紐づく履歴を取得
        List<StockHistory> stockHistories = stockHistoryRepository.findByInventoryStock(inventoryStockIdLong);

        // DTOに変換して返す
        return ResponseDTO.<List<StockHistoryDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("指定在庫の履歴取得に成功しました")
                .data(stockHistoryMapper.toDTOList(stockHistories))
                .build();

    }

    /**
     * 指定倉庫の在庫履歴を取得します。
     *
     * @param warehouseId 倉庫ID
     * @return 在庫履歴DTOのリスト
     */
    @Override
    public ResponseDTO<List<StockHistoryDTO>> getStockHistoryByWarehouse(Long warehouseId) {

        // 倉庫存在チェック
        if (!warehouseRepository.existsById(warehouseId)) {
            throw new NotFoundException("この倉庫は存在していません");
        }

        // 指定倉庫に紐づく履歴を取得
        List<StockHistory> stockHistories = stockHistoryRepository.findByWarehouse(warehouseId);

        return ResponseDTO.<List<StockHistoryDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("指定倉庫の履歴取得に成功しました")
                .data(stockHistoryMapper.toDTOList(stockHistories))
                .build();
    }

    /**
     * 指定商品に紐づく在庫履歴を取得します。
     *
     * @param productId 商品ID
     * @return 在庫履歴DTOのリスト
     */
    @Override
    public ResponseDTO<List<StockHistoryDTO>> getStockHistoryByProduct(Long productId) {

        // 商品存在チェック
        if (!productRepository.existsById(productId)) {
            throw new NotFoundException("この商品は存在していません");
        }

        // 指定商品に紐づく履歴を取得
        List<StockHistory> stockHistories = stockHistoryRepository.findByProduct(productId);

        return ResponseDTO.<List<StockHistoryDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("指定商品の履歴取得に成功しました")
                .data(stockHistoryMapper.toDTOList(stockHistories))
                .build();
    }

    /**
     * 指定日以降の最近の在庫履歴を取得します。
     *
     * @param fromDate 開始日
     * @return 在庫履歴DTOのリスト
     */
    @Override
    public ResponseDTO<List<StockHistoryDTO>> getRecentStockHistory(LocalDateTime fromDate) {

        // 日付が null の場合は5日前を初期値とする
        if (fromDate == null) {
            fromDate = LocalDateTime.now().minusDays(5);
        }

        // 指定日以降の履歴を取得
        List<StockHistory> stockHistories = stockHistoryRepository.findRecentStockHistory(fromDate);

        return ResponseDTO.<List<StockHistoryDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("最近の在庫履歴取得に成功しました")
                .data(stockHistoryMapper.toDTOList(stockHistories))
                .build();
    }

    @Override
    public ResponseDTO<List<InventoryHistoryByOrderDTO>> getInventoryHistoryByPurchaseOrder(Long poId) {

        List<InventoryHistoryByPurchaseOrderFlatDTO> inventoryHistoryByPurchaseOrderFlatDTOs = stockHistoryRepository
                .findInventoryHistoryByPurchaseOrder(poId);

        List<InventoryHistoryByOrderDTO> InventoryHistoryByOrderDTOs = inventoryHistoryByPurchaseOrderFlatDTOs
                .stream()
                .map(flatDTO -> InventoryHistoryByOrderDTO.builder()
                        .id(flatDTO.getId())
                        .location(flatDTO.getLocation())
                        .warehouseName(flatDTO.getWarehouseName())
                        .changeQty(flatDTO.getChangeQty())
                        .notes(flatDTO.getNotes())
                        .productName(flatDTO.getProductName())
                        .supplierName(flatDTO.getSupplierName())
                        .refType(flatDTO.getRefType())
                        .createdAt(flatDTO.getCreatedAt())
                        .supplierSku(flatDTO.getSupplierSku())
                        .build())
                .collect(Collectors.toList());
        return ResponseDTO.<List<InventoryHistoryByOrderDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("在庫履歴取得に成功しました")
                .data(InventoryHistoryByOrderDTOs)
                .build();
    }
    
    @Override
    public ResponseDTO<List<InventoryHistoryByOrderDTO>> getInventoryHistoryBySaleOrder(Long soId) {
        if (!salesOrderRepository.existsById(soId)) {
            throw new NotFoundException("この販売注文書は存在していません");
        }
        List<InventoryHistoryBySaleOrderFlatDTO> inventoryHistoryBySaleOrderFlatDTOs = stockHistoryRepository
                .findInventoryHistoryBySaleOrder(soId);

        List<InventoryHistoryByOrderDTO> InventoryHistoryByOrderDTOs = inventoryHistoryBySaleOrderFlatDTOs
                .stream()
                .map(flatDTO -> InventoryHistoryByOrderDTO.builder()
                        .id(flatDTO.getId())
                        .location(flatDTO.getLocation())
                        .warehouseName(flatDTO.getWarehouseName())
                        .changeQty(flatDTO.getChangeQty())
                        .notes(flatDTO.getNotes())
                        .productName(flatDTO.getProductName())
                        .customerName(flatDTO.getCustomerName())
                        .refType(flatDTO.getRefType())
                        .createdAt(flatDTO.getCreatedAt())
                        .supplierSku(flatDTO.getSupplierSku())
                        .inventoryStockId(flatDTO.getInventoryStockId())
                        .build())
                .collect(Collectors.toList());
        return ResponseDTO.<List<InventoryHistoryByOrderDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("在庫履歴取得に成功しました")
                .data(InventoryHistoryByOrderDTOs)
                .build();
    }

}
