package com.example.ReservationApp.service.impl.product;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.ReservationApp.dto.ResponseDTO;
import com.example.ReservationApp.dto.response.inventory.InventoryStockDTO;
import com.example.ReservationApp.dto.response.inventory.StockHistoryDTO;
import com.example.ReservationApp.dto.response.product.ProductDTO;
import com.example.ReservationApp.dto.response.product.ProductInfoDTO;
import com.example.ReservationApp.dto.response.product.ProductInfoDetailDTO;
import com.example.ReservationApp.dto.response.product.ProductInfoFlatDTO;
import com.example.ReservationApp.dto.response.product.ProductWithSkuByCategoryDTO;
import com.example.ReservationApp.dto.response.product.SumReceivedGroupByProductDTO;
import com.example.ReservationApp.dto.response.product.SupplierPriceDTO;
import com.example.ReservationApp.entity.inventory.InventoryStock;
import com.example.ReservationApp.entity.product.Category;
import com.example.ReservationApp.entity.product.Product;
import com.example.ReservationApp.entity.supplier.SupplierProduct;
import com.example.ReservationApp.enums.ProductStatus;
import com.example.ReservationApp.enums.StockChangeType;
import com.example.ReservationApp.enums.SupplierProductStatus;
import com.example.ReservationApp.exception.AlreadyExistException;
import com.example.ReservationApp.exception.CannotDeleteException;
import com.example.ReservationApp.exception.NotFoundException;
import com.example.ReservationApp.mapper.InventoryStockMapper;
import com.example.ReservationApp.mapper.ProductMapper;
import com.example.ReservationApp.mapper.SupplierProductMapper;
import com.example.ReservationApp.repository.inventory.InventoryStockRepository;
import com.example.ReservationApp.repository.inventory.StockHistoryRepository;
import com.example.ReservationApp.repository.product.CategoryRepository;
import com.example.ReservationApp.repository.product.ProductRepository;
import com.example.ReservationApp.repository.supplier.SupplierProductRepository;
import com.example.ReservationApp.repository.transaction.PurchaseOrderRepository;
import com.example.ReservationApp.service.product.ProductService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 商品（Product）に関するサービス実装クラス。
 * 主な機能:
 * - 商品の追加、取得、更新、削除
 * - カテゴリごとの商品取得
 * - 商品検索
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryRepository categoryRepository;
    private final SupplierProductRepository supplierProductRepository;
    private final StockHistoryRepository stockHistoryRepository;
    private final InventoryStockRepository inventoryStockRepository;
    private final PurchaseOrderRepository poRepository;
    private final SupplierProductMapper supplierProductMapper;
    private final InventoryStockMapper inventoryStockMapper;

    /**
     * 新しい商品を作成する。
     * カテゴリ名が指定されている場合、存在するカテゴリに紐付ける。
     *
     * @param productDTO 作成する商品の情報を持つDTO
     * @return 作成された商品のDTOを含むレスポンス
     * @throws NotFoundException 指定したカテゴリが存在しない場合
     */
    @Override
    public ResponseDTO<ProductDTO> createProduct(ProductDTO productDTO) {
        Product createdProduct = productMapper.toEntity(productDTO);
        if (productDTO.getCategoryName() != null && !productDTO.getCategoryName().isBlank()) {
            Category existingCategory = categoryRepository.findByName(productDTO.getCategoryName())
                    .orElseThrow(() -> new NotFoundException("このカテゴリに存在していません"));
            createdProduct.setCategory(existingCategory);
        }
        if (productRepository.existsByProductCode(productDTO.getProductCode())) {
            throw new AlreadyExistException("商品コードは既に登録されています");
        }
        if (productRepository.existsByName(productDTO.getName())) {
            throw new AlreadyExistException("商品名は既に登録されています");
        }
        Product savedProduct = productRepository.save(createdProduct);

        return ResponseDTO.<ProductDTO>builder()
                .status(HttpStatus.OK.value())
                .message("新しい商品の追加に成功しました")
                .data(productMapper.toDTO(savedProduct))
                .build();
    }

    /**
     * 全ての商品を取得する。
     *
     * @return 商品DTOのリストを含むレスポンス
     */
    @Override
    public ResponseDTO<List<ProductDTO>> getAllProducts() {

        List<Product> products = productRepository.findAll();
        List<ProductDTO> productDTOs = productMapper.toDTOList(products);

        return ResponseDTO.<List<ProductDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("全て商品の取得に成功しました")
                .data(productDTOs)
                .build();
    }

    /**
     * 指定IDの商品を取得する。
     *
     * @param id 取得対象の商品ID
     * @return 指定IDの商品DTOを含むレスポンス
     * @throws NotFoundException 指定した商品が存在しない場合
     */
    @Override
    public ResponseDTO<ProductDTO> getProductById(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(("この商品は見つかりません")));

        return ResponseDTO.<ProductDTO>builder()
                .status(HttpStatus.OK.value())
                .message(id + "-商品の取得に成功しました")
                .data(productMapper.toDTO(product))
                .build();
    }

    /**
     * 指定IDの商品情報を更新する。
     * 更新可能項目:
     * - カテゴリ
     * - 名前
     * - 商品コード
     * - 説明
     * - ステータス
     *
     * @param id         更新対象の商品ID
     * @param productDTO 更新情報を持つDTO
     * @return 更新後の商品DTOを含むレスポンス
     * @throws NotFoundException 指定した商品またはカテゴリが存在しない場合
     */
    @Override
    public ResponseDTO<ProductDTO> updateProduct(Long id, ProductDTO productDTO) {

        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(("この商品は存在していません")));

        if (productDTO.getCategoryName() != null && !productDTO.getCategoryName().isBlank()) {
            existingProduct.setCategory(
                    categoryRepository.findByName(productDTO.getCategoryName())
                            .orElseThrow(() -> new NotFoundException(productDTO.getCategoryName() + "カテゴリは見つかりません")));
        }

        // 商品名更新
        if (productDTO.getName() != null && !productDTO.getName().isBlank()) {
            if (!existingProduct.getName().equals(productDTO.getName())
                    && productRepository.existsByName(productDTO.getName())) {
                throw new AlreadyExistException("商品名は既に登録されています");
            }
            existingProduct.setName(productDTO.getName());
        }

        // 商品コード更新
        if (productDTO.getProductCode() != null && !productDTO.getProductCode().isBlank()) {
            if (!existingProduct.getProductCode().equals(productDTO.getProductCode())
                    && productRepository.existsByProductCode(productDTO.getProductCode())) {
                throw new AlreadyExistException("商品コードは既に登録されています");
            }
            existingProduct.setProductCode(productDTO.getProductCode());
        }
        if (productDTO.getDescription() != null && !productDTO.getDescription().isBlank()) {
            existingProduct.setDescription(productDTO.getDescription());
        }
        if (productDTO.getStatus() != null) {
            existingProduct.setStatus(productDTO.getStatus());
            if(ProductStatus.INACTIVE.equals(productDTO.getStatus())) {
                List<SupplierProduct> supplierProducts = supplierProductRepository.findByProductId(id);
                supplierProducts.forEach(sp -> sp.setStatus(SupplierProductStatus.INACTIVE));
                supplierProductRepository.saveAll(supplierProducts);
            }
        }
        if (productDTO.getUnit() != null && !productDTO.getUnit().isBlank()) {
            existingProduct.setUnit(productDTO.getUnit());
        }
        Product updatedProduct = productRepository.save(existingProduct);
        return ResponseDTO.<ProductDTO>builder()
                .status(HttpStatus.OK.value())
                .message("更新に成功しました")
                .data(productMapper.toDTO(updatedProduct))
                .build();
    }

    /**
     * 指定IDの商品を削除する。
     *
     * @param id 削除対象の商品ID
     * @return 成功メッセージを含むレスポンス
     * @throws NotFoundException 指定した商品が存在しない場合
     */
    @Override
    @Transactional
    public ResponseDTO<Void> deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new NotFoundException("この商品は存在していません");
        }
        try {
            productRepository.deleteById(id);

            // ★ flush() を呼び出すことで、DELETE SQLを即時DBに送信する
            // flushしない場合、トランザクションcommit時にSQLが実行され、
            // DataIntegrityViolationExceptionをここでcatchできない
            productRepository.flush();
        } catch (DataIntegrityViolationException e) {

            // 外部キー制約（supplier_products など）が存在する場合に発生
            // DBレベルの制約違反を業務例外に変換する
            throw new CannotDeleteException(
                    "仕入先情報が存在するため、商品を削除できません");
        }
        return ResponseDTO.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("削除に成功しました")
                .build();
    }

    /**
     * 指定カテゴリに属する商品一覧を取得する。
     *
     * @param categoryId 取得対象のカテゴリID
     * @return 指定カテゴリに属する商品DTOのリストを含むレスポンス
     * @throws NotFoundException 指定したカテゴリが存在しない場合
     */
    @Override
    public ResponseDTO<List<ProductDTO>> getProductsByCategory(Long categoryId) {
        boolean existingCategory = categoryRepository.existsById(categoryId);
        if (!existingCategory) {
            throw new NotFoundException("このカテゴリは存在していません");
        }
        List<Product> products = productRepository.findByCategoryId(categoryId);
        List<ProductDTO> productDTOs = productMapper.toDTOList(products);
        return ResponseDTO.<List<ProductDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("取得に成功しました")
                .data(productDTOs)
                .build();
    }

    /**
     * キーワードで商品を検索する。
     *
     * @param keyword 検索するキーワード
     * @return 検索結果の商品DTOのリストを含むレスポンス
     * @throws ResponseStatusException キーワードが空の場合（BAD_REQUEST）
     */
    @Override
    public ResponseDTO<List<ProductDTO>> searchProducts(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "検索キーワードを入力してください");
        }
        List<Product> products = productRepository.searchProducts(keyword);
        List<ProductDTO> productDTOs = productMapper.toDTOList(products);
        if (productDTOs.isEmpty()) {
            return ResponseDTO.<List<ProductDTO>>builder()
                    .status(HttpStatus.OK.value())
                    .message("該当する商品がありません")
                    .data(productDTOs)
                    .build();
        }
        return ResponseDTO.<List<ProductDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("取得に成功しました")
                .data(productDTOs)
                .build();
    }

    @Override
    public ResponseDTO<ProductInfoDTO> getProductWithSupplierAndStockById(Long productId) {
        // DBから取得した「フラット構造」の商品＋仕入先＋在庫データ
        List<ProductInfoFlatDTO> productInfoFlatDTO = productRepository.getProductWithSupplierAndStockById(productId);

        Map<String, ProductInfoDTO> productMap = builtProductMap(productInfoFlatDTO);
        ProductInfoDTO result = productMap.values().iterator().next();
        return ResponseDTO.<ProductInfoDTO>builder()
                .status(HttpStatus.OK.value())
                .message("取得に成功しました")
                .data(result)
                .build();
    }

    @Override
    public ResponseDTO<List<ProductInfoDTO>> getAllProductWithSupplierAndStock() {

        // DBから取得した「フラット構造」の商品＋仕入先＋在庫データ
        List<ProductInfoFlatDTO> productInfoFlatDTO = productRepository.getAllProductWithSupplierAndStock();

        Map<String, ProductInfoDTO> productMap = builtProductMap(productInfoFlatDTO);

        // Mapに格納された商品DTOをListに変換してレスポンスとして返却
        return ResponseDTO.<List<ProductInfoDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("取得に成功しました")
                .data(new ArrayList<>(productMap.values()))
                .build();
    }

    private Map<String, ProductInfoDTO> builtProductMap(List<ProductInfoFlatDTO> rows) {

        // 商品名をキーにして、同一商品の重複生成を防ぐためのMap
        // LinkedHashMapを使うことで、取得順（SQLのORDER BY）を保持する
        Map<String, ProductInfoDTO> productMap = new LinkedHashMap<>();

        // 商品ごとの仕入先ID管理用
        Map<String, Set<Long>> supplierMap = new HashMap<>();

        // SQL結果を1行ずつ処理
        for (ProductInfoFlatDTO row : rows) {

            // すでに存在する商品であれば取得、
            // 存在しない場合は新しくProductInfoDTOを生成してMapに登録する
            ProductInfoDTO productInfoDTO = productMap.computeIfAbsent(row.getProductName(), k -> {

                // 初回のみ商品DTOを生成
                return ProductInfoDTO.builder()
                        .id(row.getId())
                        .productName(row.getProductName())
                        .code(row.getProductCode())
                        .categoryName(row.getCategoryName())
                        .totalStock(row.getTotalQuantity())
                        .status(row.getProductStatus())
                        .supplier(new ArrayList<>())
                        .build();
            });

            /// 仕入先が存在する場合のみ処理
            if (row.getSupplierId() != null) {

                // 商品ごとの仕入先ID Set を取得 or 作成
                Set<Long> supplierIds = supplierMap.computeIfAbsent(
                        row.getProductName(),
                        k -> new HashSet<>());

                // 初登場の仕入先だけ追加（O(1)）
                if (supplierIds.add(row.getSupplierId())) {
                    productInfoDTO.getSupplier().add(
                            SupplierPriceDTO.builder()
                                    .supplierName(row.getSupplierName())
                                    .price(row.getPrice())
                                    .build());
                }
            }
        }

        return productMap;
    }

    @Override
    public ResponseDTO<ProductInfoDetailDTO> getProductInfoDetail(Long productId) {

        List<Object[]> products = productRepository.findProductWithCatName(productId);
        if (products.isEmpty()) {
            throw new NotFoundException("この商品は存在していません");
        }
        Object[] product = products.get(0);
        ProductDTO productDTO = ProductDTO.builder()
                .id(((Number) product[0]).longValue())
                .name((String) product[1])
                .productCode((String) product[2])
                .description((String) product[3])
                .unit((String) product[4])
                .status(ProductStatus.valueOf((String) product[5]))
                .categoryName((String) product[6])
                .totalStock(0)
                .build();

        List<Object[]> suppliers = supplierProductRepository.getSupplierAndPriceByProductId(productId);
        List<SupplierPriceDTO> supplierDTOs = new ArrayList<>();
        for (Object[] r : suppliers) {
            SupplierPriceDTO dto = SupplierPriceDTO.builder()
                    .supplierId(((Number) r[0]).longValue())
                    .supplierName((String) r[1])
                    .sku((String) r[2])
                    .price((BigDecimal) r[3])
                    .build();
            supplierDTOs.add(dto);
        }

        List<Object[]> historyList = stockHistoryRepository
                .findHistoryWithQuantiyAndTypeByProductId(productId);
        List<StockHistoryDTO> stockHistoryDTOs = new ArrayList<>();
        for (Object[] r : historyList) {
            StockHistoryDTO dto = StockHistoryDTO.builder()
                    .createdAt(((Timestamp) r[0]).toLocalDateTime())
                    .type((StockChangeType.valueOf((String) r[1])))
                    .changeQty(((Number) r[2]).intValue())
                    .build();
            stockHistoryDTOs.add(dto);
        }
        List<Object[]> stocks = inventoryStockRepository
                .findStockWithWarehouseAndQtyByProductId(productId);
        List<InventoryStockDTO> stockDTOs = new ArrayList<>();
        for (Object[] r : stocks) {
            InventoryStockDTO dto = InventoryStockDTO.builder()
                    .quantity(((Number) r[0]).intValue())
                    .warehouseName((String) r[1])
                    .build();
            stockDTOs.add(dto);
        }

        productDTO.setTotalStock(stockDTOs.stream()
                .mapToInt(InventoryStockDTO::getQuantity)
                .sum());
        ProductInfoDetailDTO productInfoDetailDTO = ProductInfoDetailDTO.builder()
                .productDTO(productDTO)
                .supplierPriceDTO(supplierDTOs)
                .stockHistoryDTO(stockHistoryDTOs)
                .inventoryStockDTO(stockDTOs)
                .build();
        return ResponseDTO.<ProductInfoDetailDTO>builder()
                .status(HttpStatus.OK.value())
                .message("取得に成功しました")
                .data(productInfoDetailDTO)
                .build();
    }

    public ResponseDTO<List<SumReceivedGroupByProductDTO>> getSumReceivedQtyByPoGroupByProduct(Long poId) {

        if (!poRepository.existsById(poId)) {
            throw new NotFoundException("この注文書はは存在していません");
        }
        List<Object[]> rows = stockHistoryRepository.sumReceivedQtyByPoGroupByProduct(poId);
        List<SumReceivedGroupByProductDTO> result = new ArrayList<>();
        for (Object[] row : rows) {
            Long productId = (Long) row[0];
            Long receivedQty = (Long) row[1];
            String sku = (String) row[2];

            SumReceivedGroupByProductDTO dto = SumReceivedGroupByProductDTO.builder()
                    .productId(productId)
                    .receivedQty(receivedQty)
                    .sku(sku)
                    .build();

            result.add(dto);
        }
        return ResponseDTO.<List<SumReceivedGroupByProductDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("受領数量の集計が正常に取得されました")
                .data(result)
                .build();
    }

    @Override
    public ResponseDTO<List<ProductWithSkuByCategoryDTO>> getAllSupllierProductWithSkuByCategory(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new NotFoundException(("このカテゴリは存在していません。ID:" + categoryId));
        }

        List<ProductWithSkuByCategoryDTO> summaryDTOs = productRepository
                .findAllSupllierProductWithSkuByCategory(categoryId);

        return ResponseDTO.<List<ProductWithSkuByCategoryDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("取得に成功しました")
                .data(summaryDTOs)
                .build();
    }

    @Override
    public ResponseDTO<List<InventoryStockDTO>> getAllProductsWithInventoryOptional() {

        List<InventoryStock> result = new ArrayList<>();
        List<Product> products = productRepository.findAllProductsWithInventoryOptional();
        for (Product product : products) {
            List<InventoryStock> stocks = product.getInventoryStocks();
            if (stocks.isEmpty()) {
                InventoryStock fakeInventoryStock = new InventoryStock();
                fakeInventoryStock.setProduct(product);
                fakeInventoryStock.setQuantity(0);
                fakeInventoryStock.setVirtual(true);
                result.add(fakeInventoryStock);
            } else
                result.addAll(stocks);
        }
        List<InventoryStockDTO> inventoryStockDTOs = result.stream()
                .map(stock -> {
                    InventoryStockDTO inventoryStockDTO = inventoryStockMapper.toDTO(stock);
                    inventoryStockDTO.setProduct(productMapper.toDTO(stock.getProduct()));
                    inventoryStockDTO.setSupplierProduct(supplierProductMapper.toDTO(stock.getSupplierProduct()));
                    return inventoryStockDTO;
                })
                .collect(Collectors.toList());
        return ResponseDTO.<List<InventoryStockDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("取得に成功しました")
                .data(inventoryStockDTOs)
                .build();
    }

}
