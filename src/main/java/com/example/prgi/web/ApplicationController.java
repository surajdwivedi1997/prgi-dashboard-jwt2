package com.example.prgi.web;

import com.example.prgi.repo.NewRegNewApplicationRepository;
import com.example.prgi.repo.NewRegDeficientApplicationRepository;
import com.example.prgi.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = "*")
public class ApplicationController {

    @Autowired
    private NewRegNewApplicationRepository newRepo;

    @Autowired
    private NewRegDeficientApplicationRepository defRepo;

    @Autowired
    private JwtService jwtService;

    @GetMapping("/summary")
    public Map<String, Object> summary(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpServletRequest request) {

        Map<String, Object> out = new LinkedHashMap<>();

        // ✅ Always extract role from JWT if token is valid
        String role = extractRole(request);

        LocalDate start = (startDate != null ? LocalDate.parse(startDate) : null);
        LocalDate end = (endDate != null ? LocalDate.parse(endDate) : null);

        // --- New Registration ---
        Map<String, Object> newReg = new LinkedHashMap<>();
        long newCount = newRepo.count();
        long defCount = defRepo.count();

        if ("ROLE_ADMIN".equals(role)) {
            newReg.put("New Applications (Response awaited from Specified Authority within 60 days window)", String.valueOf(newCount));
            newReg.put("Applications received from Specified Authority with/without comments after 60 days", "42");
            newReg.put("Deficient – Applications Response awaited from publishers", String.valueOf(defCount));
            newReg.put("Under Process at PRGI (Above ASO Level)", "235");
            newReg.put("Applications Rejected", "24+61 (Partial Reject)");
            newReg.put("Registration Granted", "270");
        } else {
            newReg.put("New Applications", newCount);
            newReg.put("Applications Received", 42);
            newReg.put("Deficient Applications", defCount);
            newReg.put("Under Process", 235);
            newReg.put("Rejected", "85");
            newReg.put("Granted", 270);
        }
        out.put("New Registration", newReg);

        // --- Other Modules (static data for now) ---
        out.put("New Edition", Map.of(
                "New Applications", "68",
                "Applications Received", "7",
                "Deficient Applications", "1",
                "Under Process", "61",
                "Rejected", "2",
                "Granted", "12"
        ));

        out.put("Revised Registration", Map.of(
                "New Applications", "50",
                "Applications Received", "34",
                "Deficient Applications", "17",
                "Under Process", "67",
                "Rejected", "15",
                "Granted", "103"
        ));

        out.put("Ownership Transfer", Map.of(
                "New Applications", "25",
                "Applications Received", "5",
                "Deficient Applications", "13",
                "Under Process", "21",
                "Rejected", "0",
                "Granted", "0"
        ));

        out.put("Discontinuation of Publication", Map.of(
                "New Applications", "0",
                "Applications Received", "0",
                "Deficient Applications", "0",
                "Under Process", "3",
                "Rejected", "0",
                "Granted", "0"
        ));

        out.put("Newsprint Declaration Authentication", Map.of(
                "New Applications", "0",
                "Applications Received", "9",
                "Deficient Applications", "1",
                "Under Process", "0",
                "Rejected", "0",
                "Granted", "5"
        ));

        return out;
    }

    // --- Admin-only APIs ---
    @GetMapping("/new-registration/new-applications")
    public ResponseEntity<?> getNewApplications(HttpServletRequest request) {
        String role = extractRole(request);
        if (!"ROLE_ADMIN".equals(role)) {
            // 🚫 Return proper HTTP 403
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied"));
        }
        return ResponseEntity.ok(newRepo.findAll());
    }

    @GetMapping("/new-registration/deficient")
    public ResponseEntity<?> getDeficientApplications(HttpServletRequest request) {
        String role = extractRole(request);
        if (!"ROLE_ADMIN".equals(role)) {
            // 🚫 Return proper HTTP 403
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied"));
        }
        return ResponseEntity.ok(defRepo.findAll());
    }

    // ✅ Centralized role extraction
    private String extractRole(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                return jwtService.extractRole(token);
            }
        } catch (Exception e) {
            return "ROLE_USER"; // fallback
        }
        return "ROLE_USER";
    }
}