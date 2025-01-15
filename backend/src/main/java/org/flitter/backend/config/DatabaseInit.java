package org.flitter.backend.config;

import org.flitter.backend.entity.Role;
import org.flitter.backend.entity.User;
import org.flitter.backend.entity.enums.Permission;
import org.flitter.backend.repository.RoleRepository;
import org.flitter.backend.repository.UserRepository;
import org.flitter.backend.service.RoleService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class DatabaseInit {
    @Bean
    @Order(1)
    CommandLineRunner roleInit(RoleRepository roleRepository) {
        return args -> {
            if (roleRepository.findByName("ROLE_USER") == null) {
                Role role = new Role();
                role.setName("ROLE_USER");
                Set<Permission> permissions = new HashSet<>();
                permissions.add(Permission.fromString("project:read"));
                permissions.add(Permission.fromString("project:write"));

                permissions.add(Permission.fromString("task:create"));
                permissions.add(Permission.fromString("task:read"));
                permissions.add(Permission.fromString("task:write"));
                role.setPermissions(permissions);
                roleRepository.save(role);
            }
            if(roleRepository.findByName("ROLE_PROJECT_MANAGER") == null) {
                Role role = new Role();
                role.setName("ROLE_PROJECT_MANAGER");
                Set<Permission> permissions = new HashSet<>();
                permissions.add(Permission.fromString("project:create"));
                permissions.add(Permission.fromString("project:read"));
                permissions.add(Permission.fromString("project:write"));
                permissions.add(Permission.fromString("task:create"));
                permissions.add(Permission.fromString("task:read"));
                permissions.add(Permission.fromString("task:write"));
                role.setPermissions(permissions);
                roleRepository.save(role);
            }
            if (roleRepository.findByName("ROLE_ADMIN") == null) {
                Role role = new Role();
                role.setName("ROLE_ADMIN");
                Set<Permission> permissions = new HashSet<>();
                permissions.add(Permission.fromString("project:create"));
                permissions.add(Permission.fromString("project:read"));
                permissions.add(Permission.fromString("project:write"));

                permissions.add(Permission.fromString("role:modify"));

                permissions.add(Permission.fromString("task:create"));
                permissions.add(Permission.fromString("task:read"));
                permissions.add(Permission.fromString("task:write"));
                role.setPermissions(permissions);
                roleRepository.save(role);
            }
        };
    }

    @Bean
    @Order(2)
    CommandLineRunner userInit(UserRepository userRepository,
                               BCryptPasswordEncoder bCryptPasswordEncoder,
                               RoleService roleService) {
        return args-> {
            if (userRepository.findByUsername("System") == null) {
                User user = new User();
                user.setUsername("System");
                user.setEmail("System@system.com");
                user.setPassword(bCryptPasswordEncoder.encode("system11223344556677"));
                userRepository.save(user);
                roleService.addRoleToUser("System", "ROLE_ADMIN");
            }
        };
    }
}
