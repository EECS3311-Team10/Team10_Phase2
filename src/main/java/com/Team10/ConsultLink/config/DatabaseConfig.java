package com.Team10.ConsultLink.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.Team10.ConsultLink.repository.UserRepository;
import com.Team10.ConsultLink.users.*;


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
            System.out.println("📝 Database is empty. Seeding dummy users...");

            // --- 1 ADMIN ---
            repository.save(new Admin("AD-1", "System Admin", "admin@consultlink.com", "555-0101", "admin123"));

            // --- 2 CONSULTANTS ---
            repository.save(new Consultant("CO-1", "Sarah Chen", "sarah.c@consultlink.com", "555-0201", "consult123"));
            repository.save(new Consultant("CO-2", "Marcus Thorne", "m.thorne@consultlink.com", "555-0202", "consult123"));

            // --- 3 CLIENTS ---
            repository.save(new Client("CL-1", "Lassonde Tech", "contact@lassonde.ca", "555-0301", "client123"));
            repository.save(new Client("CL-2", "York Health", "info@yorkh.ca", "555-0302", "client123"));
            repository.save(new Client("CL-3", "Ontario Infra", "leads@oninfra.gov", "555-0303", "client123"));

            System.out.println("✅ Seeding complete: 1 Admin, 2 Consultants, 3 Clients created.");
        } else {
            System.out.println("📊 Database already contains " + repository.count() + " users. Skipping seed.");
        }
    };
}
}