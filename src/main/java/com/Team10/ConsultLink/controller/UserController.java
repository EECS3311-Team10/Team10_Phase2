package com.Team10.ConsultLink.controller;

import com.Team10.ConsultLink.users.*;
import com.Team10.ConsultLink.respository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*") // Allows your partner to connect from their local dev environment
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // GET all users (Admins and Clients)
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // GET a specific user by ID
    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userRepository.findById(id).orElse(null);
    }

    // Create a new Client from the Frontend
@PostMapping("/register")
    public User registerClient(@RequestBody Client client) {
    // This takes the JSON from the frontend and saves it to Postgres
    return userRepository.save(client);
}
}
