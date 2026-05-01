package com.example.ComputerStore.service;

import com.example.ComputerStore.model.User;
import com.example.ComputerStore.repo.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.util.Base64;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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
    public User registerNewUser(User user) {
        // verific daca username-ul exista deja
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already taken");
        }
        // verific daca email-ul exista deja
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already taken");
        }

        user.setPassword(hashMe(user.getPassword()));
        return userRepository.save(user);
    }

    // comanda login
    public User login(String username, String password) {

        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found. Please create an account");
        }

        User user = userOpt.get();
        if (!user.getPassword().equals(hashMe(password))) {
            throw new IllegalArgumentException("Incorrect password");
        }

        return user;
    }

    // comanda edit account
    public User updateUser(Integer userId, User updatedDetails) {

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        existingUser.setFirstName(updatedDetails.getFirstName());
        existingUser.setLastName(updatedDetails.getLastName());
        existingUser.setPhoneNumber(updatedDetails.getPhoneNumber());
        existingUser.setAddress(updatedDetails.getAddress());
        existingUser.setEmail(updatedDetails.getEmail());

        return userRepository.save(existingUser);
    }

    public User findUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User with id " + id + " was not found"));
    }
    public void deleteUser(Integer id){
        // to do si fa l de tipul user

    }
}