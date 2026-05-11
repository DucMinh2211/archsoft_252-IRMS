package com.irms.admin.service;

import com.irms.admin.domain.Role;
import com.irms.admin.domain.User;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserMapperTest {

    private final UserMapper mapper = new UserMapper();

    @Test
    void toDto_ShouldExcludePasswordAndMapRoles() {
        Role role = new Role();
        role.setName("ROLE_ADMIN");
        role.setDescription("Admin role");

        User user = new User();
        user.setUsername("admin");
        user.setEmail("admin@test.com");
        user.setPassword("secret");
        user.setActive(true);
        user.setRoles(Set.of(role));

        var dto = mapper.toDto(user);

        assertEquals("admin", dto.getUsername());
        assertEquals("admin@test.com", dto.getEmail());
        assertEquals(1, dto.getRoles().size());
        assertEquals("ROLE_ADMIN", dto.getRoles().iterator().next().getName());
    }
}
