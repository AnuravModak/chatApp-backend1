package com.demo.chatApp.repos;

import com.demo.chatApp.entities.MessageStatus;
import com.demo.chatApp.entities.Messages;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Messages, UUID> {
    @Query("SELECT m FROM Messages m WHERE (m.sender = :user1 AND m.receiver = :user2) OR (m.sender = :user2 AND m.receiver = :user1) ORDER BY m.timestamp ASC")
    List<Messages> findChatHistory(@Param("user1") UUID user1, @Param("user2") UUID user2);

    List<Messages> findBySenderOrReceiver(UUID sender, UUID receiver);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO messages (id, sender_id, receiver_id, content, timestamp, is_read, status) " +
            "VALUES (:id, :sender, :receiver, :content, :timestamp, :isRead, :status)",
            nativeQuery = true)
    void insertMessage(@Param("id") UUID id,
                       @Param("sender") UUID sender,
                       @Param("receiver") UUID receiver,
                       @Param("content") String content,
                       @Param("timestamp") LocalDateTime timestamp,
                       @Param("isRead") boolean isRead,
                       @Param("status") String status);
}
