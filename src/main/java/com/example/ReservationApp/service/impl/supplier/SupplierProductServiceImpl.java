package com.example.ReservationApp.service.impl.supplier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ReservationApp.dto.ResponseDTO;
import com.example.ReservationApp.dto.response.supplier.CategoryProductsDTO;
import com.example.ReservationApp.dto.response.supplier.SupplierProductInCategoryDTO;
import com.example.ReservationApp.dto.response.supplier.SupplierProductStockFlatDTO;
import com.example.ReservationApp.dto.supplier.SupplierProductDTO;
import com.example.ReservationApp.dto.supplier.SupplierProductPriceHistoryDTO;
import com.example.ReservationApp.entity.product.Category;
import com.example.ReservationApp.entity.product.Product;
import com.example.ReservationApp.entity.supplier.Supplier;
import com.example.ReservationApp.entity.supplier.SupplierProduct;
import com.example.ReservationApp.entity.supplier.SupplierProductPriceHistory;
import com.example.ReservationApp.enums.CategoryStatus;
import com.example.ReservationApp.enums.SupplierProductStatus;
import com.example.ReservationApp.exception.AlreadyExistException;
import com.example.ReservationApp.exception.NotFoundException;
import com.example.ReservationApp.mapper.SupplierProductMapper;
import com.example.ReservationApp.mapper.SupplierProductPriceHistoryMapper;
import com.example.ReservationApp.repository.product.CategoryRepository;
import com.example.ReservationApp.repository.product.ProductRepository;
import com.example.ReservationApp.repository.supplier.SupplierProductPriceHistoryRepository;
import com.example.ReservationApp.repository.supplier.SupplierProductRepository;
import com.example.ReservationApp.repository.supplier.SupplierRepository;
import com.example.ReservationApp.service.supplier.SupplierProductService;

import lombok.RequiredArgsConstructor;

/**
 * 仕入れ商品（SupplierProduct）に関するサービス実装クラス。
 * 主な機能:
 * - 仕入れ商品の作成、取得、更新、無効化
 * - 価格履歴の管理
 */
@Service
@RequiredArgsConstructor
public class SupplierProductServiceImpl implements SupplierProductService {

    private final SupplierProductRepository supplierProductRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierProductMapper supplierProductMapper;
    private final SupplierProductPriceHistoryRepository supplierProductPriceHistoryRepository;
    private final SupplierProductPriceHistoryMapper supplierProductPriceHistoryMapper;

    /**
     * 新しい仕入れ商品を登録する。
     * 初回登録時に現在価格が設定されていれば、価格履歴も作成される。
     *
     * @param supplierId 登録対象の仕入先ID
     * @param productId  登録対象の商品ID
     * @param spDTO      登録情報を持つDTO
     * @return 登録された仕入れ商品のDTOを含むレスポンス
     * @throws NotFoundException     指定した仕入先または商品が存在しない場合
     * @throws AlreadyExistException 指定したSKUが既に使用されている場合
     */
    @Override
    @Transactional
    public ResponseDTO<SupplierProductDTO> createSupplierProduct(Long supplierId, Long productId,
            SupplierProductDTO spDTO) {

        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new NotFoundException("仕入先は存在していません。"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("商品は存在していません。"));

        String normalizedSku = null;
        if (spDTO.getSupplierSku() != null && !spDTO.getSupplierSku().isBlank()) {
            normalizedSku = spDTO.getSupplierSku().toUpperCase();
            boolean exists = supplierProductRepository.existsBySupplierIdAndSupplierSku(supplierId,
                    normalizedSku);
            if (exists) {
                throw new AlreadyExistException("このSKUは既に別の商品に使用されています。");
            }

        }
        SupplierProduct supplierProduct = supplierProductMapper.toEntity(spDTO);
        if (normalizedSku != null) {
            supplierProduct.setSupplierSku(normalizedSku);
        }
        supplierProduct.setSupplier(supplier);
        supplierProduct.setProduct(product);
        supplierProduct.setStatus(SupplierProductStatus.ACTIVE);

        Category category = product.getCategory();
        if (category != null && category.getStatus() != CategoryStatus.ACTIVE) {
            category.setStatus(CategoryStatus.ACTIVE);
            categoryRepository.save(category);
        }

        SupplierProduct savedSp = supplierProductRepository.save(supplierProduct);

        if (savedSp.getCurrentPrice() != null) {
            SupplierProductPriceHistory history = SupplierProductPriceHistory.builder()
                    .supplierProduct(savedSp)
                    .price(savedSp.getCurrentPrice())
                    .effectiveDate(LocalDate.now())
                    .note("初回登録価格")
                    .build();

            supplierProductPriceHistoryRepository.save(history);
        }

        return ResponseDTO.<SupplierProductDTO>builder()
                .status(HttpStatus.OK.value())
                .message("仕入れ商品の登録に成功しました。")
                .data(supplierProductMapper.toDTO(savedSp))
                .build();
    }

    /**
     * 指定した仕入先のアクティブな仕入れ商品一覧を取得する。
     *
     * @param supplierId 取得対象の仕入先ID
     * @return 仕入れ商品のDTOリストを含むレスポンス
     * @throws NotFoundException 指定した仕入先が存在しない場合
     */
    @Override
    public ResponseDTO<List<SupplierProductDTO>> getProductsBySupplier(Long supplierId) {

        if (!supplierRepository.existsById(supplierId))
            throw new NotFoundException("仕入先は存在していません。");

        List<SupplierProduct> supplierProducts = supplierProductRepository.findBySupplierIdAndStatus(supplierId,
                SupplierProductStatus.ACTIVE);

        return ResponseDTO.<List<SupplierProductDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("仕入れ商品の一覧を取得しました。")
                .data(supplierProductMapper.toDTOList(supplierProducts))
                .build();
    }

    /**
     * 指定した仕入れ商品情報を更新する。
     * 更新可能項目:
     * - リードタイム (leadTime)
     * - 現在価格 (currentPrice)
     * - SKU (supplierSku)
     * 価格が変更された場合は価格履歴を追加する。
     *
     * @param spId  更新対象の仕入れ商品ID
     * @param spDTO 更新情報を持つDTO
     * @return 更新後の仕入れ商品DTOを含むレスポンス
     * @throws NotFoundException     指定した仕入れ商品が存在しない場合
     * @throws AlreadyExistException 商品が無効化されている場合、またはSKUが重複する場合
     */
    @Override
    public ResponseDTO<SupplierProductDTO> updateSupplierProduct(Long spId, SupplierProductDTO spDTO) {
        SupplierProduct existingSp = supplierProductRepository.findById(spId)
                .orElseThrow(() -> new NotFoundException("この仕入れ商品は存在していません。"));
        if (existingSp.getStatus() == SupplierProductStatus.INACTIVE) {
            throw new AlreadyExistException("この仕入れ商品は無効化されているため、更新できません。");
        }
        if (spDTO.getLeadTime() != null) {
            existingSp.setLeadTime(spDTO.getLeadTime());
        }
        if (spDTO.getCurrentPrice() != null) {
            addPriceHistoryIfChanged(existingSp, spDTO.getCurrentPrice());
        }
        if (spDTO.getSupplierSku() != null && !spDTO.getSupplierSku().isBlank()) {
            String newSku = spDTO.getSupplierSku().toUpperCase();
            String oldSku = existingSp.getSupplierSku();
            if (!newSku.equalsIgnoreCase(oldSku)) {
                boolean exists = supplierProductRepository
                        .existsBySupplierIdAndSupplierSkuAndIdNot(
                                existingSp.getSupplier().getId(),
                                newSku,
                                existingSp.getId());

                if (exists) {
                    throw new AlreadyExistException("このSKUは既に別の商品に使用されています。");
                }
                existingSp.setSupplierSku(newSku);
            }
        }
        SupplierProduct updatedSp = supplierProductRepository.save(existingSp);
        return ResponseDTO.<SupplierProductDTO>builder()
                .status(HttpStatus.OK.value())
                .message("仕入れ商品の情報を更新しました。")
                .data(supplierProductMapper.toDTO(updatedSp))
                .build();
    }

    /**
     * 全ての仕入れ商品の価格履歴を取得する。
     *
     * @return 価格履歴DTOのリストを含むレスポンス
     */
    @Override
    public ResponseDTO<List<SupplierProductPriceHistoryDTO>> getPriceHistory() {
        List<SupplierProductPriceHistoryDTO> priceHistoryDTOs = supplierProductPriceHistoryMapper
                .toDTOList(supplierProductPriceHistoryRepository.findAll());
        return ResponseDTO.<List<SupplierProductPriceHistoryDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("仕入れ商品の価格履歴を取得しました。")
                .data(priceHistoryDTOs)
                .build();
    }

    /**
     * 指定した仕入れ商品を削除（無効化）する。
     *
     * @param spId 無効化対象の仕入れ商品ID
     * @return 成功メッセージを含むレスポンス
     * @throws NotFoundException 指定した仕入れ商品が存在しない場合
     */
    @Override
    public ResponseDTO<Void> deleteSupplierProduct(Long spId) {
        SupplierProduct supplierProduct = supplierProductRepository.findById(spId)
                .orElseThrow(() -> new NotFoundException("この仕入れ商品は存在していません。"));

        supplierProduct.setStatus(SupplierProductStatus.INACTIVE);

        supplierProductRepository.save(supplierProduct);

        return ResponseDTO.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("仕入れ商品の削除（無効化）に成功しました。")
                .build();
    }

    /**
     * 仕入れ商品の価格が変更された場合に価格履歴を追加する。
     *
     * @param sp       対象の仕入れ商品エンティティ
     * @param newPrice 新しい価格
     */

    private void addPriceHistoryIfChanged(SupplierProduct sp, BigDecimal newPrice) {
        if (sp.getCurrentPrice() == null
                || sp.getCurrentPrice().compareTo(newPrice) != 0) {
            SupplierProductPriceHistory history = SupplierProductPriceHistory.builder()
                    .supplierProduct(sp)
                    .price(newPrice)
                    .effectiveDate(LocalDate.now())
                    .note("価格が更新されました")
                    .build();
            supplierProductPriceHistoryRepository.save(history);
            sp.setCurrentPrice(newPrice);
        }
    }

    @Override
    public ResponseDTO<List<CategoryProductsDTO>> getSupplierProductsWithStock(Long supplierId) {

        if (!supplierRepository.existsById(supplierId)) {
            throw new NotFoundException("この仕入先は存在していません。");
        }
        List<SupplierProductStockFlatDTO> rows = supplierProductRepository.findSupplierProductsWithStock(supplierId);

        Map<String, List<SupplierProductInCategoryDTO>> grouped = rows.stream()
                .collect(Collectors.groupingBy(
                        SupplierProductStockFlatDTO::getCategoryName,
                        LinkedHashMap::new,
                        Collectors.mapping(p -> SupplierProductInCategoryDTO.builder()
                                .id(p.getId())
                                .sku(p.getSku())
                                .product(p.getProductName())
                                .price(p.getPrice())
                                .stock(p.getTotalQuantity())
                                .build(), Collectors.toList())));

        List<CategoryProductsDTO> responseData = grouped.entrySet().stream()
                .map(entry -> CategoryProductsDTO.builder()
                        .categoryName(entry.getKey())
                        .products(entry.getValue())
                        .build())
                .collect(Collectors.toList());
        return ResponseDTO.<List<CategoryProductsDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("取得に成功しました。")
                .data(responseData)
                .build();
    }
}
