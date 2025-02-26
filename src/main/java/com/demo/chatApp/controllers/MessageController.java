package com.demo.chatApp.controllers;

import com.demo.chatApp.entities.MessageDTO;
import com.demo.chatApp.entities.MessageStatus;
import com.demo.chatApp.entities.Messages;
import com.demo.chatApp.repos.MessageRepository;
import com.demo.chatApp.services.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Controller
public class MessageController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private final MessageService messageService;
    private final MessageRepository messageRepository;

    @Autowired
    public MessageController(MessageService messageService, MessageRepository messageRepository) {
        this.messageService = messageService;
        this.messageRepository = messageRepository;
    }


    // ✅ Keep WebSocket for real-time messaging
    @MessageMapping("/chat.privateMessage")
    @SendTo("/user/queue/messages")
    public void privateMessage(MessageDTO message) {
        UUID messageId = UUID.randomUUID();
        LocalDateTime timestamp = LocalDateTime.now();

        System.out.println("Sender: " + message.getSender() + " | Receiver: " + message.getReceiver());
        System.out.println("Sending the message out...");

        // Send message to the specific user via WebSockets
        messagingTemplate.convertAndSendToUser(
                message.getReceiver().toString(),
                "/queue/messages",
                message
        );

        System.out.println("sent out message already!!");
    }


    // ✅ Add back HTTP POST for saving messages
    @PostMapping("/api/chat/send")
    public ResponseEntity<String> saveMessage(@RequestBody MessageDTO message) {
        UUID messageId = UUID.randomUUID();
        LocalDateTime timestamp = LocalDateTime.now();

        System.out.println("Saving message from: " + message.getSender() + " to " + message.getReceiver());

        // Save message to DB
        messageRepository.insertMessage(
                messageId,
                message.getSender(),
                message.getReceiver(),
                message.getContent(),
                timestamp,
                false,
                MessageStatus.SENT.toString()
        );

        return ResponseEntity.ok("Message saved successfully!");
    }

    @GetMapping("/admin/getMessages/{senderId}/{receiverId}")
    public ResponseEntity<?> getChats(@PathVariable UUID senderId, @PathVariable UUID receiverId) {

        try {
            System.out.println("Fetching chat history between: " + senderId + " and " + receiverId);

            List<Messages> messages = messageService.getChats(senderId, receiverId);

            System.out.println("Chat history retrieved successfully: " + messages.size() + " messages found.");

            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            System.err.println("Error retrieving chat history: " + e.getMessage());
            e.printStackTrace(); // Log full stack trace

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while fetching chat history: " + e.getMessage());
        }
    }
}
