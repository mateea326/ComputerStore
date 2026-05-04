# Computer Store

A full-stack e-commerce platform for buying PC components built with Spring Boot and Thymeleaf. This project fulfills academic requirements for a web application with Spring Boot.

## Features

- **User Authentication (Spring Security)** - Secure registration/login with BCrypt hashing and role-based access control (USER/ADMIN).
- **Multi-Environment Configuration** - Support for `dev` (PostgreSQL) and `test` (H2 In-Memory) profiles.
- **Product Catalog** - Browse processors, graphics cards, motherboards, and cases with **Pagination and Sorting**.
- **Shopping Cart & Wishlist** - Manage items in a persistent cart and save favorites in a many-to-many wishlist.
- **Checkout System** - Secure checkout with card validation and order persistence.
- **Order History** - Track past purchases and detailed order items.
- **Account Management** - Update profile or delete account.
- **Logging** - Comprehensive SLF4J logging with separate files for errors.

## Tech Stack

- **Backend:** Spring Boot 3.2.0, Java 21, Spring Security
- **Frontend:** Thymeleaf, HTML/CSS (Vanilla)
- **Database:** PostgreSQL (Dev), H2 (Test)
- **Logging:** SLF4J + Logback
- **Testing:** JUnit 5, Mockito, MockMvc (Integration Tests)

## Database Schema (ER Diagram)

The project includes 11 entities with various relationship types:
- **Entities:** `User`, `Product`, `Order`, `OrderItem`, `Cart`, `CartItem`, `Wishlist`, `Processor`, `GraphicsCard`, `Motherboard`, `Case`.
- **Relationships:**
    - `@OneToOne`: User ↔ Cart, User ↔ Wishlist
    - `@OneToMany`/`@ManyToOne`: User ↔ Order, Order ↔ OrderItem, Cart ↔ CartItem
    - `@ManyToMany`: Wishlist ↔ Product
- **Inheritance:** `Product` is the base class for component-specific entities.

![ER Diagram](er_diagram.jpeg)

## Running the Project

1. Configure PostgreSQL in `application-dev.properties`.
2. Run with Maven: `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`
3. Access at: `http://localhost:8081`

## Testing

The project includes unit tests and integration tests.
Run tests: `./mvnw test -Dspring.profiles.active=test`

## API Endpoints (REST v1)

- `POST /api/v1/users/register` - User registration
- `POST /api/v1/users/login` - User login
- `GET /api/v1/products` - Get all products (supports pagination)
- `GET /api/v1/products/filter?type={type}` - Filter products
- `GET /api/v1/orders/history/{userId}` - Order history

## Logging Configuration
- Logs are stored in the `logs/` directory.
- `application.log`: All application logs.
- `error.log`: Only error-level logs.
