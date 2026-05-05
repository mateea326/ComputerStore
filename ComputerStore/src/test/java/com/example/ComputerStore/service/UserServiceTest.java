package com.example.ComputerStore.service;

import com.example.ComputerStore.dto.UserRegistrationDTO;
import com.example.ComputerStore.dto.UserResponseDTO;
import com.example.ComputerStore.model.User;
import com.example.ComputerStore.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private User testUser;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);
        
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

        when(userRepository.findByUsername(regDto.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(regDto.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponseDTO result = userService.registerNewUser(regDto);

        assertNotNull(result);
        assertEquals(regDto.getUsername(), result.getUsername());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void login_Success() {
        when(userRepository.findByUsername(testUser.getUsername()))
                .thenReturn(Optional.of(testUser));

        UserResponseDTO result = userService.login("johndoe", "password123");

        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());
    }

    @Test
    void login_Failure_GenericMessage() {
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));

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

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponseDTO result = userService.updateUser(1, updateDto);

        assertNotNull(result);
        assertEquals("Jane", result.getFirstName());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void deleteUser_Success() {
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).delete(testUser);

        UserResponseDTO result = userService.deleteUser(1);

        assertNotNull(result);
        assertEquals(testUser.getUserId(), result.getUserId());
        verify(userRepository, times(1)).delete(testUser);
    }
    @Test
    void registerNewUser_DuplicateUsername_ThrowsException() {
        UserRegistrationDTO regDto = new UserRegistrationDTO(
                "John", "Doe", "1234567890", "123 Main St",
                "john@example.com", "johndoe", "password123", "password123"
        );

        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));

        assertThrows(com.example.ComputerStore.exception.DuplicateResourceException.class, 
                () -> userService.registerNewUser(regDto));
    }

    @Test
    void findUserById_NotFound_ThrowsException() {
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(com.example.ComputerStore.exception.ResourceNotFoundException.class, 
                () -> userService.findUserById(999));
    }

    @Test
    void deleteUser_NotFound_ThrowsException() {
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(com.example.ComputerStore.exception.ResourceNotFoundException.class, 
                () -> userService.deleteUser(999));
    }
}