package com.example.ReservationApp.service.product;

import java.util.List;

import com.example.ReservationApp.dto.ResponseDTO;
import com.example.ReservationApp.dto.response.product.ProductDTO;
import com.example.ReservationApp.dto.response.product.ProductInfoDTO;
import com.example.ReservationApp.dto.response.product.ProductInfoDetailDTO;

public interface ProductService {

    ResponseDTO<ProductDTO> createProduct(ProductDTO productDTO);

    ResponseDTO<List<ProductDTO>> getAllProducts();

    ResponseDTO<ProductDTO> getProductById(Long id);

    ResponseDTO<ProductDTO> updateProduct(Long id, ProductDTO productDTO);

    ResponseDTO<Void> deleteProduct(Long id);

    ResponseDTO<List<ProductDTO>> getProductsByCategory(Long categoryId);

    ResponseDTO<List<ProductDTO>> searchProducts(String keyword);

    ResponseDTO<ProductInfoDTO> getProductWithSupplierAndStockById(Long productId);
    
    ResponseDTO<List<ProductInfoDTO>> getAllProductWithSupplierAndStock();

    ResponseDTO<ProductInfoDetailDTO> getProductInfoDetail(Long productId);
}
