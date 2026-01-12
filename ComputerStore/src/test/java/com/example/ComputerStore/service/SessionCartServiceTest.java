package com.example.ComputerStore.service;

import com.example.ComputerStore.model.Order;
import com.example.ComputerStore.model.Product;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionCartServiceTest {

    @Mock
    private OrderService orderService;
    @Mock
    private ProductService productService;
    @Mock
    private HttpSession session;

    @InjectMocks
    private SessionCartService sessionCartService;

    private Product testProduct;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setProductId(1);
        testProduct.setName("Test Product");
        testProduct.setPrice(100.0f);

        testOrder = new Order();
        testOrder.setOrderId(1);
        testOrder.setTotalPrice(100.0);
    }

    @Test
    void getCart_NewSession_ReturnsEmptyCart() {
        // Arrange
        when(session.getAttribute("USER_CART")).thenReturn(null);

        // Act
        Map<Integer, Integer> cart = sessionCartService.getCart(session);

        // Assert
        assertNotNull(cart);
        assertTrue(cart.isEmpty());
        verify(session, times(1)).setAttribute(eq("USER_CART"), any(Map.class));
    }

    @Test
    void getCart_ExistingCart_ReturnsCart() {
        // Arrange
        Map<Integer, Integer> existingCart = new HashMap<>();
        existingCart.put(1, 2);
        when(session.getAttribute("USER_CART")).thenReturn(existingCart);

        // Act
        Map<Integer, Integer> cart = sessionCartService.getCart(session);

        // Assert
        assertNotNull(cart);
        assertEquals(1, cart.size());
        assertEquals(2, cart.get(1));
    }

    @Test
    void addProductToCart_NewProduct_Success() {
        // Arrange
        Map<Integer, Integer> cart = new HashMap<>();
        when(session.getAttribute("USER_CART")).thenReturn(cart);
        when(productService.getProductDetails(1)).thenReturn(testProduct);

        // Act
        sessionCartService.addProductToCart(session, 1);

        // Assert
        assertEquals(1, cart.size());
        assertEquals(1, cart.get(1));
        verify(productService, times(1)).getProductDetails(1);
    }

    @Test
    void addProductToCart_ExistingProduct_IncrementsQuantity() {
        // Arrange
        Map<Integer, Integer> cart = new HashMap<>();
        cart.put(1, 2);
        when(session.getAttribute("USER_CART")).thenReturn(cart);
        when(productService.getProductDetails(1)).thenReturn(testProduct);

        // Act
        sessionCartService.addProductToCart(session, 1);

        // Assert
        assertEquals(1, cart.size());
        assertEquals(3, cart.get(1)); // 2 + 1 = 3
        verify(productService, times(1)).getProductDetails(1);
    }

    @Test
    void addProductToCart_InvalidProduct_ThrowsException() {
        // Arrange
        Map<Integer, Integer> cart = new HashMap<>();
        when(session.getAttribute("USER_CART")).thenReturn(cart);
        when(productService.getProductDetails(999))
                .thenThrow(new IllegalArgumentException("Product not found"));

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> sessionCartService.addProductToCart(session, 999));
        assertTrue(cart.isEmpty());
    }

    @Test
    void removeProductFromCart_QuantityGreaterThanOne_DecrementsQuantity() {
        // Arrange
        Map<Integer, Integer> cart = new HashMap<>();
        cart.put(1, 3);
        when(session.getAttribute("USER_CART")).thenReturn(cart);

        // Act
        sessionCartService.removeProductFromCart(session, 1);

        // Assert
        assertEquals(1, cart.size());
        assertEquals(2, cart.get(1)); // 3 - 1 = 2
    }

    @Test
    void removeProductFromCart_QuantityEqualsOne_RemovesProduct() {
        // Arrange
        Map<Integer, Integer> cart = new HashMap<>();
        cart.put(1, 1);
        when(session.getAttribute("USER_CART")).thenReturn(cart);

        // Act
        sessionCartService.removeProductFromCart(session, 1);

        // Assert
        assertTrue(cart.isEmpty());
    }

    @Test
    void removeProductFromCart_ProductNotInCart_NoChange() {
        // Arrange
        Map<Integer, Integer> cart = new HashMap<>();
        cart.put(1, 2);
        when(session.getAttribute("USER_CART")).thenReturn(cart);

        // Act
        sessionCartService.removeProductFromCart(session, 999);

        // Assert
        assertEquals(1, cart.size());
        assertEquals(2, cart.get(1));
    }

    @Test
    void clearCart_Success() {
        // Arrange
        Map<Integer, Integer> cart = new HashMap<>();
        cart.put(1, 2);
        cart.put(2, 3);
        when(session.getAttribute("USER_CART")).thenReturn(cart);

        // Act
        sessionCartService.clearCart(session);

        // Assert
        assertTrue(cart.isEmpty());
    }

    @Test
    void checkout_Success() {
        // Arrange
        Map<Integer, Integer> cart = new HashMap<>();
        cart.put(1, 2);
        when(session.getAttribute("USER_CART")).thenReturn(cart);
        when(orderService.createOrderWithCard(anyInt(), any(Map.class),
                anyString(), anyString(), anyString(), anyString()))
                .thenReturn(testOrder);

        // Act
        Order result = sessionCartService.checkout(session, 1,
                "1234567890123456", "John Doe", "12/25", "123");

        // Assert
        assertNotNull(result);
        assertEquals(testOrder, result);
        assertTrue(cart.isEmpty()); // Cart should be cleared after checkout
        verify(orderService, times(1)).createOrderWithCard(
                eq(1), any(Map.class), eq("1234567890123456"),
                eq("John Doe"), eq("12/25"), eq("123")
        );
    }

    @Test
    void checkout_EmptyCart_ThrowsException() {
        // Arrange
        Map<Integer, Integer> cart = new HashMap<>();
        when(session.getAttribute("USER_CART")).thenReturn(cart);

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> sessionCartService.checkout(session, 1, "1234", "John", "12/25", "123")
        );
        assertEquals("Cart is empty", exception.getMessage());
        verify(orderService, never()).createOrderWithCard(
                anyInt(), any(), anyString(), anyString(), anyString(), anyString()
        );
    }

    @Test
    void checkout_OrderServiceFails_DoesNotClearCart() {
        // Arrange
        Map<Integer, Integer> cart = new HashMap<>();
        cart.put(1, 2);
        when(session.getAttribute("USER_CART")).thenReturn(cart);
        when(orderService.createOrderWithCard(anyInt(), any(Map.class),
                anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Payment failed"));

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> sessionCartService.checkout(session, 1, "1234", "John", "12/25", "123")
        );

        // cart should still have items since checkout failed
        assertEquals(1, cart.size());
        assertEquals(2, cart.get(1));
    }
}