package com.example.ComputerStore.controller;

import com.example.ComputerStore.model.*;
import com.example.ComputerStore.service.OrderService;
import com.example.ComputerStore.service.ProductService;
import com.example.ComputerStore.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final ProductService productService;
    private final UserService userService;
    private final OrderService orderService;

    public AdminController(ProductService productService,
                           UserService userService,
                           OrderService orderService) {
        this.productService = productService;
        this.userService = userService;
        this.orderService = orderService;
    }

    // ---- Dashboard ----
    @GetMapping
    public String adminDashboard(Model model) {
        model.addAttribute("totalProducts", productService.getAllProducts().size());
        model.addAttribute("totalUsers", userService.getAllUsers().size());
        model.addAttribute("totalOrders", orderService.getAllOrders().size());
        return "admin/dashboard";
    }

    // ---- Product Management ----
    @GetMapping("/products")
    public String listProducts(@RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size,
                                @RequestParam(defaultValue = "name") String sortBy,
                                @RequestParam(defaultValue = "asc") String direction,
                                Model model) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        PageRequest pageable = PageRequest.of(page, size, sort);
        Page<Product> productPage = productService.getAllProducts(pageable);

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);
        return "admin/products";
    }

    @GetMapping("/products/add")
    public String addProductForm(@RequestParam(defaultValue = "processor") String type, Model model) {
        model.addAttribute("productType", type);
        switch (type.toLowerCase()) {
            case "gpu" -> model.addAttribute("product", new GraphicsCard());
            case "motherboard" -> model.addAttribute("product", new Motherboard());
            case "case" -> model.addAttribute("product", new Case());
            default -> model.addAttribute("product", new Processor());
        }
        return "admin/product-form";
    }

    @PostMapping("/products/add/processor")
    public String addProcessor(@Valid @ModelAttribute("product") Processor product,
                                BindingResult result,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Invalid product data. Check fields.");
            return "redirect:/admin/products/add?type=processor";
        }
        productService.saveProduct(product);
        log.info("Admin added processor: {}", product.getName());
        redirectAttributes.addFlashAttribute("success", "Processor added successfully!");
        return "redirect:/admin/products";
    }

    @PostMapping("/products/add/gpu")
    public String addGraphicsCard(@Valid @ModelAttribute("product") GraphicsCard product,
                                   BindingResult result,
                                   RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Invalid product data. Check fields.");
            return "redirect:/admin/products/add?type=gpu";
        }
        productService.saveProduct(product);
        log.info("Admin added GPU: {}", product.getName());
        redirectAttributes.addFlashAttribute("success", "Graphics Card added successfully!");
        return "redirect:/admin/products";
    }

    @PostMapping("/products/add/motherboard")
    public String addMotherboard(@Valid @ModelAttribute("product") Motherboard product,
                                  BindingResult result,
                                  RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Invalid product data. Check fields.");
            return "redirect:/admin/products/add?type=motherboard";
        }
        productService.saveProduct(product);
        log.info("Admin added motherboard: {}", product.getName());
        redirectAttributes.addFlashAttribute("success", "Motherboard added successfully!");
        return "redirect:/admin/products";
    }

    @PostMapping("/products/add/case")
    public String addCase(@Valid @ModelAttribute("product") Case product,
                           BindingResult result,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Invalid product data. Check fields.");
            return "redirect:/admin/products/add?type=case";
        }
        productService.saveProduct(product);
        log.info("Admin added case: {}", product.getName());
        redirectAttributes.addFlashAttribute("success", "Case added successfully!");
        return "redirect:/admin/products";
    }

    @GetMapping("/products/edit/{id}")
    public String editProductForm(@PathVariable Integer id, Model model) {
        Product product = productService.getProductDetails(id);
        model.addAttribute("product", product);
        String type = "processor";
        if (product instanceof GraphicsCard) type = "gpu";
        else if (product instanceof Motherboard) type = "motherboard";
        else if (product instanceof Case) type = "case";
        
        model.addAttribute("productType", type);
        model.addAttribute("isEdit", true);
        return "admin/product-form";
    }

    @PostMapping("/products/edit/processor/{id}")
    public String editProcessor(@PathVariable Integer id, @Valid @ModelAttribute("product") Processor product, BindingResult result, RedirectAttributes ra) {
        if (result.hasErrors()) return "admin/product-form";
        productService.updateProduct(id, product);
        ra.addFlashAttribute("success", "Processor updated!");
        return "redirect:/admin/products";
    }

    @PostMapping("/products/edit/gpu/{id}")
    public String editGpu(@PathVariable Integer id, @Valid @ModelAttribute("product") GraphicsCard product, BindingResult result, RedirectAttributes ra) {
        if (result.hasErrors()) return "admin/product-form";
        productService.updateProduct(id, product);
        ra.addFlashAttribute("success", "Graphics Card updated!");
        return "redirect:/admin/products";
    }

    @PostMapping("/products/edit/motherboard/{id}")
    public String editMotherboard(@PathVariable Integer id, @Valid @ModelAttribute("product") Motherboard product, BindingResult result, RedirectAttributes ra) {
        if (result.hasErrors()) return "admin/product-form";
        productService.updateProduct(id, product);
        ra.addFlashAttribute("success", "Motherboard updated!");
        return "redirect:/admin/products";
    }

    @PostMapping("/products/edit/case/{id}")
    public String editCase(@PathVariable Integer id, @Valid @ModelAttribute("product") Case product, BindingResult result, RedirectAttributes ra) {
        if (result.hasErrors()) return "admin/product-form";
        productService.updateProduct(id, product);
        ra.addFlashAttribute("success", "Case updated!");
        return "redirect:/admin/products";
    }

    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        productService.deleteProduct(id);
        log.info("Admin deleted product: id={}", id);
        redirectAttributes.addFlashAttribute("success", "Product deleted successfully!");
        return "redirect:/admin/products";
    }

    // ---- User Management ----
    @GetMapping("/users")
    public String listUsers(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size,
                             @RequestParam(defaultValue = "username") String sortBy,
                             @RequestParam(defaultValue = "asc") String direction,
                             Model model) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        PageRequest pageable = PageRequest.of(page, size, sort);
        Page<User> userPage = userService.getAllUsers(pageable);

        model.addAttribute("users", userPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);
        return "admin/users";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        User user = userService.findUserById(id);
        if ("admin".equals(user.getUsername())) {
            redirectAttributes.addFlashAttribute("error", "The main admin account cannot be deleted!");
            return "redirect:/admin/users";
        }
        userService.deleteUser(id);
        log.info("Admin deleted user: id={}", id);
        redirectAttributes.addFlashAttribute("success", "User deleted successfully!");
        return "redirect:/admin/users";
    }

    // ---- Order Management ----
    @GetMapping("/orders")
    public String listOrders(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(defaultValue = "orderDate") String sortBy,
                              @RequestParam(defaultValue = "desc") String direction,
                              Model model) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        PageRequest pageable = PageRequest.of(page, size, sort);
        Page<Order> orderPage = orderService.getAllOrders(pageable);

        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);
        return "admin/orders";
    }

    @PostMapping("/orders/delete/{id}")
    public String deleteOrder(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        orderService.deleteOrder(id);
        log.info("Admin deleted order: id={}", id);
        redirectAttributes.addFlashAttribute("success", "Order deleted successfully!");
        return "redirect:/admin/orders";
    }
}
