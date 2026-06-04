package com.example.ComputerStore.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "processors")
public class Processor extends Product {
    @NotNull(message = "Core count is required")
    @Min(value = 1, message = "Core count must be at least 1")
    private Integer coreCount;

    @NotNull(message = "Core clock is required")
    @Min(value = 0, message = "Core clock must be positive")
    private Float coreClock;

    @NotBlank(message = "Socket is required")
    private String socket;

    public Processor() {}

    public Processor(Integer productId, String name, float price, Integer coreCount, Float coreClock, String socket) {
        super(productId, name, price);
        this.coreCount = coreCount;
        this.coreClock = coreClock;
        this.socket = socket;
    }

    public Integer getCoreCount() { return coreCount; }
    public void setCoreCount(Integer coreCount) { this.coreCount = coreCount; }
    public Float getCoreClock() { return coreClock; }
    public void setCoreClock(Float coreClock) { this.coreClock = coreClock; }
    public String getSocket() { return socket; }
    public void setSocket(String socket) { this.socket = socket; }
}