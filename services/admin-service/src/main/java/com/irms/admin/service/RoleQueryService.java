package com.irms.admin.service;

import com.irms.admin.dto.RoleResponseDTO;

import java.util.List;

public interface RoleQueryService {
    List<RoleResponseDTO> getAllRoles();
}
