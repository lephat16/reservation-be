package com.example.ReservationApp.service.impl.transaction;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import com.example.ReservationApp.dto.ResponseDTO;
import com.example.ReservationApp.dto.transaction.SalesOrderDetailDTO;
import com.example.ReservationApp.entity.product.Product;
import com.example.ReservationApp.entity.transaction.SalesOrder;
import com.example.ReservationApp.entity.transaction.SalesOrderDetail;
import com.example.ReservationApp.enums.OrderStatus;
import com.example.ReservationApp.exception.InvalidCredentialException;
import com.example.ReservationApp.exception.NotFoundException;
import com.example.ReservationApp.mapper.SalesOrderDetailMapper;
import com.example.ReservationApp.repository.inventory.InventoryStockRepository;
import com.example.ReservationApp.repository.product.ProductRepository;
import com.example.ReservationApp.repository.transaction.SalesOrderDetailRepository;
import com.example.ReservationApp.repository.transaction.SalesOrderRepository;

public class SalesOrderDetailServiceImplTest {

    @InjectMocks
    private SalesOrderDetailServiceImpl soDetailService;

    @Mock
    private SalesOrderDetailRepository soDetailRepository;

    @Mock
    private SalesOrderRepository soRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SalesOrderDetailMapper soDetailMapper;

    @Mock
    private InventoryStockRepository inventoryStockRepository;

    private SalesOrder salesOrder;
    private Product product;
    private SalesOrderDetail detail;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        salesOrder = new SalesOrder();
        salesOrder.setId(1L);
        salesOrder.setStatus(OrderStatus.NEW);

        product = new Product();
        product.setId(1L);

        detail = new SalesOrderDetail();
        detail.setId(1L);
        detail.setSalesOrder(salesOrder);
        detail.setProduct(product);
        detail.setQty(5);
        detail.setPrice(BigDecimal.valueOf(100));
    }

    @Test
    void testAddDetailSuccess_NewDetail() {
        SalesOrderDetailDTO dto = SalesOrderDetailDTO.builder()
                .productId(product.getId())
                .qty(3)
                .price(BigDecimal.valueOf(50))
                .build();

        when(soRepository.findById(1L)).thenReturn(Optional.of(salesOrder));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(soDetailRepository.findBySalesOrderIdAndProductId(1L, product.getId())).thenReturn(Optional.empty());
        when(inventoryStockRepository.getAvailableStock(product.getId())).thenReturn(10);
        doNothing().when(inventoryStockRepository).reserveStock(product.getId(), 3);
        when(soDetailMapper.toDTO(any(SalesOrderDetail.class))).thenReturn(dto);

        ResponseDTO<SalesOrderDetailDTO> response = soDetailService.addDetail(1L, dto);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("販売の詳細が正常に追加されました", response.getMessage());
        verify(soDetailRepository, times(1)).save(any(SalesOrderDetail.class));
    }

    @Test
    void testAddDetailFails_ProductNotFound() {
        SalesOrderDetailDTO dto = SalesOrderDetailDTO.builder()
                .productId(999L)
                .qty(3)
                .price(BigDecimal.valueOf(50))
                .build();

        when(soRepository.findById(1L)).thenReturn(Optional.of(salesOrder));
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> soDetailService.addDetail(1L, dto));
        assertEquals("この商品は存在していません", ex.getMessage());
    }

    @Test
    void testAddDetailFails_InsufficientStock() {
        SalesOrderDetailDTO dto = SalesOrderDetailDTO.builder()
                .productId(product.getId())
                .qty(20)
                .price(BigDecimal.valueOf(50))
                .build();

        when(soRepository.findById(1L)).thenReturn(Optional.of(salesOrder));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(soDetailRepository.findBySalesOrderIdAndProductId(1L,
                product.getId())).thenReturn(Optional.empty());
        when(inventoryStockRepository.getAvailableStock(product.getId())).thenReturn(10);

        InvalidCredentialException ex = assertThrows(InvalidCredentialException.class,
                () -> soDetailService.addDetail(1L, dto));
        assertTrue(ex.getMessage().contains("在庫が不足しています"));
    }
}
