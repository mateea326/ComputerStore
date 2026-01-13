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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionCartServiceTest {

    @Mock
    private OrderService orderService;

    @Mock
    private ProductService productService;

    @Mock
    private HttpSession mockSession;

    @InjectMocks
    private SessionCartService sessionCartService;

    private Map<Integer, Integer> testCart;

    @BeforeEach
    void setUp() {
        testCart = new HashMap<>();
    }

    @Test
    void getCart_NewSession_ReturnsEmptyCart() {
        when(mockSession.getAttribute("USER_CART")).thenReturn(null);

        Map<Integer, Integer> cart = sessionCartService.getCart(mockSession);

        assertNotNull(cart);
        assertTrue(cart.isEmpty());
        verify(mockSession).setAttribute("USER_CART", cart);
    }

    @Test
    void getCart_ExistingCart_ReturnsCart() {
        testCart.put(1, 2);
        when(mockSession.getAttribute("USER_CART")).thenReturn(testCart);

        Map<Integer, Integer> cart = sessionCartService.getCart(mockSession);

        assertNotNull(cart);
        assertEquals(1, cart.size());
        assertEquals(2, cart.get(1));
    }

    @Test
    void addProductToCart_ValidProduct_AddsToCart() {
        when(mockSession.getAttribute("USER_CART")).thenReturn(testCart);
        Product mockProduct = new Product();
        when(productService.getProductDetails(1)).thenReturn(mockProduct);

        sessionCartService.addProductToCart(mockSession, 1);

        assertEquals(1, testCart.get(1));
        verify(productService).getProductDetails(1);
    }

    @Test
    void addProductToCart_ExistingProduct_IncrementsQuantity() {
        testCart.put(1, 2);
        when(mockSession.getAttribute("USER_CART")).thenReturn(testCart);
        Product mockProduct = new Product();
        when(productService.getProductDetails(1)).thenReturn(mockProduct);

        sessionCartService.addProductToCart(mockSession, 1);

        assertEquals(3, testCart.get(1));
    }

    @Test
    void removeProductFromCart_MultipleQuantity_DecrementsQuantity() {
        testCart.put(1, 3);
        when(mockSession.getAttribute("USER_CART")).thenReturn(testCart);

        sessionCartService.removeProductFromCart(mockSession, 1);

        assertEquals(2, testCart.get(1));
    }

    @Test
    void removeProductFromCart_SingleQuantity_RemovesProduct() {
        testCart.put(1, 1);
        when(mockSession.getAttribute("USER_CART")).thenReturn(testCart);

        sessionCartService.removeProductFromCart(mockSession, 1);

        assertFalse(testCart.containsKey(1));
    }

    @Test
    void removeProductFromCart_NonExistentProduct_DoesNothing() {
        when(mockSession.getAttribute("USER_CART")).thenReturn(testCart);

        sessionCartService.removeProductFromCart(mockSession, 999);

        assertTrue(testCart.isEmpty());
    }

    @Test
    void clearCart_RemovesAllItems() {
        testCart.put(1, 2);
        testCart.put(2, 3);
        when(mockSession.getAttribute("USER_CART")).thenReturn(testCart);

        sessionCartService.clearCart(mockSession);

        assertTrue(testCart.isEmpty());
    }

    @Test
    void checkout_ValidCart_CreatesOrderAndClearsCart() {
        testCart.put(1, 2);
        when(mockSession.getAttribute("USER_CART")).thenReturn(testCart);
        Order mockOrder = new Order();
        when(orderService.createOrderWithCard(anyInt(), anyMap(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(mockOrder);

        Order result = sessionCartService.checkout(mockSession, 1, "1234", "John Doe", "12/25", "123");

        assertNotNull(result);
        assertTrue(testCart.isEmpty());
        verify(orderService).createOrderWithCard(eq(1), any(), eq("1234"), eq("John Doe"), eq("12/25"), eq("123"));
    }

    @Test
    void checkout_EmptyCart_ThrowsException() {
        when(mockSession.getAttribute("USER_CART")).thenReturn(testCart);

        assertThrows(IllegalStateException.class, () -> {
            sessionCartService.checkout(mockSession, 1, "1234", "John Doe", "12/25", "123");
        });
    }
}