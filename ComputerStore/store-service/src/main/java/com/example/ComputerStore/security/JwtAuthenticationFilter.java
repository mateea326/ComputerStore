package com.example.ComputerStore.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.example.ComputerStore.client.UserServiceClient;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserServiceClient userServiceClient;

    @Value("${jwt.cookieName:jwt}")
    private String jwtCookieName;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserServiceClient userServiceClient) {
        this.jwtUtil = jwtUtil;
        this.userServiceClient = userServiceClient;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String jwt = null;

        // 1. Incercam sa luam JWT-ul din Header (pentru API calls / Feign)
        final String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
        }

        // 2. Daca nu e in Header, incercam din Cookie (pentru UI Thymeleaf)
        if (jwt == null && request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (jwtCookieName.equals(cookie.getName())) {
                    jwt = cookie.getValue();
                    break;
                }
            }
        }

        // Daca avem token si nu e deja autentificat in context
        if (jwt != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtUtil.validateToken(jwt)) {
                Claims claims = jwtUtil.extractAllClaims(jwt);
                String username = claims.getSubject();
                
                @SuppressWarnings("unchecked")
                List<String> roles = claims.get("roles", List.class);

                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        username, null, authorities);

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // Setam securitatea in context (STATELESS)
                SecurityContextHolder.getContext().setAuthentication(authToken);

                // Asiguram ca sesiunea are userId si userName populate (necesar pentru controller-ul legacy de Thymeleaf)
                jakarta.servlet.http.HttpSession session = request.getSession(true);
                if (session.getAttribute("userId") == null) {
                    try {
                        com.example.ComputerStore.model.User user = userServiceClient.getUserByUsernameInternal(username);
                        if (user != null) {
                            session.setAttribute("userId", user.getUserId());
                            session.setAttribute("userName", user.getFirstName());
                            session.setAttribute("userRole", user.getRole());
                        }
                    } catch (Exception e) {
                        logger.error("Could not pre-populate session with user details for JWT", e);
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
