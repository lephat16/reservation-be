package com.example.ReservationApp.service.impl.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import com.example.ReservationApp.dto.ResponseDTO;
import com.example.ReservationApp.dto.response.inventory.InventoryStockDTO;
import com.example.ReservationApp.dto.transaction.SalesOrderDTO;
import com.example.ReservationApp.dto.transaction.SalesOrderDetailDTO;
import com.example.ReservationApp.entity.inventory.InventoryStock;
import com.example.ReservationApp.entity.product.Product;
import com.example.ReservationApp.entity.transaction.SalesOrder;
import com.example.ReservationApp.entity.transaction.SalesOrderDetail;
import com.example.ReservationApp.entity.user.User;
import com.example.ReservationApp.mapper.InventoryStockMapper;
import com.example.ReservationApp.mapper.SalesOrderDetailMapper;
import com.example.ReservationApp.mapper.SalesOrderMapper;
import com.example.ReservationApp.repository.inventory.InventoryStockRepository;
import com.example.ReservationApp.repository.product.ProductRepository;
import com.example.ReservationApp.repository.transaction.SalesOrderRepository;
import com.example.ReservationApp.service.impl.auth.UserServiceImpl;
import com.example.ReservationApp.service.transaction.SalesOrderDetailService;

class SalesOrderServiceImplTest {

    @InjectMocks
    private SalesOrderServiceImpl soService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SalesOrderMapper soMapper;

    @Mock
    private SalesOrderDetailMapper soDetailMapper;

    @Mock
    private InventoryStockMapper inventoryStockMapper;

    @Mock
    private UserServiceImpl userService;

    @Mock
    private SalesOrderDetailService soDetailService;

    @Mock
    private InventoryStockRepository inventoryStockRepository;

    @Mock
    private SalesOrderRepository soRepository;

    private Product product;
    private SalesOrder salesOrder;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        product = new Product();
        product.setId(1L);

        salesOrder = new SalesOrder();
        salesOrder.setId(1L);

        user = new User();
        user.setId(1L);
    }

    @Test
    void testCreateSalesOrderSuccess() {

        SalesOrderDetailDTO detailDTO = SalesOrderDetailDTO.builder()
                .productId(product.getId())
                .qty(5)
                .price(BigDecimal.valueOf(100))
                .build();

        SalesOrderDTO salesOrderDTO = new SalesOrderDTO();
        salesOrderDTO.setCustomerName("Nguyen Van A");
        salesOrderDTO.setDetails(List.of(detailDTO));

        when(inventoryStockRepository.findByProductIdIn(anyList()))
                .thenReturn(List.of(new InventoryStock() {
                    {
                        setId(1L);
                        setProduct(product);
                        setQuantity(10);
                        setReservedQuantity(0);
                    }
                }));

        when(inventoryStockRepository.findAllByIdsWithWarehouse(anyList()))
                .thenAnswer(invocation -> {
                    List<Long> ids = invocation.getArgument(0);
                    InventoryStock stock = new InventoryStock();
                    stock.setId(ids.get(0));
                    stock.setProduct(product);
                    stock.setQuantity(10);
                    stock.setReservedQuantity(0);
                    return List.of(stock);
                });

        when(userService.getCurrentUserEntity()).thenReturn(user);

        when(soMapper.toEntity(any(SalesOrderDTO.class))).thenReturn(salesOrder);
        when(soMapper.toDTO(any(SalesOrder.class))).thenReturn(salesOrderDTO);
        when(soRepository.save(any(SalesOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(productRepository.findAllById(anyList())).thenReturn(List.of(product));

        when(inventoryStockMapper.toDTO(any(InventoryStock.class))).thenAnswer(invocation -> {
            InventoryStock stock = invocation.getArgument(0);
            return InventoryStockDTO.builder()
                    .id(stock.getId())
                    .productId(stock.getProduct().getId())
                    .quantity(stock.getQuantity())
                    .reservedQuantity(stock.getReservedQuantity())
                    .build();
        });

        when(soDetailMapper.toEntity(any(SalesOrderDetailDTO.class))).thenAnswer(invocation -> {
            SalesOrderDetailDTO dto = invocation.getArgument(0);
            SalesOrderDetail detail = new SalesOrderDetail();
            detail.setQty(dto.getQty());
            detail.setPrice(dto.getPrice());
            detail.setProduct(product);
            detail.setSalesOrder(salesOrder);
            return detail;
        });

        when(soDetailMapper.toDTOList(anyList())).thenReturn(salesOrderDTO.getDetails());

        ResponseDTO<SalesOrderDTO> response = soService.createSalesOrder(salesOrderDTO);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("販売注文が正常に作成されました", response.getMessage());
    }
}
