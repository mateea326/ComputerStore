# Computer Store

A full-stack e-commerce platform for buying PC components built with Spring Boot and Thymeleaf.

## Features

- **User Authentication** - Secure registration/login with password hashing
- **Product Catalog** - Browse processors, graphics cards, motherboards, and cases
- **Shopping Cart** - Session-based cart with real-time total calculations
- **Checkout System** - Payment processing with card validation
- **Order History** - Track past purchases and order details
- **Account Management** - Update profile or delete account

## Tech Stack

- **Backend:** Spring Boot 4.0.0, Java
- **Frontend:** Thymeleaf, HTML/CSS
- **Database:** PostgreSQL
- **Testing:** JUnit 5, Mockito

## Database Schema

9 entities with inheritance-based product types:
- `customers`, `orders`, `order_items`, `cards`
- `products` (parent) → `processors`, `graphicscards`, `motherboards`, `cases`

## Running the Project

1. Clone the repository
2. Configure PostgreSQL connection in `application.properties`
3. Run: `mvn spring-boot:run`
4. Access at: `http://localhost:8081`

## Testing

Run tests: mvn test

## API Endpoints

- `POST /api/v1/customers/register` - User registration
- `POST /api/v1/customers/login` - User login
- `GET /api/v1/products` - Get all products
- `GET /api/v1/products/filter?type={type}` - Filter products
- `GET /api/v1/orders/history/{customerId}` - Order history
