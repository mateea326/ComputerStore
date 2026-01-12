# Computer Store - MVP Requirements Document

## Project Overview
**PC Components E-commerce Store** - A web-based platform for buying computer hardware components including processors, graphics cards, motherboards, and cases.

**Technology Stack:**
- Backend: Spring Boot 4.0.0
- Frontend: Thymeleaf
- Database: PostgreSQL
- Testing: JUnit 5, Mockito

---

## Business Requirements

### 1. User Account Management
**Description:** Users must be able to create accounts, login, and manage their personal information.

**Acceptance Criteria:**
- Users can register with email, username, and password
- Users can login with username and password
- Users can update their profile information (name, address, phone, email)
- Users can delete their account
- Password is hashed for security

**Business Value:** Enables personalized shopping experience and order tracking.

---

### 2. Product Catalog Management
**Description:** The system must display a comprehensive catalog of computer components with detailed specifications.

**Acceptance Criteria:**
- Products are categorized by type (Processors, Graphics Cards, Motherboards, Cases)
- Each product displays: name, price, and technical specifications
- Users can view all products or filter by category
- Product information is stored persistently in database

**Business Value:** Provides customers with detailed product information to make informed purchasing decisions.

---

### 3. Shopping Cart Functionality
**Description:** Users must be able to add products to a cart and manage quantities before checkout.

**Acceptance Criteria:**
- Users can add products to cart
- Cart displays product name, price, and quantity
- Users can remove items or adjust quantities
- Cart persists during user session
- Cart calculates total price automatically

**Business Value:** Enables users to shop for multiple items and review before purchase.

---

### 4. Order Processing
**Description:** Users must be able to complete purchases with payment card information.

**Acceptance Criteria:**
- Users can proceed to checkout from cart
- System collects payment card details (card number, name, expiry date, CVV)
- System validates card information format
- Order is created with timestamp and total price
- Cart is cleared after successful order
- Order details are persisted in database

**Business Value:** Core revenue-generating functionality enabling sales transactions.

---

### 5. Order History
**Description:** Users must be able to view their past orders.

**Acceptance Criteria:**
- Users can view list of all their previous orders
- Each order shows: order ID, date, total price, and items purchased
- Orders are sorted by date (newest first)

**Business Value:** Provides transparency and enables users to track purchase history.

---

### 6. Product Filtering and Search
**Description:** Users must be able to filter products by component type to find specific items quickly.

**Acceptance Criteria:**
- Filter buttons for: All Products, Processors, Motherboards, Graphics Cards, Cases
- Filtered results display only products of selected type
- Filter selection persists when adding items to cart
- System returns appropriate products based on filter

**Business Value:** Improves user experience by reducing time to find desired products.

---

### 7. Data Validation
**Description:** All user inputs must be validated to ensure data integrity and security.

**Acceptance Criteria:**
- Email addresses must be in valid format
- Passwords must be minimum 8 characters
- Phone numbers must be provided
- Card numbers must be 10-20 characters
- CVV must be 3-4 digits

**Business Value:** Prevents invalid data entry and protects system integrity.

---

### 8. Session Management
**Description:** The system must maintain user session state for authenticated users.

**Acceptance Criteria:**
- User login creates a session
- Session maintains user identity across pages
- Shopping cart is tied to user session
- Users can logout to end session
- Unauthenticated users are redirected to login

**Business Value:** Provides seamless user experience and security.

---

### 9. Secure Payment Information Storage
**Description:** Payment card information must be stored securely and associated with orders.

**Acceptance Criteria:**
- Card details are validated before storage
- Each card is linked to exactly one order
- Card information includes: number, cardholder name, expiry date, CVV
- Card data is stored in separate secure table

**Business Value:** Enables order fulfillment and payment processing while maintaining security.

---

### 10. Product Inventory Management
**Description:** The system must maintain accurate product information including pricing and specifications.

**Acceptance Criteria:**
- Products have unique identifiers
- Product specifications vary by type (cores for CPU, memory for GPU, etc.)
- Products can be added, updated, or removed from catalog
- All product changes are persisted in database

**Business Value:** Ensures accurate product information and pricing for customers.

---

## MVP Features

### Feature 1: User Authentication System
**Description:** Complete user registration and login system with secure password hashing.

**Technical Implementation:**
- REST endpoints: POST `/api/v1/customers/register`, POST `/api/v1/customers/login`
- Service: `CustomerService` with SHA-256 password hashing
- Database: `customers` table with validated fields
- Frontend: Login and registration pages with Thymeleaf

**User Story:** As a customer, I want to create an account and login so that I can make purchases and track my orders.

**Priority:** Critical (P0) - Required for all other features

---

### Feature 2: Product Browsing and Filtering
**Description:** Comprehensive product catalog with category-based filtering.

**Technical Implementation:**
- REST endpoints: GET `/api/v1/products`, GET `/api/v1/products/filter?type={type}`
- Service: `ProductService` with inheritance-based product types
- Database: `products` parent table with child tables (processors, graphicscards, motherboards, cases)
- Frontend: Product grid with filter buttons and detailed specifications

**User Story:** As a customer, I want to browse computer components by category so that I can find the parts I need for my build.

**Priority:** Critical (P0) - Core shopping functionality

---

### Feature 3: Shopping Cart Management
**Description:** Session-based shopping cart with add/remove functionality.

**Technical Implementation:**
- Service: `SessionCartService` using HTTP session storage
- Cart operations: add product, remove product, view cart, clear cart
- Frontend: Cart page showing items, quantities, and total price
- Session persistence: Cart maintained during user session

**User Story:** As a customer, I want to add multiple items to my cart and review them before checkout so that I can ensure I'm buying everything I need.

**Priority:** Critical (P0) - Required for purchasing

---

### Feature 4: Checkout and Payment Processing
**Description:** Complete checkout flow with payment card validation and order creation.

**Technical Implementation:**
- REST endpoint: POST `/api/v1/orders/checkout/{customerId}`
- Services: `OrderService`, `CardService` for order creation and payment processing
- Database: `orders` table with `order_items` (many-to-many) and `cards` table (one-to-one)
- Frontend: Checkout page with card input form and order summary
- Validation: Card number (10-20 chars), CVV (3-4 digits), expiry date format

**User Story:** As a customer, I want to securely enter my payment information and complete my purchase so that I can receive my computer components.

**Priority:** Critical (P0) - Revenue generation

---

### Feature 5: Order History and Tracking
**Description:** View past orders with details of purchased items.

**Technical Implementation:**
- REST endpoint: GET `/api/v1/orders/history/{customerId}`
- Service: `OrderService` retrieves orders by customer
- Database: Queries `orders` joined with `order_items` and `customers`
- Frontend: Order history page

**User Story:** As a customer, I want to view my past orders so that I can track my purchases and reference my order details.

**Priority:** High (P1) - Important for customer satisfaction

---

## Technical Architecture

### Database Schema (6+ Entities)
1. **customers** - User accounts
2. **products** - Base product information
3. **processors** - CPU-specific specs (inherits from products)
4. **graphicscards** - GPU-specific specs (inherits from products)
5. **motherboards** - Motherboard-specific specs (inherits from products)
6. **cases** - Case-specific specs (inherits from products)
7. **orders** - Purchase orders
8. **order_items** - Order line items (junction table)
9. **cards** - Payment card information

### Key Relationships (4+)
1. Customer → Orders (One-to-Many)
2. Order → OrderItems (One-to-Many)
3. OrderItem → Product (Many-to-One)
4. Order → Card (One-to-One)
5. Product → Subclasses (Inheritance: JOINED strategy)

---

## Success Metrics
- Users can successfully register and login
- Products display with correct specifications
- Shopping cart maintains state throughout session
- Orders are created and persisted correctly
- All API endpoints return appropriate responses
- All unit tests pass (55+ tests)

---

## Future Enhancements (Post-MVP)
- Admin panel for product management
- Product reviews and ratings
- Advanced search with multiple filters
- Wishlist functionality
- Inventory tracking and stock management