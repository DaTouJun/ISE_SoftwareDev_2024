package org.flitter.backend.service;

import org.flitter.backend.config.SecurityConfig;
import org.flitter.backend.dto.UserIDRoleDTO;
import org.flitter.backend.dto.UserNameIdDTO;
import org.flitter.backend.entity.Project;
import org.flitter.backend.entity.Role;
import org.flitter.backend.entity.User;
import org.flitter.backend.repository.ProjectRepository;
import org.flitter.backend.repository.RoleRepository;
import org.flitter.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HumanResourceService {
    private final UserRepository userRepository;
    private final SecurityConfig securityConfig;
    private final ProjectRepository projectRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public HumanResourceService(UserRepository userRepository,
                                SecurityConfig securityConfig,
                                ProjectRepository projectRepository,
                                RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.securityConfig = securityConfig;
        this.projectRepository = projectRepository;
        this.roleRepository = roleRepository;
    }

    public List<UserNameIdDTO> fetchAllUsersIdNameWithLimit1000() {
        Pageable pageable = PageRequest.of(0, 1000);
        return userRepository.findAllUserNameId(pageable);
    }

    public List<UserNameIdDTO> fetchALlUserWhichParticipatedIn(Long projectId) {
        Project p = projectRepository.findById(projectId).orElse(null);
        if (p == null)
            throw new IllegalArgumentException("找不到对应的项目");
        return userRepository.findAllByProjectsContains(p);
    }

    public List<UserNameIdDTO> searchUserIdNameLimit1000(String username) {
        Pageable pageable = PageRequest.of(0, 1000);
        return userRepository.searchUserByUsernameLike("%" + username + "%", pageable);
    }


    public Page<UserIDRoleDTO> fetchAllUsersIdRole(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAllUserIDRole(pageable);
    }

    public Object searchUserRole(int i, int size, String username) {
        Pageable pageable = PageRequest.of(i, size);
        return userRepository.findAllUserIDRole(pageable, username);
    }

    public void updateUserRole(Long userId, Long roleId) {
        // 角色ID检查
        Role role = roleRepository.findById(roleId).orElseThrow(() ->
                new IllegalArgumentException("角色不存在"));

        // 获取待更新的用户信息
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        // 更新用户角色
        user.getRoles().clear(); // 清除旧角色
        user.getRoles().add(role); // 添加新角色
        userRepository.save(user); // 保存更新后的用户
    }

    public List<Role> fetchAllRoles() {
        return roleRepository.findAll();
    }
}
