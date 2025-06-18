package com.studytracker.mapper;

import com.studytracker.dto.TaskDTO;
import com.studytracker.model.Task;

public class TaskMapper {
    public static TaskDTO toDTO(Task task) {
        return TaskDTO.builder()
                .id(task.getId())
                .title(task.getTitle())
                .category(task.getCategory())
                .scheduledDate(task.getScheduledDate())
                .estimatedHours(task.getEstimatedHours())
                .actualHours(task.getActualHours())
                .isRollover(task.isRollover())
                .status(task.getStatus())
                .prerequisites(task.getPrerequisites())
                .build();
    }

    public static Task toEntity(TaskDTO dto) {
        return Task.builder()
                .id(dto.getId())
                .title(dto.getTitle())
                .category(dto.getCategory())
                .scheduledDate(dto.getScheduledDate())
                .estimatedHours(dto.getEstimatedHours())
                .actualHours(dto.getActualHours())
                .isRollover(dto.isRollover())
                .status(dto.getStatus())
                .prerequisites(dto.getPrerequisites())
                .build();
    }
}