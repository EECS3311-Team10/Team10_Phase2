package com.Team10.ConsultLink.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

        if (user != null && user.getPassword() != null &&
            user.getPassword().equals(password)) {

            // block consultant if not approved
            if (user instanceof Consultant consultant) {
                if (!consultant.isApproved()) {
                    return ResponseEntity
                            .status(HttpStatus.FORBIDDEN)
                            .body("Consultant not approved yet");
                }
            }

            return ResponseEntity.ok(user);
        }

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Invalid credentials");
    }

    // REGISTER CLIENT OR CONSULTANT
    @PostMapping("/register")
    public User register(@RequestBody Map<String, String> data) {

        String role = data.get("role");

        if ("CONSULTANT".equalsIgnoreCase(role)) {

            Consultant consultant = new Consultant(
                    data.get("userId"),
                    data.get("name"),
                    data.get("email"),
                    data.get("phone"),
                    data.get("password")
            );

            consultant.setApproved(false); // admin must approve
            return userRepository.save(consultant);
        }

        Client client = new Client(
                data.get("userId"),
                data.get("name"),
                data.get("email"),
                data.get("phone"),
                data.get("password")
        );

        return userRepository.save(client);
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PutMapping("/consultants/{id}/approve")
    public ResponseEntity<?> approveConsultant(@PathVariable Long id) {

        User user = userRepository.findById(id).orElse(null);

        if (!(user instanceof Consultant consultant)) {
            return ResponseEntity.badRequest().body("Not consultant");
        }

        consultant.setApproved(true);
        return ResponseEntity.ok(userRepository.save(consultant));
    }

    @PutMapping("/consultants/{id}/reject")
    public ResponseEntity<?> rejectConsultant(@PathVariable Long id) {

        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found");
        }

        if (!(user instanceof Consultant)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("User is not a consultant");
        }

        userRepository.deleteById(id);

        return ResponseEntity.ok("Consultant registration rejected and deleted");
    }
}