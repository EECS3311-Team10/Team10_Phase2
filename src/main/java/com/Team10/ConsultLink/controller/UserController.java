package com.Team10.ConsultLink.controller;

import com.Team10.ConsultLink.repository.UserRepository;
import com.Team10.ConsultLink.users.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*") 
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // 1. LOGIN: Checks database for matching username/password
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");

        User user = userRepository.findByUserId(username);

        if (user != null && user.getPassword().equals(password)) {
            // Return the full user object (including role) to the frontend
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    // 2. REGISTER: Saves a new Client to Postgres
    @PostMapping("/register")
    public User registerClient(@RequestBody Client client) {
        return userRepository.save(client);
    }

    // 3. GET ALL: Returns all users (Admins and Clients)
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 4. GET BY ID: Finds a specific user
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 5. DELETE: Removes a user by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
