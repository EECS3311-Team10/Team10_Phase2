package com.Team10.ConsultLink.users;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")

@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_type", discriminatorType = DiscriminatorType.STRING)
@Getter @Setter 
@NoArgsConstructor // Required for JPA to work

public abstract class User {
  
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    protected Long id;
    protected String userId;
    private String name;

    private String email;
    private String phone;
    private String role;
 
    protected User(String name, String email, String phone, String role) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
    }


    
    public void setName(String name) {
        this.name = name;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public void setRole(String role) {
        this.role = role;
    }

    public String getUserID() {
        return userId;
    }
    public String getName() {
        return name;
    }
    public String getEmail() {
        return email;
    }
    public String getPhone() {
        return phone;
    }
    public String getRole() {
        return role;
    }
}
