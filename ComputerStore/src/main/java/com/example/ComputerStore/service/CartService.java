package com.example.ComputerStore.service;

import com.example.ComputerStore.exception.EmptyCartException;
import com.example.ComputerStore.model.Cart;
import com.example.ComputerStore.model.CartItem;
import com.example.ComputerStore.model.Order;
import com.example.ComputerStore.model.Product;
import com.example.ComputerStore.model.User;
import com.example.ComputerStore.repo.CartItemRepository;
import com.example.ComputerStore.repo.CartRepository;
import com.example.ComputerStore.repo.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class CartService {

    private final OrderService orderService;
    private final ProductService productService;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;

    public CartService(OrderService orderService,
                       ProductService productService,
                       CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       UserRepository userRepository) {
        this.orderService = orderService;
        this.productService = productService;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
    }

    private Cart getOrCreateDbCart(Integer userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return cartRepository.findByUser(user).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUser(user);
            return cartRepository.save(newCart);
        });
    }

    public Map<Integer, Integer> getCart(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return new HashMap<>();

        Cart dbCart = getOrCreateDbCart(userId);
        Map<Integer, Integer> cartMap = new HashMap<>();
        if (dbCart.getItems() != null) {
            for (CartItem item : dbCart.getItems()) {
                cartMap.put(item.getProduct().getProductId(), item.getQuantity());
            }
        }
        return cartMap;
    }

    public void addProductToCart(HttpSession session, Integer productId) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return;

        Product product = productService.getProductDetails(productId);
        Cart dbCart = getOrCreateDbCart(userId);

        CartItem itemToUpdate = null;
        if (dbCart.getItems() != null) {
            itemToUpdate = dbCart.getItems().stream()
                    .filter(item -> item.getProduct().getProductId().equals(productId))
                    .findFirst().orElse(null);
        }

        if (itemToUpdate != null) {
            itemToUpdate.setQuantity(itemToUpdate.getQuantity() + 1);
            cartItemRepository.save(itemToUpdate);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(dbCart);
            newItem.setProduct(product);
            newItem.setQuantity(1);
            if (dbCart.getItems() == null) {
                dbCart.setItems(new java.util.ArrayList<>());
            }
            dbCart.getItems().add(newItem);
            cartItemRepository.save(newItem);
        }
    }

    public void removeProductFromCart(HttpSession session, Integer productId) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return;

        Cart dbCart = getOrCreateDbCart(userId);
        if (dbCart.getItems() != null) {
            dbCart.getItems().stream()
                    .filter(item -> item.getProduct().getProductId().equals(productId))
                    .findFirst()
                    .ifPresent(item -> {
                        if (item.getQuantity() > 1) {
                            item.setQuantity(item.getQuantity() - 1);
                            cartItemRepository.save(item);
                        } else {
                            cartItemRepository.delete(item);
                            dbCart.getItems().remove(item);
                        }
                    });
        }
    }

    public void removeEntireProductFromCart(HttpSession session, Integer productId) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return;

        Cart dbCart = getOrCreateDbCart(userId);
        if (dbCart.getItems() != null) {
            dbCart.getItems().removeIf(item -> {
                if (item.getProduct().getProductId().equals(productId)) {
                    cartItemRepository.delete(item);
                    return true;
                }
                return false;
            });
        }
    }

    public void clearCart(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return;
        
        Cart dbCart = getOrCreateDbCart(userId);
        if (dbCart.getCartId() != null) {
            cartItemRepository.deleteByCartId(dbCart.getCartId());
        }
        if (dbCart.getItems() != null) {
            dbCart.getItems().clear();
        }
    }

    public Order checkout(HttpSession session, Integer userId) {
        Map<Integer, Integer> cart = getCart(session);

        if (cart.isEmpty()) {
            throw new EmptyCartException();
        }

        Order savedOrder = orderService.createOrder(userId, cart);

        clearCart(session);
        return savedOrder;
    }
}
