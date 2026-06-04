package com.example.ComputerStore.service;

import com.example.ComputerStore.exception.EmptyCartException;
import com.example.ComputerStore.model.Cart;
import com.example.ComputerStore.model.CartItem;
import com.example.ComputerStore.model.Order;
import com.example.ComputerStore.model.Product;
import com.example.ComputerStore.model.User;
import com.example.ComputerStore.repo.CartItemRepository;
import com.example.ComputerStore.repo.CartRepository;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionCartServiceTest {

    @Mock
    private OrderService orderService;

    @Mock
    private ProductService productService;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private UserService userService;

    @Mock
    private HttpSession mockSession;

    @InjectMocks
    private CartService cartService;

    private User testUser;
    private Cart testCart;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1);

        testCart = new Cart();
        testCart.setCartId(1);
        testCart.setUser(testUser);
        testCart.setItems(new ArrayList<>());
    }

    @Test
    void getCart_NewSession_ReturnsEmptyCart() {
        when(mockSession.getAttribute("userId")).thenReturn(null);

        Map<Integer, Integer> cart = cartService.getCart(mockSession);

        assertNotNull(cart);
        assertTrue(cart.isEmpty());
    }

    @Test
    void getCart_ExistingCart_ReturnsCart() {
        when(mockSession.getAttribute("userId")).thenReturn(1);
        when(userService.findUserById(1)).thenReturn(testUser);
        
        Product p = new Product();
        p.setProductId(1);
        CartItem item = new CartItem();
        item.setProduct(p);
        item.setQuantity(2);
        testCart.getItems().add(item);
        
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));

        Map<Integer, Integer> cart = cartService.getCart(mockSession);

        assertNotNull(cart);
        assertEquals(1, cart.size());
        assertEquals(2, cart.get(1));
    }

    @Test
    void addProductToCart_ValidProduct_AddsToCart() {
        when(mockSession.getAttribute("userId")).thenReturn(1);
        when(userService.findUserById(1)).thenReturn(testUser);
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));
        
        Product mockProduct = new Product();
        mockProduct.setProductId(1);
        when(productService.getProductDetails(1)).thenReturn(mockProduct);

        cartService.addProductToCart(mockSession, 1);

        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void checkout_ValidCart_CreatesOrderAndClearsCart() {
        when(mockSession.getAttribute("userId")).thenReturn(1);
        when(userService.findUserById(1)).thenReturn(testUser);
        
        Product p = new Product();
        p.setProductId(1);
        CartItem item = new CartItem();
        item.setProduct(p);
        item.setQuantity(2);
        testCart.getItems().add(item);
        
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));

        Order mockOrder = new Order();
        when(orderService.createOrder(eq(1), anyMap())).thenReturn(mockOrder);

        Order result = cartService.checkout(mockSession, 1);

        assertNotNull(result);
        verify(orderService).createOrder(eq(1), anyMap());
        verify(cartItemRepository).deleteByCartId(1);
    }

    @Test
    void checkout_EmptyCart_ThrowsException() {
        when(mockSession.getAttribute("userId")).thenReturn(1);
        when(userService.findUserById(1)).thenReturn(testUser);
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));

        assertThrows(EmptyCartException.class, () -> {
            cartService.checkout(mockSession, 1);
        });
    }
}