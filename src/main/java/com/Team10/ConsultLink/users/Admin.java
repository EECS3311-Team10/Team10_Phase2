package com.Team10.ConsultLink.users;

import jakarta.persistence.Entity;

@Entity
public class Admin extends User {

    public Admin() { super(); }

    public Admin(String userId, String name, String email, String phone, String password) {
        super(userId, name, email, phone, "ADMIN", password);
    }

    public void defineSystemPolicies() {
        // Logic for system policies
    }
}
