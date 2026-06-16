package com.example.ComputerStore.service;

import com.example.ComputerStore.exception.EmptyCartException;
import com.example.ComputerStore.model.*;
import com.example.ComputerStore.repo.OrderRepository;
import com.example.ComputerStore.repo.OrderItemRepository;
import com.example.ComputerStore.client.NotificationServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserService userService;
    @Mock
    private ProductService productService;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private NotificationServiceClient notificationServiceClient;

    @InjectMocks
    private OrderService orderService;

    private User testUser;
    private Product testProduct1;
    private Product testProduct2;
    private Order testOrder;
    private Map<Integer, Integer> testCart;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setOrders(new ArrayList<>());

        testProduct1 = new Product();
        testProduct1.setProductId(1);
        testProduct1.setName("AMD Ryzen 5");
        testProduct1.setPrice(150.0f);

        testProduct2 = new Product();
        testProduct2.setProductId(2);
        testProduct2.setName("RTX 3060");
        testProduct2.setPrice(400.0f);

        testCart = new HashMap<>();
        testCart.put(1, 2); // 2x Product 1
        testCart.put(2, 1); // 1x Product 2

        testOrder = new Order();
        testOrder.setOrderId(1);
        testOrder.setUser(testUser);
        testOrder.setOrderDate(LocalDateTime.now());
        testOrder.setTotalPrice(700.0);
        testOrder.setOrderItems(new ArrayList<>());
    }

    @Test
    void createOrder_Success() {
        when(userService.findUserById(1)).thenReturn(testUser);
        when(productService.getProductDetails(1)).thenReturn(testProduct1);
        when(productService.getProductDetails(2)).thenReturn(testProduct2);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(notificationServiceClient.sendNotification(any())).thenReturn("Sent");

        Order result = orderService.createOrder(1, testCart);

        assertNotNull(result);
        assertEquals(testUser, result.getUser());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(notificationServiceClient, times(1)).sendNotification(any());
    }

    @Test
    void getOrderHistory_Success() {
        List<Order> orders = Arrays.asList(testOrder);
        when(userService.findUserById(1)).thenReturn(testUser);
        when(orderRepository.findByUser(testUser)).thenReturn(orders);

        List<Order> result = orderService.getOrderHistory(1);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testOrder, result.get(0));
    }

    @Test
    void getOrderHistoryPageable_Success() {
        Page<Order> page = new PageImpl<>(Arrays.asList(testOrder));
        Pageable pageable = PageRequest.of(0, 10);
        when(userService.findUserById(1)).thenReturn(testUser);
        when(orderRepository.findByUser(testUser, pageable)).thenReturn(page);

        Page<Order> result = orderService.getOrderHistory(1, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getOrderById_Success() {
        when(orderRepository.findById(1)).thenReturn(Optional.of(testOrder));

        Order result = orderService.getOrderById(1);

        assertNotNull(result);
        assertEquals(testOrder.getOrderId(), result.getOrderId());
    }

    @Test
    void getOrderById_NotFound_ThrowsException() {
        when(orderRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(com.example.ComputerStore.exception.ResourceNotFoundException.class, () -> {
            orderService.getOrderById(999);
        });
    }

    @Test
    void getAllOrders_Success() {
        List<Order> orders = Arrays.asList(testOrder);
        when(orderRepository.findAll()).thenReturn(orders);

        List<Order> result = orderService.getAllOrders();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getAllOrdersPageable_Success() {
        Page<Order> page = new PageImpl<>(Arrays.asList(testOrder));
        Pageable pageable = PageRequest.of(0, 10);
        when(orderRepository.findAll(pageable)).thenReturn(page);

        Page<Order> result = orderService.getAllOrders(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void createOrder_EmptyCart_ThrowsException() {
        Map<Integer, Integer> emptyCart = new HashMap<>();

        assertThrows(EmptyCartException.class, () -> {
            orderService.createOrder(1, emptyCart);
        });
    }

    @Test
    void updateOrder_Success() {
        when(orderRepository.findById(1)).thenReturn(Optional.of(testOrder));
        when(productService.getProductDetails(1)).thenReturn(testProduct1);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<Integer, Integer> newCart = new HashMap<>();
        newCart.put(1, 3);

        Order result = orderService.updateOrder(1, newCart);

        assertNotNull(result);
        assertEquals(450.0, result.getTotalPrice());
    }

    @Test
    void deleteOrder_Success() {
        testUser.getOrders().add(testOrder);
        when(orderRepository.findById(1)).thenReturn(Optional.of(testOrder));
        doNothing().when(orderRepository).delete(testOrder);

        orderService.deleteOrder(1);

        assertFalse(testUser.getOrders().contains(testOrder));
        verify(orderRepository, times(1)).delete(testOrder);
    }

    @Test
    void getOrderHistoryFallback_Success() {
        List<Order> response = orderService.getOrderHistoryFallback(1, new RuntimeException("Error"));
        assertTrue(response.isEmpty());
    }

    @Test
    void createOrderFallback_ThrowsException() {
        assertThrows(RuntimeException.class, () -> {
            orderService.createOrderFallback(1, testCart, new RuntimeException("Error"));
        });
    }

    @Test
    void createOrderWithPaymentDetails_Success() {
        when(userService.findUserById(1)).thenReturn(testUser);
        when(productService.getProductDetails(1)).thenReturn(testProduct1);
        when(productService.getProductDetails(2)).thenReturn(testProduct2);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.createOrderWithPaymentDetails(1, testCart);

        assertNotNull(result);
    }
}