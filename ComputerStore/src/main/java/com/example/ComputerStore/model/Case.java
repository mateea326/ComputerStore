package com.example.ComputerStore.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "cases")
public class Case extends Product {
    private Integer vents;
    private String type;
    private String format;

    public Case() {}

    public Case(Integer productId, String name, float price, Integer vents, String type, String format) {
        super(productId, name, price);
        this.vents = vents;
        this.type = type;
        this.format = format;
    }

    public Integer getVents() { return vents; }
    public void setVents(Integer vents) { this.vents = vents; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
}
