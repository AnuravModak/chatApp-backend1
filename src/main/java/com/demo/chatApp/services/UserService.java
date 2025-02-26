package com.demo.chatApp.services;

import com.demo.chatApp.entities.*;
import com.demo.chatApp.exceptions.EmailAlreadyInUseException;
import com.demo.chatApp.exceptions.InvalidCredentialsException;
import com.demo.chatApp.exceptions.UsernameAlreadyInUseException;
import com.demo.chatApp.repos.JwtTokenRepository;
import com.demo.chatApp.repos.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenRepository jwtTokenRepository;

    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private  JwtTokenUtil jwtUtil;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    public UserService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = (BCryptPasswordEncoder) passwordEncoder;
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByUserId(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            return userRepository.findUserById(uuid);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid UUID format: " + id);
            return Optional.empty();
        }
    }

    public List<User> findAllUser(){
        return  userRepository.findAll();
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public User registerUser(User user) {
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            throw new EmailAlreadyInUseException("Email already in use.");
        }

        Optional<User> existingUsername = userRepository.findByUsername(user.getUsername());
        if (existingUsername.isPresent()) {
            throw new UsernameAlreadyInUseException("Username already in use.");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Transactional
    public String loginUser(String username, String password) {
        try {
            Optional<User> userOpt = userRepository.findByUsername(username);

            if (userOpt.isPresent() && passwordEncoder.matches(password, userOpt.get().getPassword())) {
                User user = userOpt.get();
                String token = jwtUtil.generateToken(user);

                // Calculate token expiry (e.g., 1 day from now)
                LocalDateTime expiryDate = LocalDateTime.now().plusDays(1);
                UUID tokenId = UUID.randomUUID();

                // 🔹 Insert token into the database
                jwtTokenRepository.insertToken(tokenId, token, user.getId(), expiryDate, false);

                // 🔹 Update user status to ONLINE
                userRepository.updateOnlineStatus(username, true);

                return token;
            }

            throw new InvalidCredentialsException("Invalid username or password.");
        } catch (Exception e) {
            System.err.println("Error during login: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Login process failed. Please try again later.");
        }
    }



    public User oauthLogin(String email, String provider, String oauthId) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            return userOpt.get();
        } else {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setOauthProvider(provider);
            newUser.setOauthId(oauthId);
            return userRepository.save(newUser);  // Add this line
        }

    }

    @Transactional
    public void logoutUser(String username) {
        try {
            Optional<User> userOpt = userRepository.findByUsername(username);

            if (userOpt.isPresent()) {
                User user = userOpt.get();

                // 🔹 Delete JWT for this user
                jwtTokenRepository.deleteTokenByUserId(user.getId());

                // 🔹 Mark user as offline
                userRepository.updateOnlineStatus(username, false);
            }
        } catch (Exception e) {
            System.err.println("Error during logout: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Logout process failed. Please try again later.");
        }
    }

    // Update user profile
    public User updateUserProfile(User user) {
        return userRepository.save(user);
    }

    // Validate JWT Token
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

}
