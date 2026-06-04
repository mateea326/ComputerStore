package com.example.ComputerStore;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AdminIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.example.ComputerStore.client.UserServiceClient userServiceClient;

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testAdminAccess_WithAdminRole() throws Exception {
        org.mockito.Mockito.when(userServiceClient.getAllUsersList()).thenReturn(java.util.Collections.emptyList());
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void testAdminAccess_WithUserRole_ShouldBeForbidden() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAdminAccess_Unauthenticated_ShouldRedirect() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}
