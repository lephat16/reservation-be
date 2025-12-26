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
import com.example.ReservationApp.dto.transaction.PurchaseOrderDetailDTO;
import com.example.ReservationApp.entity.product.Product;
import com.example.ReservationApp.entity.transaction.PurchaseOrder;
import com.example.ReservationApp.entity.transaction.PurchaseOrderDetail;
import com.example.ReservationApp.enums.OrderStatus;
import com.example.ReservationApp.exception.InvalidCredentialException;
import com.example.ReservationApp.exception.NotFoundException;
import com.example.ReservationApp.mapper.PurchaseOrderDetailMapper;
import com.example.ReservationApp.repository.product.ProductRepository;
import com.example.ReservationApp.repository.transaction.PurchaseOrderDetailRepository;
import com.example.ReservationApp.repository.transaction.PurchaseOrderRepository;

class PurchaseOrderDetailServiceImplTest {

    @Mock
    private PurchaseOrderDetailRepository poDetailRepository;
    @Mock
    private PurchaseOrderRepository poRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private PurchaseOrderDetailMapper poDetailMapper;

    @InjectMocks
    private PurchaseOrderDetailServiceImpl poDetailService;

    private PurchaseOrder po;
    private Product product;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        po = new PurchaseOrder();
        po.setId(1L);
        po.setStatus(OrderStatus.NEW);

        product = new Product();
        product.setId(1L);
    }

    @Test
    void testAddDetailSuccess_NewProduct() {
        PurchaseOrderDetailDTO dto = new PurchaseOrderDetailDTO();
        dto.setProductId(1L);
        dto.setQty(5);
        dto.setCost(BigDecimal.valueOf(100));

        when(poRepository.findById(1L)).thenReturn(Optional.of(po));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(poDetailRepository.findByPurchaseOrderIdAndProductId(1L, 1L)).thenReturn(Optional.empty());
        when(poDetailMapper.toDTO(any())).thenReturn(dto);

        ResponseDTO<PurchaseOrderDetailDTO> response = poDetailService.addDetail(1L, dto);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("購入明細が正常に追加されました。", response.getMessage());
        assertEquals(5, response.getData().getQty());
        verify(poDetailRepository, times(1)).save(any(PurchaseOrderDetail.class));
    }

    @Test
    void testAddDetailFails_ProductNotFound() {
        PurchaseOrderDetailDTO dto = new PurchaseOrderDetailDTO();
        dto.setProductId(999L);
        dto.setQty(5);
        dto.setCost(BigDecimal.valueOf(100));

        when(poRepository.findById(1L)).thenReturn(Optional.of(po));
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> poDetailService.addDetail(1L, dto));
        assertEquals("この商品は存在していません。", ex.getMessage());
    }

    @Test
    void testAddDetailFails_InvalidQty() {
        PurchaseOrderDetailDTO dto = new PurchaseOrderDetailDTO();
        dto.setProductId(1L);
        dto.setQty(0);
        dto.setCost(BigDecimal.valueOf(100));

        when(poRepository.findById(1L)).thenReturn(Optional.of(po));

        InvalidCredentialException ex = assertThrows(InvalidCredentialException.class,
                () -> poDetailService.addDetail(1L, dto));
        assertEquals("数量は0より大きくなければなりません。", ex.getMessage());
    }

    @Test
    void testAddDetailFails_OrderNotEditable() {
        po.setStatus(OrderStatus.COMPLETED);
        PurchaseOrderDetailDTO dto = new PurchaseOrderDetailDTO();
        dto.setProductId(1L);
        dto.setQty(5);
        dto.setCost(BigDecimal.valueOf(100));

        when(poRepository.findById(1L)).thenReturn(Optional.of(po));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> poDetailService.addDetail(1L, dto));
        assertEquals("この注文書は編集できません。", ex.getMessage());
    }

    @Test
    void testUpdateDetailSuccess() {
        PurchaseOrderDetail existingDetail = new PurchaseOrderDetail();
        existingDetail.setId(1L);
        existingDetail.setQty(5);
        existingDetail.setCost(BigDecimal.valueOf(100));
        existingDetail.setPurchaseOrder(po);

        PurchaseOrderDetailDTO dto = new PurchaseOrderDetailDTO();
        dto.setQty(10);
        dto.setCost(BigDecimal.valueOf(120));

        when(poDetailRepository.findById(1L)).thenReturn(Optional.of(existingDetail));
        when(poDetailMapper.toDTO(any())).thenReturn(dto);

        ResponseDTO<PurchaseOrderDetailDTO> response = poDetailService.updateDetail(1L, dto);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("購入の詳細が正常に更新されました。", response.getMessage());
        assertEquals(10, response.getData().getQty());
        assertEquals(BigDecimal.valueOf(120), response.getData().getCost());
        verify(poDetailRepository, times(1)).save(existingDetail);
    }

    @Test
    void testUpdateDetailFails_NotFound() {
        PurchaseOrderDetailDTO dto = new PurchaseOrderDetailDTO();
        dto.setQty(10);
        dto.setCost(BigDecimal.valueOf(120));

        when(poDetailRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> poDetailService.updateDetail(1L, dto));
        assertEquals("この購入の詳細は存在していません。", ex.getMessage());
    }

    @Test
    void testDeleteDetailSuccess() {
        PurchaseOrderDetail poDetail = new PurchaseOrderDetail();
        poDetail.setId(1L);
        poDetail.setPurchaseOrder(po);
        po.getDetails().add(poDetail);

        when(poDetailRepository.findById(1L)).thenReturn(Optional.of(poDetail));

        ResponseDTO<Void> response = poDetailService.deleteDetail(1L);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("購入の詳細が正常に削除されました。", response.getMessage());
        assertFalse(po.getDetails().contains(poDetail));
        verify(poDetailRepository, times(1)).delete(poDetail);
    }

    @Test
    void testDeleteDetailFails_NotFound() {
        when(poDetailRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> poDetailService.deleteDetail(1L));
        assertEquals("この購入の詳細は存在していません。", ex.getMessage());
    }

}
