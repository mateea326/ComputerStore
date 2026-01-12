package com.example.ComputerStore.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity // o marcheaza ca entitate JPA
@Table(name = "processors")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Processor extends Product {
    private Integer coreCount;
    private Integer coreClock;
    private String socket;
}