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

// filtru care intercepteaza fiecare cerere http pentru a verifica prezenta tokenului jwt
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

    // filtreaza cererea extrage tokenul jwt il valideaza si configureaza contextul de securitate
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String jwt = null;

        // 1 incercam sa luam jwt-ul din header pentru apeluri api sau feign
        final String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
        }

        // 2 daca nu este in header cautam jwt-ul in cookie-uri pentru interfata web
        if (jwt == null && request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (jwtCookieName.equals(cookie.getName())) {
                    jwt = cookie.getValue();
                    break;
                }
            }
        }

        // daca avem un token valid si utilizatorul nu este deja autentificat in context
        if (jwt != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtUtil.validateToken(jwt)) {
                Claims claims = jwtUtil.extractAllClaims(jwt);
                String username = claims.getSubject();
                
                @SuppressWarnings("unchecked")
                List<String> roles = claims.get("roles", List.class);

                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                // creeaza obiectul de autentificare pe baza datelor din token
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        username, null, authorities);

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // seteaza autentificarea in contextul spring security
                SecurityContextHolder.getContext().setAuthentication(authToken);

                // salveaza detaliile utilizatorului in sesiune pentru controllerele mvc
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
