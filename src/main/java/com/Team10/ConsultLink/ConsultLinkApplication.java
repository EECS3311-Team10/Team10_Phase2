package com.Team10.ConsultLink;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration; // Add this import

// The 'exclude' tells Spring to ignore the auto-generated login screen
@SpringBootApplication(exclude = { SecurityAutoConfiguration.class }) 
public class ConsultLinkApplication {

	@Value("${GROQ_API_KEY}")
    private String apiKey;

    public static void main(String[] args) {
        SpringApplication.run(ConsultLinkApplication.class, args);
    }

	@jakarta.annotation.PostConstruct
    public void checkKey() {
        System.out.println("========== CONFIG CHECK ==========");
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("${GROQ_API_KEY}")) {
            System.err.println("❌ ERROR: GROQ API Key is MISSING!");
            System.err.println("Check your .env file and application.properties.");
        } else {
            System.out.println("✅ SUCCESS: GROQ API Key loaded.");
            // Print just the first 4 chars for security
            System.out.println("Key starts with: " + apiKey.substring(0, 4) + "...");
        }
        System.out.println("==================================");
    }
}
