package com.example.ComputerStore.controller;

import com.example.ComputerStore.model.Customer;
import com.example.ComputerStore.model.Product;
import com.example.ComputerStore.model.Order;
import com.example.ComputerStore.service.CustomerService;
import com.example.ComputerStore.service.ProductService;
import com.example.ComputerStore.service.SessionCartService;
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

    private final CustomerService customerService;
    private final ProductService productService;
    private final SessionCartService cartService;
    private final OrderService orderService;

    public ViewController(CustomerService customerService,
                          ProductService productService,
                          SessionCartService cartService,
                          OrderService orderService) {
        this.customerService = customerService;
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
            Customer customer = customerService.login(username, password);
            session.setAttribute("customerId", customer.getCustomerId());
            session.setAttribute("customerName", customer.getFirstName());
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
    public String register(@Valid @ModelAttribute Customer customer,
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
            customerService.registerNewCustomer(customer);
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
        Integer customerId = (Integer) session.getAttribute("customerId");
        if (customerId == null) {
            return "redirect:/login";
        }

        List<? extends Product> products;
        if (type != null && !type.isEmpty()) {
            products = productService.filterProductsByType(type);
        } else {
            products = productService.getAllProducts();
        }

        model.addAttribute("products", products);
        model.addAttribute("customerName", session.getAttribute("customerName"));
        model.addAttribute("selectedType", type);
        return "products";
    }

    // add to cart
    @PostMapping("/cart/add/{productId}")
    public String addToCart(@PathVariable Integer productId,
                            @RequestParam(required = false) String returnType,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        Integer customerId = (Integer) session.getAttribute("customerId");
        if (customerId == null) {
            return "redirect:/login";
        }

        cartService.addProductToCart(session, productId);
        redirectAttributes.addFlashAttribute("success", "Product added to cart!");

        if (returnType != null && !returnType.isEmpty()) {
            return "redirect:/products?type=" + returnType.replace(" ", "%20");
        }
        return "redirect:/products";
    }

    // view cart
    @GetMapping("/cart")
    public String cartPage(Model model, HttpSession session) {
        Integer customerId = (Integer) session.getAttribute("customerId");
        if (customerId == null) {
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
        model.addAttribute("customerName", session.getAttribute("customerName"));
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
        Integer customerId = (Integer) session.getAttribute("customerId");
        if (customerId == null) {
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
        model.addAttribute("customerName", session.getAttribute("customerName"));
        return "checkout";
    }

    @PostMapping("/checkout")
    public String processCheckout(@RequestParam String cardNumber,
                                  @RequestParam String cardName,
                                  @RequestParam String expiryDate,
                                  @RequestParam String cvv,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        Integer customerId = (Integer) session.getAttribute("customerId");
        if (customerId == null) {
            return "redirect:/login";
        }

        try {
            cartService.checkout(session, customerId, cardNumber, cardName, expiryDate, cvv);
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
        Integer customerId = (Integer) session.getAttribute("customerId");
        if (customerId == null) {
            return "redirect:/login";
        }

        try {
            List<Order> orders = orderService.getOrderHistory(customerId);

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
            model.addAttribute("customerName", session.getAttribute("customerName"));
        } catch (Exception e) {
            model.addAttribute("orders", new ArrayList<>());
            model.addAttribute("productNames", new HashMap<>());
            model.addAttribute("productPrices", new HashMap<>());
            model.addAttribute("customerName", session.getAttribute("customerName"));
        }

        return "order-history";
    }

    @GetMapping("/account-settings")
    public String accountSettingsPage(Model model, HttpSession session) {
        Integer customerId = (Integer) session.getAttribute("customerId");
        if (customerId == null) {
            return "redirect:/login";
        }

        try {
            Customer customer = customerService.findCustomerById(customerId);
            model.addAttribute("customer", customer);
            model.addAttribute("customerName", session.getAttribute("customerName"));
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
        Integer customerId = (Integer) session.getAttribute("customerId");
        if (customerId == null) {
            return "redirect:/login";
        }

        try {
            Customer updatedDetails = new Customer();
            updatedDetails.setFirstName(firstName);
            updatedDetails.setLastName(lastName);
            updatedDetails.setEmail(email);
            updatedDetails.setPhoneNumber(phoneNumber);
            updatedDetails.setAddress(address);

            customerService.updateCustomer(customerId, updatedDetails);

            // updatam sesiunea cu noul nume
            session.setAttribute("customerName", firstName);

            redirectAttributes.addFlashAttribute("success", "Account updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/account-settings";
    }

    // stergerea contului
    @PostMapping("/account-settings/delete")
    public String deleteAccount(HttpSession session, RedirectAttributes redirectAttributes) {
        Integer customerId = (Integer) session.getAttribute("customerId");
        if (customerId == null) {
            return "redirect:/login";
        }

        try {
            customerService.deleteCustomer(customerId);
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