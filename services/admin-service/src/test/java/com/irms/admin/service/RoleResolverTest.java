package com.irms.admin.service;

import com.irms.admin.domain.Role;
import com.irms.admin.exception.RoleNotFoundException;
import com.irms.admin.repository.RoleRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RoleResolverTest {

    @Test
    void resolve_ShouldReturnEmptySetForNullRoleNames() {
        RoleResolver resolver = new RoleResolver(mock(RoleRepository.class));

        assertTrue(resolver.resolve(null).isEmpty());
    }

    @Test
    void resolve_ShouldLoadRolesByName() {
        RoleRepository repository = mock(RoleRepository.class);
        Role admin = new Role();
        admin.setName("ROLE_ADMIN");
        when(repository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(admin));

        RoleResolver resolver = new RoleResolver(repository);

        Set<Role> roles = resolver.resolve(Set.of("ROLE_ADMIN"));

        assertEquals(Set.of(admin), roles);
    }

    @Test
    void resolve_ShouldThrowWhenRoleIsMissing() {
        RoleRepository repository = mock(RoleRepository.class);
        when(repository.findByName("ROLE_UNKNOWN")).thenReturn(Optional.empty());

        RoleResolver resolver = new RoleResolver(repository);

        assertThrows(RoleNotFoundException.class, () -> resolver.resolve(Set.of("ROLE_UNKNOWN")));
    }
}
