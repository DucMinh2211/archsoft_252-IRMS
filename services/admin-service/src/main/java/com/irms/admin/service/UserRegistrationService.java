package com.irms.admin.service;

import com.irms.admin.domain.Role;
import com.irms.admin.domain.User;
import com.irms.admin.exception.DuplicateUserException;
import com.irms.admin.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserRegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleResolver roleResolver;

    @Transactional
    public User createUser(String username, String email, String password, boolean active, Set<String> roleNames) {
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateUserException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateUserException("Email already exists");
        }

        Set<Role> roles = roleResolver.resolve(roleNames);

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setActive(active);
        user.setRoles(roles);

        return userRepository.save(user);
    }
}
