package com.example.ComputerStore.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity // o marcheaza ca entitate JPA
@Table(name = "motherboards")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Motherboard extends Product {
    private Integer slots;
    private String cpu_socket;
    private String chipset;
}
