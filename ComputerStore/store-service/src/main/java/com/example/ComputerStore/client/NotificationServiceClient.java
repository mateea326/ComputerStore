package com.example.ComputerStore.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Map;

@FeignClient(name = "notification-service")
public interface NotificationServiceClient {

    @PostMapping("/api/v1/notifications/send")
    String sendNotification(@RequestBody Map<String, Object> request);
}
