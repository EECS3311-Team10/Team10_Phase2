package com.Team10.ConsultLink.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.Team10.ConsultLink.model.Service;
import com.Team10.ConsultLink.repository.ServiceRepository;
import com.Team10.ConsultLink.repository.UserRepository;
import com.Team10.ConsultLink.users.Admin;
import com.Team10.ConsultLink.users.Client;
import com.Team10.ConsultLink.users.Consultant;

import jakarta.transaction.Transactional;

@Configuration
public class DatabaseConfig {

    @Bean
    @Transactional
    CommandLineRunner initDatabase(UserRepository repository,
                                   ServiceRepository serviceRepository) {
        return args -> {

            // seed users
            if (repository.count() == 0) {
                System.out.println("📝 Database is empty. Seeding users...");

                repository.save(new Admin(
                        "AD-1",
                        "System Admin",
                        "admin@consultlink.com",
                        "555-0101",
                        "admin123"
                ));

                repository.save(new Consultant(
                        "CO-1",
                        "Sarah Chen",
                        "sarah.c@consultlink.com",
                        "555-0201",
                        "consult123"
                ));

                repository.save(new Consultant(
                        "CO-2",
                        "Marcus Thorne",
                        "m.thorne@consultlink.com",
                        "555-0202",
                        "consult123"
                ));

                repository.save(new Client(
                        "CL-1",
                        "Lassonde Tech",
                        "contact@lassonde.ca",
                        "555-0301",
                        "client123"
                ));

                repository.save(new Client(
                        "CL-2",
                        "York Health",
                        "info@yorkh.ca",
                        "555-0302",
                        "client123"
                ));

                repository.save(new Client(
                        "CL-3",
                        "Ontario Infra",
                        "leads@oninfra.gov",
                        "555-0303",
                        "client123"
                ));

                System.out.println("✅ Users seeded.");
            }

            // seed services
            if (serviceRepository.count() == 0) {
                System.out.println("📝 Seeding services...");

                serviceRepository.save(
                        new Service("Consulting", "Intro session", 100.00)
                );

                serviceRepository.save(
                        new Service("Resume Review", "30-min review", 60.00)
                );

                serviceRepository.save(
                        new Service("Mock Interview", "45-min interview", 120.00)
                );

                System.out.println("✅ Services seeded.");
            }

        };
    }
}