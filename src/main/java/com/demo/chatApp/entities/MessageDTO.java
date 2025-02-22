package com.demo.chatApp.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {

    private UUID sender;
    private UUID receiver;
    private String content;
    private MessageStatus status;

    public MessageDTO() {
    }

    public MessageDTO(UUID sender, UUID receiver, String content, MessageStatus status) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.status = status;
    }

    public UUID getSender() {
        return sender;
    }

    public void setSender(UUID sender) {
        this.sender = sender;
    }

    public UUID getReceiver() {
        return receiver;
    }

    public void setReceiver(UUID receiver) {
        this.receiver = receiver;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }
}
