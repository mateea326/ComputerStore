package com.example.ComputerStore.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity // o marcheaza ca entitate JPA
@Table(name = "cases")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Case extends Product {
    private int vents;
    private String type;
    private String format;
}
