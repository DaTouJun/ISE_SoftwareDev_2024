package org.flitter.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import org.flitter.backend.entity.enums.Priority;

import java.time.LocalDate;

@Data
@ToString
@AllArgsConstructor
public class ProjectListDTO {
    private Long id;
    private String projectName;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Priority priority;
    private Double progress;
    private Boolean isCompleted;
}
