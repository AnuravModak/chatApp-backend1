package com.demo.chatApp.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor

public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;


    private String oauthProvider;
    private String oauthId;
    @Column(name = "is_online", nullable = false)
    private boolean isOnline = false;

    @Column(name = "last_seen")
    private LocalDateTime lastSeen;

    @Enumerated(EnumType.STRING)
    @Column(name = "typing_status", nullable = false)
    private TypingStatus typingStatus = TypingStatus.NOT_TYPING;

    public User() {
    }

    public User(UUID id, String username, String email, String password, LocalDateTime createdAt, String oauthProvider, String oauthId, boolean isOnline, LocalDateTime lastSeen) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.createdAt = createdAt;
        this.oauthProvider = oauthProvider;
        this.oauthId = oauthId;
        this.isOnline = isOnline;
        this.lastSeen = lastSeen;

    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getOauthProvider() {
        return oauthProvider;
    }

    public void setOauthProvider(String oauthProvider) {
        this.oauthProvider = oauthProvider;
    }

    public String getOauthId() {
        return oauthId;
    }

    public void setOauthId(String oauthId) {
        this.oauthId = oauthId;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public LocalDateTime getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(LocalDateTime lastSeen) {
        this.lastSeen = lastSeen;
    }

    public TypingStatus getTypingStatus() {
        return typingStatus;
    }

    public void setTypingStatus(TypingStatus typingStatus) {
        this.typingStatus = typingStatus;
    }
}
