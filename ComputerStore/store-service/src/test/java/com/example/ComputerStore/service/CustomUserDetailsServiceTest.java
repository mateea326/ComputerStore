package com.example.ComputerStore.service;

import com.example.ComputerStore.model.User;
import com.example.ComputerStore.client.UserServiceClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTest {

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsername_Success() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("hashedpass");
        user.setRole("USER");

        when(userServiceClient.getUserByUsernameInternal("testuser")).thenReturn(user);

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("testuser");

        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("hashedpass", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void loadUserByUsername_UserNull_ThrowsException() {
        when(userServiceClient.getUserByUsernameInternal("nonexistent")).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername("nonexistent");
        });
    }

    @Test
    void loadUserByUsername_ClientException_ThrowsException() {
        when(userServiceClient.getUserByUsernameInternal("error")).thenThrow(new RuntimeException("Connection error"));

        assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername("error");
        });
    }
}
