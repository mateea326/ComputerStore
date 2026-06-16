package com.example.ComputerStore.service;

import com.example.ComputerStore.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductQueryService productQueryService;
    @Mock
    private ProductCommandService productCommandService;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private Processor testProcessor;
    private GraphicsCard testGraphicsCard;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setProductId(1);
        testProduct.setName("Test Product");
        testProduct.setPrice(100.0f);

        testProcessor = new Processor();
        testProcessor.setProductId(2);
        testProcessor.setName("AMD Ryzen 5");
        testProcessor.setPrice(150.0f);
        testProcessor.setCoreCount(6);
        testProcessor.setCoreClock(4.0f);
        testProcessor.setSocket("AM4");

        testGraphicsCard = new GraphicsCard();
        testGraphicsCard.setProductId(3);
        testGraphicsCard.setName("RTX 3060");
        testGraphicsCard.setPrice(400.0f);
        testGraphicsCard.setMemorySize(8);
        testGraphicsCard.setCoreClock(1800);
        testGraphicsCard.setMemoryClock(14000);
    }

    @Test
    void getAllProducts_Success() {
        // Arrange
        List<Product> products = Arrays.asList(testProduct, testProcessor);
        when(productQueryService.getAllProducts()).thenReturn(products);

        // Act
        List<Product> result = productService.getAllProducts();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(productQueryService, times(1)).getAllProducts();
    }

    @Test
    void getProductDetails_Success() {
        // Arrange
        when(productQueryService.getProductDetails(1)).thenReturn(testProduct);

        // Act
        Product result = productService.getProductDetails(1);

        // Assert
        assertNotNull(result);
        assertEquals(testProduct.getProductId(), result.getProductId());
        assertEquals(testProduct.getName(), result.getName());
        verify(productQueryService, times(1)).getProductDetails(1);
    }

    @Test
    void getProductDetails_NotFound_ThrowsException() {
        // Arrange
        when(productQueryService.getProductDetails(999)).thenThrow(
                new com.example.ComputerStore.exception.ResourceNotFoundException("Product not found")
        );

        // Act & Assert
        assertThrows(
                com.example.ComputerStore.exception.ResourceNotFoundException.class,
                () -> productService.getProductDetails(999)
        );
    }

    @Test
    void saveProduct_Success() {
        // Arrange
        when(productCommandService.saveProduct(any(Product.class))).thenReturn(testProduct);

        // Act
        Product result = productService.saveProduct(testProduct);

        // Assert
        assertNotNull(result);
        assertEquals(testProduct.getName(), result.getName());
        verify(productCommandService, times(1)).saveProduct(any(Product.class));
    }

    @Test
    void filterProductsByType_Processors_Success() {
        // Arrange
        List<Product> processors = Arrays.asList(testProcessor);
        doReturn(processors).when(productQueryService).filterProductsByType("processors");

        // Act
        List<? extends Product> result = productService.filterProductsByType("processors");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productQueryService, times(1)).filterProductsByType("processors");
    }

    @Test
    void filterProductsByType_GraphicsCards_Success() {
        // Arrange
        List<Product> gpus = Arrays.asList(testGraphicsCard);
        doReturn(gpus).when(productQueryService).filterProductsByType("graphics cards");

        // Act
        List<? extends Product> result = productService.filterProductsByType("graphics cards");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productQueryService, times(1)).filterProductsByType("graphics cards");
    }

    @Test
    void filterProductsByType_InvalidType_ThrowsException() {
        // Arrange
        when(productQueryService.filterProductsByType("invalid")).thenThrow(
                new IllegalArgumentException("Invalid product type")
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.filterProductsByType("invalid")
        );
        assertTrue(exception.getMessage().contains("Invalid product type"));
    }

    @Test
    void deleteProduct_Success() {
        // Arrange
        doNothing().when(productCommandService).deleteProduct(1);

        // Act
        productService.deleteProduct(1);

        // Assert
        verify(productCommandService, times(1)).deleteProduct(1);
    }
}

