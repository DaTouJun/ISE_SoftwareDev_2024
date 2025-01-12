package org.flitter.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import org.flitter.backend.entity.Project;
import org.flitter.backend.entity.User;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@ToString
@AllArgsConstructor
public class TaskAssigneeDTO {
    private Long id;
    private Long projectId;
    private Project belongedProject;
    private Set<User> assignees = new HashSet<>();
    private List<Long> assigneesId;
    private String title;
    private String description;
    private User publisher;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isCompleted; //项目是否完成
    private Double percentCompleted; //完成程度

    public TaskAssigneeDTO() {
    }
}
