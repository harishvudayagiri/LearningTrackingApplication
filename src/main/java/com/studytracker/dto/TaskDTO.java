package com.studytracker.dto;

import com.studytracker.model.Task;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class TaskDTO {
    private UUID id;
    private String title;
    private String category;
    private LocalDate scheduledDate;
    private Double estimatedHours;
    private Double actualHours;
    private boolean isRollover;
    private Task.Status status;
    private List<String> prerequisites;

    public TaskDTO() {
    }

    public TaskDTO(TaskDTO other) {
        this.id = other.id;
        this.title = other.title;
        this.category = other.category;
        this.scheduledDate = other.scheduledDate;
        this.estimatedHours = other.estimatedHours;
        this.actualHours = other.actualHours;
        this.isRollover = other.isRollover;
        this.status = other.status;
        this.prerequisites = other.prerequisites != null ? new ArrayList<>(other.prerequisites) : null;
    }

}