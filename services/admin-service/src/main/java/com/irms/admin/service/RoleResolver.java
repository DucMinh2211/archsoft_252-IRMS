package com.irms.admin.service;

import com.irms.admin.domain.Role;
import com.irms.admin.exception.RoleNotFoundException;
import com.irms.admin.repository.RoleRepository;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class RoleResolver {

    private final RoleRepository roleRepository;

    public RoleResolver(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Set<Role> resolve(Set<String> roleNames) {
        Set<Role> roles = new HashSet<>();
        if (roleNames != null) {
            for (String roleName : roleNames) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new RoleNotFoundException("Role not found: " + roleName));
                roles.add(role);
            }
        }
        return roles;
    }
}
