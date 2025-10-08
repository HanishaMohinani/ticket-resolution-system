package com.ticketsystem.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ticketsystem.entity.RateLimitBucket;

@Repository
public interface RateLimitBucketRepository extends JpaRepository<RateLimitBucket, Long> {
    Optional<RateLimitBucket> findByBucketKey(String bucketKey);
    void deleteByBucketKey(String bucketKey);
}