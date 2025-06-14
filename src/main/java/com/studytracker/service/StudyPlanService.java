package com.studytracker.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studytracker.dto.TaskDTO;
import com.studytracker.model.Task;
import com.studytracker.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudyPlanService {

    private final TaskRepository taskRepository;

    public String loadTasksFromJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream inputStream = new ClassPathResource("studyPlan.json").getInputStream();
            List<TaskDTO> taskDTOs = mapper.readValue(inputStream, new TypeReference<List<TaskDTO>>() {});

            List<Task> tasks = taskDTOs.stream().map(dto -> Task.builder()
                    .title(dto.getTitle())
                    .category(dto.getCategory())
                    .status(dto.getStatus())
                    .scheduledDate(dto.getScheduledDate())
                    .isRolledOver(false)
                    .build()
            ).collect(Collectors.toList());

            taskRepository.saveAll(tasks);
            return "Successfully loaded " + tasks.size() + " tasks.";

        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to load tasks: " + e.getMessage();
        }
    }
}