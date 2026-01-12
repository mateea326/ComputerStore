package com.example.ComputerStore.service;

import com.example.ComputerStore.model.Customer;
import com.example.ComputerStore.model.Order;
import com.example.ComputerStore.model.Product;
import com.example.ComputerStore.repo.OrderRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class SessionCartService {

    private static final String CART_SESSION_KEY = "USER_CART";

    private final OrderService orderService;
    private final ProductService productService;

    public SessionCartService(OrderService orderService,
                              ProductService productService) {
        this.orderService = orderService;
        this.productService = productService;
    }

    @SuppressWarnings("unchecked")
    public Map<Integer, Integer> getCart(HttpSession session) {
        Map<Integer, Integer> cart = (Map<Integer, Integer>) session.getAttribute(CART_SESSION_KEY);
        if (cart == null) {
            cart = new HashMap<>();
            session.setAttribute(CART_SESSION_KEY, cart);
        }
        return cart;
    }

    public void addProductToCart(HttpSession session, Integer productId) {
        productService.getProductDetails(productId); // validate product exists
        Map<Integer, Integer> cart = getCart(session);
        cart.merge(productId, 1, Integer::sum);
    }

    public void removeProductFromCart(HttpSession session, Integer productId) {
        Map<Integer, Integer> cart = getCart(session);
        cart.computeIfPresent(productId, (key, count) -> (count > 1) ? count - 1 : null);
    }

    public void clearCart(HttpSession session) {
        Map<Integer, Integer> cart = getCart(session);
        cart.clear();
    }

    public Order checkout(HttpSession session, Integer customerId,
                          String cardNumber, String cardName, String expiryDate, String cvv) {
        Map<Integer, Integer> cart = getCart(session);

        if (cart.isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        Order savedOrder = orderService.createOrderWithCard(customerId, cart,
                cardNumber, cardName, expiryDate, cvv);

        clearCart(session);

        return savedOrder;
    }
}