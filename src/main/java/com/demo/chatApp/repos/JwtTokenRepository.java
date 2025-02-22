package com.demo.chatApp.repos;

import com.demo.chatApp.entities.JwtToken;
import com.demo.chatApp.entities.User;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JwtTokenRepository extends JpaRepository<JwtToken, UUID> {

    // Find token by value
    Optional<JwtToken> findByToken(String token);

    // Find all tokens for a user
    List<JwtToken> findByUser(User user);

    // Find all valid (non-revoked) tokens for a user
    List<JwtToken> findByUserAndRevokedFalse(User user);

    // Check if a token exists and is still valid
    boolean existsByTokenAndRevokedFalse(String token);

    // Delete expired tokens
    @Modifying
    @Transactional
    @Query("DELETE FROM JwtToken j WHERE j.token = :token")
    void deleteToken(@Param("token") String token);

    @Query("SELECT j FROM JwtToken j WHERE j.user.id = :userId")
    List<JwtToken> findByUserId(@Param("userId") UUID userId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO jwt_tokens (id, token, user_id, expiry_date, revoked) VALUES (:id, :token, :userId, :expiryDate, :revoked)", nativeQuery = true)
    void insertToken(
            @Param("id") UUID id,
            @Param("token") String token,
            @Param("userId") UUID userId,
            @Param("expiryDate") LocalDateTime expiryDate,
            @Param("revoked") boolean revoked);

    @Query("SELECT COUNT(j) > 0 FROM JwtToken j WHERE j.token = :token")
    boolean isTokenValid(@Param("token") String token);

    @Modifying
    @Query("DELETE FROM JwtToken jt WHERE jt.user.id = :userId")
    void deleteTokenByUserId(@Param("userId") UUID userId);

}
