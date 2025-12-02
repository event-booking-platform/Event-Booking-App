package com.eventbooking.controller;

import com.eventbooking.dto.AuthResponse;
import com.eventbooking.dto.LoginRequest;
import com.eventbooking.entity.User;
import com.eventbooking.entity.UserRole;
import com.eventbooking.service.UserService;
import com.eventbooking.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = userService.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String jwt = jwtUtil.generateToken(user.getUsername());

        return ResponseEntity
                .ok(new AuthResponse(jwt, user.getId(), user.getUsername(), user.getEmail(), user.getRole()));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody User user) {
        try {
            System.out.println("Received user: " + user.getUsername());
            System.out.println("Password received: " + (user.getPassword() != null ? "NOT NULL" : "NULL"));
            User registeredUser = userService.registerUser(user);
            String jwt = jwtUtil.generateToken(registeredUser.getUsername());

            return ResponseEntity.ok(new AuthResponse(jwt, registeredUser.getId(),
                    registeredUser.getUsername(), registeredUser.getEmail(), registeredUser.getRole()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/register/admin")
    public ResponseEntity<?> registerAdmin(@Valid @RequestBody User user) {
        try {

            User registeredUser = userService.registerUser(user, UserRole.ROLE_ADMIN);
            String jwt = jwtUtil.generateToken(registeredUser.getUsername());

            return ResponseEntity.ok(new AuthResponse(jwt, registeredUser.getId(),
                    registeredUser.getUsername(), registeredUser.getEmail(), registeredUser.getRole()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}