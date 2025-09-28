package com.example.prgi.web;

import com.example.prgi.domain.User;
import com.example.prgi.service.JwtService;
import com.example.prgi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class UserRegistrationController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @PostMapping("/regis")
    public ResponseEntity<?> registration(@RequestBody User user) {
        if (userService.existsByEmail(user.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("❌ Email already registered!");
        }

        // Always assign ROLE_USER
        user.setRole("ROLE_USER");

        // Encode password
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        User savedUser = userService.save(user);

        return ResponseEntity.ok(savedUser);
    }
    @PostMapping("/admin/regis")
    public ResponseEntity<?> registerAdmin(@RequestBody User user) {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_ADMIN");  // force admin
        User savedUser = userService.save(user);
        return ResponseEntity.ok(savedUser);
    }


    @GetMapping("/userlist")
    public List<User> getAllUsers() {
        return userService.find();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> userMap) {
        String email = userMap.get("email");
        String password = userMap.get("password");

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        if (authentication.isAuthenticated()) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Add role claim
            Map<String, Object> claims = new HashMap<>();
            boolean isAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (isAdmin) {
                claims.put("role", "ROLE_ADMIN");
            } else {
                claims.put("role", "ROLE_USER");
            }

            String token = jwtService.generateToken(claims, userDetails);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("email", userDetails.getUsername());
            response.put("role", claims.get("role"));

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }
}