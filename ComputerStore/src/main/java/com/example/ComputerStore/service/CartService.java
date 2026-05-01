package com.example.ComputerStore.service;

import com.example.ComputerStore.model.Order;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class CartService {

    private static final String CART_SESSION_KEY = "USER_CART";

    private final OrderService orderService;
    private final ProductService productService;

    public CartService(OrderService orderService,
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
        } // se memoreaza temporar ce vrea userul sa cumpere
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

    public Order checkout(HttpSession session, Integer userId,
                          String cardNumber, String cardName, String expiryDate, String cvv) {
        Map<Integer, Integer> cart = getCart(session);

        if (cart.isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        Order savedOrder = orderService.createOrderWithCard(userId, cart,
                cardNumber, cardName, expiryDate, cvv);

        clearCart(session);
        return savedOrder;
    }
}