package com.example.ComputerStore.repo;

import com.example.ComputerStore.model.Processor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessorRepository extends JpaRepository<Processor, Integer> {
}