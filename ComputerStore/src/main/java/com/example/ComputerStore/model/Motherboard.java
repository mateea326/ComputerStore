package com.example.ComputerStore.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "motherboards")
public class Motherboard extends Product {
    private Integer slots;
    private String cpu_socket;
    private String chipset;

    public Motherboard() {}

    public Motherboard(Integer productId, String name, float price, Integer slots, String cpu_socket, String chipset) {
        super(productId, name, price);
        this.slots = slots;
        this.cpu_socket = cpu_socket;
        this.chipset = chipset;
    }

    public Integer getSlots() { return slots; }
    public void setSlots(Integer slots) { this.slots = slots; }
    public String getCpu_socket() { return cpu_socket; }
    public void setCpu_socket(String cpu_socket) { this.cpu_socket = cpu_socket; }
    public String getChipset() { return chipset; }
    public void setChipset(String chipset) { this.chipset = chipset; }
}
