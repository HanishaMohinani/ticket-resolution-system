package com.ticketsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketsystem.dto.request.LoginRequest;
import com.ticketsystem.dto.request.RegisterRequest;
import com.ticketsystem.dto.response.AuthResponse;
import com.ticketsystem.entity.Company;
import com.ticketsystem.entity.CompanyTier;
import com.ticketsystem.entity.User;
import com.ticketsystem.entity.UserRole;
import com.ticketsystem.exception.BadRequestException;
import com.ticketsystem.repository.CompanyRepository;
import com.ticketsystem.repository.UserRepository;
import com.ticketsystem.security.JwtTokenProvider;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CompanyRepository companyRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }
        
        // Get or create company
        Company company = companyRepository.findByName(request.getCompanyName())
                .orElseGet(() -> {
                    Company newCompany = Company.builder()
                            .name(request.getCompanyName())
                            .tier(CompanyTier.FREE)
                            .ticketLimitPerDay(100)
                            .isActive(true)
                            .build();
                    return companyRepository.save(newCompany);
                });
        
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .company(company)
                .role(UserRole.valueOf(request.getRole().toUpperCase()))
                .isActive(true)
                .build();
        
        user = userRepository.save(user);
        
        String token = jwtTokenProvider.generateTokenFromUsername(user.getEmail());
        
        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .companyId(company.getId())
                .companyName(company.getName())
                .build();
    }
    
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        String token = jwtTokenProvider.generateToken(authentication);
        
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found"));
        
        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .companyId(user.getCompany().getId())
                .companyName(user.getCompany().getName())
                .build();
    }
    
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));
    }
}