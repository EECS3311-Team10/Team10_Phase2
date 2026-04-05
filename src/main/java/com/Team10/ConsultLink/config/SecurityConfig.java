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
            .csrf(csrf -> csrf.disable()) // 1. Kill the CSRF wall so your form works
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/index.html", "/css/**").permitAll() // 2. Let people see the login page
                .anyRequest().authenticated() // 3. Lock everything else
            )
            .formLogin(form -> form
                .loginPage("/index.html")
                .defaultSuccessUrl("/admin/admin.html", true) // 4. Tell it where to go!
                .permitAll()
            );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Since you saved "admin123" as plain text in the DB, we use NoOp for now
        // (In a real project, you'd use BCrypt)
        return NoOpPasswordEncoder.getInstance(); 
    }
}