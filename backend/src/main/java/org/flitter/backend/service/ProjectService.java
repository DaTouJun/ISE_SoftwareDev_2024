package org.flitter.backend.service;

import jakarta.transaction.Transactional;
import org.flitter.backend.config.SecurityConfig;
import org.flitter.backend.dto.ProjectAddParticipantDTO;
import org.flitter.backend.dto.ProjectCreateDTO;
import org.flitter.backend.dto.ProjectListDTO;
import org.flitter.backend.entity.Project;
import org.flitter.backend.entity.User;
import org.flitter.backend.repository.ProjectRepository;
import org.flitter.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final SecurityConfig securityConfig;
    private final UserRepository userRepository;

    @Autowired
    public ProjectService(ProjectRepository projectRepository,
                          SecurityConfig securityConfig,
                          UserRepository userRepository) {
        this.projectRepository = projectRepository; // service可以做业务逻辑
        this.securityConfig = securityConfig;
        this.userRepository = userRepository;
    }

    public Project getProject(Long id) {
        if (projectRepository.findById(id).isPresent()) {
            return projectRepository.findById(id).get();
        } else {
            return null;
        }
    }

    @Transactional
    public Project createProject(ProjectCreateDTO projectCreate) {
        Project project = new Project();

        Project exitsProject = projectRepository.findProjectByProjectName(projectCreate.getName());
        if (exitsProject != null) {
            throw new IllegalArgumentException("项目名已经被使用过了");
        }
        User manager = userRepository.findById(projectCreate.getManagerId()).orElse(null);
        if (manager == null) {
            throw new IllegalArgumentException("负责人的ID无效");
        }
        project.setManager(manager);
        project.setProjectName(projectCreate.getName());
        project.setStartDate(projectCreate.getStartDate());
        project.setEndDate(projectCreate.getEndDate());
        project.setDescription(projectCreate.getDescription());
        project.setPriority(projectCreate.getPriority());

        project.setIsCompleted(false);
        if (project.getParticipants() == null) {
            project.setParticipants(new HashSet<>());
        }

        projectRepository.save(project);
        return project;
    }

    @Transactional
    public Page<ProjectListDTO> getAllProjects(Pageable pageable) {
        return projectRepository.findAllProjectsDTO(pageable);
    }

    @Transactional
    public Page<ProjectListDTO> getProjectsLike(String name, Pageable pageable) {
        return projectRepository.findProjectsDTOWhichNameLike(name, pageable);
    }

    @Transactional
    public List<ProjectListDTO> getAllParticipatedProjects() {
        User currentUser = securityConfig.getCurrentUser();
        return projectRepository.findProjectListDTOByParticipant(currentUser);
    }


    public Project updateProject(ProjectListDTO dto) {
        Project project = projectRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("未找到对应的项目"));

        if (dto.getProjectName() != null) {
            project.setProjectName(dto.getProjectName());
        }
        if (dto.getDescription() != null) {
            project.setDescription(dto.getDescription());
        }
        if (dto.getStartDate() != null) {
            project.setStartDate(dto.getStartDate());
        }
        if (dto.getEndDate() != null) {
            project.setEndDate(dto.getEndDate());
        }
        if (dto.getPriority() != null) {
            project.setPriority(dto.getPriority());
        }
        if (dto.getProgress() != null) {
            project.setProgress(dto.getProgress());
        }
        if (dto.getIsCompleted() != null) {
            project.setIsCompleted(dto.getIsCompleted());
        }

        return projectRepository.save(project);
    }

    @Transactional
    public Project addParticipants(ProjectAddParticipantDTO dto) {
        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException("未找到对应的项目"));

        Iterable<User> iterableUsers = userRepository.findAllById(dto.getUserIds()); // 提供的 Iterable<User>
        List<User> userList = StreamSupport.stream(iterableUsers.spliterator(), false)
                .toList();
        if (userList.isEmpty()) {
            throw new IllegalArgumentException("未找到对应的用户");
        }

        for (User user : userList) {
            project.getParticipants().add(user);
        }

        return projectRepository.save(project);
    }

    @Transactional
    public Project removeParticipants(ProjectAddParticipantDTO dto) {
        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException("未找到对应的项目"));

        Iterable<User> iterableUsers = userRepository.findAllById(dto.getUserIds());
        List<User> userList = StreamSupport.stream(iterableUsers.spliterator(), false)
                .toList();
        if (userList.isEmpty()) {
            throw new IllegalArgumentException("未找到对应的用户");
        }

        for (User user : userList) {
            if (!project.getParticipants().contains(user)) {
                throw new IllegalArgumentException("用户不在该项目中: " + user.getId());
            }
            project.getParticipants().remove(user);
        }

        return projectRepository.save(project);
    }
}
