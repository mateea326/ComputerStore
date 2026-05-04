package com.example.ComputerStore.service;

import com.example.ComputerStore.model.Product;
import com.example.ComputerStore.model.User;
import com.example.ComputerStore.model.Wishlist;
import com.example.ComputerStore.repo.WishlistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WishlistServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private UserService userService;

    @Mock
    private ProductService productService;

    @InjectMocks
    private WishlistService wishlistService;

    private User testUser;
    private Product testProduct;
    private Wishlist testWishlist;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1);
        testUser.setUsername("testuser");

        testProduct = new Product();
        testProduct.setProductId(100);
        testProduct.setName("Test Product");
        testProduct.setPrice(99.99f);

        testWishlist = new Wishlist(testUser);
    }

    @Test
    void testGetOrCreateWishlist_Existing() {
        when(userService.findUserById(1)).thenReturn(testUser);
        when(wishlistRepository.findByUser(testUser)).thenReturn(Optional.of(testWishlist));

        Wishlist result = wishlistService.getOrCreateWishlist(1);

        assertNotNull(result);
        assertEquals(testUser, result.getUser());
        verify(wishlistRepository, never()).save(any());
    }

    @Test
    void testGetOrCreateWishlist_New() {
        when(userService.findUserById(1)).thenReturn(testUser);
        when(wishlistRepository.findByUser(testUser)).thenReturn(Optional.empty());
        when(wishlistRepository.save(any(Wishlist.class))).thenReturn(testWishlist);

        Wishlist result = wishlistService.getOrCreateWishlist(1);

        assertNotNull(result);
        verify(wishlistRepository).save(any(Wishlist.class));
    }

    @Test
    void testAddProductToWishlist() {
        when(userService.findUserById(1)).thenReturn(testUser);
        when(wishlistRepository.findByUser(testUser)).thenReturn(Optional.of(testWishlist));
        when(productService.getProductDetails(100)).thenReturn(testProduct);
        when(wishlistRepository.save(any(Wishlist.class))).thenReturn(testWishlist);

        Wishlist result = wishlistService.addProductToWishlist(1, 100);

        assertTrue(result.containsProduct(testProduct));
        verify(wishlistRepository).save(testWishlist);
    }

    @Test
    void testAddProductToWishlist_AlreadyExists() {
        testWishlist.addProduct(testProduct);
        when(userService.findUserById(1)).thenReturn(testUser);
        when(wishlistRepository.findByUser(testUser)).thenReturn(Optional.of(testWishlist));
        when(productService.getProductDetails(100)).thenReturn(testProduct);

        assertThrows(IllegalArgumentException.class, () -> {
            wishlistService.addProductToWishlist(1, 100);
        });
        
        verify(wishlistRepository, never()).save(any());
    }

    @Test
    void testRemoveProductFromWishlist() {
        testWishlist.addProduct(testProduct);
        when(userService.findUserById(1)).thenReturn(testUser);
        when(wishlistRepository.findByUser(testUser)).thenReturn(Optional.of(testWishlist));
        when(productService.getProductDetails(100)).thenReturn(testProduct);
        when(wishlistRepository.save(any(Wishlist.class))).thenReturn(testWishlist);

        Wishlist result = wishlistService.removeProductFromWishlist(1, 100);

        assertFalse(result.containsProduct(testProduct));
        verify(wishlistRepository).save(testWishlist);
    }

    @Test
    void testIsProductInWishlist() {
        testWishlist.addProduct(testProduct);
        when(userService.findUserById(1)).thenReturn(testUser);
        when(wishlistRepository.findByUser(testUser)).thenReturn(Optional.of(testWishlist));
        when(productService.getProductDetails(100)).thenReturn(testProduct);

        boolean result = wishlistService.isProductInWishlist(1, 100);

        assertTrue(result);
    }
}
