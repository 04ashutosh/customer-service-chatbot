package com.enterprise.chatbot.service;

import com.enterprise.chatbot.dto.AuthResponse;
import com.enterprise.chatbot.dto.LoginRequest;
import com.enterprise.chatbot.dto.RegisterRequest;
import com.enterprise.chatbot.model.Company;
import com.enterprise.chatbot.model.User;
import com.enterprise.chatbot.repository.CompanyRepository;
import com.enterprise.chatbot.repository.UserRepository;
import com.enterprise.chatbot.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if company already exists
        if (companyRepository.existsByCompanyName(request.getCompanyName())) {
            throw new RuntimeException("Company name already exists");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        // Create company
        Company company = new Company();
        company.setCompanyName(request.getCompanyName());
        company.setDomain(request.getDomain());
        company.setStatus(Company.CompanyStatus.ACTIVE);
        company = companyRepository.save(company);
        
        // Create admin user
        User user = new User();
        user.setCompanyId(company.getCompanyId());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.UserRole.ADMIN);
        user = userRepository.save(user);
        
        // Generate JWT token
        String token = jwtTokenProvider.generateToken(
                user.getEmail(),
                user.getCompanyId(),
                user.getRole().name()
        );
        
        return new AuthResponse(
                token,
                user.getEmail(),
                user.getRole().name(),
                user.getCompanyId(),
                company.getCompanyName(),
                "Registration successful"
        );
    }
    
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        
        // Get user details
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Company company = companyRepository.findById(user.getCompanyId())
                .orElseThrow(() -> new RuntimeException("Company not found"));
        
        // Generate JWT token
        String token = jwtTokenProvider.generateToken(
                user.getEmail(),
                user.getCompanyId(),
                user.getRole().name()
        );
        
        return new AuthResponse(
                token,
                user.getEmail(),
                user.getRole().name(),
                user.getCompanyId(),
                company.getCompanyName(),
                "Login successful"
        );
    }
}
