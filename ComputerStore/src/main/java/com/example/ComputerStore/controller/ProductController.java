package com.example.ComputerStore.controller;

import com.example.ComputerStore.model.Product;
import com.example.ComputerStore.service.ProductService;
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
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Products", description = "Product catalog management APIs")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(
            summary = "Add a new product",
            description = "Create a new product in the catalog. Can be a processor, graphics card, motherboard, or case."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Product successfully created",
                    content = @Content(schema = @Schema(implementation = Product.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid product data"
            )
    })
    @PostMapping
    public ResponseEntity<Product> addProduct(
            @Parameter(description = "Product details", required = true)
            @Valid @RequestBody Product product) {
        Product savedProduct = productService.saveProduct(product);
        return new ResponseEntity<>(savedProduct, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Get all products",
            description = "Retrieve a list of all products in the catalog across all categories"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved product list",
                    content = @Content(schema = @Schema(implementation = Product.class))
            )
    })
    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @Operation(
            summary = "Get product by ID",
            description = "Retrieve detailed information about a specific product"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Product found",
                    content = @Content(schema = @Schema(implementation = Product.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductDetails(
            @Parameter(description = "Product ID", required = true) @PathVariable Integer id) {
        Product product = productService.getProductDetails(id);
        return ResponseEntity.ok(product);
    }

    @Operation(
            summary = "Filter products by type",
            description = "Get products filtered by category. Valid types: 'processors', 'motherboards', 'graphics cards', 'gpus', 'cases'"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully filtered products"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid product type"
            )
    })
    @GetMapping("/filter")
    public ResponseEntity<List<? extends Product>> filterProductsByType(
            @Parameter(
                    description = "Product type (processors/motherboards/graphics cards/cases)",
                    required = true,
                    example = "processors"
            )
            @RequestParam String type) {
        List<? extends Product> filteredProducts = productService.filterProductsByType(type);
        return ResponseEntity.ok(filteredProducts);
    }
}