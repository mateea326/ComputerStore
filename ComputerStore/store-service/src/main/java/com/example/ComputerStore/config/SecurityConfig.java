package com.example.ComputerStore.config;

import com.example.ComputerStore.service.CustomUserDetailsService;
import com.example.ComputerStore.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;


// configurarea de securitate a aplicatiei web folosind spring security
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final CustomLoginSuccessHandler successHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(CustomUserDetailsService userDetailsService, CustomLoginSuccessHandler successHandler, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.successHandler = successHandler;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    // defineste encoderul bcrypt pentru hashingul parolelor
    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // configureaza regulile de securitate rutele protejate si dezactivarea csrf pe anumite apiuri
    // csrf vulerabilitate web de tip atac care permite unui atacator sa modifice
    // parametrii requesturilor trimise de userul conectat
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf
                // dezactiveaza protectia csrf pentru login register si apelurile de tip rest api
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                .ignoringRequestMatchers(
                    AntPathRequestMatcher.antMatcher("/login"),
                    AntPathRequestMatcher.antMatcher("/register"),
                    AntPathRequestMatcher.antMatcher("/api/**"),
                    AntPathRequestMatcher.antMatcher("/api/store/**"),
                    AntPathRequestMatcher.antMatcher("/swagger-ui/**"),
                    AntPathRequestMatcher.antMatcher("/v3/api-docs/**"),
                    AntPathRequestMatcher.antMatcher("/cart/**"),
                    AntPathRequestMatcher.antMatcher("/wishlist/**"),
                    AntPathRequestMatcher.antMatcher("/order-history/**"),
                    AntPathRequestMatcher.antMatcher("/account-settings/**"),
                    AntPathRequestMatcher.antMatcher("/checkout/**"),
                    AntPathRequestMatcher.antMatcher("/admin/**")
                )
            )
            // seteaza modul stateless pentru sesiuni deoarece folosim tokenuri jwt
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // defineste regulile de acces pe baza de roluri pentru rutele din aplicatie
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(AntPathRequestMatcher.antMatcher("/register")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/login")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/css/**")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/js/**")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/images/**")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/products/**")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/error")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/actuator")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/actuator/**")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/api/v1/users/register")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/api/v1/users/login")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/admin/**")).hasRole("ADMIN")
                .requestMatchers(AntPathRequestMatcher.antMatcher(org.springframework.http.HttpMethod.POST, "/api/v1/products/**")).hasRole("ADMIN")
                .requestMatchers(AntPathRequestMatcher.antMatcher(org.springframework.http.HttpMethod.PUT, "/api/v1/products/**")).hasRole("ADMIN")
                .requestMatchers(AntPathRequestMatcher.antMatcher(org.springframework.http.HttpMethod.DELETE, "/api/v1/products/**")).hasRole("ADMIN")
                .requestMatchers(AntPathRequestMatcher.antMatcher(org.springframework.http.HttpMethod.GET, "/api/v1/products/**")).permitAll()
                .requestMatchers(AntPathRequestMatcher.antMatcher(org.springframework.http.HttpMethod.DELETE, "/api/v1/users/**")).hasRole("ADMIN")
                .requestMatchers(AntPathRequestMatcher.antMatcher(org.springframework.http.HttpMethod.PUT, "/api/v1/users/**")).authenticated()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/api/v1/orders/**")).authenticated()
                .anyRequest().authenticated()
            )
            // configureaza pagina si handlerul de login
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(successHandler)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            // configureaza stergerea cookieurilor si redirectionarea la logout
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "jwt")
                .permitAll()
            )
            // seteaza mecanismul de remember me
            .rememberMe(remember -> remember
                .key("uniqueAndSecret")
                .tokenValiditySeconds(86400) // 1 day
            )
            // adauga filtrul jwt inaintea filtrului de autentificare standard
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // expune managerul de autentificare folosit de spring security
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
        return authBuilder.build();
    }
}
