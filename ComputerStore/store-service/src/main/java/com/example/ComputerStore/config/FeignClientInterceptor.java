package com.example.ComputerStore.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@RefreshScope
public class FeignClientInterceptor implements RequestInterceptor {

    @Value("${jwt.cookieName:jwt}")
    private String jwtCookieName;

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            HttpServletRequest request = requestAttributes.getRequest();

            // 1. Incercam sa preluam token-ul din header-ul Authorization existent
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                template.header("Authorization", authHeader);
                return;
            }

            // 2. Daca nu este in header, il cautam in Cookie (pentru UI Thymeleaf)
            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    if (jwtCookieName.equals(cookie.getName())) {
                        template.header("Authorization", "Bearer " + cookie.getValue());
                        return;
                    }
                }
            }
        }
    }
}
