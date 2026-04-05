package com.Team10.ConsultLink.users;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Database primary key (Internal use)

    @Column(unique = true, nullable = false)
    private String userId; // The "CL-1" / "AD-1" from your UML

    private String name;
    private String email;
    private String phone;
    private String role;
    
    // Implementation detail for Phase 2 login functionality
    private String password;

    // Default Constructor (Required by JPA)
    public User() {}

    // Constructor for Seeding Data
    public User(String userId, String name, String email, String phone, String role, String password) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.password = password;
    }

    // --- Getters and Setters ---

    public Long getId() { return id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}