package com.demo.chatApp.services;

import com.demo.chatApp.entities.MessageDTO;
import com.demo.chatApp.entities.Messages;
import com.demo.chatApp.repos.MessageRepository;
import com.fasterxml.jackson.databind.deser.std.UUIDDeserializer;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;


//    @Autowired
//    private RedisTemplate<String, Object> redisTemplate;  // RedisTemplate for publishing messages

    @Autowired
    private SimpMessagingTemplate messagingTemplate;  // Spring's template to send WebSocket messages

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    public List<Messages> getMessages(UUID userId) {
        return messageRepository.findBySenderOrReceiver(userId, userId);
    }

    public void markMessageAsRead(UUID messageId){
        Optional<Messages> messagesOptional=messageRepository.findById(messageId);
        if (messagesOptional.isPresent()){
            Messages message= messagesOptional.get();
            message.setRead(true);
            messageRepository.save(message);
            messagingTemplate.convertAndSendToUser(message.getSender().toString(),"/queue/read-receipts",messageId);
        }
    }

    public Messages save(Messages message) {
        if (message.getTimestamp()==null){
            message.setTimestamp(LocalDateTime.now());
        }

        if (message.isRead()==false){
            message.setRead(false);
        }
        // Save message to the database
        Messages savedMessage = messageRepository.save(message);

//        redisTemplate.convertAndSend("chatRoom: "+message.getReceiver(), savedMessage);

        messagingTemplate.convertAndSendToUser(message.getReceiver().toString(),
                "/queue/messages",
                savedMessage
                );

        return savedMessage;
    }
    // Method to handle JWT validation for WebSocket connections
    public boolean validateJwt(String token) {
        return jwtTokenUtil.validateToken(token);  // Use your JWT utility to validate the token
    }

    public List<Messages> getChats( UUID senderId, UUID receiverId){
        List<Messages> messages=messageRepository.findChatHistory(senderId, receiverId);
        System.out.println("Inside message service..");
        return messages;
    }
}
