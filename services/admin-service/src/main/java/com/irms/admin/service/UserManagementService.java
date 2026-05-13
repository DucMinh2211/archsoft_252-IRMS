package com.irms.admin.service;

import com.irms.admin.dto.UserRequestDTO;
import com.irms.admin.dto.UserResponseDTO;

import java.util.List;
import java.util.UUID;

public interface UserManagementService {
    UserResponseDTO createUser(UserRequestDTO request);
    UserResponseDTO updateUser(UUID userId, UserRequestDTO request);
    void deleteUser(UUID userId);
    UserResponseDTO getUserById(UUID userId);
    List<UserResponseDTO> getAllUsers();
}
