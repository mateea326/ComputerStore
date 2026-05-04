package com.example.ComputerStore.controller;

import com.example.ComputerStore.model.User;
import com.example.ComputerStore.model.Product;
import com.example.ComputerStore.model.Order;
import com.example.ComputerStore.service.UserService;
import com.example.ComputerStore.service.ProductService;
import com.example.ComputerStore.service.CartService;
import com.example.ComputerStore.service.OrderService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
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

    public ViewController(UserService userService,
                          ProductService productService,
                          CartService cartService,
                          OrderService orderService) {
        this.userService = userService;
        this.productService = productService;
        this.cartService = cartService;
        this.orderService = orderService;
    }

    // homepage - redirect catre login
    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        try {
            User user = userService.login(username, password);
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("userName", user.getFirstName());
            return "redirect:/products";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/login";
        }
    }

    // pagina de inregistrare
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute User user,
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
            userService.registerNewUser(user);
            redirectAttributes.addFlashAttribute("success", "Account created! Please login.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/products")
    public String productsPage(@RequestParam(required = false) String type,
                               Model model,
                               HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        List<? extends Product> products;
        if (type != null && !type.isEmpty()) {
            products = productService.filterProductsByType(type);
        } else {
            products = productService.getAllProducts();
        }

        model.addAttribute("products", products);
        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("selectedType", type);
        return "products";
    }

    // add to cart
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

    // view cart
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

    // remove from cart
    @PostMapping("/cart/remove/{productId}")
    public String removeFromCart(@PathVariable Integer productId,
                                 HttpSession session) {
        cartService.removeProductFromCart(session, productId);
        return "redirect:/cart";
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
    public String processCheckout(HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            cartService.checkout(session, userId);
            redirectAttributes.addFlashAttribute("success", "Order placed successfully!");
            return "redirect:/products";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/checkout";
        }
    }

    // istoria comenzilor
    @GetMapping("/order-history")
    public String orderHistory(Model model, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            List<Order> orders = orderService.getOrderHistory(userId);

            Map<Integer, String> productNames = new HashMap<>();
            Map<Integer, Float> productPrices = new HashMap<>();

            for (Order order : orders) {
                Map<Integer, Integer> quantities = order.getProductQuantities();
                if (quantities != null && !quantities.isEmpty()) {
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
        } catch (Exception e) {
            model.addAttribute("orders", new ArrayList<>());
            model.addAttribute("productNames", new HashMap<>());
            model.addAttribute("productPrices", new HashMap<>());
            model.addAttribute("userName", session.getAttribute("userName"));
        }

        return "order-history";
    }

    @GetMapping("/account-settings")
    public String accountSettingsPage(Model model, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            User user = userService.findUserById(userId);
            model.addAttribute("user", user);
            model.addAttribute("userName", session.getAttribute("userName"));
        } catch (Exception e) {
            model.addAttribute("error", "Could not load account details");
        }

        return "account-settings";
    }

    // editarea contului
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
            User updatedDetails = new User();
            updatedDetails.setFirstName(firstName);
            updatedDetails.setLastName(lastName);
            updatedDetails.setEmail(email);
            updatedDetails.setPhoneNumber(phoneNumber);
            updatedDetails.setAddress(address);

            userService.updateUser(userId, updatedDetails);

            // updatam sesiunea cu noul nume
            session.setAttribute("userName", firstName);

            redirectAttributes.addFlashAttribute("success", "Account updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/account-settings";
    }

    // stergerea contului
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

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}