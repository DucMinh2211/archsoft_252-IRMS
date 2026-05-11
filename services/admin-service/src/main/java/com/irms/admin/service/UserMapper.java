package com.irms.admin.service;

import com.irms.admin.domain.Role;
import com.irms.admin.domain.User;
import com.irms.admin.dto.RoleResponseDTO;
import com.irms.admin.dto.UserResponseDTO;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserResponseDTO toDto(User user) {
        Set<RoleResponseDTO> roleDtos = user.getRoles().stream()
                .map(this::toDto)
                .collect(Collectors.toSet());

        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .active(user.isActive())
                .roles(roleDtos)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public RoleResponseDTO toDto(Role role) {
        return RoleResponseDTO.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .build();
    }
}
