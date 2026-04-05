package com.Team10.ConsultLink.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.Team10.ConsultLink.repository.UserRepository;
import com.Team10.ConsultLink.users.Admin;
import com.Team10.ConsultLink.users.User;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.Connection;

@Configuration
public class DatabaseConfig {

    @Bean
    @Transactional
    CommandLineRunner initDatabase(UserRepository repository) {
    return args -> {
       if (repository.count() == 0) {
                System.out.println("📝 Database is empty. Creating default admin...");
                
                // For User constructor (email, password, role)
                User admin = new Admin("AD-1", "System Admin", "admin@consultlink.com", "555-0199", "admin123");
                
                repository.save(admin); // This sends the data to Docker
                System.out.println("✅ Default admin created: admin@consultlink.com");
            } else {
                System.out.println("📊 Database already contains " + repository.count() + " users.");
            }
    };
}
}