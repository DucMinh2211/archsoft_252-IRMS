package com.irms.admin.service;

import com.irms.admin.domain.Role;
import com.irms.admin.dto.RoleResponseDTO;
import com.irms.admin.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService implements RoleQueryService {

    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    @Override
    public List<RoleResponseDTO> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }
}
