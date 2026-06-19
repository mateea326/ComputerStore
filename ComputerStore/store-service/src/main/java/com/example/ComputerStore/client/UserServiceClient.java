package com.example.ComputerStore.client;

import com.example.ComputerStore.dto.UserRegistrationDTO;
import com.example.ComputerStore.dto.UserResponseDTO;
import com.example.ComputerStore.model.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

// client feign folosit pentru comunicarea http declarativa cu microserviciul user-service
// daca user-service este offline, se va folosi clasa de fallback.

// il folosim ca sa simplificam apelurile http intre servicii fara sa scriem cod manual de retea
@FeignClient(name = "user-service", fallback = UserServiceFallback.class)
public interface UserServiceClient {

    // obtine detaliile unui utilizator dupa id direct din user-service pentru uz intern
    @GetMapping("/api/v1/users/internal/id/{id}")
    User getUserByIdInternal(@PathVariable("id") Integer id);

    @GetMapping("/api/v1/users/internal/username/{username}")
    User getUserByUsernameInternal(@PathVariable("username") String username);

    // trimite o cerere de inregistrare a unui utilizator nou in user-service
    @PostMapping("/api/v1/users/register")
    UserResponseDTO registerUser(@RequestBody UserRegistrationDTO userDTO);

    @PutMapping("/api/v1/users/{id}")
    UserResponseDTO updateUser(@PathVariable("id") Integer id, @RequestBody UserRegistrationDTO updatedDetails);

    @DeleteMapping("/api/v1/users/{id}")
    UserResponseDTO deleteUser(@PathVariable("id") Integer id);

    // obtine o pagina de utilizatori filtrati si sortati din user-service
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
