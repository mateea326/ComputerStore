package com.example.ComputerStore.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "graphicscards")
public class GraphicsCard extends Product {
    private Integer memorySize;
    private Integer coreClock;
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
