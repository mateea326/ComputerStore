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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
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
    private CartService sessionCartService;

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
    void checkout_ValidCart_CreatesOrderAndClearsCart() {
        testCart.put(1, 2);
        when(mockSession.getAttribute("USER_CART")).thenReturn(testCart);
        Order mockOrder = new Order();
        when(orderService.createOrderWithPaymentDetails(anyInt(), anyMap()))
                .thenReturn(mockOrder);

        Order result = sessionCartService.checkout(mockSession, 1);

        assertNotNull(result);
        assertTrue(testCart.isEmpty());
        verify(orderService).createOrderWithPaymentDetails(eq(1), any());
    }

    @Test
    void checkout_EmptyCart_ThrowsException() {
        when(mockSession.getAttribute("USER_CART")).thenReturn(new HashMap<>());

        assertThrows(com.example.ComputerStore.exception.EmptyCartException.class, () -> {
            sessionCartService.checkout(mockSession, 1);
        });
    }
}