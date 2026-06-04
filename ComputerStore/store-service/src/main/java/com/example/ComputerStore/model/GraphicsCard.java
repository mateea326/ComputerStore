package com.example.ComputerStore.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "graphicscards")
public class GraphicsCard extends Product {
    @NotNull(message = "Memory size is required")
    @Min(value = 1, message = "Memory size must be at least 1")
    private Integer memorySize;

    @NotNull(message = "Core clock is required")
    @Min(value = 1, message = "Core clock must be at least 1")
    private Integer coreClock;

    @NotNull(message = "Memory clock is required")
    @Min(value = 1, message = "Memory clock must be at least 1")
    private Integer memoryClock;

    public GraphicsCard() {}

    public GraphicsCard(Integer productId, String name, float price, Integer memorySize, Integer coreClock, Integer memoryClock) {
        super(productId, name, price);
        this.memorySize = memorySize;
        this.coreClock = coreClock;
        this.memoryClock = memoryClock;
    }

    public Integer getMemorySize() { return memorySize; }
    public void setMemorySize(Integer memorySize) { this.memorySize = memorySize; }
    public Integer getCoreClock() { return coreClock; }
    public void setCoreClock(Integer coreClock) { this.coreClock = coreClock; }
    public Integer getMemoryClock() { return memoryClock; }
    public void setMemoryClock(Integer memoryClock) { this.memoryClock = memoryClock; }
}
