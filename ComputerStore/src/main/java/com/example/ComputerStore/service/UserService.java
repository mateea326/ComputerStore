package com.example.ComputerStore.service;

import com.example.ComputerStore.dto.UserRegistrationDTO;
import com.example.ComputerStore.dto.UserResponseDTO;
import com.example.ComputerStore.model.User;
import com.example.ComputerStore.repo.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    String hashMe(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            return password;
        }
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
            throw new IllegalArgumentException("Username already taken");
        }
        if (userRepository.findByEmail(registrationDTO.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already taken");
        }

        // 3. Creăm entitatea
        User user = new User();
        user.setFirstName(registrationDTO.getFirstName());
        user.setLastName(registrationDTO.getLastName());
        user.setEmail(registrationDTO.getEmail());
        user.setPhoneNumber(registrationDTO.getPhoneNumber());
        user.setAddress(registrationDTO.getAddress());
        user.setUsername(registrationDTO.getUsername());
        user.setPassword(hashMe(registrationDTO.getPassword()));

        // 4. Salvăm și returnăm versiunea sigură
        User savedUser = userRepository.save(user);
        return mapToResponseDTO(savedUser);
    }

    // comanda login
    public UserResponseDTO login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty() || !passwordEncoder.matches(password, userOpt.get().getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        User user = userOpt.get();
        if (!user.getPassword().equals(hashMe(password))) {
            throw new IllegalArgumentException("Incorrect password");
        }

        return mapToResponseDTO(user);
    }

    // comanda edit account
    public UserResponseDTO updateUser(Integer userId, UserRegistrationDTO updatedDetails) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        existingUser.setFirstName(updatedDetails.getFirstName());
        existingUser.setLastName(updatedDetails.getLastName());
        existingUser.setPhoneNumber(updatedDetails.getPhoneNumber());
        existingUser.setAddress(updatedDetails.getAddress());
        existingUser.setEmail(updatedDetails.getEmail());
        // Permitem schimbarea username-ului doar dacă vrem, altfel îl lăsăm nemodificat.
        // Parolele se schimbă de obicei printr-un alt endpoint specific "change-password".

        User savedUser = userRepository.save(existingUser);
        return mapToResponseDTO(savedUser);
    }

    // Metodă pentru backend/alte servicii, returnează entitatea
    public User findUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User with id " + id + " was not found"));
    }

    // comanda delete user
    public UserResponseDTO deleteUser(Integer id) {
        User userToDelete = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User with id " + id + " was not found"));

        userRepository.delete(userToDelete);
        return mapToResponseDTO(userToDelete); // Poți returna detaliile utilizatorului șters
    }
}