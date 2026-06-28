package com.example.ComputerStore;

import com.example.ComputerStore.model.Product;
import com.example.ComputerStore.model.User;
import com.example.ComputerStore.repo.ProductRepository;
import com.example.ComputerStore.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class CheckoutIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.example.ComputerStore.client.UserServiceClient userServiceClient;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.example.ComputerStore.client.NotificationServiceClient notificationServiceClient;

    private int savedUserId;
    private int savedProductId;

    @BeforeEach
    void setUp() {
        // Create and save a test product
        Product product = new Product();
        product.setName("Test Processor Unit");
        product.setPrice(249.99f);
        product = productRepository.save(product);
        savedProductId = product.getProductId();

        // Create and save a test user
        User testUser = new User();
        testUser.setFirstName("Checkout");
        testUser.setLastName("Tester");
        testUser.setUsername("checkoutuser");
        testUser.setEmail("checkout@example.com");
        testUser.setRole("USER");
        testUser.setPassword("password123");
        testUser.setPhoneNumber("0712345678");
        testUser.setAddress("456 Test Avenue");
        testUser = userRepository.save(testUser);
        savedUserId = testUser.getUserId();

        // Mock the Feign client calls since user-service won't be running
        org.mockito.Mockito.when(userServiceClient.getUserByIdInternal(anyInt())).thenReturn(testUser);
        org.mockito.Mockito.when(userServiceClient.getUserByUsernameInternal(anyString())).thenReturn(testUser);
    }

    @Test
    void testCheckoutPage_unauthenticated_redirectsToLogin() throws Exception {
        // Unauthenticated users should be redirected to login
        mockMvc.perform(get("/checkout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "checkoutuser", roles = "USER")
    void testCheckoutPage_authenticatedWithEmptyCart_redirectsToCart() throws Exception {
        // Authenticated user with an empty cart should be redirected to the cart page
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", savedUserId);

        mockMvc.perform(get("/checkout").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));
    }

    @Test
    @WithMockUser(username = "checkoutuser", roles = "USER")
    void testCheckoutFlow_addToCartThenCheckout_success() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", savedUserId);

        // Step 1: Add a product to the cart
        mockMvc.perform(post("/cart/add/" + savedProductId)
                        .with(csrf())
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products"));

        // Step 2: View checkout page - should render correctly with items in cart
        mockMvc.perform(get("/checkout").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("checkout"))
                .andExpect(model().attributeExists("cartProducts"))
                .andExpect(model().attributeExists("cart"))
                .andExpect(model().attributeExists("total"));

        // Step 3: Process checkout with valid card details
        mockMvc.perform(post("/checkout")
                        .with(csrf())
                        .session(session)
                        .param("cardNumber", "4111111111111111")
                        .param("cardName", "Checkout Tester")
                        .param("expiryDate", "12/28")
                        .param("cvv", "123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/order-history"))
                .andExpect(flash().attributeExists("success"));
    }
}
