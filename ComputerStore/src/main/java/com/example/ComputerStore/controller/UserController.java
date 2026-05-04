package com.example.ComputerStore.controller;

import org.springframework.web.util.HtmlUtils;
import com.example.ComputerStore.model.User;
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
                    content = @Content(schema = @Schema(implementation = User.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input - username or email already exists"
            )
    })
    @PostMapping("/register")
    public ResponseEntity<User> registerUser(
            @Parameter(description = "User registration details", required = true)
            @Valid @RequestBody User user) {
        user.setUsername(HtmlUtils.htmlEscape(user.getUsername()));
        user.setEmail(HtmlUtils.htmlEscape(user.getEmail()));

        User newUser = userService.registerNewUser(user);
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
                    content = @Content(schema = @Schema(implementation = User.class))
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
            @RequestBody User loginDetails) {
        if (loginDetails.getUsername() == null || loginDetails.getPassword() == null) {
            return ResponseEntity.badRequest().body("Username or password missing");
        }

        User user = userService.login(loginDetails.getUsername(), loginDetails.getPassword());

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        user.setPassword(null); // Security: don't send password back
        return ResponseEntity.ok(user);
    }

    @Operation(
            summary = "Update user information",
            description = "Update user profile details (name, address, phone, email). Password cannot be updated through this endpoint."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User successfully updated",
                    content = @Content(schema = @Schema(implementation = User.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @Parameter(description = "User ID", required = true) @PathVariable Integer id,
            @Parameter(description = "Updated user details", required = true) @Valid @RequestBody User updatedDetails,
            HttpSession session) {
        Integer sessionUserId = (Integer) session.getAttribute("userId");
        if (sessionUserId == null || !sessionUserId.equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }
        User updated = userService.updateUser(id, updatedDetails);
        updated.setPassword(null);
        return ResponseEntity.ok(updated);
    }

    @Operation(
            summary = "Delete user account",
            description = "Permanently delete a user account. This action cannot be undone."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "User successfully deleted"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(
            @Parameter(description = "User ID", required = true) @PathVariable Integer id,
            HttpSession session) {
        Integer sessionUserId = (Integer) session.getAttribute("userId");
        if (sessionUserId == null || !sessionUserId.equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}