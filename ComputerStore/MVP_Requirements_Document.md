# Computer Store - MVP Requirements Document

## Project Overview
**ComputerStore** - An e-commerce platform for computer hardware components.

**Technology Stack:**
- **Backend:** Java 21, Spring Boot 3.2.0
- **Security:** Spring Security 6.2 (RBAC, CSRF Protection, BCrypt)
- **Database:** PostgreSQL (Persistence), H2 (Testing)
- **Frontend:** Thymeleaf
- **Logging:** Spring AOP (Aspect-Oriented Programming)
- **Testing:** JUnit 5, Mockito, Jacoco (Code Coverage)
- **API Documentation:** SpringDoc OpenAPI (Swagger)

---

## Technical Architecture

### Database Schema (11 Entities)

**Entities:**
1. **users** - Secure user accounts (user_id, first_name, last_name, phone_number, address, email, username, password, role)
2. **products** - Base product catalog (product_id, name, price)
3. **processors** - CPU specifications (product_id FK, core_count, core_clock, socket)
4. **graphicscards** - GPU specifications (product_id FK, memory_size, core_clock, memory_clock)
5. **motherboards** - Motherboard specifications (product_id FK, slots, cpu_socket, chipset)
6. **cases** - Chassis specifications (product_id FK, vents, type, format)
7. **orders** - Customer orders (order_id, user_id FK, order_date, total_price)
8. **order_items** - Order line items (order_item_id, order_id FK, product_id FK, quantity, price_at_purchase)
9. **carts** - Persistent shopping carts (cart_id, user_id FK)
10. **cart_items** - Items within a cart (cart_item_id, cart_id FK, product_id FK, quantity)
11. **wishlists** - User wishlists (wishlist_id, user_id FK)

### Key Relationships
1. **User → Orders** (One-to-Many) - A user can place multiple orders.
2. **User → Cart** (One-to-One) - Each user has one shopping cart.
3. **User → Wishlist** (One-to-One) - Each user has one wishlist.
4. **Order → OrderItems** (One-to-Many) - One order contains multiple products.
5. **Cart → CartItems** (One-to-Many) - A cart manages multiple items.
6. **Product → Subclasses** (Joined Inheritance) - Specialized components inherit from the Product base.

---

## Business Requirements

### 1. User Account Management
**Description:** Users must be able to create accounts, login and manage their personal profile.

**Acceptance Criteria:**
- Users can register with valid email, unique username, and strong password.
- Secure login using Spring Security and BCrypt password hashing.
- Users can update their profile information (name, address, phone, email).
- Account deletion with full data cleanup (cascade deletion).
- Role-based access (USER role by default).

---

### 2. Product Catalog Management
**Description:** The system must display a comprehensive catalog of computer components with detailed specifications.

**Acceptance Criteria:**
- Products are categorized by type (Processors, Graphics Cards, Motherboards, Cases)
- Each product displays: name, price, and technical specifications
- Users can view all products or filter by category
- Product information is stored persistently in database

---

### 3. Shopping Cart and Wishlist
**Description:** Users can manage a persistent shopping cart and a wishlist for future purchases.

**Acceptance Criteria:**
- Authenticated users have a persistent cart stored in the database.
- Products can be added to the cart or wishlist.
- Cart displays product details, quantities, and real-time total price.
- Users can adjust quantities or remove items from the cart.
- Wishlist items can be moved directly to the cart.

---

### 4. Order Processing
**Description:** A streamlined checkout process that creates persistent orders and manages inventory state.

**Acceptance Criteria:**
- Secure checkout flow restricted to authenticated users.
- Order creation includes timestamp, user reference, and item snapshots.
- Cart is automatically cleared upon successful order placement.
- Total price calculation includes all items and potential discounts.

---

### 5. Order History
**Description:** Users must be able to view their past orders.

**Acceptance Criteria:**
- Users can view list of all their previous orders
- Each order shows: order ID, date, total price, and items purchased
- Orders are sorted by date (newest first)

---

### 6. Product Filtering and Search
**Description:** Users must be able to filter products by component type to find specific items quickly.

**Acceptance Criteria:**
- Filter buttons for: All Products, Processors, Motherboards, Graphics Cards, Cases
- Filtered results display only products of selected type
- Filter selection persists when adding items to cart
- System returns appropriate products based on filter

---

### 7. Security and Data Validation
**Description:** Comprehensive security measures and strict input validation to protect user data and system integrity.

**Acceptance Criteria:**
- CSRF (Cross-Site Request Forgery) protection on all state-changing forms.
- Password requirements: Minimum 8 characters with hashing via BCrypt.
- Email format validation using Jakarta Validation API.
- Role-Based Access Control (RBAC) ensuring users only access their own data.
- Protection against unauthorized access to administrative endpoints.

---

### 8. Session Management
**Description:** The system must maintain user session state for authenticated users.

**Acceptance Criteria:**
- User login creates a session
- Session maintains user identity across pages
- Shopping cart is tied to user session
- Users can logout to end session
- Unauthenticated users are redirected to login

---

### 9. Logging and Monitoring (AOP)
**Description:** The system must implement automated logging to track application behavior and troubleshoot issues.

**Acceptance Criteria:**
- Aspect-Oriented Programming (AOP) used to log service method executions.
- Request/Response logging for critical business flows (login, checkout).
- Performance tracking (execution time) for database queries.
- Error and Exception logging with stack traces for debugging.

---

### 10. Product Inventory Management
**Description:** The system must maintain accurate product information including pricing and specifications.

**Acceptance Criteria:**
- Products have unique identifiers
- Product specifications vary by type (cores for CPU, memory for GPU, etc.)
- Products can be added, updated, or removed from catalog
- All product changes are persisted in database

---

## MVP Features

### Feature 1: Robust Authentication & RBAC
**Description:** A secure authentication system using Spring Security with Role-Based Access Control (RBAC).

**Technical Implementation:**
- **Security:** BCrypt password encoding and CSRF protection.
- **Roles:** `ROLE_USER` for customers and `ROLE_ADMIN` for system management.
- **Persistence:** User details stored in the `users` table with Jakarta Validation.
- **Session:** Secure session management with "Remember Me" functionality.

**User Story:** As a user, I want a secure way to access my personal data and orders so that my information remains private.

**Priority:** Critical (P0)

---

### Feature 2: Advanced Product Catalog
**Description:** A dynamic product catalog with category-based filtering and high-performance pagination.

**Technical Implementation:**
- **Filtering:** Category-specific views for Processors, GPUs, Motherboards, and Cases.
- **Pagination:** Server-side pagination (Spring Data JPA) to handle large datasets efficiently.
- **Inheritance:** `JOINED` inheritance strategy for specialized product specifications.
- **UI:** Responsive grid layout with real-time specification display.

**User Story:** As a user, I want to filter and paginate through components so that I can quickly find parts that fit my specific build requirements.

**Priority:** Critical (P0)

---

### Feature 3: Persistent Cart & Wishlist
**Description:** Persistent storage for user shopping carts and wishlists, enabling cross-device shopping.

**Technical Implementation:**
- **Service:** `CartService` and `WishlistService` managing database persistence.
- **Operations:** Real-time quantity adjustments, price calculations, and "Move to Cart" logic.
- **Integration:** Automated cart cleanup upon successful order placement.

**User Story:** As a user, I want my cart to be saved across sessions so that I can place my order later.

**Priority:** High (P1)

---

### Feature 4: Secure Checkout & Transaction Flow
**Description:** A checkout system that validates payment details and ensures transactional integrity.

**Technical Implementation:**
- **Transaction:** Atomic order creation and cart clearing using `@Transactional`.
- **Validation:** Server-side validation of payment information (Luhn check simulation).
- **History:** Immutable order snapshots capturing price-at-purchase to prevent history drift.

**User Story:** As a user, I want a secure checkout process so that I can finalize my purchase.

**Priority:** Critical (P0)

---

### Feature 5: Admin User Management
**Description:** A dedicated administrative dashboard for managing system users and monitoring activity.

**Technical Implementation:**
- **Authorization:** Restrictive access via `hasRole('ADMIN')`.
- **Management:** Paginated view of all registered users with administrative controls.
- **Auditing:** AOP-based logging of administrative actions for security audits.

**User Story:** As an administrator, I want to manage user accounts so that I can ensure the platform remains secure and well-moderated.

**Priority:** High (P1)

---

## Technical Standards & Quality
- **AOP Logging:** Automated logging of all service layer methods for better observability.
- **Exception Handling:** Centralized `@ControllerAdvice` for consistent error responses and user feedback.
- **Testing Coverage:** Target >80% coverage using Jacoco, JUnit 5, and Mockito.
- **Documentation:** Full OpenAPI/Swagger integration for API exploration.

---

## Success Metrics
- **Performance:** Product pages load in under 500ms even with pagination.
- **Security:** 100% of sensitive data is encrypted; no unauthorized access to admin panels.
- **Reliability:** Successful order persistence and transactional integrity across all flows.
- **Quality:** All 50+ unit and integration tests passing in the CI/CD pipeline.