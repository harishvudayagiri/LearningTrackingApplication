package com.studytracker.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studytracker.dto.StudyPlanInput;
import com.studytracker.dto.TaskDTO;
import com.studytracker.model.Task;
import com.studytracker.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudyPlanService {

    private final TaskRepository taskRepository;

    public String loadTasksFromJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream inputStream = new ClassPathResource("studyPlan.json").getInputStream();
            StudyPlanInput plan=mapper.readValue(inputStream, StudyPlanInput.class);
            //List<TaskDTO> taskDTOs = mapper.readValue(inputStream, new TypeReference<List<TaskDTO>>() {});

            Map<String, LocalDate> topicScheduleMap = new HashMap<>();
            List<Task> tasks = new ArrayList<>();
            LocalDate currentDate = LocalDate.now();

            for (StudyPlanInput.CategoryWrapper category : plan.getStudyPlan()) {
                for (StudyPlanInput.Topic topic : category.getTopics()) {
                    LocalDate scheduledDate = currentDate;
                    for (String prerequisite : topic.getPrerequisites()) {
                        if (topicScheduleMap.containsKey(prerequisite)) {
                            LocalDate preDate = topicScheduleMap.get(prerequisite);
                            if (scheduledDate.isBefore(preDate.plusDays(1))) {
                                scheduledDate = preDate.plusDays(1);
                            }
                        }
                    }
                    Task task = Task.builder()
                            .title(topic.getTitle())
                            .category(category.getCategory())
                            .status("Not Started")
                            .scheduledDate(scheduledDate)
                            .isRolledOver(false)
                            .build();
                    topicScheduleMap.put(topic.getTitle(), scheduledDate);
                    tasks.add(task);
                }
            }

            taskRepository.deleteAll(); // 🧹 Clear previous tasks before loading new ones
            taskRepository.saveAll(tasks);
            return "Successfully loaded " + tasks.size() + " tasks.";

        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to load tasks: " + e.getMessage();
        }
    }
}