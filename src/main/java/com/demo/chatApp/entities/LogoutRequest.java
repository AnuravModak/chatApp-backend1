package com.demo.chatApp.entities;

public class LogoutRequest {
    private String username;

    public LogoutRequest() {
    }

    public LogoutRequest(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
