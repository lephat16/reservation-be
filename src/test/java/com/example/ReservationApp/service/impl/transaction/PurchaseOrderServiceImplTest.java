package com.example.ReservationApp.service.impl.transaction;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import com.example.ReservationApp.dto.ResponseDTO;
import com.example.ReservationApp.dto.transaction.PurchaseOrderDTO;
import com.example.ReservationApp.dto.transaction.PurchaseOrderDetailDTO;
import com.example.ReservationApp.entity.product.Product;
import com.example.ReservationApp.entity.supplier.Supplier;
import com.example.ReservationApp.entity.supplier.SupplierProduct;
import com.example.ReservationApp.entity.transaction.PurchaseOrder;
import com.example.ReservationApp.entity.transaction.PurchaseOrderDetail;
import com.example.ReservationApp.entity.user.User;
import com.example.ReservationApp.exception.NotFoundException;
import com.example.ReservationApp.mapper.PurchaseOrderDetailMapper;
import com.example.ReservationApp.mapper.PurchaseOrderMapper;
import com.example.ReservationApp.repository.product.ProductRepository;
import com.example.ReservationApp.repository.supplier.SupplierRepository;
import com.example.ReservationApp.repository.transaction.PurchaseOrderRepository;
import com.example.ReservationApp.service.impl.auth.UserServiceImpl;
import com.example.ReservationApp.service.transaction.PurchaseOrderDetailService;

public class PurchaseOrderServiceImplTest {

    @Mock
    private PurchaseOrderRepository poRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private UserServiceImpl userService;

    @Mock
    private PurchaseOrderMapper poMapper;

    @Mock
    private PurchaseOrderDetailMapper poDetailMapper;

    @Mock
    private PurchaseOrderDetailService poDetailService;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private PurchaseOrderServiceImpl poService;

    private Supplier supplier;
    private User user;
    private Product product;
    private SupplierProduct supplierProduct;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        product = new Product();
        product.setId(1L);

        supplier = new Supplier();
        supplier.setId(1L);

        supplierProduct = new SupplierProduct();
        supplierProduct.setProduct(product);
        supplierProduct.setSupplier(supplier);

        supplier.setSupplierProducts(List.of(supplierProduct));

        user = new User();
        user.setId(1L);

        when(poDetailMapper.toEntity(any(PurchaseOrderDetailDTO.class)))
                .thenAnswer(invocation -> {
                    PurchaseOrderDetailDTO dto = invocation.getArgument(0);
                    PurchaseOrderDetail detail = new PurchaseOrderDetail();
                    detail.setQty(dto.getQty());
                    detail.setCost(dto.getCost());
                    return detail;
                });

        when(poMapper.toEntity(any(PurchaseOrderDTO.class)))
                .thenAnswer(invocation -> new PurchaseOrder());
        when(poMapper.toDTO(any(PurchaseOrder.class)))
                .thenAnswer(invocation -> {
                    PurchaseOrderDTO dto = new PurchaseOrderDTO();
                    dto.setSupplierId(1L);
                    return dto;
                });
    }

    @Test
    void testCreatePurchaseOrderSuccess() {
        PurchaseOrderDetailDTO detailDTO = PurchaseOrderDetailDTO.builder()
                .productId(1L)
                .qty(5)
                .cost(BigDecimal.valueOf(100))
                .build();

        PurchaseOrderDTO poDTO = new PurchaseOrderDTO();
        poDTO.setSupplierId(1L);
        poDTO.setDetails(List.of(detailDTO));

        when(supplierRepository.findSupplierWithProductsAndCategory(1L))
                .thenReturn(Optional.of(supplier));
        when(userService.getCurrentUserEntity()).thenReturn(user);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        PurchaseOrder poEntity = new PurchaseOrder();
        when(poRepository.save(any(PurchaseOrder.class))).thenReturn(poEntity);

        ResponseDTO<PurchaseOrderDTO> response = poService.createPurchaseOrder(poDTO);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("注文書が正常に作成されました。", response.getMessage());

        verify(poRepository, times(1)).save(any(PurchaseOrder.class));
    }

    @Test
    void testCreatePurchaseOrderFailsWhenSupplierNotFound() {
        PurchaseOrderDTO poDTO = new PurchaseOrderDTO();
        poDTO.setSupplierId(999L);

        when(supplierRepository.findSupplierWithProductsAndCategory(999L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> poService.createPurchaseOrder(poDTO));
        assertEquals("この仕入先は存在していません。", ex.getMessage());
    }

    @Test
    void testGetPurchaseOrderByIdSuccess() {
        PurchaseOrder po = new PurchaseOrder();
        po.setId(1L);

        when(poRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(po));
        when(poMapper.toDTO(po)).thenReturn(new PurchaseOrderDTO());
        when(poDetailService.getDetailEntitysByOrder(1L)).thenReturn(List.of(new PurchaseOrderDetailDTO()));

        ResponseDTO<PurchaseOrderDTO> response = poService.getPurchaseOrderById(1L);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("注文書が正常に取得されました。", response.getMessage());
    }

    @Test
    void testUpdatePurchaseOrderSuccess() {
        PurchaseOrder po = new PurchaseOrder();
        po.setId(1L);

        PurchaseOrderDTO dto = new PurchaseOrderDTO();
        dto.setStatus(null);
        dto.setDescription("Updated description");

        when(poRepository.findById(1L)).thenReturn(Optional.of(po));
        when(poMapper.toDTO(po)).thenReturn(dto);

        ResponseDTO<PurchaseOrderDTO> response = poService.updatePurchaseOrder(1L, dto);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("購入注文が正常に更新されました。", response.getMessage());
        assertEquals("Updated description", po.getDescription());
        verify(poRepository, times(1)).save(po);
    }

    @Test
    void testDeletePurchaseOrderSuccess() {
        PurchaseOrder po = new PurchaseOrder();
        po.setId(1L);

        when(poRepository.findById(1L)).thenReturn(Optional.of(po));

        ResponseDTO<Void> response = poService.deletePurchaseOrder(1L);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("購入注文が正常に削除されました。", response.getMessage());
        verify(poRepository, times(1)).delete(po);
    }
}
