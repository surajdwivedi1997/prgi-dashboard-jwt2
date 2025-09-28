package com.example.prgi.service;

import com.example.prgi.domain.User;
import com.example.prgi.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepo userRepo;

    public User save(User user) {
        return userRepo.save(user);
    }

    public List<User> find() {
        return userRepo.findAll();
    }

    public boolean existsByEmail(String email) {
        return userRepo.findByEmail(email).isPresent();
    }

    // ✅ Add this
    public Optional<User> findByEmail(String email) {
        return userRepo.findByEmail(email);
    }
}