package com.example.ComputerStore.config;

import com.example.ComputerStore.model.User;
import com.example.ComputerStore.client.UserServiceClient;
import com.example.ComputerStore.security.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RefreshScope
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserServiceClient userServiceClient;
    private final JwtUtil jwtUtil;

    @Value("${jwt.cookieName:jwt}")
    private String jwtCookieName;

    @Value("${jwt.expirationMs:86400000}")
    private int jwtExpirationMs;

    public CustomLoginSuccessHandler(UserServiceClient userServiceClient, JwtUtil jwtUtil) {
        this.userServiceClient = userServiceClient;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String username = authentication.getName();
        User user = userServiceClient.getUserByUsernameInternal(username);
        if (user == null) {
            throw new ServletException("User details could not be retrieved from user-service for user: " + username);
        }

        // Generam JWT
        String jwt = jwtUtil.generateToken((UserDetails) authentication.getPrincipal());

        // Cream cookie-ul HttpOnly
        Cookie jwtCookie = new Cookie(jwtCookieName, jwt);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false); // set to true if using HTTPS in prod
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(jwtExpirationMs / 1000);
        response.addCookie(jwtCookie);

        // Pre-populate session for legacy Thymeleaf controllers immediately upon successful login
        HttpSession session = request.getSession(true);
        session.setAttribute("userId", user.getUserId());
        session.setAttribute("userName", user.getFirstName());
        session.setAttribute("userRole", user.getRole());

        response.sendRedirect(request.getContextPath() + "/products");
    }
}
