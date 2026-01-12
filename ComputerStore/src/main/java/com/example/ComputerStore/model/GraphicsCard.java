package com.example.ComputerStore.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity // o marcheaza ca entitate JPA
@Table(name = "graphicscards")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraphicsCard extends Product {
    private Integer memorySize;
    private Integer coreClock;
    private Integer memoryClock;
}
