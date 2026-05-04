package com.example.ComputerStore.controller;

import com.example.ComputerStore.dto.UserRegistrationDTO; // Import nou
import com.example.ComputerStore.dto.UserResponseDTO;     // Import nou
import com.example.ComputerStore.model.User;
import com.example.ComputerStore.model.Product;
import com.example.ComputerStore.model.Order;
import com.example.ComputerStore.service.UserService;
import com.example.ComputerStore.service.ProductService;
import com.example.ComputerStore.service.CartService;
import com.example.ComputerStore.service.OrderService;
import com.example.ComputerStore.service.WishlistService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@Controller
public class ViewController {

    private final UserService userService;
    private final ProductService productService;
    private final CartService cartService;
    private final OrderService orderService;
    private final WishlistService wishlistService;

    public ViewController(UserService userService,
                          ProductService productService,
                          CartService cartService,
                          OrderService orderService,
                          WishlistService wishlistService) {
        this.userService = userService;
        this.productService = productService;
        this.cartService = cartService;
        this.orderService = orderService;
        this.wishlistService = wishlistService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }


    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") UserRegistrationDTO userDto, // Folosește DTO
                           BindingResult result,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            String errorMsg = result.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .findFirst()
                    .orElse("Invalid input");
            redirectAttributes.addFlashAttribute("error", errorMsg);
            return "redirect:/register";
        }

        try {
            userService.registerNewUser(userDto); // Trimite DTO-ul
            redirectAttributes.addFlashAttribute("success", "Account created! Please login.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/products")
    public String productsPage(@RequestParam(required = false) String type,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               @RequestParam(defaultValue = "name") String sortBy,
                               @RequestParam(defaultValue = "asc") String direction,
                               Model model,
                               HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        PageRequest pageable = PageRequest.of(page, size, sort);

        if (type != null && !type.isEmpty()) {
            // Paginarea pentru filtrare ar necesita metode noi in repository, 
            // pentru simplitate paginam doar lista totala sau lasam filtrarea asa
            List<? extends Product> products = productService.filterProductsByType(type);
            model.addAttribute("products", products);
            model.addAttribute("isPaginated", false);
        } else {
            Page<Product> productPage = productService.getAllProducts(pageable);
            model.addAttribute("products", productPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", productPage.getTotalPages());
            model.addAttribute("totalItems", productPage.getTotalElements());
            model.addAttribute("isPaginated", true);
        }

        model.addAttribute("wishlistProductIds", wishlistService.getWishlistProductIds(userId));
        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("selectedType", type);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);
        model.addAttribute("size", size);
        return "products";
    }

    @PostMapping("/cart/add/{productId}")
    public String addToCart(@PathVariable Integer productId,
                            @RequestParam(required = false) String returnType,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        cartService.addProductToCart(session, productId);
        redirectAttributes.addFlashAttribute("success", "Product added to cart!");

        if (returnType != null && !returnType.isEmpty()) {
            List<String> validTypes = List.of("processors", "motherboards", "graphics cards", "gpus", "cases");
            if (validTypes.contains(returnType.toLowerCase().trim())) {
                return "redirect:/products?type=" + returnType.replace(" ", "%20");
            }
        }
        return "redirect:/products";
    }

    @GetMapping("/cart")
    public String cartPage(Model model, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        Map<Integer, Integer> cart = cartService.getCart(session);
        List<Product> cartProducts = cart.keySet().stream()
                .map(productService::getProductDetails)
                .toList();

        double total = cartProducts.stream()
                .mapToDouble(p -> p.getPrice() * cart.get(p.getProductId()))
                .sum();

        model.addAttribute("cartProducts", cartProducts);
        model.addAttribute("cart", cart);
        model.addAttribute("total", total);
        model.addAttribute("userName", session.getAttribute("userName"));
        return "cart";
    }

    @PostMapping("/cart/remove/{productId}")
    public String removeFromCart(@PathVariable Integer productId,
                                 HttpSession session) {
        cartService.removeEntireProductFromCart(session, productId);
        return "redirect:/cart";
    }

    @PostMapping("/cart/increase/{productId}")
    public String increaseQuantity(@PathVariable Integer productId,
                                   HttpSession session,
                                   @RequestParam(required = false, defaultValue = "cart") String redirect) {
        cartService.addProductToCart(session, productId);
        return "redirect:/" + redirect;
    }

    @PostMapping("/cart/decrease/{productId}")
    public String decreaseQuantity(@PathVariable Integer productId,
                                   HttpSession session,
                                   @RequestParam(required = false, defaultValue = "cart") String redirect) {
        cartService.removeProductFromCart(session, productId);
        return "redirect:/" + redirect;
    }

    @GetMapping("/checkout")
    public String checkoutPage(Model model, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        Map<Integer, Integer> cart = cartService.getCart(session);
        if (cart.isEmpty()) {
            return "redirect:/cart";
        }

        List<Product> cartProducts = cart.keySet().stream()
                .map(productService::getProductDetails)
                .toList();

        double total = cartProducts.stream()
                .mapToDouble(p -> p.getPrice() * cart.get(p.getProductId()))
                .sum();

        model.addAttribute("cartProducts", cartProducts);
        model.addAttribute("cart", cart);
        model.addAttribute("total", total);
        model.addAttribute("userName", session.getAttribute("userName"));
        return "checkout";
    }

    @PostMapping("/checkout")
    public String processCheckout(@RequestParam(required = false) String cardNumber,
                                   @RequestParam(required = false) String cardName,
                                   @RequestParam(required = false) String expiryDate,
                                   @RequestParam(required = false) String cvv,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        // Validare simpla a datelor cardului (nu se salveaza)
        if (cardNumber == null || cardNumber.replaceAll("\\s", "").length() < 13) {
            redirectAttributes.addFlashAttribute("error", "Please enter a valid card number");
            return "redirect:/checkout";
        }
        if (cardName == null || cardName.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please enter the cardholder name");
            return "redirect:/checkout";
        }
        if (expiryDate == null || !expiryDate.matches("\\d{2}/\\d{2}")) {
            redirectAttributes.addFlashAttribute("error", "Please enter a valid expiry date (MM/YY)");
            return "redirect:/checkout";
        }
        if (cvv == null || !cvv.matches("\\d{3,4}")) {
            redirectAttributes.addFlashAttribute("error", "Please enter a valid CVV");
            return "redirect:/checkout";
        }

        try {
            cartService.checkout(session, userId);
            redirectAttributes.addFlashAttribute("success", "Payment processed and order placed successfully!");
            return "redirect:/products";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/checkout";
        }
    }

    @GetMapping("/order-history")
    public String orderHistory(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "5") int size,
                               @RequestParam(defaultValue = "orderDate") String sortBy,
                               @RequestParam(defaultValue = "desc") String direction,
                               Model model,
                               HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        PageRequest pageable = PageRequest.of(page, size, sort);

        try {
            Page<Order> orderPage = orderService.getOrderHistory(userId, pageable);
            List<Order> orders = orderPage.getContent();
            Map<Integer, String> productNames = new HashMap<>();
            Map<Integer, Float> productPrices = new HashMap<>();

            for (Order order : orders) {
                Map<Integer, Integer> quantities = order.getProductQuantities();
                if (quantities != null) {
                    for (Integer productId : quantities.keySet()) {
                        try {
                            Product product = productService.getProductDetails(productId);
                            productNames.put(productId, product.getName());
                            productPrices.put(productId, product.getPrice());
                        } catch (Exception e) {
                            productNames.put(productId, "Product #" + productId);
                            productPrices.put(productId, 0.0f);
                        }
                    }
                }
            }

            model.addAttribute("orders", orders);
            model.addAttribute("productNames", productNames);
            model.addAttribute("productPrices", productPrices);
            model.addAttribute("userName", session.getAttribute("userName"));
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", orderPage.getTotalPages());
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("direction", direction);
            model.addAttribute("size", size);
            return "order-history";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading order history: " + e.getMessage());
            return "order-history";
        }
    }

    @GetMapping("/account-settings")
    public String accountSettingsPage(Model model, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            User user = userService.findUserById(userId);
            model.addAttribute("user", user); // Aici trimitem încă entitatea pentru a popula formularul
            model.addAttribute("userName", session.getAttribute("userName"));
        } catch (Exception e) {
            model.addAttribute("error", "Could not load account details");
        }

        return "account-settings";
    }

    @PostMapping("/account-settings/update")
    public String updateAccount(@RequestParam String firstName,
                                @RequestParam String lastName,
                                @RequestParam String email,
                                @RequestParam String phoneNumber,
                                @RequestParam String address,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            // Mapăm datele primite într-un DTO pentru a apela serviciul
            UserRegistrationDTO updateDto = new UserRegistrationDTO();
            updateDto.setFirstName(firstName);
            updateDto.setLastName(lastName);
            updateDto.setEmail(email);
            updateDto.setPhoneNumber(phoneNumber);
            updateDto.setAddress(address);

            // UserService.updateUser primește acum DTO
            userService.updateUser(userId, updateDto);

            session.setAttribute("userName", firstName);
            redirectAttributes.addFlashAttribute("success", "Account updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/account-settings";
    }

    @PostMapping("/account-settings/delete")
    public String deleteAccount(HttpSession session, RedirectAttributes redirectAttributes) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            userService.deleteUser(userId);
            session.invalidate();
            redirectAttributes.addFlashAttribute("success", "Account deleted successfully");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Could not delete account: " + e.getMessage());
            return "redirect:/account-settings";
        }
    }

    // --- Wishlist ---
    @PostMapping("/wishlist/toggle/{productId}")
    public String toggleWishlist(@PathVariable Integer productId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            if (wishlistService.isProductInWishlist(userId, productId)) {
                wishlistService.removeProductFromWishlist(userId, productId);
                redirectAttributes.addFlashAttribute("success", "Removed from wishlist");
            } else {
                wishlistService.addProductToWishlist(userId, productId);
                redirectAttributes.addFlashAttribute("success", "Added to wishlist!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/products";
    }

    @GetMapping("/wishlist")
    public String wishlistPage(Model model, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        model.addAttribute("wishlistProducts", wishlistService.getWishlistProducts(userId));
        model.addAttribute("userName", session.getAttribute("userName"));
        return "wishlist";
    }

}
