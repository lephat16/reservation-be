package com.example.ReservationApp.service.impl.inventory;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.ReservationApp.dto.ResponseDTO;
import com.example.ReservationApp.dto.response.inventory.WarehouseDTO;
import com.example.ReservationApp.dto.response.inventory.WarehouseWithTotalChangedQtyDTO;
import com.example.ReservationApp.entity.inventory.Warehouse;
import com.example.ReservationApp.exception.BadRequestException;
import com.example.ReservationApp.exception.NotFoundException;
import com.example.ReservationApp.mapper.InventoryStockMapper;
import com.example.ReservationApp.mapper.WarehouseMapper;
import com.example.ReservationApp.repository.inventory.WarehouseRepository;
import com.example.ReservationApp.service.inventory.WarehouseService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final InventoryStockMapper inventoryStockMapper;
    private final WarehouseMapper warehouseMapper;

    /**
     * 新しい倉庫を作成します。
     *
     * @param warehouseDTO 作成する倉庫情報
     * @return 作成された倉庫DTO
     */
    @Override
    public ResponseDTO<WarehouseDTO> createWarehouse(WarehouseDTO warehouseDTO) {
        // DTOからエンティティに変換
        Warehouse warehouse = warehouseMapper.toEntity(warehouseDTO);

        // DBに保存
        warehouseRepository.save(warehouse);

        // 保存結果をDTOで返却
        return ResponseDTO.<WarehouseDTO>builder()
                .status(HttpStatus.OK.value())
                .message("倉庫の新規登録に成功しました")
                .data(warehouseMapper.toDTO(warehouse))
                .build();
    }

    /**
     * すべての倉庫情報を取得します。
     *
     * @return 倉庫DTOリスト
     */
    @Override
    public ResponseDTO<List<WarehouseDTO>> getAllWarehouse() {
        // 全件取得
        List<Warehouse> warehouses = warehouseRepository.findAllWithStocks();

        // エンティティをDTOに変換
        List<WarehouseDTO> warehouseDTOs = warehouses.stream()
                .map(wh -> {
                    WarehouseDTO warehouseDTO = warehouseMapper.toDTO(wh);
                    warehouseDTO.setStocks(inventoryStockMapper.toDTOList(wh.getInventoryStocks()));
                    return warehouseDTO;
                })
                .collect(Collectors.toList());

        return ResponseDTO.<List<WarehouseDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("倉庫一覧の取得に成功しました")
                .data(warehouseDTOs)
                .build();
    }

    public ResponseDTO<List<WarehouseDTO>> getAllWarehouseWithLocation() {

        List<WarehouseDTO> warehouseDTOs = warehouseRepository.findActiveWarehousesWithLocation();

        return ResponseDTO.<List<WarehouseDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("倉庫一覧の取得に成功しました")
                .data(warehouseDTOs)
                .build();
    }

    /**
     * IDから倉庫情報を取得します。
     *
     * @param warehouseId 倉庫ID
     * @return 倉庫DTO
     */
    @Override
    public ResponseDTO<WarehouseDTO> getWarehouseById(Long warehouseId) {
        // 指定IDの倉庫を取得。存在しなければ例外
        Warehouse warehouse = warehouseRepository.findByIdWithStocks(warehouseId)
                .orElseThrow(() -> new NotFoundException("倉庫が見つかりません。ID = " + warehouseId));

        WarehouseDTO warehouseDTO = warehouseMapper.toDTO(warehouse);
        warehouseDTO.setStocks(
                inventoryStockMapper.toDTOList(warehouse.getInventoryStocks()));

        return ResponseDTO.<WarehouseDTO>builder()
                .status(HttpStatus.OK.value())
                .message("倉庫情報の取得に成功しました")
                .data(warehouseDTO)
                .build();
    }

    @Override
    public ResponseDTO<List<WarehouseDTO>> getWarehouseBySkuWithStocks(String sku) {
        // 全件取得
        List<Warehouse> warehouses = warehouseRepository.findAllBySkuWithStocks(sku);

        // エンティティをDTOに変換
        List<WarehouseDTO> warehouseDTOs = warehouses.stream()
                .map(wh -> {
                    // WarehouseDTO warehouseDTO = warehouseMapper.toDTO(wh);
                    // warehouseDTO.setStocks(inventoryStockMapper.toDTOList(wh.getInventoryStocks()));
                    WarehouseDTO warehouseDTO = new WarehouseDTO(
                            wh.getId(),
                            wh.getName(),
                            wh.getLocation());
                    return warehouseDTO;
                })
                .collect(Collectors.toList());

        return ResponseDTO.<List<WarehouseDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("倉庫一覧の取得に成功しました")
                .data(warehouseDTOs)
                .build();
    }

    /**
     * 倉庫の所在地で検索します。
     *
     * @param location 倉庫住所（部分一致）
     * @return 倉庫DTOリスト
     */
    @Override
    public ResponseDTO<List<WarehouseDTO>> getWarehouseByLocation(String location) {
        // 住所に部分一致する倉庫を検索
        List<Warehouse> warehouses = warehouseRepository.findByLocationContainingIgnoreCase(location);

        // 見つからなければ例外
        if (warehouses.isEmpty()) {
            throw new NotFoundException("該当する倉庫が見つかりません。住所 = " + location);
        }

        return ResponseDTO.<List<WarehouseDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("倉庫情報の取得に成功しました")
                .data(warehouseMapper.toDTOList(warehouses))
                .build();
    }

    /**
     * 倉庫情報を更新します。
     *
     * @param warehouseId  更新対象の倉庫ID
     * @param warehouseDTO 更新内容
     * @return 更新後の倉庫DTO
     */
    @Override
    public ResponseDTO<WarehouseDTO> updateWarehouse(Long warehouseId, WarehouseDTO warehouseDTO) {
        // 対象倉庫を取得
        Warehouse existingWarehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new NotFoundException("倉庫は存在していません。ID = " + warehouseId));

        // 各フィールドがnullでなければ更新
        if (warehouseDTO.getName() != null && !warehouseDTO.getName().isBlank()) {
            existingWarehouse.setName(warehouseDTO.getName());
        }
        if (warehouseDTO.getStockLimit() != null && warehouseDTO.getStockLimit() > 0) {
            existingWarehouse.setStockLimit(warehouseDTO.getStockLimit());
        }
        if (warehouseDTO.getLocation() != null && !warehouseDTO.getLocation().isBlank()) {
            existingWarehouse.setLocation(warehouseDTO.getLocation());
        }
        if (warehouseDTO.getStatus() != null) {
            existingWarehouse.setStatus(warehouseDTO.getStatus());
        }

        // 更新をDBに保存
        Warehouse updatedWarehouse = warehouseRepository.save(existingWarehouse);

        return ResponseDTO.<WarehouseDTO>builder()
                .status(HttpStatus.OK.value())
                .message("倉庫情報の更新に成功しました")
                .data(warehouseMapper.toDTO(updatedWarehouse))
                .build();
    }

    /**
     * 倉庫を削除します。
     *
     * @param warehouseId 削除対象の倉庫ID
     * @return 処理結果
     */
    @Override
    public ResponseDTO<Void> deleteWarehouse(Long warehouseId) {
        // 対象倉庫を取得。存在しなければ例外
        Warehouse existingWarehouse = warehouseRepository.findByIdWithStocks(warehouseId)
                .orElseThrow(() -> new NotFoundException("倉庫は存在していません。ID = " + warehouseId));
        boolean hasStockLeft = existingWarehouse.getInventoryStocks().stream()
                .anyMatch(stock -> stock.getQuantity() > 0);
        if (hasStockLeft) {
            throw new BadRequestException("倉庫には在庫が残っているため、削除できません。");
        }
        // 削除
        warehouseRepository.delete(existingWarehouse);

        return ResponseDTO.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("倉庫の削除に成功しました")
                .build();
    }

    @Override
    public ResponseDTO<List<WarehouseWithTotalChangedQtyDTO>> getWarehouseWithTotalChangedQty() {
        // 住所に部分一致する倉庫を検索
        List<WarehouseWithTotalChangedQtyDTO> warehouses = warehouseRepository.findWarehouseWithTotalChangedQty();

        return ResponseDTO.<List<WarehouseWithTotalChangedQtyDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("倉庫情報の取得に成功しました")
                .data(warehouses)
                .build();
    }
}
