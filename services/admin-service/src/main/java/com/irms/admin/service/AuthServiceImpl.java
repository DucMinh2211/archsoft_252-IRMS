package com.irms.admin.service;

import com.irms.admin.domain.User;
import com.irms.admin.dto.AuthRequest;
import com.irms.admin.dto.AuthResponse;
import com.irms.admin.dto.RegisterRequest;
import com.irms.admin.exception.DuplicateUserException;
import com.irms.admin.repository.UserRepository;
import com.irms.admin.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuditLogger auditLogger;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateUserException("Username is already taken");
        }
        
        var user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setActive(true);
        
        userRepository.save(user);

        auditLogger.logAction("REGISTER_SUCCESS", "User", user.getId() != null ? user.getId().toString() : null, user.getUsername(), "New user registered");

        var jwtToken = jwtService.generateToken(new CustomUserDetails(user));
        return AuthResponse.builder()
                .token(jwtToken)
                .build();
    }

    @Override
    public AuthResponse authenticate(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow();
                
        var jwtToken = jwtService.generateToken(new CustomUserDetails(user));
        
        auditLogger.logAction("LOGIN_SUCCESS", "User", user.getId().toString(), user.getUsername(), "User logged in successfully");
        
        return AuthResponse.builder()
                .token(jwtToken)
                .build();
    }
    
}
