package com.example.ComputerStore.controller.advice;

import com.example.ComputerStore.service.CartService;
import com.example.ComputerStore.service.WishlistService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import java.util.Map;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final CartService cartService;
    private final WishlistService wishlistService;

    public GlobalControllerAdvice(CartService cartService, WishlistService wishlistService) {
        this.cartService = cartService;
        this.wishlistService = wishlistService;
    }

    @ModelAttribute("cartCount")
    public int getCartCount(HttpSession session) {
        Map<Integer, Integer> cart = cartService.getCart(session);
        return cart.values().stream().mapToInt(Integer::intValue).sum();
    }

    @ModelAttribute("wishlistCount")
    public int getWishlistCount(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return 0;
        try {
            return wishlistService.getWishlistProducts(userId).size();
        } catch (Exception e) {
            return 0;
        }
    }

    @ModelAttribute("userName")
    public String getUserName(HttpSession session) {
        return (String) session.getAttribute("userName");
    }
}
