package com.example.caloriecounter.repos;

import com.example.caloriecounter.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    public User findBySessionId(String sessionId);

    public User findByEmail(String email);
}
