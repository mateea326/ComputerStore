package com.example.ComputerStore.controller;

import com.example.ComputerStore.model.Order;
import com.example.ComputerStore.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Order management and history APIs")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(
            summary = "Get user order history",
            description = "Retrieve all orders placed by a specific user, including order details and purchased items"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved order history",
                    content = @Content(schema = @Schema(implementation = Order.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            )
    })
    @GetMapping("/history/{userId}")
    public ResponseEntity<?> getOrderHistory(
            @Parameter(description = "ID of the user", required = true, example = "1")
            @PathVariable Integer userId,
            HttpSession session) {
        Integer sessionUserId = (Integer) session.getAttribute("userId");
        if (sessionUserId == null || !sessionUserId.equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }
        List<Order> orders = orderService.getOrderHistory(userId);
        return ResponseEntity.ok(orders);
    }

    @Operation(
            summary = "Get order by ID",
            description = "Retrieve detailed information about a specific order including all items and payment details"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved order",
                    content = @Content(schema = @Schema(implementation = Order.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Order not found"
            )
    })
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrder(
            @Parameter(description = "ID of the order", required = true, example = "1")
            @PathVariable Integer orderId,
            HttpSession session) {
        Order order = orderService.getOrderById(orderId);
        Integer sessionUserId = (Integer) session.getAttribute("userId");
        if (sessionUserId == null || order.getUser().getUserId() != sessionUserId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }
        return ResponseEntity.ok(order);
    }

    @Operation(
            summary = "Get all orders",
            description = "Retrieve all orders in the system (typically used for admin purposes)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved all orders",
                    content = @Content(schema = @Schema(implementation = Order.class))
            )
    })
    @GetMapping
    public ResponseEntity<?> getAllOrders(HttpSession session) {
        // Simple placeholder for admin check
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        if (isAdmin == null || !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }
}