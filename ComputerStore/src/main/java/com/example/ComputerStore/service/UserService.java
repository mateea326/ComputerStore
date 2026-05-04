package com.example.ComputerStore.service;

import com.example.ComputerStore.dto.UserRegistrationDTO;
import com.example.ComputerStore.dto.UserResponseDTO;
import com.example.ComputerStore.exception.ResourceNotFoundException;
import com.example.ComputerStore.exception.DuplicateResourceException;
import com.example.ComputerStore.model.User;
import com.example.ComputerStore.repo.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    // MAPPER: Metodă internă de transformare a Entității în DTO de răspuns
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

    // comanda new account
    public UserResponseDTO registerNewUser(UserRegistrationDTO registrationDTO) {
        // 1. Verificăm validitatea parolelor (Confirm Password)
        if (!registrationDTO.getPassword().equals(registrationDTO.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        // 2. Verificăm unicitatea
        if (userRepository.findByUsername(registrationDTO.getUsername()).isPresent()) {
            throw new DuplicateResourceException("Username already taken");
        }
        if (userRepository.findByEmail(registrationDTO.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Email already taken");
        }

        // 3. Creăm entitatea
        User user = new User();
        user.setFirstName(registrationDTO.getFirstName());
        user.setLastName(registrationDTO.getLastName());
        user.setEmail(registrationDTO.getEmail());
        user.setPhoneNumber(registrationDTO.getPhoneNumber());
        user.setAddress(registrationDTO.getAddress());
        user.setUsername(registrationDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));

        // 4. Salvăm și returnăm versiunea sigură
        User savedUser = userRepository.save(user);
        log.info("New user registered: {}", savedUser.getUsername());
        return mapToResponseDTO(savedUser);
    }

    public UserResponseDTO login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid username or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Failed login attempt for user: {}", username);
            throw new ResourceNotFoundException("Invalid username or password");
        }

        log.info("User logged in: {}", username);
        return mapToResponseDTO(user);
    }

    // comanda edit account
    public UserResponseDTO updateUser(Integer userId, UserRegistrationDTO updatedDetails) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        existingUser.setFirstName(updatedDetails.getFirstName());
        existingUser.setLastName(updatedDetails.getLastName());
        existingUser.setPhoneNumber(updatedDetails.getPhoneNumber());
        existingUser.setAddress(updatedDetails.getAddress());
        existingUser.setEmail(updatedDetails.getEmail());
        // Permitem schimbarea username-ului doar dacă vrem, altfel îl lăsăm nemodificat.
        // Parolele se schimbă de obicei printr-un alt endpoint specific "change-password".

        User savedUser = userRepository.save(existingUser);
        log.info("User updated: id={}", userId);
        return mapToResponseDTO(savedUser);
    }

    // Metodă pentru backend/alte servicii, returnează entitatea
    public User findUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    // comanda delete user
    public UserResponseDTO deleteUser(Integer id) {
        User userToDelete = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        userRepository.delete(userToDelete);
        log.info("User deleted: id={}", id);
        return mapToResponseDTO(userToDelete);
    }

    // metoda pentru a obtine toti utilizatorii
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
}