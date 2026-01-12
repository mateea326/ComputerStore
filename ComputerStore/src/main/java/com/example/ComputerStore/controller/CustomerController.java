package com.example.ComputerStore.controller;

import org.springframework.web.util.HtmlUtils;
import com.example.ComputerStore.model.Customer;
import com.example.ComputerStore.service.CustomerService;
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

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/customers")
@Tag(name = "Customers", description = "Customer management and authentication APIs")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Operation(
            summary = "Register a new customer",
            description = "Create a new customer account with email, username, and password. Password will be hashed for security."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Customer successfully registered",
                    content = @Content(schema = @Schema(implementation = Customer.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input - username or email already exists"
            )
    })
    @PostMapping("/register")
    public ResponseEntity<Customer> registerCustomer(
            @Parameter(description = "Customer registration details", required = true)
            @Valid @RequestBody Customer customer) {
        customer.setUsername(HtmlUtils.htmlEscape(customer.getUsername()));
        customer.setEmail(HtmlUtils.htmlEscape(customer.getEmail()));

        Customer newCustomer = customerService.registerNewCustomer(customer);
        return new ResponseEntity<>(newCustomer, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Customer login",
            description = "Authenticate a customer using username and password. Returns customer details on success."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(schema = @Schema(implementation = Customer.class))
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
            @RequestBody Customer loginDetails) {
        if (loginDetails.getUsername() == null || loginDetails.getPassword() == null) {
            return ResponseEntity.badRequest().body("Username or password missing");
        }

        Customer customer = customerService.login(loginDetails.getUsername(), loginDetails.getPassword());

        if (customer == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        customer.setPassword(null); // Security: don't send password back
        return ResponseEntity.ok(customer);
    }

    @Operation(
            summary = "Update customer information",
            description = "Update customer profile details (name, address, phone, email). Password cannot be updated through this endpoint."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Customer successfully updated",
                    content = @Content(schema = @Schema(implementation = Customer.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Customer not found"
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(
            @Parameter(description = "Customer ID", required = true) @PathVariable Integer id,
            @Parameter(description = "Updated customer details", required = true) @Valid @RequestBody Customer updatedDetails) {
        Customer updated = customerService.updateCustomer(id, updatedDetails);
        updated.setPassword(null);
        return ResponseEntity.ok(updated);
    }

    @Operation(
            summary = "Delete customer account",
            description = "Permanently delete a customer account. This action cannot be undone."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Customer successfully deleted"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Customer not found"
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(
            @Parameter(description = "Customer ID", required = true) @PathVariable Integer id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}