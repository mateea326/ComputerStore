package com.example.ComputerStore.integration;

import com.example.ComputerStore.client.UserServiceClient;
import com.example.ComputerStore.model.User;
import com.example.ComputerStore.model.Product;
import com.example.ComputerStore.service.CartService;
import com.example.ComputerStore.service.OrderService;
import com.example.ComputerStore.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CheckoutIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserServiceClient userServiceClient;

    @MockBean
    private CartService cartService;

    @MockBean
    private OrderService orderService;

    @MockBean
    private ProductService productService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setUserId(1);
        mockUser.setUsername("testuser");
        mockUser.setEmail("test@test.com");
    }

    @Test
    void checkoutPage_Unauthenticated_RedirectsToLogin() throws Exception {
        mockMvc.perform(get("/checkout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void checkoutPage_Authenticated_EmptyCart_RedirectsToCart() throws Exception {
        // Setup session with userId
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 1);
        
        when(userServiceClient.getUserByUsernameInternal("testuser")).thenReturn(mockUser);
        when(cartService.getCart(any())).thenReturn(new HashMap<>()); // Empty cart

        mockMvc.perform(get("/checkout").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void checkoutPage_Authenticated_WithItems_ReturnsView() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 1);
        
        when(userServiceClient.getUserByUsernameInternal("testuser")).thenReturn(mockUser);
        
        Map<Integer, Integer> cart = new HashMap<>();
        cart.put(1, 1); // 1 item of product 1
        when(cartService.getCart(any())).thenReturn(cart);

        Product p = new Product();
        p.setProductId(1);
        p.setName("Test Product");
        p.setPrice(100.0f);
        when(productService.getProductDetails(1)).thenReturn(p);

        mockMvc.perform(get("/checkout").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("checkout"))
                .andExpect(model().attributeExists("cartProducts"))
                .andExpect(model().attributeExists("productMap"))
                .andExpect(model().attributeExists("cart"))
                .andExpect(model().attributeExists("total"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void processCheckout_ValidCart_RedirectsToOrderHistory() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 1);
        
        when(userServiceClient.getUserByUsernameInternal("testuser")).thenReturn(mockUser);
        
        Map<Integer, Integer> cart = new HashMap<>();
        cart.put(1, 1);
        when(cartService.getCart(any())).thenReturn(cart);
        
        mockMvc.perform(post("/checkout")
                .session(session)
                .with(csrf())
                .param("cardNumber", "4111111111111111")
                .param("cardName", "Checkout Tester")
                .param("expiryDate", "12/28")
                .param("cvv", "123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/order-history"))
                .andExpect(flash().attributeExists("success"));
                
        verify(cartService, times(1)).checkout(any(), eq(1));
    }
}
