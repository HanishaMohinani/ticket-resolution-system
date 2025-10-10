package com.ticketsystem.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rate_limit_buckets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RateLimitBucket {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String identifier;
    
    /**
     * Unique key combining identifier and action
     * Example: "user_123_create_ticket", "user_456_add_comment"
     */
    @Column(name = "bucket_key", nullable = false, unique = true)
    private String bucketKey;

    @Column(name = "tokens_remaining", nullable = false)
    private Integer tokensRemaining;
    
    @Column(name = "max_tokens", nullable = false)
    private Integer maxTokens;
    
    @Column(name = "refill_rate", nullable = false)
    private Integer refillRate;

    @Column(name = "window_duration_seconds", nullable = false)
    private Integer windowDurationSeconds;
    
    @Column(name = "last_refill_at", nullable = false)
    private LocalDateTime lastRefillAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
        if (this.lastRefillAt == null) {
            this.lastRefillAt = now;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public boolean shouldRefill() {
        if (lastRefillAt == null) {
            lastRefillAt = LocalDateTime.now();
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        long secondsElapsed = java.time.Duration.between(lastRefillAt, now).getSeconds();
        return secondsElapsed >= windowDurationSeconds;
    }
    
    public void refill() {
        if (lastRefillAt == null) {
            lastRefillAt = LocalDateTime.now();
            return;
        }
        
        LocalDateTime now = LocalDateTime.now();
        long secondsElapsed = java.time.Duration.between(lastRefillAt, now).getSeconds();
        
        if (secondsElapsed < windowDurationSeconds) {
            return;
        }
        
        long windowsPassed = secondsElapsed / windowDurationSeconds;
        
        if (windowsPassed > 0) {
            int tokensToAdd = (int) (windowsPassed * refillRate);
            this.tokensRemaining = Math.min(tokensRemaining + tokensToAdd, maxTokens);
            this.lastRefillAt = lastRefillAt.plusSeconds(windowsPassed * windowDurationSeconds);
        }
    }
    
    public boolean consumeToken() {
        refill();
        
        if (tokensRemaining > 0) {
            tokensRemaining--;
            return true;
        }
        return false;
    }
}