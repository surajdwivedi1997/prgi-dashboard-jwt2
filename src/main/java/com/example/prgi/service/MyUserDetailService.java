package com.example.prgi.service;

import com.example.prgi.domain.User;
import com.example.prgi.domain.UserPrinciple;
import com.example.prgi.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailService implements UserDetailsService {

    @Autowired
    private UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("🔍 Trying to load user with email: " + email);

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> {
                    System.out.println("❌ User not found with email: " + email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });

        System.out.println("✅ User found: " + user.getEmail() + " | ID: " + user.getId());
        System.out.println("👉 Encoded password in DB: " + user.getPassword());
        System.out.println("Trying login for: " + email);
        System.out.println("Encoded password: " + user.getPassword());

        return new UserPrinciple(user);
    }
}