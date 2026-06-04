package com.example.ComputerStore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ComputerStoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(ComputerStoreApplication.class, args);
	}

}