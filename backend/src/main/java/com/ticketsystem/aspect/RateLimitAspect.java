package com.ticketsystem.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ticketsystem.annotation.RateLimited;
import com.ticketsystem.entity.RateLimitBucket;
import com.ticketsystem.entity.User;
import com.ticketsystem.exception.RateLimitExceededException;
import com.ticketsystem.repository.RateLimitBucketRepository;
import com.ticketsystem.repository.UserRepository;

@Aspect
@Component
public class RateLimitAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(RateLimitAspect.class);
    
    @Autowired
    private RateLimitBucketRepository rateLimitBucketRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Around("@annotation(rateLimited)")
    @Transactional
    public Object checkRateLimit(ProceedingJoinPoint joinPoint, RateLimited rateLimited) throws Throwable {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return joinPoint.proceed();
        }
        
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        String identifier = rateLimited.byUser() ? 
                "user_" + user.getId() : 
                "company_" + user.getCompany().getId();
        
        String bucketKey = identifier + "_" + rateLimited.action();
        
        RateLimitBucket bucket = rateLimitBucketRepository.findByBucketKey(bucketKey)
                .orElseGet(() -> createNewBucket(identifier, bucketKey, rateLimited));
        
        boolean allowed = bucket.consumeToken();
        
        if (!allowed) {
            int retryAfter = bucket.getWindowDurationSeconds();
            logger.warn("Rate limit exceeded for user: {} on action: {}", email, rateLimited.action());
            
            throw new RateLimitExceededException(
                String.format("Rate limit exceeded. Maximum %d requests per %d seconds allowed.", 
                    rateLimited.maxRequests(), rateLimited.windowSeconds()),
                retryAfter
            );
        }
        rateLimitBucketRepository.save(bucket);
        
        logger.debug("Rate limit check passed for user: {} on action: {}. Tokens remaining: {}", 
            email, rateLimited.action(), bucket.getTokensRemaining());
        return joinPoint.proceed();
    }

    private RateLimitBucket createNewBucket(String identifier, String bucketKey, RateLimited rateLimited) {
        return RateLimitBucket.builder()
                .identifier(identifier)
                .bucketKey(bucketKey)
                .tokensRemaining(rateLimited.maxRequests())
                .maxTokens(rateLimited.maxRequests())
                .refillRate(rateLimited.maxRequests())
                .windowDurationSeconds(rateLimited.windowSeconds())
                .build();
    }
}