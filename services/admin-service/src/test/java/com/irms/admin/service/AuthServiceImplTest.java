package com.irms.admin.service;

import com.irms.admin.domain.User;
import com.irms.admin.dto.RegisterRequest;
import com.irms.admin.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtService jwtService;
    
    @Mock
    private AuthenticationManager authenticationManager;
    
    @Mock
    private AuditLogger auditLogger;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void register_ShouldSaveUserWithEncodedPasswordAndReturnToken() {
        // Arrange
        RegisterRequest request = new RegisterRequest("newuser", "newuser@test.com", "plainPassword");
        
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        
        doAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        }).when(userRepository).save(any(User.class));
        
        when(jwtService.generateToken(any())).thenReturn("mockJwtToken");

        // Act
        var response = authService.register(request);

        // Assert
        assertNotNull(response);
        assertEquals("mockJwtToken", response.getToken());
        
        verify(passwordEncoder).encode("plainPassword");
        verify(userRepository).save(argThat(user -> 
            user.getUsername().equals("newuser") && 
            user.getPassword().equals("encodedPassword")
        ));
        verify(auditLogger).logAction(eq("REGISTER_SUCCESS"), eq("User"), any(), eq("newuser"), eq("New user registered"));
    }

    @Test
    void register_ShouldThrowExceptionWhenUsernameExists() {
        // Arrange
        RegisterRequest request = new RegisterRequest("existinguser", "test@test.com", "pw");
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }
}
