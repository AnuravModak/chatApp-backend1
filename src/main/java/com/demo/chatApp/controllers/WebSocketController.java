package com.demo.chatApp.controllers;

import com.demo.chatApp.repos.JwtTokenRepository;
import com.demo.chatApp.repos.UserRepository;
import com.demo.chatApp.services.JwtTokenUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/chat")
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final JwtTokenUtil jwtTokenUtil;
    private final JwtTokenRepository jwtTokenRepository;
    private final UserRepository userRepository;
    private final Set<String> onlineUsers = ConcurrentHashMap.newKeySet();

    public WebSocketController(SimpMessagingTemplate messagingTemplate,
                               JwtTokenUtil jwtTokenUtil,
                               JwtTokenRepository jwtTokenRepository,
                               UserRepository userRepository) {
        this.messagingTemplate = messagingTemplate;
        this.jwtTokenUtil = jwtTokenUtil;
        this.jwtTokenRepository = jwtTokenRepository;
        this.userRepository = userRepository;
    }

    @MessageMapping("/user-online")
    @SendTo("/topic/online-users")
    public Set<String> handleUserOnline(Message<String> message) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        String payload = message.getPayload();
        System.out.println("Received Payload: " + payload);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(payload);
            String username = jsonNode.get("username").asText();

            // ✅ Ensure session attributes exist
            if (accessor.getSessionAttributes() == null) {
                accessor.setSessionAttributes(new HashMap<>());
            }
            accessor.getSessionAttributes().put("username", username);
            System.out.println("Username stored in session: " + username);

            // ✅ Step 1: Get `userId` (UUID) from the `users` table using `username`
            String userIdStr = userRepository.findUserIdByUsername(username);
            if (userIdStr == null) {
                System.out.println("❌ User ID not found for username: " + username);
                return onlineUsers;
            }

            UUID userId = UUID.fromString(userIdStr);

            // ✅ Step 2: Fetch JWT token using `userId`
            String jwtToken = jwtTokenRepository.findByUserId(userId).toString();
            if (jwtToken == null) {
                System.out.println("❌ JWT token not found for user: " + username);
                return onlineUsers;
            }


            // ✅ Step 4: Mark user as online
            onlineUsers.add(username);
            userRepository.updateOnlineStatus(username, true);

            System.out.println("✅ Username: " + username + " is now online.");
            messagingTemplate.convertAndSend("/topic/online-users", onlineUsers);

            return onlineUsers;
        } catch (Exception e) {
            System.err.println("❌ Failed to process user login: " + e.getMessage());
            return onlineUsers; // Ensure return even in case of failure
        }
    }




    // 🔴 Handle user disconnect (WebSocket closed/browser closed)
    @EventListener
    public void handleUserDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) accessor.getSessionAttributes().get("username");

        if (username != null) {
            onlineUsers.remove(username);
            userRepository.updateOnlineStatus(username, false); // 🔴 Mark user offline
            messagingTemplate.convertAndSend("/topic/online-users", onlineUsers);
        }
    }

    // 🔵 Handle manual logout via REST API
    @PostMapping("/logout")
    public ResponseEntity<?> handleLogout(@RequestHeader("Authorization") String token) {
        if (token != null) {
            token = token.replace("Bearer ", "");

            if (jwtTokenUtil.validateToken(token)) {
                String username = jwtTokenUtil.getUsernameFromToken(token);

                // 🔴 Remove from online users
                onlineUsers.remove(username);
                messagingTemplate.convertAndSend("/topic/online-users", onlineUsers);

                // 🔴 Delete token from DB
                jwtTokenRepository.deleteToken(token);

                // 🔴 Mark user as offline
                userRepository.updateOnlineStatus(username, false);
            }
        }
        return ResponseEntity.ok().body("Logged out successfully.");
    }
}
