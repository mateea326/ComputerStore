package com.example.ComputerStore.service;

import com.example.ComputerStore.model.*;
import com.example.ComputerStore.repo.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private CardService cardService;
    @Mock
    private CustomerService customerService;
    @Mock
    private ProductService productService;

    @InjectMocks
    private OrderService orderService;

    private Customer testCustomer;
    private Product testProduct1;
    private Product testProduct2;
    private Order testOrder;
    private Map<Integer, Integer> testCart;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setCustomerId(1);
        testCustomer.setUsername("testuser");
        testCustomer.setEmail("test@example.com");

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
        testOrder.setCustomer(testCustomer);
        testOrder.setOrderDate(LocalDateTime.now());
        testOrder.setTotalPrice(700.0);
    }

    @Test
    void createOrder_Success() {
        // Arrange
        when(customerService.findCustomerById(1)).thenReturn(testCustomer);
        when(productService.getProductDetails(1)).thenReturn(testProduct1);
        when(productService.getProductDetails(2)).thenReturn(testProduct2);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        Order result = orderService.createOrder(1, testCart);

        // Assert
        assertNotNull(result);
        assertEquals(testCustomer, result.getCustomer());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(customerService, times(1)).findCustomerById(1);
        verify(productService, times(1)).getProductDetails(1);
        verify(productService, times(1)).getProductDetails(2);
    }

    @Test
    void createOrder_EmptyCart_ThrowsException() {
        // Arrange
        Map<Integer, Integer> emptyCart = new HashMap<>();

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> orderService.createOrder(1, emptyCart)
        );
        assertEquals("Cart is empty", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_NullCart_ThrowsException() {
        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> orderService.createOrder(1, null)
        );
        assertEquals("Cart is empty", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrderWithCard_Success() {
        // Arrange
        when(customerService.findCustomerById(1)).thenReturn(testCustomer);
        when(productService.getProductDetails(1)).thenReturn(testProduct1);
        when(productService.getProductDetails(2)).thenReturn(testProduct2);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Card testCard = new Card();
        testCard.setCardId(1);
        when(cardService.processPayment(any(Order.class), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(testCard);

        // Act
        Order result = orderService.createOrderWithCard(
                1, testCart, "1234567890123456", "John Doe", "12/25", "123"
        );

        // Assert
        assertNotNull(result);
        assertEquals(testCustomer, result.getCustomer());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(cardService, times(1)).processPayment(
                any(Order.class), eq("1234567890123456"), eq("John Doe"), eq("12/25"), eq("123")
        );
    }

    @Test
    void createOrderWithCard_EmptyCart_ThrowsException() {
        // Arrange
        Map<Integer, Integer> emptyCart = new HashMap<>();

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> orderService.createOrderWithCard(1, emptyCart, "1234", "John", "12/25", "123")
        );
        assertEquals("Cart is empty", exception.getMessage());
        verify(cardService, never()).processPayment(any(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void getOrderHistory_Success() {
        // Arrange
        List<Order> orders = Arrays.asList(testOrder);
        when(customerService.findCustomerById(1)).thenReturn(testCustomer);
        when(orderRepository.findByCustomer(testCustomer)).thenReturn(orders);

        // Act
        List<Order> result = orderService.getOrderHistory(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testOrder, result.get(0));
        verify(orderRepository, times(1)).findByCustomer(testCustomer);
    }

    @Test
    void getOrderById_Success() {
        // Arrange
        when(orderRepository.findById(1)).thenReturn(Optional.of(testOrder));

        // Act
        Order result = orderService.getOrderById(1);

        // Assert
        assertNotNull(result);
        assertEquals(testOrder.getOrderId(), result.getOrderId());
        verify(orderRepository, times(1)).findById(1);
    }

    @Test
    void getOrderById_NotFound_ThrowsException() {
        // Arrange
        when(orderRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.getOrderById(999)
        );
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void getAllOrders_Success() {
        // Arrange
        List<Order> orders = Arrays.asList(testOrder);
        when(orderRepository.findAll()).thenReturn(orders);

        // Act
        List<Order> result = orderService.getAllOrders();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void createOrder_CalculatesTotalPriceCorrectly() {
        // Arrange
        when(customerService.findCustomerById(1)).thenReturn(testCustomer);
        when(productService.getProductDetails(1)).thenReturn(testProduct1);
        when(productService.getProductDetails(2)).thenReturn(testProduct2);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            // 2 * 150 + 1 * 400 = 700
            assertEquals(700.0, savedOrder.getTotalPrice(), 0.01);
            return savedOrder;
        });

        // Act
        orderService.createOrder(1, testCart);

        // Assert
        verify(orderRepository, times(1)).save(any(Order.class));
    }
}