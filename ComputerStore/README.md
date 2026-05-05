# 🖥️ ComputerStore - E-commerce Application

Acesta este un proiect academic reprezentând un magazin online de componente PC, construit cu **Spring Boot 3.2**, **Thymeleaf**, **Spring Security** și **Spring Data JPA**.

## 🚀 Tehnologii Utilizate
- **Backend**: Spring Boot, Spring Security (BCrypt, Session based, CSRF protected)
- **Database**: PostgreSQL (Dev), H2 (Test)
- **Frontend**: Thymeleaf, Vanilla CSS, JavaScript Validation
- **Logging**: SLF4J + Logback + Spring AOP (Automatic Service Logging)
- **Testing**: JUnit 5, Mockito, Jacoco (Coverage)

## 📊 Model de Date (ER Diagram)

```mermaid
erDiagram
    USER ||--o| CART : "has"
    USER ||--o| WISHLIST : "has"
    USER ||--o{ ORDER : "places"
    
    CART ||--o{ CART_ITEM : "contains"
    PRODUCT ||--o{ CART_ITEM : "in"
    
    WISHLIST ||--o{ PRODUCT : "contains (ManyToMany)"
    
    ORDER ||--o{ ORDER_ITEM : "contains"
    PRODUCT ||--o{ ORDER_ITEM : "purchased_as"
    
    PRODUCT <|-- PROCESSOR : "is a"
    PRODUCT <|-- GRAPHICS_CARD : "is a"
    PRODUCT <|-- MOTHERBOARD : "is a"
    PRODUCT <|-- CASE : "is a"

    USER {
        int userId PK
        string username UK
        string email UK
        string password
        string role
    }
    
    PRODUCT {
        int productId PK
        string name
        double price
    }
    
    ORDER {
        int orderId PK
        datetime orderDate
        double totalPrice
    }
```

## 🛠️ Cerințe Implementate

1. **Model de Date (Lab 2)**:
   - 11 entități corelate.
   - Relații: `@OneToOne` (User-Cart), `@OneToMany` (User-Order), `@ManyToMany` (Wishlist-Product), Inheritance (`JOINED`).
2. **Operații CRUD Complete**:
   - Create, Read, Update, Delete pentru User, Product, Order.
   - Service layer cu logică de business și excepții custom (`ResourceNotFoundException`, etc.).
3. **Multi-Environment**:
   - `dev` profile (PostgreSQL).
   - `test` profile (H2).
4. **Testing**:
   - Unit tests (Mockito) pentru servicii.
   - Integration tests pentru flow-ul de checkout.
   - Jacoco coverage plugin.
5. **Views & Validation**:
   - Interfață Thymeleaf responsivă.
   - Validare Server-side (@Valid) + Client-side (JS).
   - Pagini de eroare custom (404, 500).
6. **Logging**:
   - Configurare Logback (fișier separat pentru erori).
   - Logging automată prin **Aspect Oriented Programming (@Aspect)**.
7. **Paginare și Sortare**:
   - Implementat pentru **Products**, **Orders** și **Users**.
   - Sortare după multiple criterii în UI.
8. **Spring Security**:
   - Autentificare JDBC.
   - Roluri: `USER`, `ADMIN`.
   - Protecție **CSRF activă**.
   - Parole securizate cu BCrypt.

## 🏃 Cum se rulează
1. Configurați baza de date PostgreSQL în `application-dev.properties`.
2. Rulați cu profilul dev: `mvn spring-boot:run -Dspring-boot.run.profiles=dev`.
3. Admin implicit: `admin` / `admin123`.
