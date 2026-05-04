package com.example.ComputerStore.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "products")
@Inheritance(strategy = InheritanceType.JOINED)
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer productId;

    @Size(min = 5, max = 100)
    @NotBlank(message = "Input product name")
    protected String name;

    @Min(value = 0)
    protected float price;

    public Product() {}

    public Product(Integer productId, String name, float price) {
        this.productId = productId;
        this.name = name;
        this.price = price;
    }

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public float getPrice() { return price; }
    public void setPrice(float price) { this.price = price; }
}
