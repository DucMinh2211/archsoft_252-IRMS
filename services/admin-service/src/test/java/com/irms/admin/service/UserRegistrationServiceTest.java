package com.irms.admin.service;

import com.irms.admin.domain.Role;
import com.irms.admin.domain.User;
import com.irms.admin.exception.DuplicateUserException;
import com.irms.admin.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRegistrationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleResolver roleResolver;

    @InjectMocks
    private UserRegistrationService userRegistrationService;

    @Test
    void createUser_ShouldValidateEncodeResolveRolesAndSave() {
        Role admin = new Role();
        admin.setName("ROLE_ADMIN");
        Set<String> roleNames = Set.of("ROLE_ADMIN");
        Set<Role> roles = Set.of(admin);

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@test.com")).thenReturn(false);
        when(roleResolver.resolve(roleNames)).thenReturn(roles);
        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User user = userRegistrationService.createUser("newuser", "newuser@test.com", "plainPassword", true, roleNames);

        assertEquals("newuser", user.getUsername());
        assertEquals("newuser@test.com", user.getEmail());
        assertEquals("encodedPassword", user.getPassword());
        assertEquals(roles, user.getRoles());
        verify(userRepository).save(user);
    }

    @Test
    void createUser_ShouldThrowWhenEmailExists() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@test.com")).thenReturn(true);

        assertThrows(DuplicateUserException.class,
                () -> userRegistrationService.createUser("newuser", "newuser@test.com", "plainPassword", true, null));

        verify(userRepository, never()).save(any());
    }
}
