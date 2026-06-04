package com.example.ComputerStore.client;

import com.example.ComputerStore.dto.UserRegistrationDTO;
import com.example.ComputerStore.dto.UserResponseDTO;
import com.example.ComputerStore.model.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "user-service", fallback = UserServiceFallback.class)
public interface UserServiceClient {

    @GetMapping("/api/v1/users/internal/id/{id}")
    User getUserByIdInternal(@PathVariable("id") Integer id);

    @GetMapping("/api/v1/users/internal/username/{username}")
    User getUserByUsernameInternal(@PathVariable("username") String username);

    @PostMapping("/api/v1/users/register")
    UserResponseDTO registerUser(@RequestBody UserRegistrationDTO userDTO);

    @PutMapping("/api/v1/users/{id}")
    UserResponseDTO updateUser(@PathVariable("id") Integer id, @RequestBody UserRegistrationDTO updatedDetails);

    @DeleteMapping("/api/v1/users/{id}")
    UserResponseDTO deleteUser(@PathVariable("id") Integer id);

    @GetMapping("/api/v1/users")
    com.example.ComputerStore.dto.CustomPage<User> getAllUsers(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("sort") String sort
    );

    @GetMapping("/api/v1/users/all")
    java.util.List<User> getAllUsersList();

    @PostMapping("/api/v1/users/{id}/role")
    void changeUserRole(@PathVariable("id") Integer id, @RequestParam("newRole") String newRole);
}
