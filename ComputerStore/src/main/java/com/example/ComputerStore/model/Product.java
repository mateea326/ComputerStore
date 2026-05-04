package com.example.ComputerStore.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "products")
@Inheritance(strategy = InheritanceType.JOINED)

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type") // "type" va fi câmpul din JSON care decide clasa
@JsonSubTypes({
        @JsonSubTypes.Type(value = Processor.class, name = "processor"),
        @JsonSubTypes.Type(value = GraphicsCard.class, name = "gpu"),
        @JsonSubTypes.Type(value = Motherboard.class, name = "motherboard"),
        @JsonSubTypes.Type(value = Case.class, name = "case")
})
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
