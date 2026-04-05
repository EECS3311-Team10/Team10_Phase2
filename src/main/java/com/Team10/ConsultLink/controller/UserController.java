package com.Team10.ConsultLink.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Team10.ConsultLink.repository.UserRepository;
import com.Team10.ConsultLink.users.Client;
import com.Team10.ConsultLink.users.Consultant;
import com.Team10.ConsultLink.users.User;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
        String userId = loginData.get("userId");
        String password = loginData.get("password");

        User user = userRepository.findByUserId(userId);

        if (user != null && user.getPassword() != null && user.getPassword().equals(password)) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    @PostMapping("/register")
    public User registerClient(@RequestBody Client client) {
        return userRepository.save(client);
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/consultants/{id}/approve")
    public ResponseEntity<?> approveConsultant(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        if (!(user instanceof Consultant consultant)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("User is not a consultant");
        }

        consultant.setApproved(true);
        return ResponseEntity.ok(userRepository.save(consultant));
    }

    @PutMapping("/consultants/{id}/reject")
    public ResponseEntity<?> rejectConsultant(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        if (!(user instanceof Consultant consultant)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("User is not a consultant");
        }

        consultant.setApproved(false);
        return ResponseEntity.ok(userRepository.save(consultant));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}