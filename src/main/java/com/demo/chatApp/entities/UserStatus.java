package com.demo.chatApp.entities;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatus {
    private UUID id;
    private boolean isOnline;

    public UserStatus() {
    }

    public UserStatus(UUID id, boolean isOnline) {
        this.id = id;
        this.isOnline = isOnline;
    }
}
