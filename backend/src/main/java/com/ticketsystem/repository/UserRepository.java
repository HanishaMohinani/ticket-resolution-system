package com.ticketsystem.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ticketsystem.entity.User;
import com.ticketsystem.entity.UserRole;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByCompanyId(Long companyId);
    List<User> findByCompanyIdAndRole(Long companyId, UserRole role);
    List<User> findByRole(UserRole role);
}