package com.example.ComputerStore;

import com.example.ComputerStore.model.Product;
import com.example.ComputerStore.repo.ProductRepository;
import com.example.ComputerStore.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
public class StoreIntegrationTest {

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

    @BeforeEach
    void setUp() {
        // Prepare some data
        Product p1 = new Product();
        p1.setName("Test Processor");
        p1.setPrice(199.99f);
        productRepository.save(p1);

        com.example.ComputerStore.model.User testUser = new com.example.ComputerStore.model.User();
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setRole("USER");
        testUser.setPassword("password123");
        testUser.setPhoneNumber("1234567890");
        testUser.setAddress("123 Main St");
        testUser = userRepository.save(testUser);
        savedUserId = testUser.getUserId();

        org.mockito.Mockito.when(userServiceClient.getUserByIdInternal(anyInt())).thenReturn(testUser);
        org.mockito.Mockito.when(userServiceClient.getUserByUsernameInternal(anyString())).thenReturn(testUser);
    }

    @Test
    void testPublicPagesAccessible() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));

        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void testAuthenticatedPagesAccessible() throws Exception {
        mockMvc.perform(get("/products")
                .sessionAttr("userId", savedUserId))
                .andExpect(status().isOk())
                .andExpect(view().name("products"))
                .andExpect(model().attributeExists("products"));
    }

    @Test
    void testUnauthenticatedRedirect() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void testAddToCartFlow() throws Exception {
        Product p = productRepository.findAll().get(0);
        
        // Add to cart
        mockMvc.perform(post("/cart/add/" + p.getProductId())
                .with(csrf())
                .sessionAttr("userId", savedUserId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products"));
        
        // Check cart page
        mockMvc.perform(get("/cart")
                .sessionAttr("userId", savedUserId))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attributeExists("cartProducts"));
    }
}
