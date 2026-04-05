package com.Team10.ConsultLink.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable()) // Stops 403 on POST
        .authorizeHttpRequests(auth -> auth
            // The "Universal Access" rule:
            .requestMatchers("/", "/index.html", "/css/**", "/js/**", "/api/users/**").permitAll()
            .requestMatchers("/admin/**", "/client/**", "/consultant/**").permitAll() 
            // Change this to permitAll() just to test if it's the culprit
            .anyRequest().permitAll() 
        )
        // Completely disable all built-in Spring login/logout redirects
        .formLogin(form -> form.disable())
        .httpBasic(basic -> basic.disable())
        .logout(logout -> logout.disable());

    return http.build();
}

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Required because your dummy data uses plain text "admin123"
        return NoOpPasswordEncoder.getInstance(); 
    }
}