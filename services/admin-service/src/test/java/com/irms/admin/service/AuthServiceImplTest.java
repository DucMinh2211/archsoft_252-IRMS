package com.irms.admin.service;

import com.irms.admin.domain.User;
import com.irms.admin.dto.RegisterRequest;
import com.irms.admin.exception.DuplicateUserException;
import com.irms.admin.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;

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
    private UserRegistrationService userRegistrationService;
    
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
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("newuser");
        user.setEmail("newuser@test.com");
        user.setPassword("encodedPassword");

        when(userRegistrationService.createUser("newuser", "newuser@test.com", "plainPassword", true, null))
                .thenReturn(user);
        
        when(jwtService.generateToken(any())).thenReturn("mockJwtToken");

        // Act
        var response = authService.register(request);

        // Assert
        assertNotNull(response);
        assertEquals("mockJwtToken", response.getToken());
        
        verify(userRegistrationService).createUser("newuser", "newuser@test.com", "plainPassword", true, null);
        verify(userRepository, never()).save(any());
        verify(auditLogger).logAction(eq("REGISTER_SUCCESS"), eq("User"), any(), eq("newuser"), eq("New user registered"));
    }

    @Test
    void register_ShouldThrowExceptionWhenUsernameExists() {
        // Arrange
        RegisterRequest request = new RegisterRequest("existinguser", "test@test.com", "pw");
        when(userRegistrationService.createUser("existinguser", "test@test.com", "pw", true, null))
                .thenThrow(new DuplicateUserException("Username already exists"));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
        verifyNoInteractions(auditLogger);
    }
}
