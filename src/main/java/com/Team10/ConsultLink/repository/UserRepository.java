package com.Team10.ConsultLink.repository;

import com.Team10.ConsultLink.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // This magic method will automatically write the SQL to find a user by name
    User findByName(String name);
}
