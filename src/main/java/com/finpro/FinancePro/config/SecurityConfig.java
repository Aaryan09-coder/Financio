package com.finpro.FinancePro.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/users/**","/budgets/**", "/transactions/**", "/investments/**", "/goals/**").permitAll() // Allow unauthenticated access to /users
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.disable()) // Disable CSRF for simplicity (not recommended for production)
                .httpBasic(withDefaults()); // Use default HTTP Basic authentication

        return http.build();
    }
}
