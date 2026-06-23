package com.example.ComputerStore.controller;

import com.example.ComputerStore.exception.ResourceNotFoundException;
import com.example.ComputerStore.model.*;
import com.example.ComputerStore.repo.ProductRepository;
import com.example.ComputerStore.service.OrderService;
import com.example.ComputerStore.service.ProductService;
import com.example.ComputerStore.service.UserService;
import com.example.ComputerStore.repo.CartRepository;
import com.example.ComputerStore.repo.WishlistRepository;
import com.example.ComputerStore.repo.OrderRepository;
import org.hibernate.Hibernate;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final ProductService productService;
    private final UserService userService;
    private final OrderService orderService;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final WishlistRepository wishlistRepository;
    private final OrderRepository orderRepository;

    @Value("${upload.path:uploads/products}")
    private String uploadPath;

    public AdminController(ProductService productService,
                           UserService userService,
                           OrderService orderService,
                           ProductRepository productRepository,
                           CartRepository cartRepository,
                           WishlistRepository wishlistRepository,
                           OrderRepository orderRepository) {
        this.productService = productService;
        this.userService = userService;
        this.orderService = orderService;
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.wishlistRepository = wishlistRepository;
        this.orderRepository = orderRepository;
    }

    /**
     * Saves uploaded image to the external uploads folder and returns the URL path.
     * Returns null if no file was uploaded.
     */
    private String handleImageUpload(MultipartFile imageFile) throws IOException {
        if (imageFile == null || imageFile.isEmpty()) {
            return null;
        }
        String originalFilename = imageFile.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
        String filename = UUID.randomUUID() + extension;

        Path uploadDir = Paths.get(uploadPath);
        Files.createDirectories(uploadDir);
        Path filePath = uploadDir.resolve(filename);
        Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        log.info("Image uploaded: {}", filePath);
        // URL: Spring serves "uploads/" folder as static, so /products/<filename> maps to uploads/products/<filename>
        return "/products/" + filename;
    }

    // ---- Dashboard ----
    @GetMapping
    public String adminDashboard(Model model) {
        int totalProducts = 0;
        int totalUsers = 0;
        int totalOrders = 0;
        
        try { totalProducts = productService.getAllProducts().size(); } catch (Exception e) { log.error("Failed to get total products", e); }
        try { totalUsers = userService.getAllUsers().size(); } catch (Exception e) { log.error("Failed to get total users", e); }
        try { totalOrders = orderService.getAllOrders().size(); } catch (Exception e) { log.error("Failed to get total orders", e); }
        
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalOrders", totalOrders);
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
        model.addAttribute("isEdit", false);
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
                                BindingResult result, Model model,
                                @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("productType", "processor");
            model.addAttribute("isEdit", false);
            return "admin/product-form";
        }
        try {
            String imageUrl = handleImageUpload(imageFile);
            if (imageUrl != null) product.setImageUrl(imageUrl);
            productService.saveProduct(product);
            log.info("Admin added processor: {}", product.getName());
            redirectAttributes.addFlashAttribute("success", "Processor added successfully!");
        } catch (Exception e) {
            model.addAttribute("productType", "processor");
            model.addAttribute("isEdit", false);
            model.addAttribute("error", "Failed to save: " + e.getMessage());
            return "admin/product-form";
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/products/add/gpu")
    public String addGraphicsCard(@Valid @ModelAttribute("product") GraphicsCard product,
                                   BindingResult result, Model model,
                                   @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                   RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("productType", "gpu");
            model.addAttribute("isEdit", false);
            return "admin/product-form";
        }
        try {
            String imageUrl = handleImageUpload(imageFile);
            if (imageUrl != null) product.setImageUrl(imageUrl);
            productService.saveProduct(product);
            log.info("Admin added GPU: {}", product.getName());
            redirectAttributes.addFlashAttribute("success", "Graphics Card added successfully!");
        } catch (Exception e) {
            model.addAttribute("productType", "gpu");
            model.addAttribute("isEdit", false);
            model.addAttribute("error", "Failed to save: " + e.getMessage());
            return "admin/product-form";
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/products/add/motherboard")
    public String addMotherboard(@Valid @ModelAttribute("product") Motherboard product,
                                  BindingResult result, Model model,
                                  @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                  RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("productType", "motherboard");
            model.addAttribute("isEdit", false);
            return "admin/product-form";
        }
        try {
            String imageUrl = handleImageUpload(imageFile);
            if (imageUrl != null) product.setImageUrl(imageUrl);
            productService.saveProduct(product);
            log.info("Admin added motherboard: {}", product.getName());
            redirectAttributes.addFlashAttribute("success", "Motherboard added successfully!");
        } catch (Exception e) {
            model.addAttribute("productType", "motherboard");
            model.addAttribute("isEdit", false);
            model.addAttribute("error", "Failed to save: " + e.getMessage());
            return "admin/product-form";
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/products/add/case")
    public String addCase(@Valid @ModelAttribute("product") Case product,
                           BindingResult result, Model model,
                           @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("productType", "case");
            model.addAttribute("isEdit", false);
            return "admin/product-form";
        }
        try {
            String imageUrl = handleImageUpload(imageFile);
            if (imageUrl != null) product.setImageUrl(imageUrl);
            productService.saveProduct(product);
            log.info("Admin added case: {}", product.getName());
            redirectAttributes.addFlashAttribute("success", "Case added successfully!");
        } catch (Exception e) {
            model.addAttribute("productType", "case");
            model.addAttribute("isEdit", false);
            model.addAttribute("error", "Failed to save: " + e.getMessage());
            return "admin/product-form";
        }
        return "redirect:/admin/products";
    }

    @GetMapping("/products/edit/{id}")
    public String editProductForm(@PathVariable Integer id, Model model) {
        // Hibernate.unproxy() forteaza initializarea si returneaza subclasa reala (Processor, GraphicsCard etc.)
        // findById singur poate returna un HibernateProxy de tip Product generic cu JOINED inheritance
        Product product = (Product) Hibernate.unproxy(
                productRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id))
        );

        model.addAttribute("product", product);
        String className = product.getClass().getName();
        String type = "processor";
        if (className.contains("GraphicsCard")) type = "gpu";
        else if (className.contains("Motherboard")) type = "motherboard";
        else if (className.contains("Case")) type = "case";
        
        model.addAttribute("productType", type);
        model.addAttribute("isEdit", true);
        return "admin/product-form";
    }

    @PostMapping("/products/edit/processor/{id}")
    public String editProcessor(@PathVariable Integer id, @Valid @ModelAttribute("product") Processor product,
                                 BindingResult result, Model model,
                                 @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                 RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("productType", "processor");
            model.addAttribute("isEdit", true);
            return "admin/product-form";
        }
        try {
            String imageUrl = handleImageUpload(imageFile);
            if (imageUrl != null) product.setImageUrl(imageUrl);
            productService.updateProduct(id, product);
            ra.addFlashAttribute("success", "Processor updated!");
        } catch (Exception e) {
            model.addAttribute("productType", "processor");
            model.addAttribute("isEdit", true);
            model.addAttribute("error", "Failed to update: " + e.getMessage());
            return "admin/product-form";
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/products/edit/gpu/{id}")
    public String editGpu(@PathVariable Integer id, @Valid @ModelAttribute("product") GraphicsCard product,
                           BindingResult result, Model model,
                           @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                           RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("productType", "gpu");
            model.addAttribute("isEdit", true);
            return "admin/product-form";
        }
        try {
            String imageUrl = handleImageUpload(imageFile);
            if (imageUrl != null) product.setImageUrl(imageUrl);
            productService.updateProduct(id, product);
            ra.addFlashAttribute("success", "Graphics Card updated!");
        } catch (Exception e) {
            model.addAttribute("productType", "gpu");
            model.addAttribute("isEdit", true);
            model.addAttribute("error", "Failed to update: " + e.getMessage());
            return "admin/product-form";
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/products/edit/motherboard/{id}")
    public String editMotherboard(@PathVariable Integer id, @Valid @ModelAttribute("product") Motherboard product,
                                   BindingResult result, Model model,
                                   @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                   RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("productType", "motherboard");
            model.addAttribute("isEdit", true);
            return "admin/product-form";
        }
        try {
            String imageUrl = handleImageUpload(imageFile);
            if (imageUrl != null) product.setImageUrl(imageUrl);
            productService.updateProduct(id, product);
            ra.addFlashAttribute("success", "Motherboard updated!");
        } catch (Exception e) {
            model.addAttribute("productType", "motherboard");
            model.addAttribute("isEdit", true);
            model.addAttribute("error", "Failed to update: " + e.getMessage());
            return "admin/product-form";
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/products/edit/case/{id}")
    public String editCase(@PathVariable Integer id, @Valid @ModelAttribute("product") Case product,
                            BindingResult result, Model model,
                            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                            RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("productType", "case");
            model.addAttribute("isEdit", true);
            return "admin/product-form";
        }
        try {
            String imageUrl = handleImageUpload(imageFile);
            if (imageUrl != null) product.setImageUrl(imageUrl);
            productService.updateProduct(id, product);
            ra.addFlashAttribute("success", "Case updated!");
        } catch (Exception e) {
            model.addAttribute("productType", "case");
            model.addAttribute("isEdit", true);
            model.addAttribute("error", "Failed to update: " + e.getMessage());
            return "admin/product-form";
        }
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
        model.addAttribute("size", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);
        return "admin/users";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        User user = userService.findUserById(id);
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            redirectAttributes.addFlashAttribute("error", "Admin accounts cannot be deleted!");
            return "redirect:/admin/users";
        }
        
        try {
            // Stergem dependentele locale inainte sa cerem stergerea user-ului pentru a evita Foreign Key Constraints
            userService.deleteUserDependencies(id);
        } catch (Exception e) {
            log.error("Error deleting user dependencies locally: {}", e.getMessage());
        }

        userService.deleteUser(id);
        log.info("Admin deleted user: id={}", id);
        redirectAttributes.addFlashAttribute("success", "User deleted successfully!");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/change-role/{id}")
    public String changeUserRole(@PathVariable Integer id,
                                 @RequestParam("newRole") String newRole,
                                 RedirectAttributes redirectAttributes) {
        try {
            userService.changeUserRole(id, newRole);
            log.info("Admin changed role for user id={} to {}", id, newRole);
            redirectAttributes.addFlashAttribute("success", "Role updated successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
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
