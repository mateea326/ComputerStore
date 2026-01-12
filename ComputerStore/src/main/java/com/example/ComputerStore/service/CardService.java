package com.example.ComputerStore.service;

import com.example.ComputerStore.model.Card;
import com.example.ComputerStore.model.Order;
import com.example.ComputerStore.repo.CardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CardService {

    private final CardRepository cardRepository;

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public Card processPayment(Order order, String cardNumber, String cardName,
                               String expiryDate, String cvv) {
        validateCardNumber(cardNumber);
        validateExpiryDate(expiryDate);
        validateCVV(cvv);

        Card card = new Card();
        card.setCardNumber(cardNumber);
        card.setCardName(cardName);
        card.setExpiryDate(expiryDate);
        card.setCVV(cvv);
        card.setOrder(order);

        return cardRepository.save(card);
    }

    // gaseste cardul pentru o comanda
    public Card getCardByOrderId(Integer orderId) {
        return cardRepository.findAll().stream()
                .filter(card -> card.getOrder().getOrderId() == orderId)
                .findFirst()
                .orElse(null);
    }

    // obtine toate cardurile (pentru admin)
    public List<Card> getAllCards() {
        return cardRepository.findAll();
    }

    private void validateCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 10 || cardNumber.length() > 20) {
            throw new IllegalArgumentException("Invalid card number length");
        }
    }

    private void validateExpiryDate(String expiryDate) {
        if (expiryDate == null || expiryDate.length() < 5 || expiryDate.length() > 20) {
            throw new IllegalArgumentException("Invalid expiry date format");
        }
    }

    private void validateCVV(String cvv) {
        if (cvv == null || cvv.length() < 3 || cvv.length() > 4) {
            throw new IllegalArgumentException("Invalid CVV length");
        }
    }
}