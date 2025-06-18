package com.studytracker.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studytracker.dto.StudyPlanInput;
import com.studytracker.dto.TaskDTO;
import com.studytracker.mapper.TaskMapper;
import com.studytracker.model.Task;
import com.studytracker.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaskService {
    @Autowired
    private TaskRepository taskRepository;

    public List<TaskDTO> getTasksForDate(LocalDate date) {
        return taskRepository.findByScheduledDate(date).stream()
                .map(TaskMapper::toDTO)
                .collect(Collectors.toList());
    }

    public TaskDTO updateTaskStatus(UUID id, Task.Status newStatus) {
        Task task = taskRepository.findById(id).orElseThrow();
        task.setStatus(newStatus);
        return TaskMapper.toDTO(taskRepository.save(task));
    }

    public TaskDTO updateTask(UUID id, TaskDTO updatedDto) {
        Task task = taskRepository.findById(id).orElseThrow();
        task.setStatus(updatedDto.getStatus());
        task.setScheduledDate(updatedDto.getScheduledDate());
        task.setEstimatedHours(updatedDto.getEstimatedHours());
        task.setActualHours(updatedDto.getActualHours());
        task.setRollover(updatedDto.isRollover());
        task.setPrerequisites(updatedDto.getPrerequisites());
        return TaskMapper.toDTO(taskRepository.save(task));
    }

    public TaskDTO createTask(TaskDTO dto) {
        Task task = TaskMapper.toEntity(dto);
        return TaskMapper.toDTO(taskRepository.save(task));
    }

    public String loadTasksFromJson() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            InputStream inputStream = new ClassPathResource("studyPlan.json").getInputStream();
            List<TaskDTO> taskDTOs = objectMapper.readValue(inputStream, new TypeReference<>() {});

            Map<String, TaskDTO> taskMap = taskDTOs.stream()
                    .collect(Collectors.toMap(TaskDTO::getTitle, t -> t));

            Map<String, LocalDate> scheduledDates = new HashMap<>();
            LocalDate currentDate = LocalDate.now();

            for (TaskDTO taskDTO : taskDTOs) {
                if (taskDTO.getPrerequisites() == null || taskDTO.getPrerequisites().isEmpty()) {
                    scheduledDates.put(taskDTO.getTitle(), currentDate);
                } else {
                    LocalDate maxDate = currentDate;
                    for (String prereq : taskDTO.getPrerequisites()) {
                        LocalDate prereqDate = scheduledDates.getOrDefault(prereq, currentDate);
                        if (prereqDate.isAfter(maxDate)) {
                            maxDate = prereqDate;
                        }
                    }
                    scheduledDates.put(taskDTO.getTitle(), maxDate.plusDays(1));
                }
            }

            for (TaskDTO dto : taskDTOs) {
                dto.setScheduledDate(scheduledDates.get(dto.getTitle()));
                Task task = TaskMapper.toEntity(dto);
                taskRepository.save(task);
            }

            return "Tasks loaded and scheduled successfully.";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error loading tasks: " + e.getMessage();
        }
    }
}