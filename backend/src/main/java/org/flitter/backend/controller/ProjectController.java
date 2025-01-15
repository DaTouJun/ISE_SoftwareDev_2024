package org.flitter.backend.controller;

import org.flitter.backend.dto.ProjectAddParticipantDTO;
import org.flitter.backend.dto.ProjectCreateDTO;
import org.flitter.backend.dto.ProjectIdDTO;
import org.flitter.backend.dto.ProjectListDTO;
import org.flitter.backend.entity.Project;
import org.flitter.backend.entity.enums.Priority;
import org.flitter.backend.service.ProjectService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/project")
public class ProjectController {
    private final ProjectService projectService;

    ProjectController(@Autowired ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('project:create')")
    public ResponseEntity<?> create(@RequestBody ProjectCreateDTO project) {
        if (project.getName() == null || project.getName().isEmpty() ||
                project.getDescription() == null || project.getDescription().isEmpty()) {
            return ResponseEntity.badRequest().body("项目名称和描述不能为空");
        }
        if (project.getPriority() == null) {
            return ResponseEntity.badRequest().body("项目优先级不能为空");
        }
        if (project.getStartDate() == null || project.getEndDate() == null) {
            return ResponseEntity.badRequest().body("项目开始截止日期不能为空");
        }
        if (project.getManagerId() == null) {
            return ResponseEntity.badRequest().body("负责人ID不能为空");
        }

        try {
            Priority.valueOf(project.getPriority().toString());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("优先级字段应为HIGH,LOW或者MEDIUM");
        }

        try {
            var project1 = projectService.createProject(project);
            return ResponseEntity.ok(project1);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/list/all")// 看到所有的项目列表
    public ResponseEntity<?> getProjectList(@RequestParam int page, @RequestParam int size) {
        return ResponseEntity.ok(projectService.getAllProjects(PageRequest.of(page - 1, size)));
    }

    @GetMapping("/list/search")
    public ResponseEntity<?> getProjectListSearch(@RequestParam String name, @RequestParam int page, @RequestParam int size) {
        return ResponseEntity.ok(projectService.getProjectsLike(name, PageRequest.of(page - 1, size)));
    }

    @GetMapping("/list/participated")
    public ResponseEntity<?> getParticipatedProjects() {
        return ResponseEntity.ok(projectService.getAllParticipatedProjects());
    }

    @PostMapping("/get")
    public ResponseEntity<?> getProjectDetail(@RequestBody ProjectIdDTO projectIdDTO) {
        if (projectIdDTO == null || projectIdDTO.getId() == null) {
            return ResponseEntity.badRequest().body("项目id不能为空");
        }
        Project project = projectService.getProject(projectIdDTO.getId());
        if (project == null) {
            return ResponseEntity.badRequest().body("查找不到项目");
        }
        return ResponseEntity.ok(projectService.getProject(projectIdDTO.getId()));
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateProject(@RequestBody ProjectListDTO projectUpdateDTO) {
        if (projectUpdateDTO.getId() == null) {
            return ResponseEntity.badRequest().body("项目ID不能为空");
        }
        if (projectUpdateDTO.getEndDate() != null && projectUpdateDTO.getEndDate().isBefore(projectUpdateDTO.getStartDate())) {
            return ResponseEntity.badRequest().body("截止日期不能早于开始日期");
        }

        try {
            Project updatedProject = projectService.updateProject(projectUpdateDTO);
            return ResponseEntity.ok(updatedProject);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PostMapping("/add-participant")
    public ResponseEntity<?> addParticipants(@RequestBody ProjectAddParticipantDTO dto) {
        if (dto.getProjectId() == null || dto.getUserIds() == null || dto.getUserIds().isEmpty()) {
            return ResponseEntity.badRequest().body("项目ID和用户ID列表不能为空");
        }

        try {
            Project updatedProject = projectService.addParticipants(dto);
            return ResponseEntity.ok(updatedProject);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/delete-participants")
    public ResponseEntity<?> deleteParticipants(@RequestBody ProjectAddParticipantDTO dto) {
        if (dto.getProjectId() == null || dto.getUserIds() == null || dto.getUserIds().isEmpty()) {
            return ResponseEntity.badRequest().body("项目ID和用户列表不能为空");
        }

        try {
            Project updatedProject = projectService.removeParticipants(dto);
            return ResponseEntity.ok(updatedProject);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
