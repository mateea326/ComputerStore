package com.example.ComputerStore.service;

import com.example.ComputerStore.model.Customer;
import com.example.ComputerStore.repo.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.util.Base64;
import java.util.Optional;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    String hashMe(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            return password;
        }
    }

    // comanda new account
    public Customer registerNewCustomer(Customer customer) {
        // verific daca username-ul exista deja
        if (customerRepository.findByUsername(customer.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already taken");
        }
        // verific daca email-ul exista deja
        if (customerRepository.findByEmail(customer.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already taken");
        }

        customer.setPassword(hashMe(customer.getPassword()));
        return customerRepository.save(customer);
    }

    // comanda login
    public Customer login(String username, String password) {

        Optional<Customer> customerOpt = customerRepository.findByUsername(username);

        if (customerOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found. Please create an account");
        }

        Customer customer = customerOpt.get();
        if (!customer.getPassword().equals(hashMe(password))) {
            throw new IllegalArgumentException("Incorrect password");
        }

        return customer;
    }

    // comanda edit account
    public Customer updateCustomer(Integer customerId, Customer updatedDetails) {

        Customer existingCustomer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        existingCustomer.setFirstName(updatedDetails.getFirstName());
        existingCustomer.setLastName(updatedDetails.getLastName());
        existingCustomer.setPhoneNumber(updatedDetails.getPhoneNumber());
        existingCustomer.setAddress(updatedDetails.getAddress());
        existingCustomer.setEmail(updatedDetails.getEmail());

        return customerRepository.save(existingCustomer);
    }

    // comanda delete account
    public void deleteCustomer(Integer customerId) {
        customerRepository.deleteById(customerId);
    }

    public Customer findCustomerById(Integer id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer with id " + id + " was not found"));
    }
}