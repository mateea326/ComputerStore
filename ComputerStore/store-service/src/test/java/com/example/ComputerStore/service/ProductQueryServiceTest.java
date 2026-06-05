package com.example.ComputerStore.service;

import com.example.ComputerStore.exception.ResourceNotFoundException;
import com.example.ComputerStore.model.*;
import com.example.ComputerStore.repo.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductQueryServiceTest {

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
    private ProductQueryService productQueryService;

    @Test
    void getAllProducts_ReturnsList() {
        List<Product> expected = Arrays.asList(new Processor(), new Case());
        when(productRepository.findAll()).thenReturn(expected);

        List<Product> actual = productQueryService.getAllProducts();

        assertEquals(2, actual.size());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void getAllProductsPageable_ReturnsPage() {
        Page<Product> expected = new PageImpl<>(Arrays.asList(new Processor()));
        Pageable pageable = PageRequest.of(0, 10);
        when(productRepository.findAll(pageable)).thenReturn(expected);

        Page<Product> actual = productQueryService.getAllProducts(pageable);

        assertEquals(1, actual.getTotalElements());
        verify(productRepository, times(1)).findAll(pageable);
    }

    @Test
    void searchProducts_ReturnsPage() {
        Page<Product> expected = new PageImpl<>(Arrays.asList(new Processor()));
        Pageable pageable = PageRequest.of(0, 10);
        when(productRepository.findByNameContainingIgnoreCase("intel", pageable)).thenReturn(expected);

        Page<Product> actual = productQueryService.searchProducts("intel", pageable);

        assertEquals(1, actual.getTotalElements());
        verify(productRepository, times(1)).findByNameContainingIgnoreCase("intel", pageable);
    }

    @Test
    void getProductDetails_ExistingId_ReturnsProduct() {
        Processor processor = new Processor();
        processor.setProductId(1);
        processor.setName("i9");
        when(productRepository.findById(1)).thenReturn(Optional.of(processor));

        Product actual = productQueryService.getProductDetails(1);

        assertNotNull(actual);
        assertEquals("i9", actual.getName());
        verify(productRepository, times(1)).findById(1);
    }

    @Test
    void getProductDetails_NonExistingId_ThrowsException() {
        when(productRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productQueryService.getProductDetails(99));
        verify(productRepository, times(1)).findById(99);
    }

    @Test
    void getAllProductsOrderByPopularity_ReturnsPage() {
        Page<Product> expected = new PageImpl<>(Collections.emptyList());
        Pageable pageable = PageRequest.of(0, 10);
        when(productRepository.findAllOrderByPopularity(pageable)).thenReturn(expected);

        Page<Product> actual = productQueryService.getAllProductsOrderByPopularity(pageable);

        assertNotNull(actual);
        verify(productRepository, times(1)).findAllOrderByPopularity(pageable);
    }

    @Test
    void filterProductsByType_Motherboards_ReturnsList() {
        List<Motherboard> expected = Arrays.asList(new Motherboard());
        when(motherboardRepository.findAll()).thenReturn(expected);

        List<? extends Product> actual = productQueryService.filterProductsByType("motherboards");

        assertEquals(1, actual.size());
        verify(motherboardRepository, times(1)).findAll();
    }

    @Test
    void filterProductsByType_Gpus_ReturnsList() {
        List<GraphicsCard> expected = Arrays.asList(new GraphicsCard());
        when(graphicsCardRepository.findAll()).thenReturn(expected);

        List<? extends Product> actual = productQueryService.filterProductsByType("gpus");

        assertEquals(1, actual.size());
        verify(graphicsCardRepository, times(1)).findAll();
    }

    @Test
    void filterProductsByType_Processors_ReturnsList() {
        List<Processor> expected = Arrays.asList(new Processor());
        when(processorRepository.findAll()).thenReturn(expected);

        List<? extends Product> actual = productQueryService.filterProductsByType("processors");

        assertEquals(1, actual.size());
        verify(processorRepository, times(1)).findAll();
    }

    @Test
    void filterProductsByType_Cases_ReturnsList() {
        List<Case> expected = Arrays.asList(new Case());
        when(caseRepository.findAll()).thenReturn(expected);

        List<? extends Product> actual = productQueryService.filterProductsByType("cases");

        assertEquals(1, actual.size());
        verify(caseRepository, times(1)).findAll();
    }

    @Test
    void filterProductsByType_Invalid_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> productQueryService.filterProductsByType("invalidType"));
    }
}
