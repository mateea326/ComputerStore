package com.example.ComputerStore.controller;

import com.example.ComputerStore.dto.NotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    @PostMapping("/send")
    public ResponseEntity<String> sendNotification(@RequestBody NotificationRequest request) {
        log.info("[NOTIFICATION SERVICE] Se trimite notificare tip [{}] catre '{}': {}", 
                 request.getType(), request.getRecipient(), request.getMessage());
        
        // Simulam trimiterea cu succes si raspundem cu HTTP 200 OK
        return ResponseEntity.ok("Notificare trimisa cu succes catre " + request.getRecipient());
    }
}
