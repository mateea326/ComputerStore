package com.example.ComputerStore.client;

import com.example.ComputerStore.dto.CustomPage;
import com.example.ComputerStore.dto.UserRegistrationDTO;
import com.example.ComputerStore.dto.UserResponseDTO;
import com.example.ComputerStore.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Fallback implementation pentru UserServiceClient.
 * Aceasta clasa este apelata automat de Feign + Circuit Breaker
 * cand user-service este indisponibil sau raspunde prea lent.
 */
@Component
public class UserServiceFallback implements UserServiceClient {

    private static final Logger log = LoggerFactory.getLogger(UserServiceFallback.class);

    @Override
    public User getUserByIdInternal(Integer id) {
        log.warn("[CIRCUIT BREAKER - FALLBACK] user-service indisponibil! getUserByIdInternal(id={}) -> returnez user gol", id);
        User fallbackUser = new User();
        fallbackUser.setUserId(id);
        fallbackUser.setUsername("unavailable");
        fallbackUser.setFirstName("Service");
        fallbackUser.setLastName("Unavailable");
        return fallbackUser;
    }

    @Override
    public User getUserByUsernameInternal(String username) {
        log.warn("[CIRCUIT BREAKER - FALLBACK] user-service indisponibil! getUserByUsernameInternal(username={}) -> returnez null", username);
        // Returnam null ca sa fie prins de Spring Security si sa refuze autentificarea
        return null;
    }

    @Override
    public UserResponseDTO registerUser(UserRegistrationDTO userDTO) {
        log.warn("[CIRCUIT BREAKER - FALLBACK] user-service indisponibil! registerUser -> operatie imposibila");
        UserResponseDTO fallback = new UserResponseDTO();
        fallback.setUsername("error");
        return fallback;
    }

    @Override
    public UserResponseDTO updateUser(Integer id, UserRegistrationDTO updatedDetails) {
        log.warn("[CIRCUIT BREAKER - FALLBACK] user-service indisponibil! updateUser(id={}) -> operatie imposibila", id);
        UserResponseDTO fallback = new UserResponseDTO();
        fallback.setUsername("error");
        return fallback;
    }

    @Override
    public UserResponseDTO deleteUser(Integer id) {
        log.warn("[CIRCUIT BREAKER - FALLBACK] user-service indisponibil! deleteUser(id={}) -> operatie imposibila", id);
        UserResponseDTO fallback = new UserResponseDTO();
        fallback.setUsername("error");
        return fallback;
    }

    @Override
    public CustomPage<User> getAllUsers(int page, int size, String sort) {
        log.warn("[CIRCUIT BREAKER - FALLBACK] user-service indisponibil! getAllUsers -> returnez lista goala");
        CustomPage<User> fallback = new CustomPage<>();
        fallback.setContent(Collections.emptyList());
        fallback.setTotalElements(0);
        fallback.setTotalPages(0);
        return fallback;
    }

    @Override
    public List<User> getAllUsersList() {
        log.warn("[CIRCUIT BREAKER - FALLBACK] user-service indisponibil! getAllUsersList -> returnez lista goala");
        return Collections.emptyList();
    }

    @Override
    public void changeUserRole(Integer id, String newRole) {
        log.warn("[CIRCUIT BREAKER - FALLBACK] user-service indisponibil! changeUserRole(id={}) -> operatie imposibila", id);
    }
}
