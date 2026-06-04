package com.example.ComputerStore.controller;

import com.example.ComputerStore.dto.UserRegistrationDTO;
import com.example.ComputerStore.dto.UserResponseDTO;
import com.example.ComputerStore.dto.UserLoginDTO;
import com.example.ComputerStore.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;
import jakarta.servlet.http.HttpSession;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "User management and authentication APIs")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(
            summary = "Register a new user",
            description = "Create a new user account with email, username, and password. Password will be hashed for security."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "User successfully registered",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input - username or email already exists"
            )
    })
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerUser(
            @Parameter(description = "User registration details", required = true)
            @Valid @RequestBody UserRegistrationDTO userDTO) {

        // Curățăm datele de intrare pentru securitate (XSS)[cite: 1]
        userDTO.setUsername(HtmlUtils.htmlEscape(userDTO.getUsername()));
        userDTO.setEmail(HtmlUtils.htmlEscape(userDTO.getEmail()));

        // Serviciul se ocupă de transformarea DTO în Entitate și salvare[cite: 24]
        UserResponseDTO newUser = userService.registerNewUser(userDTO);
        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }

    @Operation(
            summary = "User login",
            description = "Authenticate a user using username and password. Returns user details on success."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials - incorrect username or password"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Missing username or password"
            )
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Parameter(description = "Login credentials (username and password)", required = true)
            @RequestBody UserLoginDTO loginDetails) {

        // Verificăm dacă datele de autentificare sunt prezente[cite: 1]
        if (loginDetails.getUsername() == null || loginDetails.getPassword() == null) {
            return ResponseEntity.badRequest().body("Username or password missing");
        }

        try {
            // Tentativă de autentificare prin intermediul serviciului[cite: 24]
            UserResponseDTO user = userService.login(loginDetails.getUsername(), loginDetails.getPassword());
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            // Returnăm 401 Unauthorized în cazul în care credențialele sunt greșite[cite: 1]
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @Operation(
            summary = "Update user information",
            description = "Update user profile details. Password update is usually handled by a different process."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User successfully updated",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @Parameter(description = "User ID", required = true) @PathVariable Integer id,
            @Parameter(description = "Updated user details", required = true) @Valid @RequestBody UserRegistrationDTO updatedDetails) {

        // Actualizăm profilul folosind DTO-ul primit[cite: 24]
        UserResponseDTO updated = userService.updateUser(id, updatedDetails);
        return ResponseEntity.ok(updated);
    }

    @Operation(
            summary = "Delete user account",
            description = "Permanently delete a user account. This action cannot be undone."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User successfully deleted"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<UserResponseDTO> deleteUser(
            @Parameter(description = "User ID", required = true) @PathVariable Integer id) {

        // Ștergem utilizatorul și returnăm detaliile acestuia pentru confirmare[cite: 24]
        UserResponseDTO deletedUser = userService.deleteUser(id);
        return ResponseEntity.ok(deletedUser);
    }
}