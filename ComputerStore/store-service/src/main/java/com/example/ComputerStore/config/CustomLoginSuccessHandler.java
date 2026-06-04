package com.example.ComputerStore.config;

import com.example.ComputerStore.model.User;
import com.example.ComputerStore.client.UserServiceClient;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserServiceClient userServiceClient;

    public CustomLoginSuccessHandler(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String username = authentication.getName();
        User user = userServiceClient.getUserByUsernameInternal(username);
        if (user == null) {
            throw new ServletException("User details could not be retrieved from user-service for user: " + username);
        }

        HttpSession session = request.getSession();
        session.setAttribute("userId", user.getUserId());
        session.setAttribute("userName", user.getFirstName());

        response.sendRedirect("/products");
    }
}
