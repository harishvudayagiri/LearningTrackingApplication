package com.studytracker.dto;

import com.studytracker.model.Task;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
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
}
