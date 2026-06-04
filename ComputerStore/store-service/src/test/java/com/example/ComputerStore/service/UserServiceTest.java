package com.example.ComputerStore.service;

import com.example.ComputerStore.dto.UserRegistrationDTO;
import com.example.ComputerStore.dto.UserResponseDTO;
import com.example.ComputerStore.model.User;
import com.example.ComputerStore.client.UserServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserServiceClient userServiceClient;

    private UserService userService;
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private User testUser;

    @BeforeEach
    void setUp() {
        userService = new UserService(userServiceClient, passwordEncoder);
        
        testUser = new User();
        testUser.setUserId(1);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john@example.com");
        testUser.setUsername("johndoe");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setPhoneNumber("1234567890");
        testUser.setAddress("123 Main St");
        testUser.setRole("USER");
    }

    @Test
    void registerNewUser_Success() {
        UserRegistrationDTO regDto = new UserRegistrationDTO(
                "John", "Doe", "1234567890", "123 Main St",
                "john@example.com", "johndoe", "password123", "password123"
        );

        UserResponseDTO mockResponse = new UserResponseDTO();
        mockResponse.setUserId(1);
        mockResponse.setUsername("johndoe");

        when(userServiceClient.registerUser(any(UserRegistrationDTO.class))).thenReturn(mockResponse);

        UserResponseDTO result = userService.registerNewUser(regDto);

        assertNotNull(result);
        assertEquals(regDto.getUsername(), result.getUsername());
        verify(userServiceClient, times(1)).registerUser(any(UserRegistrationDTO.class));
    }

    @Test
    void login_Success() {
        when(userServiceClient.getUserByUsernameInternal(testUser.getUsername()))
                .thenReturn(testUser);

        UserResponseDTO result = userService.login("johndoe", "password123");

        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());
    }

    @Test
    void login_Failure_GenericMessage() {
        when(userServiceClient.getUserByUsernameInternal("johndoe")).thenReturn(testUser);

        com.example.ComputerStore.exception.ResourceNotFoundException exception = assertThrows(
                com.example.ComputerStore.exception.ResourceNotFoundException.class,
                () -> userService.login("johndoe", "wrongpassword")
        );
        assertEquals("Invalid username or password", exception.getMessage());
    }

    @Test
    void updateUser_Success() {
        UserRegistrationDTO updateDto = new UserRegistrationDTO();
        updateDto.setFirstName("Jane");
        updateDto.setLastName("Smith");
        updateDto.setEmail("jane@example.com");
        updateDto.setPhoneNumber("0987654321");
        updateDto.setAddress("456 Oak Ave");

        UserResponseDTO mockResponse = new UserResponseDTO();
        mockResponse.setUserId(1);
        mockResponse.setFirstName("Jane");

        when(userServiceClient.updateUser(eq(1), any(UserRegistrationDTO.class))).thenReturn(mockResponse);

        UserResponseDTO result = userService.updateUser(1, updateDto);

        assertNotNull(result);
        assertEquals("Jane", result.getFirstName());
        verify(userServiceClient, times(1)).updateUser(eq(1), any(UserRegistrationDTO.class));
    }

    @Test
    void deleteUser_Success() {
        UserResponseDTO mockResponse = new UserResponseDTO();
        mockResponse.setUserId(1);

        when(userServiceClient.deleteUser(1)).thenReturn(mockResponse);

        UserResponseDTO result = userService.deleteUser(1);

        assertNotNull(result);
        assertEquals(testUser.getUserId(), result.getUserId());
        verify(userServiceClient, times(1)).deleteUser(1);
    }

    @Test
    void findUserById_Success() {
        when(userServiceClient.getUserByIdInternal(1)).thenReturn(testUser);

        User result = userService.findUserById(1);

        assertNotNull(result);
        assertEquals(testUser.getUserId(), result.getUserId());
    }

    @Test
    void findUserById_NotFound_ThrowsException() {
        when(userServiceClient.getUserByIdInternal(999)).thenReturn(null);

        assertThrows(com.example.ComputerStore.exception.ResourceNotFoundException.class, 
                () -> userService.findUserById(999));
    }
}