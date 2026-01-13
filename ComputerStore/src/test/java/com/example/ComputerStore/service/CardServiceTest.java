package com.example.ComputerStore.service;

import com.example.ComputerStore.model.Card;
import com.example.ComputerStore.model.Order;
import com.example.ComputerStore.repo.CardRepository;
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
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private CardService cardService;

    private Order testOrder;
    private Card testCard;

    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setOrderId(1);

        testCard = new Card();
        testCard.setCardId(1);
        testCard.setCardNumber("1234567890123456");
        testCard.setCardName("John Doe");
        testCard.setExpiryDate("12/25");
        testCard.setCVV("123");
        testCard.setOrder(testOrder);
    }

    @Test
    void processPayment_Success() {
        // Arrange
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        // Act
        Card result = cardService.processPayment(
                testOrder, "1234567890123456", "John Doe", "12/25", "123"
        );

        // Assert
        assertNotNull(result);
        assertEquals("1234567890123456", result.getCardNumber());
        assertEquals("John Doe", result.getCardName());
        assertEquals("12/25", result.getExpiryDate());
        assertEquals("123", result.getCVV());
        assertEquals(testOrder, result.getOrder());
        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    void processPayment_InvalidCardNumber_TooShort_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cardService.processPayment(testOrder, "123", "John Doe", "12/25", "123")
        );
        assertEquals("Invalid card number length", exception.getMessage());
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void processPayment_InvalidCardNumber_TooLong_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cardService.processPayment(testOrder, "123456789012345678901", "John Doe", "12/25", "123")
        );
        assertEquals("Invalid card number length", exception.getMessage());
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void processPayment_InvalidCardNumber_Null_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cardService.processPayment(testOrder, null, "John Doe", "12/25", "123")
        );
        assertEquals("Invalid card number length", exception.getMessage());
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void processPayment_InvalidExpiryDate_TooShort_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cardService.processPayment(testOrder, "1234567890123456", "John Doe", "12", "123")
        );
        assertEquals("Invalid expiry date format", exception.getMessage());
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void processPayment_InvalidExpiryDate_Null_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cardService.processPayment(testOrder, "1234567890123456", "John Doe", null, "123")
        );
        assertEquals("Invalid expiry date format", exception.getMessage());
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void processPayment_InvalidCVV_TooShort_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cardService.processPayment(testOrder, "1234567890123456", "John Doe", "12/25", "12")
        );
        assertEquals("Invalid CVV length", exception.getMessage());
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void processPayment_InvalidCVV_TooLong_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cardService.processPayment(testOrder, "1234567890123456", "John Doe", "12/25", "12345")
        );
        assertEquals("Invalid CVV length", exception.getMessage());
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void processPayment_InvalidCVV_Null_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cardService.processPayment(testOrder, "1234567890123456", "John Doe", "12/25", null)
        );
        assertEquals("Invalid CVV length", exception.getMessage());
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void getCardByOrderId_Success() {
        // Arrange
        List<Card> cards = Arrays.asList(testCard);
        when(cardRepository.findAll()).thenReturn(cards);

        // Act
        Card result = cardService.getCardByOrderId(1);

        // Assert
        assertNotNull(result);
        assertEquals(testCard.getCardId(), result.getCardId());
        assertEquals(1, result.getOrder().getOrderId());
    }

    @Test
    void getCardByOrderId_NotFound_ReturnsNull() {
        // Arrange
        List<Card> cards = Arrays.asList(testCard);
        when(cardRepository.findAll()).thenReturn(cards);

        // Act
        Card result = cardService.getCardByOrderId(999);

        // Assert
        assertNull(result);
    }

    @Test
    void getAllCards_Success() {
        // Arrange
        List<Card> cards = Arrays.asList(testCard);
        when(cardRepository.findAll()).thenReturn(cards);

        // Act
        List<Card> result = cardService.getAllCards();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCard, result.get(0));
        verify(cardRepository, times(1)).findAll();
    }

    @Test
    void processPayment_ValidMinimumLength_Success() {
        // Arrange
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        // Act
        Card result = cardService.processPayment(
                testOrder, "1234567890", "John", "12/25", "123"
        );

        // Assert
        assertNotNull(result);
        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    void processPayment_ValidMaximumLength_Success() {
        // Arrange
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        // Act
        Card result = cardService.processPayment(
                testOrder, "12345678901234567890", "John Doe", "12/2025 Extra Info", "1234"
        );

        // Assert
        assertNotNull(result);
        verify(cardRepository, times(1)).save(any(Card.class));
    }
}