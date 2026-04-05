package com.Team10.ConsultLink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration; // Add this import

// The 'exclude' tells Spring to ignore the auto-generated login screen
@SpringBootApplication(exclude = { SecurityAutoConfiguration.class }) 
public class ConsultLinkApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsultLinkApplication.class, args);
    }
}
