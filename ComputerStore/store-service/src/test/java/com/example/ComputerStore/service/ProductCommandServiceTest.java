package com.example.ComputerStore.service;

import com.example.ComputerStore.model.*;
import com.example.ComputerStore.repo.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductCommandServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductQueryService productQueryService;
    @Mock
    private WishlistRepository wishlistRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private ProductCommandService productCommandService;

    @Test
    void saveProduct_Success() {
        Processor processor = new Processor();
        processor.setName("Intel i5");
        
        when(productRepository.save(any(Product.class))).thenReturn(processor);

        Product saved = productCommandService.saveProduct(processor);

        assertNotNull(saved);
        assertEquals("Intel i5", saved.getName());
        verify(productRepository, times(1)).save(processor);
    }

    @Test
    void updateProduct_Processor_Success() {
        Processor existing = new Processor();
        existing.setProductId(1);
        existing.setName("Old Intel");
        existing.setCoreCount(4);

        Processor updated = new Processor();
        updated.setName("New Intel");
        updated.setPrice(200.0);
        updated.setCoreCount(8);

        when(productQueryService.getProductDetails(1)).thenReturn(existing);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productCommandService.updateProduct(1, updated);

        assertTrue(result instanceof Processor);
        assertEquals("New Intel", result.getName());
        assertEquals(200.0, result.getPrice());
        assertEquals(8, ((Processor) result).getCoreCount());
        
        verify(productQueryService, times(1)).getProductDetails(1);
        verify(productRepository, times(1)).save(existing);
    }

    @Test
    void updateProduct_GraphicsCard_Success() {
        GraphicsCard existing = new GraphicsCard();
        existing.setProductId(2);
        
        GraphicsCard updated = new GraphicsCard();
        updated.setName("RTX");
        updated.setMemorySize(8);

        when(productQueryService.getProductDetails(2)).thenReturn(existing);
        when(productRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productCommandService.updateProduct(2, updated);

        assertTrue(result instanceof GraphicsCard);
        assertEquals(8, ((GraphicsCard) result).getMemorySize());
    }

    @Test
    void updateProduct_Motherboard_Success() {
        Motherboard existing = new Motherboard();
        existing.setProductId(3);
        
        Motherboard updated = new Motherboard();
        updated.setSlots(4);

        when(productQueryService.getProductDetails(3)).thenReturn(existing);
        when(productRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productCommandService.updateProduct(3, updated);

        assertTrue(result instanceof Motherboard);
        assertEquals(4, ((Motherboard) result).getSlots());
    }

    @Test
    void updateProduct_Case_Success() {
        Case existing = new Case();
        existing.setProductId(4);
        
        Case updated = new Case();
        updated.setType("ATX");

        when(productQueryService.getProductDetails(4)).thenReturn(existing);
        when(productRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productCommandService.updateProduct(4, updated);

        assertTrue(result instanceof Case);
        assertEquals("ATX", ((Case) result).getType());
    }

    @Test
    void updateProduct_UpdateImageUrl() {
        Processor existing = new Processor();
        existing.setProductId(1);
        existing.setImageUrl("old.jpg");

        Processor updated = new Processor();
        updated.setImageUrl("new.jpg");

        when(productQueryService.getProductDetails(1)).thenReturn(existing);
        when(productRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productCommandService.updateProduct(1, updated);
        
        assertEquals("new.jpg", result.getImageUrl());
    }

    @Test
    void deleteProduct_Success() {
        Processor processor = new Processor();
        processor.setProductId(1);

        when(productQueryService.getProductDetails(1)).thenReturn(processor);

        productCommandService.deleteProduct(1);

        verify(productQueryService, times(1)).getProductDetails(1);
        verify(wishlistRepository, times(1)).removeProductFromAllWishlists(1);
        verify(orderItemRepository, times(1)).deleteByProduct(processor);
        verify(cartItemRepository, times(1)).deleteByProduct(processor);
        verify(productRepository, times(1)).delete(processor);
    }
}
