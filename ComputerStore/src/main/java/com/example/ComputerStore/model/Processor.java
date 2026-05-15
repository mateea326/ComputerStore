package com.example.ComputerStore.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "processors")
public class Processor extends Product {
    private Integer coreCount;
    private Float coreClock;
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