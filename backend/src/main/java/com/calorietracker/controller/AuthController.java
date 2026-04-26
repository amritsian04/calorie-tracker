package com.calorietracker.controller;

import com.calorietracker.model.User;
import com.calorietracker.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Hash password with SHA-256 (no external crypto library needed)
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 unavailable", e);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body, HttpSession session) {
        String firstName = body.get("firstName");
        String lastName  = body.get("lastName");
        String email     = body.get("email");
        String password  = body.get("password");

        if (firstName == null || lastName == null || email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "All fields are required"));
        }

        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.status(409).body(Map.of("error", "Email already registered"));
        }

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPasswordHash(hashPassword(password));

        User saved = userRepository.save(user);

        // Log the new user in immediately by storing their ID in the session
        session.setAttribute("userId", saved.getUserId());

        return ResponseEntity.ok(Map.of(
                "userId",    saved.getUserId(),
                "firstName", saved.getFirstName(),
                "lastName",  saved.getLastName(),
                "email",     saved.getEmail()
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpSession session) {
        String email    = body.get("email");
        String password = body.get("password");

        if (email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email and password are required"));
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        User user = userOpt.get();
        if (!user.getPasswordHash().equals(hashPassword(password))) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        session.setAttribute("userId", user.getUserId());

        return ResponseEntity.ok(Map.of(
                "userId",    user.getUserId(),
                "firstName", user.getFirstName(),
                "lastName",  user.getLastName(),
                "email",     user.getEmail()
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        User user = userOpt.get();
        return ResponseEntity.ok(Map.of(
                "userId",    user.getUserId(),
                "firstName", user.getFirstName(),
                "lastName",  user.getLastName(),
                "email",     user.getEmail()
        ));
    }
}
