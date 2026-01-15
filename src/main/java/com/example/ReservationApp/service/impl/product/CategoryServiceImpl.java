package com.example.ReservationApp.service.impl.product;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.ReservationApp.dto.ResponseDTO;
import com.example.ReservationApp.dto.response.product.CategoryDTO;
import com.example.ReservationApp.dto.response.product.CategorySummariesDTO;
import com.example.ReservationApp.dto.response.product.CategorySummaryDTO;
import com.example.ReservationApp.dto.response.product.ProductStockDTO;
import com.example.ReservationApp.dto.response.product.CategorySummaryFlatDTO;
import com.example.ReservationApp.dto.response.product.StockDTO;
import com.example.ReservationApp.dto.response.product.SupplierPriceDTO;
import com.example.ReservationApp.entity.product.Category;
import com.example.ReservationApp.enums.CategoryStatus;
import com.example.ReservationApp.exception.NotFoundException;
import com.example.ReservationApp.mapper.CategoryMapper;
import com.example.ReservationApp.repository.product.CategoryRepository;
import com.example.ReservationApp.exception.AlreadyExistException;
import com.example.ReservationApp.exception.CannotDeleteException;
import com.example.ReservationApp.service.product.CategoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * CategoryServiceImplクラスは、カテゴリのCRUD操作を実装。
 * ModelMapperを使用してEntityとDTOの変換を行い、
 * カスタム例外やHTTPステータスでエラー処理を行い。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

        private final CategoryRepository categoryRepository;
        private final CategoryMapper categoryMapper;

        /**
         * 新しいカテゴリを作成。
         * すでに同じ名前のカテゴリが存在する場合、BAD_REQUESTをスロー。
         * 
         * @param categoryDTO 作成するカテゴリのDTO
         * @return 作成結果を含むResponseDTO
         */
        @Override
        public ResponseDTO<CategoryDTO> createCategory(CategoryDTO categoryDTO) {
                if (categoryRepository.existsByname(categoryDTO.getName())) {
                        throw new AlreadyExistException("このカテゴリはすでに存在しています");
                }
                Category newCategory = categoryMapper.toEntity(categoryDTO);
                categoryRepository.save(newCategory);
                categoryDTO.setId(newCategory.getId());
                return ResponseDTO.<CategoryDTO>builder()
                                .status(HttpStatus.OK.value())
                                .message("新しいカテゴリの追加に成功しました")
                                .data(categoryDTO)
                                .build();
        }

        /**
         * すべてのカテゴリを取得。
         * 
         * @return カテゴリ一覧を含むResponseDTO
         */
        @Override
        public ResponseDTO<List<CategoryDTO>> getAllCategories() {
                List<Category> categories = categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
                List<CategoryDTO> categoryDTOs = categories.stream()
                                .map(category -> categoryMapper.toDTO(category)).collect(Collectors.toList());
                return ResponseDTO.<List<CategoryDTO>>builder()
                                .status(HttpStatus.OK.value())
                                .message("全てのカテゴリの取得に成功しました")
                                .data(categoryDTOs)
                                .build();
        }

        /**
         * 指定IDのカテゴリを取得。
         * 存在しない場合はNotFoundExceptionをスロー。
         * 
         * @param id 取得対象のカテゴリID
         * @return 取得したカテゴリ情報を含むResponseDTO
         */
        @Override
        public ResponseDTO<CategoryDTO> getCategoryById(Long id) {
                Category category = categoryRepository.findById(id)
                                .orElseThrow(() -> new NotFoundException(("このカテゴリは存在していません。ID:")));

                return ResponseDTO.<CategoryDTO>builder()
                                .status(HttpStatus.OK.value())
                                .message("成功しました")
                                .data(categoryMapper.toDTO(category))
                                .build();

        }

        @Override
        public ResponseDTO<CategoryDTO> getCategoryByName(String name) {
                Category category = categoryRepository.findByName(name)
                                .orElseThrow(() -> new NotFoundException(("このカテゴリは存在していません")));
                return ResponseDTO.<CategoryDTO>builder()
                                .status(HttpStatus.OK.value())
                                .message("成功しました")
                                .data(categoryMapper.toDTO(category))
                                .build();

        }

        /**
         * 指定IDのカテゴリ情報を更新。
         * nullの値はマッピング時にスキップ。
         * 存在しない場合はNotFoundExceptionをスロー。
         * 
         * @param id          更新対象のカテゴリID
         * @param categoryDTO 更新内容を含むCategoryDTO
         * @return 更新結果を含むResponseDTO
         */
        @Override
        public ResponseDTO<CategoryDTO> updateCategory(Long id, CategoryDTO categoryDTO) {
                Category existingCategory = categoryRepository.findById(id)
                                .orElseThrow(() -> new NotFoundException("このカテゴリは見つかりません"));

                if (categoryDTO.getName() != null && !categoryDTO.getName().isBlank()) {
                        existingCategory.setName(categoryDTO.getName());
                }
                if (categoryDTO.getDescription() != null && !categoryDTO.getDescription().isBlank()) {
                        existingCategory.setDescription(categoryDTO.getDescription());
                }
                if (categoryDTO.getImageUrl() != null && !categoryDTO.getImageUrl().isBlank()) {
                        existingCategory.setImageUrl(categoryDTO.getImageUrl());
                }
                if (categoryDTO.getStatus() != null) {
                        existingCategory.setStatus(categoryDTO.getStatus());
                }

                categoryRepository.save(existingCategory);

                return ResponseDTO.<CategoryDTO>builder()
                                .status(HttpStatus.OK.value())
                                .message("編集に成功しました")
                                .data(categoryMapper.toDTO(existingCategory))
                                .build();
        }

        /**
         * 指定したIDのカテゴリを削除。
         * 存在しない場合はBAD_REQUESTをスロー。
         * 
         * @param id 削除対象のカテゴリID
         * @return 削除結果を含むResponseDTO
         */
        @Override
        public ResponseDTO<Void> deleteCategory(Long id) {
                if (!categoryRepository.existsById(id)) {
                        throw new NotFoundException("このカテゴリはありません");
                }
                try {
                        categoryRepository.deleteById(id);

                        // ★ flush() を呼び出すことで、DELETE SQLを即時DBに送信する
                        // flushしない場合、トランザクションcommit時にSQLが実行され、
                        // 外部キー制約違反（DataIntegrityViolationException）を
                        // ここでcatchできない
                        categoryRepository.flush();
                } catch (DataIntegrityViolationException e) {

                        // 商品がこのカテゴリを参照している場合に発生
                        // DB制約違反を業務例外に変換
                        throw new CannotDeleteException(
                                        "このカテゴリに紐づく商品が存在するため、削除できません");
                }

                return ResponseDTO.<Void>builder()
                                .status(HttpStatus.OK.value())
                                .message("削除に成功しました")
                                .build();
        }

        /**
         * ステータスがACTIVEのカテゴリ一覧を取得する。
         *
         * @return アクティブなカテゴリDTOのリストを含むレスポンス
         */
        @Override
        public ResponseDTO<List<CategoryDTO>> getActiveCategories() {
                List<Category> categories = categoryRepository.findByStatus(CategoryStatus.ACTIVE);
                List<CategoryDTO> categoryDTOs = categoryMapper.toDTOList(categories);

                return ResponseDTO.<List<CategoryDTO>>builder()
                                .status(HttpStatus.OK.value())
                                .message("成功しました")
                                .data(categoryDTOs)
                                .build();
        }

        @Override
        public ResponseDTO<List<CategorySummariesDTO>> getAllCategorySummaries() {
                List<CategorySummariesDTO> summaryDTOs = categoryRepository.getAllCategorySummary();

                return ResponseDTO.<List<CategorySummariesDTO>>builder()
                                .status(HttpStatus.OK.value())
                                .message("取得に成功しました")
                                .data(summaryDTOs)
                                .build();
        }

        @Override
        public ResponseDTO<CategorySummaryDTO> getCategorySummariesById(Long categoryId) {
                Category category = categoryRepository.findById(categoryId)
                                .orElseThrow(() -> new NotFoundException("このカテゴリは存在していません"));

                List<CategorySummaryFlatDTO> rows = categoryRepository.getCategorySummaryById(categoryId);

                CategorySummaryDTO categorySummaryDTO = CategorySummaryDTO.builder()
                                .categoryId(categoryId)
                                .categoryName(category.getName())
                                .products(buildProducts(rows))
                                .build();
                return ResponseDTO.<CategorySummaryDTO>builder()
                                .status(HttpStatus.OK.value())
                                .message("取得に成功しました")
                                .data(categorySummaryDTO)
                                .build();
        }

        public List<ProductStockDTO> buildProducts(List<CategorySummaryFlatDTO> rows) {

                Map<String, ProductStockDTO> productMap = new LinkedHashMap<>();

                for (CategorySummaryFlatDTO r : rows) {

                        ProductStockDTO product = productMap.computeIfAbsent(r.getProductName(), k -> {
                                ProductStockDTO p = ProductStockDTO.builder()
                                                .productName(r.getProductName())
                                                .suppliers(new ArrayList<>())
                                                .stocks(new ArrayList<>())
                                                .build();

                                return p;
                        });

                        // Supplier (dedupe)
                        if (r.getSupplierId() != null) {
                                boolean exists = product.getSuppliers().stream()
                                                .anyMatch(s -> s.getSupplierName().equals(r.getSupplierName()));

                                if (!exists) {
                                        product.getSuppliers().add(
                                                        SupplierPriceDTO.builder()
                                                                        .supplierName(r.getSupplierName())
                                                                        .price(r.getPrice())
                                                                        .build());
                                }
                        }

                        // Stock
                        if (r.getQuantity() != null) {
                                product.getStocks().add(
                                                StockDTO.builder()
                                                                .quantity(r.getQuantity())
                                                                .warehouse(r.getWarehouse())
                                                                .build());
                        }
                }

                return new ArrayList<>(productMap.values());
        }

}
