package com.Team10.ConsultLink.model;

import jakarta.persistence.Column; // Required for JPA
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "services")
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Use Long and let JPA handle the incrementing

    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private double price;

    // 1. Required Default Constructor for JPA
    public Service() {}

    // 2. Updated Constructor (Removed manual ID counter)
    public Service(String name, String description, double price) {
        this.name = name;
        this.description = description;
        this.price = price;
    }

    // --- Getters and Setters ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
