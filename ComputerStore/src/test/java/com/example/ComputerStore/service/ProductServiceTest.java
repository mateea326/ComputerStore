package com.example.ComputerStore.service;

import com.example.ComputerStore.model.*;
import com.example.ComputerStore.repo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private MotherboardRepository motherboardRepository;
    @Mock
    private GraphicsCardRepository graphicsCardRepository;
    @Mock
    private ProcessorRepository processorRepository;
    @Mock
    private CaseRepository caseRepository;

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
        testProcessor.setCoreClock(4);
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
        when(productRepository.findAll()).thenReturn(products);

        // Act
        List<Product> result = productService.getAllProducts();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void getProductDetails_Success() {
        // Arrange
        when(productRepository.findById(1)).thenReturn(Optional.of(testProduct));

        // Act
        Product result = productService.getProductDetails(1);

        // Assert
        assertNotNull(result);
        assertEquals(testProduct.getProductId(), result.getProductId());
        assertEquals(testProduct.getName(), result.getName());
    }

    @Test
    void getProductDetails_NotFound_ThrowsException() {
        // Arrange
        when(productRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.getProductDetails(999)
        );
        assertTrue(exception.getMessage().contains("was not found"));
    }

    @Test
    void saveProduct_Success() {
        // Arrange
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        Product result = productService.saveProduct(testProduct);

        // Assert
        assertNotNull(result);
        assertEquals(testProduct.getName(), result.getName());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void filterProductsByType_Processors_Success() {
        // Arrange
        List<Processor> processors = Arrays.asList(testProcessor);
        when(processorRepository.findAll()).thenReturn(processors);

        // Act
        List<? extends Product> result = productService.filterProductsByType("processors");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(processorRepository, times(1)).findAll();
    }

    @Test
    void filterProductsByType_GraphicsCards_Success() {
        // Arrange
        List<GraphicsCard> gpus = Arrays.asList(testGraphicsCard);
        when(graphicsCardRepository.findAll()).thenReturn(gpus);

        // Act
        List<? extends Product> result = productService.filterProductsByType("graphics cards");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(graphicsCardRepository, times(1)).findAll();
    }

    @Test
    void filterProductsByType_InvalidType_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.filterProductsByType("invalid")
        );
        assertTrue(exception.getMessage().contains("not valid"));
    }

    @Test
    void deleteProduct_Success() {
        // Arrange
        doNothing().when(productRepository).deleteById(1);

        // Act
        productService.deleteProduct(1);

        // Assert
        verify(productRepository, times(1)).deleteById(1);
    }
}