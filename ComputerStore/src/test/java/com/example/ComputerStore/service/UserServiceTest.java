package com.example.ComputerStore.service;

import com.example.ComputerStore.model.User;
import com.example.ComputerStore.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository customerRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setCustomerId(1);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john@example.com");
        testUser.setUsername("johndoe");
        testUser.setPassword("password123");
        testUser.setPhoneNumber("1234567890");
        testUser.setAddress("123 Main St");
    }

    @Test
    void registerNewCustomer_Success() {
        // Arrange
        when(customerRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.empty());
        when(customerRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.empty());
        when(customerRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.registerNewCustomer(testUser);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());
        verify(customerRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerNewCustomer_UsernameAlreadyExists_ThrowsException() {
        // Arrange
        when(customerRepository.findByUsername(testUser.getUsername()))
                .thenReturn(Optional.of(testUser));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.registerNewCustomer(testUser)
        );
        assertEquals("Username already taken", exception.getMessage());
        verify(customerRepository, never()).save(any(User.class));
    }

    @Test
    void registerNewCustomer_EmailAlreadyExists_ThrowsException() {
        // Arrange
        when(customerRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.empty());
        when(customerRepository.findByEmail(testUser.getEmail()))
                .thenReturn(Optional.of(testUser));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.registerNewCustomer(testUser)
        );
        assertEquals("Email already taken", exception.getMessage());
        verify(customerRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success() {
        // Arrange
        String hashedPassword = userService.hashMe("password123");
        testUser.setPassword(hashedPassword);
        when(customerRepository.findByUsername(testUser.getUsername()))
                .thenReturn(Optional.of(testUser));

        // Act
        User result = userService.login("johndoe", "password123");

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());
    }

    @Test
    void login_UserNotFound_ThrowsException() {
        // Arrange
        when(customerRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.login("nonexistent", "password")
        );
        assertEquals("User not found. Please create an account", exception.getMessage());
    }

    @Test
    void login_IncorrectPassword_ThrowsException() {
        // Arrange
        String hashedPassword = userService.hashMe("password123");
        testUser.setPassword(hashedPassword);
        when(customerRepository.findByUsername(testUser.getUsername()))
                .thenReturn(Optional.of(testUser));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.login("johndoe", "wrongpassword")
        );
        assertEquals("Incorrect password", exception.getMessage());
    }

    @Test
    void findCustomerById_Success() {
        // Arrange
        when(customerRepository.findById(1)).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.findCustomerById(1);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getCustomerId(), result.getCustomerId());
    }

    @Test
    void findCustomerById_NotFound_ThrowsException() {
        // Arrange
        when(customerRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.findCustomerById(999)
        );
        assertTrue(exception.getMessage().contains("was not found"));
    }

    @Test
    void updateCustomer_Success() {
        // Arrange
        User updatedDetails = new User();
        updatedDetails.setFirstName("Jane");
        updatedDetails.setLastName("Smith");
        updatedDetails.setEmail("jane@example.com");
        updatedDetails.setPhoneNumber("0987654321");
        updatedDetails.setAddress("456 Oak Ave");

        when(customerRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(customerRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.updateCustomer(1, updatedDetails);

        // Assert
        assertNotNull(result);
        assertEquals("Jane", result.getFirstName());
        verify(customerRepository, times(1)).save(any(User.class));
    }

    @Test
    void deleteCustomer_Success() {
        // Arrange
        doNothing().when(customerRepository).deleteById(1);

        // Act
        userService.deleteCustomer(1);

        // Assert
        verify(customerRepository, times(1)).deleteById(1);
    }
}