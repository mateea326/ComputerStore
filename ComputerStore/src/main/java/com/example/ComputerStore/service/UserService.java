package com.example.ComputerStore.service;

import com.example.ComputerStore.dto.UserRegistrationDTO;
import com.example.ComputerStore.dto.UserResponseDTO;
import com.example.ComputerStore.exception.DuplicateResourceException;
import com.example.ComputerStore.exception.ResourceNotFoundException;
import com.example.ComputerStore.model.User;
import com.example.ComputerStore.repo.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.example.ComputerStore.repo.CartRepository cartRepository;
    private final com.example.ComputerStore.repo.CartItemRepository cartItemRepository;
    private final com.example.ComputerStore.repo.WishlistRepository wishlistRepository;
    private final com.example.ComputerStore.repo.OrderRepository orderRepository;
    private final com.example.ComputerStore.repo.OrderItemRepository orderItemRepository;

    public UserService(UserRepository userRepository, 
                       PasswordEncoder passwordEncoder,
                       com.example.ComputerStore.repo.CartRepository cartRepository,
                       com.example.ComputerStore.repo.CartItemRepository cartItemRepository,
                       com.example.ComputerStore.repo.WishlistRepository wishlistRepository,
                       com.example.ComputerStore.repo.OrderRepository orderRepository,
                       com.example.ComputerStore.repo.OrderItemRepository orderItemRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.wishlistRepository = wishlistRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    // MAPPER: Entity -> DTO răspuns (fără parolă)
    private UserResponseDTO mapToResponseDTO(User user) {
        UserResponseDTO responseDTO = new UserResponseDTO();
        responseDTO.setUserId(user.getUserId());
        responseDTO.setFirstName(user.getFirstName());
        responseDTO.setLastName(user.getLastName());
        responseDTO.setEmail(user.getEmail());
        responseDTO.setUsername(user.getUsername());
        responseDTO.setPhoneNumber(user.getPhoneNumber());
        responseDTO.setAddress(user.getAddress());
        return responseDTO;
    }

    // CREATE – înregistrare utilizator nou
    public UserResponseDTO registerNewUser(UserRegistrationDTO registrationDTO) {
        // Validare parole
        if (!registrationDTO.getPassword().equals(registrationDTO.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        // Unicitate username și email
        if (userRepository.findByUsername(registrationDTO.getUsername()).isPresent()) {
            throw new DuplicateResourceException("User", "username", registrationDTO.getUsername());
        }
        if (userRepository.findByEmail(registrationDTO.getEmail()).isPresent()) {
            throw new DuplicateResourceException("User", "email", registrationDTO.getEmail());
        }

        User user = new User();
        user.setFirstName(registrationDTO.getFirstName());
        user.setLastName(registrationDTO.getLastName());
        user.setEmail(registrationDTO.getEmail());
        user.setPhoneNumber(registrationDTO.getPhoneNumber());
        user.setAddress(registrationDTO.getAddress());
        user.setUsername(registrationDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));

        User savedUser = userRepository.save(user);
        log.info("New user registered: {}", savedUser.getUsername());
        return mapToResponseDTO(savedUser);
    }

    // READ – autentificare manuală (folosit de API REST, nu de Spring Security)
    public UserResponseDTO login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid username or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Failed login attempt for user: {}", username);
            throw new ResourceNotFoundException("Invalid username or password");
        }

        log.info("User logged in via API: {}", username);
        return mapToResponseDTO(user);
    }

    // UPDATE – editare profil utilizator
    public UserResponseDTO updateUser(Integer userId, UserRegistrationDTO updatedDetails) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        existingUser.setFirstName(updatedDetails.getFirstName());
        existingUser.setLastName(updatedDetails.getLastName());
        existingUser.setPhoneNumber(updatedDetails.getPhoneNumber());
        existingUser.setAddress(updatedDetails.getAddress());
        existingUser.setEmail(updatedDetails.getEmail());

        User savedUser = userRepository.save(existingUser);
        log.info("User updated: id={}", userId);
        return mapToResponseDTO(savedUser);
    }

    // READ – găsire utilizator după ID (pentru alte servicii)
    public User findUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    // DELETE – ștergere cont utilizator
    @Transactional
    public UserResponseDTO deleteUser(Integer id) {
        User userToDelete = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // 1. Curățăm coșul (Cart)
        if (userToDelete.getCart() != null) {
            cartItemRepository.deleteByCartId(userToDelete.getCart().getCartId());
            cartRepository.delete(userToDelete.getCart());
        }

        // 2. Curățăm wishlist-ul (produsele din tabelul de legătură)
        if (userToDelete.getWishlist() != null) {
            wishlistRepository.deleteProductsByWishlistId(userToDelete.getWishlist().getWishlistId());
            wishlistRepository.delete(userToDelete.getWishlist());
        }

        // 3. Curățăm comenzile (Orders)
        if (userToDelete.getOrders() != null) {
            for (com.example.ComputerStore.model.Order order : userToDelete.getOrders()) {
                orderItemRepository.deleteByOrderId(order.getOrderId());
            }
            orderRepository.deleteAll(userToDelete.getOrders());
        }

        // 4. Ștergem utilizatorul
        userRepository.delete(userToDelete);
        log.info("User and all related data deleted: id={}", id);
        return mapToResponseDTO(userToDelete);
    }

    // READ – toți utilizatorii (fără paginare)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // READ – toți utilizatorii cu paginare și sortare
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
}