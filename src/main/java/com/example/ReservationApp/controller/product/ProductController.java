package com.example.ReservationApp.controller.product;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ReservationApp.dto.ResponseDTO;
import com.example.ReservationApp.dto.response.inventory.InventoryStockDTO;
import com.example.ReservationApp.dto.response.product.ProductDTO;
import com.example.ReservationApp.dto.response.product.ProductInfoDTO;
import com.example.ReservationApp.dto.response.product.ProductInfoDetailDTO;
import com.example.ReservationApp.dto.response.product.ProductWithSkuByCategoryDTO;
import com.example.ReservationApp.dto.response.product.SumReceivedGroupByProductDTO;
import com.example.ReservationApp.service.product.ProductService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping("/add-prod")
    public ResponseEntity<ResponseDTO<ProductDTO>> addProduct(@RequestBody ProductDTO productDTO) {

        return ResponseEntity.ok(productService.createProduct(productDTO));
    }

    @GetMapping("/all")
    public ResponseEntity<ResponseDTO<List<ProductDTO>>> getAllProducts() {

        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<ProductDTO>> getProductById(@PathVariable Long id) {

        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PutMapping(value = "/{id}/update-prod", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDTO<ProductDTO>> updateProduct(@PathVariable Long id,
            @ModelAttribute ProductDTO productDTO) {

        return ResponseEntity.ok(productService.updateProduct(id, productDTO));
    }

    @DeleteMapping("/{id}/delete-cat")
    public ResponseEntity<ResponseDTO<Void>> deleteProduct(@PathVariable Long id) {

        return ResponseEntity.ok(productService.deleteProduct(id));
    }

    @GetMapping("/by-category/{id}")
    public ResponseEntity<ResponseDTO<List<ProductDTO>>> getProductsByCategory(@PathVariable Long id) {

        return ResponseEntity.ok(productService.getProductsByCategory(id));
    }

    @GetMapping("/search")
    public ResponseEntity<ResponseDTO<List<ProductDTO>>> searchProducts(@RequestParam String keyword) {

        return ResponseEntity.ok(productService.searchProducts(keyword));
    }

    @GetMapping("/info/{id}")
    public ResponseEntity<ResponseDTO<ProductInfoDTO>> getProductWithSupplierAndStockById(@PathVariable Long id) {

        return ResponseEntity.ok(productService.getProductWithSupplierAndStockById(id));
    }

    @GetMapping("/info/all-prod")
    public ResponseEntity<ResponseDTO<List<ProductInfoDTO>>> getAllProductWithSupplierAndStock() {

        return ResponseEntity.ok(productService.getAllProductWithSupplierAndStock());
    }

    @GetMapping("/{id}/info-detail")
    public ResponseEntity<ResponseDTO<ProductInfoDetailDTO>> getProductInfoDetail(@PathVariable Long id) {

        return ResponseEntity.ok(productService.getProductInfoDetail(id));
    }

    @GetMapping("/{poId}/received-qty")
    public ResponseEntity<ResponseDTO<List<SumReceivedGroupByProductDTO>>> getSumReceivedQtyByPoGroupByProduct(
            @PathVariable Long poId) {

        return ResponseEntity.ok(productService.getSumReceivedQtyByPoGroupByProduct(poId));
    }

    @GetMapping("/{categoryId}/with-sku-by-category")
    public ResponseEntity<ResponseDTO<List<ProductWithSkuByCategoryDTO>>> getAllSupllierProductWithSkuByCategory(
            @PathVariable Long categoryId) {

        return ResponseEntity.ok(productService.getAllSupllierProductWithSkuByCategory(categoryId));
    }

    @GetMapping("/all/with-inventory-optional")
    public ResponseEntity<ResponseDTO<List<InventoryStockDTO>>> getAllProductsWithInventoryOptional() {

        return ResponseEntity.ok(productService.getAllProductsWithInventoryOptional());
    }
}
