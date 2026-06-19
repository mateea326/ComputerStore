package com.example.ComputerStore.service;

import com.example.ComputerStore.dto.UserRegistrationDTO;
import com.example.ComputerStore.dto.UserResponseDTO;
import com.example.ComputerStore.exception.ResourceNotFoundException;
import com.example.ComputerStore.model.User;
import com.example.ComputerStore.client.UserServiceClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Collections;

@Service
@Transactional
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserServiceClient userServiceClient;
    private final PasswordEncoder passwordEncoder;
    private final com.example.ComputerStore.repo.UserRepository userRepository;

    public UserService(UserServiceClient userServiceClient,
                       PasswordEncoder passwordEncoder,
                       com.example.ComputerStore.repo.UserRepository userRepository) {
        this.userServiceClient = userServiceClient;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    // MAPPER: Entity -> DTO raspuns (fara parola)
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

    // CREATE - inregistrare utilizator nou prin Feign Client
    // verifica daca parola introdusa si parola de confirmare conincid
    @CircuitBreaker(name = "userService", fallbackMethod = "registerUserFallback")
    @Retry(name = "userService")
    public UserResponseDTO registerNewUser(UserRegistrationDTO registrationDTO) {
        log.info("Registering user via Feign Client: {}", registrationDTO.getUsername());
        return userServiceClient.registerUser(registrationDTO);
    }

    // Fallback pentru registerNewUser
    public UserResponseDTO registerUserFallback(UserRegistrationDTO registrationDTO, Exception ex) {
        log.error("[CIRCUIT BREAKER] registerNewUser fallback activat pentru user={}: {}", registrationDTO.getUsername(), ex.getMessage());
        UserResponseDTO fallback = new UserResponseDTO();
        fallback.setUsername("SERVICE_UNAVAILABLE");
        return fallback;
    }

    // READ - autentificare manuala (folosit de API REST, nu de Spring Security)
    // folosim CircuitBreaker ca in cazul in care user-service nu mai merge desi am incercat de 2-3ori
    // sa nu blocam intreg store-service, ci doar o sa returneze o eroare de autentificare
    @CircuitBreaker(name = "userService", fallbackMethod = "loginFallback")
    @Retry(name = "userService")
    public UserResponseDTO login(String username, String password) {
        User user = null;
        try {
            user = userServiceClient.getUserByUsernameInternal(username);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Invalid username or password");
        }

        if (user == null) {
            throw new ResourceNotFoundException("Invalid username or password");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Failed login attempt for user: {}", username);
            throw new ResourceNotFoundException("Invalid username or password");
        }

        log.info("User logged in via Feign API: {}", username);
        return mapToResponseDTO(user);
    }

    // Fallback pentru login
    public UserResponseDTO loginFallback(String username, String password, Exception ex) {
        log.error("[CIRCUIT BREAKER] login fallback activat pentru user={}: {}", username, ex.getMessage());
        throw new ResourceNotFoundException("Serviciul de autentificare este temporar indisponibil. Incercati din nou.");
    }

    // UPDATE - editare profil utilizator prin Feign Client
    @CircuitBreaker(name = "userService", fallbackMethod = "updateUserFallback")
    @Retry(name = "userService")
    public UserResponseDTO updateUser(Integer userId, UserRegistrationDTO updatedDetails) {
        log.info("Updating user id={} via Feign Client", userId);
        return userServiceClient.updateUser(userId, updatedDetails);
    }

    // Fallback pentru updateUser
    public UserResponseDTO updateUserFallback(Integer userId, UserRegistrationDTO updatedDetails, Exception ex) {
        log.error("[CIRCUIT BREAKER] updateUser fallback activat pentru id={}: {}", userId, ex.getMessage());
        UserResponseDTO fallback = new UserResponseDTO();
        fallback.setUsername("SERVICE_UNAVAILABLE");
        return fallback;
    }

    // READ - gasire utilizator dupa ID (pentru alte servicii)
    @CircuitBreaker(name = "userService", fallbackMethod = "findUserByIdFallback")
    @Retry(name = "userService")
    public User findUserById(Integer id) {
        log.info("Fetching user id={} from local DB", id);
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    // Fallback pentru findUserById
    public User findUserByIdFallback(Integer id, Exception ex) {
        log.error("[CIRCUIT BREAKER] findUserById fallback activat pentru id={}: {}", id, ex.getMessage());
        User fallback = new User();
        fallback.setUserId(id);
        fallback.setUsername("unavailable");
        fallback.setFirstName("User");
        fallback.setLastName("Unavailable");
        return fallback;
    }

    // DELETE - stergere cont utilizator prin Feign Client
    @CircuitBreaker(name = "userService", fallbackMethod = "deleteUserFallback")
    @Retry(name = "userService")
    public UserResponseDTO deleteUser(Integer id) {
        log.info("Deleting user id={} via Feign Client", id);
        return userServiceClient.deleteUser(id);
    }

    // Fallback pentru deleteUser
    public UserResponseDTO deleteUserFallback(Integer id, Exception ex) {
        log.error("[CIRCUIT BREAKER] deleteUser fallback activat pentru id={}: {}", id, ex.getMessage());
        UserResponseDTO fallback = new UserResponseDTO();
        fallback.setUsername("SERVICE_UNAVAILABLE");
        return fallback;
    }

    // READ - toti utilizatorii (fara paginare) prin Feign Client
    @CircuitBreaker(name = "userService", fallbackMethod = "getAllUsersFallback")
    @Retry(name = "userService")
    public List<User> getAllUsers() {
        log.info("Fetching all users via Feign Client");
        return userServiceClient.getAllUsersList();
    }

    // Fallback pentru getAllUsers
    public List<User> getAllUsersFallback(Exception ex) {
        log.error("[CIRCUIT BREAKER] getAllUsers fallback activat: {}", ex.getMessage());
        return Collections.emptyList();
    }

    // READ - toti utilizatorii cu paginare prin Feign Client
    @CircuitBreaker(name = "userService", fallbackMethod = "getAllUsersPaginatedFallback")
    @Retry(name = "userService")
    public org.springframework.data.domain.Page<User> getAllUsers(org.springframework.data.domain.Pageable pageable) {
        log.info("Fetching paginated users via Feign Client");
        String sortParam = pageable.getSort().toString().replace(": ", ",");
        com.example.ComputerStore.dto.CustomPage<User> customPage = userServiceClient.getAllUsers(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sortParam
        );
        return new org.springframework.data.domain.PageImpl<>(
                customPage.getContent(),
                pageable,
                customPage.getTotalElements()
        );
    }

    // Fallback pentru getAllUsers paginat
    public org.springframework.data.domain.Page<User> getAllUsersPaginatedFallback(org.springframework.data.domain.Pageable pageable, Exception ex) {
        log.error("[CIRCUIT BREAKER] getAllUsersPaginated fallback activat: {}", ex.getMessage());
        return org.springframework.data.domain.Page.empty(pageable);
    }

    // UPDATE - schimbare rol utilizator prin Feign Client
    @CircuitBreaker(name = "userService", fallbackMethod = "changeUserRoleFallback")
    @Retry(name = "userService")
    public void changeUserRole(Integer userId, String newRole) {
        log.info("Changing role for user id={} to {} via Feign Client", userId, newRole);
        userServiceClient.changeUserRole(userId, newRole);
    }

    // Fallback pentru changeUserRole
    public void changeUserRoleFallback(Integer userId, String newRole, Exception ex) {
        log.error("[CIRCUIT BREAKER] changeUserRole fallback activat pentru id={}: {}", userId, ex.getMessage());
    }
}