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
    public MessageController(MessageService messageService, MessageRepository messageRepository){

        this.messageService=messageService;
        this.messageRepository= messageRepository;
    }

    @PostMapping("/api/chat/send")
    public ResponseEntity<?> sendMessage(@RequestBody MessageDTO messageDTO) {
        try {
            UUID messageId = UUID.randomUUID();

            System.out.println("Sender: " + messageDTO.getSender() + " | Receiver: " + messageDTO.getReceiver());

            messageRepository.insertMessage(
                    messageId,
                    messageDTO.getSender(),
                    messageDTO.getReceiver(),
                    messageDTO.getContent(),
                    LocalDateTime.now(),
                    false,
                    MessageStatus.SENT.toString()

            );

            // Send message via WebSocket
            messagingTemplate.convertAndSendToUser(
                    messageDTO.getReceiver().toString(),
                    "/queue/messages",
                    messageDTO
            );

            return ResponseEntity.ok("Message sent successfully with ID: " + messageId);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error sending message: " + e.getMessage());
        }
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
