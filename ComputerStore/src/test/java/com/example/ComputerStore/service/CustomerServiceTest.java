package com.example.ComputerStore.service;

import com.example.ComputerStore.model.Customer;
import com.example.ComputerStore.repo.CustomerRepository;
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
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setCustomerId(1);
        testCustomer.setFirstName("John");
        testCustomer.setLastName("Doe");
        testCustomer.setEmail("john@example.com");
        testCustomer.setUsername("johndoe");
        testCustomer.setPassword("password123");
        testCustomer.setPhoneNumber("1234567890");
        testCustomer.setAddress("123 Main St");
    }

    @Test
    void registerNewCustomer_Success() {
        // Arrange
        when(customerRepository.findByUsername(testCustomer.getUsername())).thenReturn(Optional.empty());
        when(customerRepository.findByEmail(testCustomer.getEmail())).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        // Act
        Customer result = customerService.registerNewCustomer(testCustomer);

        // Assert
        assertNotNull(result);
        assertEquals(testCustomer.getUsername(), result.getUsername());
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void registerNewCustomer_UsernameAlreadyExists_ThrowsException() {
        // Arrange
        when(customerRepository.findByUsername(testCustomer.getUsername()))
                .thenReturn(Optional.of(testCustomer));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> customerService.registerNewCustomer(testCustomer)
        );
        assertEquals("Username already taken", exception.getMessage());
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void registerNewCustomer_EmailAlreadyExists_ThrowsException() {
        // Arrange
        when(customerRepository.findByUsername(testCustomer.getUsername())).thenReturn(Optional.empty());
        when(customerRepository.findByEmail(testCustomer.getEmail()))
                .thenReturn(Optional.of(testCustomer));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> customerService.registerNewCustomer(testCustomer)
        );
        assertEquals("Email already taken", exception.getMessage());
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void login_Success() {
        // Arrange
        String hashedPassword = customerService.hashMe("password123");
        testCustomer.setPassword(hashedPassword);
        when(customerRepository.findByUsername(testCustomer.getUsername()))
                .thenReturn(Optional.of(testCustomer));

        // Act
        Customer result = customerService.login("johndoe", "password123");

        // Assert
        assertNotNull(result);
        assertEquals(testCustomer.getUsername(), result.getUsername());
    }

    @Test
    void login_UserNotFound_ThrowsException() {
        // Arrange
        when(customerRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> customerService.login("nonexistent", "password")
        );
        assertEquals("User not found. Please create an account", exception.getMessage());
    }

    @Test
    void login_IncorrectPassword_ThrowsException() {
        // Arrange
        String hashedPassword = customerService.hashMe("password123");
        testCustomer.setPassword(hashedPassword);
        when(customerRepository.findByUsername(testCustomer.getUsername()))
                .thenReturn(Optional.of(testCustomer));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> customerService.login("johndoe", "wrongpassword")
        );
        assertEquals("Incorrect password", exception.getMessage());
    }

    @Test
    void findCustomerById_Success() {
        // Arrange
        when(customerRepository.findById(1)).thenReturn(Optional.of(testCustomer));

        // Act
        Customer result = customerService.findCustomerById(1);

        // Assert
        assertNotNull(result);
        assertEquals(testCustomer.getCustomerId(), result.getCustomerId());
    }

    @Test
    void findCustomerById_NotFound_ThrowsException() {
        // Arrange
        when(customerRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> customerService.findCustomerById(999)
        );
        assertTrue(exception.getMessage().contains("was not found"));
    }

    @Test
    void updateCustomer_Success() {
        // Arrange
        Customer updatedDetails = new Customer();
        updatedDetails.setFirstName("Jane");
        updatedDetails.setLastName("Smith");
        updatedDetails.setEmail("jane@example.com");
        updatedDetails.setPhoneNumber("0987654321");
        updatedDetails.setAddress("456 Oak Ave");

        when(customerRepository.findById(1)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        // Act
        Customer result = customerService.updateCustomer(1, updatedDetails);

        // Assert
        assertNotNull(result);
        assertEquals("Jane", result.getFirstName());
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void deleteCustomer_Success() {
        // Arrange
        doNothing().when(customerRepository).deleteById(1);

        // Act
        customerService.deleteCustomer(1);

        // Assert
        verify(customerRepository, times(1)).deleteById(1);
    }
}