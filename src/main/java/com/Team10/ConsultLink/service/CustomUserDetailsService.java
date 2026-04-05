package com.Team10.ConsultLink.service;

import com.Team10.ConsultLink.users.*;
import com.Team10.ConsultLink.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Look for "AD-1" in your DB
       User user = userRepository.findByUserId(username);
if (user == null) {
    throw new UsernameNotFoundException("User not found");
}

        // Return a Spring Security 'User' object
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUserId())
                .password(user.getPassword()) // "admin123"
                .roles(user.getRole())        // "ADMIN"
                .build();
    }
}
