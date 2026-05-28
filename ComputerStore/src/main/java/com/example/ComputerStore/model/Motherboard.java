package com.example.ComputerStore.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "motherboards")
public class Motherboard extends Product {
    @NotNull(message = "Slots count is required")
    @Min(value = 1, message = "Slots count must be at least 1")
    private Integer slots;

    @NotBlank(message = "CPU socket is required")
    private String cpu_socket;

    @NotBlank(message = "Chipset is required")
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
