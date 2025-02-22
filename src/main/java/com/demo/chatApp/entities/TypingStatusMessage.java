package com.demo.chatApp.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TypingStatusMessage {
    private UUID senderId;
    private TypingStatus status;

    public TypingStatusMessage() {
    }

    public TypingStatusMessage(UUID senderId, TypingStatus status) {
        this.senderId = senderId;
        this.status = status;
    }

    public UUID getSenderId() {
        return senderId;
    }

    public void setSenderId(UUID senderId) {
        this.senderId = senderId;
    }

    public TypingStatus getStatus() {
        return status;
    }

    public void setStatus(TypingStatus status) {
        this.status = status;
    }
}
