package com.irms.admin.service;

import com.irms.admin.domain.User;
import com.irms.admin.dto.UserRequestDTO;
import com.irms.admin.dto.UserResponseDTO;
import com.irms.admin.exception.DuplicateUserException;
import com.irms.admin.exception.UserNotFoundException;
import com.irms.admin.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class UserService implements UserManagementService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRegistrationService userRegistrationService;
    private final RoleResolver roleResolver;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponseDTO createUser(UserRequestDTO request) {
        User user = userRegistrationService.createUser(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.isActive(),
                request.getRoleNames()
        );

        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public UserResponseDTO updateUser(UUID userId, UserRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateUserException("Email already exists");
        }

        user.setEmail(request.getEmail());
        user.setActive(request.isActive());

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getRoleNames() != null) {
            user.setRoles(roleResolver.resolve(request.getRoleNames()));
        }

        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        // Soft delete
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    public UserResponseDTO getUserById(UUID userId) {
        return userRepository.findById(userId)
                .map(userMapper::toDto)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    @Override
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }
}
