package org.flitter.backend.repository;

import org.flitter.backend.dto.ProjectListDTO;
import org.flitter.backend.entity.Project;

import org.flitter.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Project findProjectByProjectName(String projectName);

    @Query("SELECT new org.flitter.backend.dto.ProjectListDTO(p.id, p.projectName," +
            "p.description, p.startDate, p.endDate, p.priority, p.progress," +
            "p.isCompleted) FROM Project p JOIN p.participants u " +
            "WHERE u = :user")
    List<ProjectListDTO> findProjectListDTOByParticipant(@Param("user") User user);

    @Query("SELECT new org.flitter.backend.dto.ProjectListDTO(p.id, p.projectName, " +
            "p.description, p.startDate, p.endDate, p.priority, p.progress, " +
            "p.isCompleted) FROM Project p")
    Page<ProjectListDTO> findAllProjectsDTO(Pageable pageable);


    @Query("SELECT new org.flitter.backend.dto.ProjectListDTO(p.id, p.projectName, " +
            "p.description, p.startDate, p.endDate, p.priority, p.progress, " +
            "p.isCompleted) FROM Project p WHERE p.projectName LIKE CONCAT('%', :name, '%')")
    Page<ProjectListDTO> findProjectsDTOWhichNameLike(@Param("name") String name, Pageable pageable);
}

