package com.Team10.ConsultLink.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.Team10.ConsultLink.respository.UserRepository;
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
                User admin = new Admin("admin@yorku.ca", "password123", "ADMIN");
                
                repository.save(admin); // This sends the data to Docker
                System.out.println("✅ Default admin created: admin@yorku.ca");
            } else {
                System.out.println("📊 Database already contains " + repository.count() + " users.");
            }
    };
}
}