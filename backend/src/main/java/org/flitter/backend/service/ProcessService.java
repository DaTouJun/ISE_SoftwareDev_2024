package org.flitter.backend.service;

import org.flitter.backend.entity.Project;
import org.flitter.backend.repository.ProjectRepository;
import org.flitter.backend.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProcessService {
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    @Autowired
    public ProcessService(ProjectRepository projectRepository,
                          TaskRepository taskRepository) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
    }


    public void computeProgress(Long id) {
        Project project = projectRepository.findById(id).orElse(null);
        if (project == null) {
            throw new IllegalArgumentException("要更新进度的项目找不到");
        }
        Long countAll = taskRepository.countAllTasksByProjectId(project.getId());
        Long countFinished = taskRepository.countCompletedTasksByProjectId(project.getId());
        double process = (double) countFinished / countAll;
        project.setProgress(process);
        projectRepository.save(project);
    }
}
