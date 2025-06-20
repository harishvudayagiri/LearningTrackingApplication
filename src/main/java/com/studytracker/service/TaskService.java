package com.studytracker.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studytracker.dto.TaskDTO;
import com.studytracker.mapper.TaskMapper;
import com.studytracker.model.Task;
import com.studytracker.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
@Service
public class TaskService {
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final Map<String, Set<DayOfWeek>> CATEGORY_DAY_MAP = Map.of(
            "DSA", Set.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY),
            "LLD", Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
            "Java", EnumSet.allOf(DayOfWeek.class)
    );

    private static final List<String> CATEGORY_PRIORITY = List.of(
            "Java", "DSA", "Springboot", "Microservices", "Rest API", "System Design", "SQL"
    );

    private Map<LocalDate, Set<String>> dailyCategoryBlock = new HashMap<>();

    public List<TaskDTO> getTasksForDate(LocalDate date) {
        return taskRepository.findByScheduledDate(date).stream()
                .map(TaskMapper::toDTO)
                .collect(Collectors.toList());
    }

    public TaskDTO updateTaskStatus(UUID id, Task.Status newStatus) {
        taskRepository.updateTaskStatus(id, newStatus);
        Task updated = taskRepository.findById(id).orElseThrow(); // only if DTO is needed
        return TaskMapper.toDTO(updated);
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
        List<TaskDTO> taskDTOs;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            InputStream inputStream = new ClassPathResource("studyPlan.json").getInputStream();
            taskDTOs = objectMapper.readValue(inputStream, new TypeReference<>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to load JSON: " + e.getMessage();
        }

        return saveTasks(taskDTOs);
    }

    @Transactional
    public String saveTasks(List<TaskDTO> taskDTOs) {
        long start = System.currentTimeMillis();

        taskRepository.truncateTableTask();
        taskRepository.truncateTableTaskPrerequisites();
        dailyCategoryBlock.clear();

        long afterTruncate = System.currentTimeMillis();

        List<String> activeCategories = CATEGORY_PRIORITY.subList(0, 2);
        List<TaskDTO> activeTasks = taskDTOs.stream()
                .filter(task -> activeCategories.contains(task.getCategory()))
                .collect(Collectors.toList());

        List<TaskDTO> sortedByPrerequisite = sortByPrerequisites(activeTasks);

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        List<Task> scheduledEntities = new ArrayList<>();

        for (TaskDTO task : sortedByPrerequisite) {
            List<TaskDTO> parts = splitIfNeeded(task);
            for (TaskDTO part : parts) {
                LocalDate date = getNextValidDate(today, part.getCategory());
                part.setScheduledDate(date);
                blockDay(date, part.getCategory());
                scheduledEntities.add(TaskMapper.toEntity(part));
            }
        }

        bulkInsert(scheduledEntities); // 🔁 new

        List<Task> unscheduledEntities = taskDTOs.stream()
                .filter(task -> !activeCategories.contains(task.getCategory()))
                .map(TaskMapper::toEntity)
                .collect(Collectors.toList());

        bulkInsert(unscheduledEntities); // 🔁 new

        long end = System.currentTimeMillis();
        System.out.println("Truncate time: " + (afterTruncate - start) + "ms");
        System.out.println("Total time: " + (end - start) + "ms");

        return "Tasks loaded and scheduled successfully.";
    }

    private void bulkInsert(List<Task> tasks) {
        if (tasks.isEmpty()) return;

        for (Task task : tasks) {
            if (task.getId() == null) {
                task.setId(UUID.randomUUID());
            }
        }

        String sql = "INSERT INTO task (id, title, category, scheduled_date, estimated_hours, actual_hours, status, is_rollover) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Task t = tasks.get(i);
                ps.setObject(1, t.getId());
                ps.setString(2, t.getTitle());
                ps.setString(3, t.getCategory());
                ps.setObject(4, t.getScheduledDate());
                ps.setDouble(5, t.getEstimatedHours());
                ps.setDouble(6, t.getActualHours());
                ps.setString(7, t.getStatus().name());
                ps.setBoolean(8, t.isRollover());
            }
            public int getBatchSize() {
                return tasks.size();
            }
        });
    }

    private List<TaskDTO> splitIfNeeded(TaskDTO original) {
        double est = original.getEstimatedHours();
        if (est <= 2) return List.of(original);

        int parts = (int) Math.ceil(est / 2.0);
        List<TaskDTO> split = new ArrayList<>();
        for (int i = 1; i <= parts; i++) {
            TaskDTO part = new TaskDTO(original);
            part.setTitle(original.getTitle() + " (Part " + i + "/" + parts + ")");
            part.setEstimatedHours((i == parts && est % 2 != 0) ? est % 2 : 2);
            split.add(part);
        }
        return split;
    }

    private List<TaskDTO> sortByPrerequisites(List<TaskDTO> tasks) {
        List<TaskDTO> sorted = new ArrayList<>();
        Set<String> added = new HashSet<>();
        while (sorted.size() < tasks.size()) {
            for (TaskDTO task : tasks) {
                if (added.contains(task.getTitle())) continue;
                if (task.getPrerequisites() == null || added.containsAll(task.getPrerequisites())) {
                    sorted.add(task);
                    added.add(task.getTitle());
                }
            }
        }
        return sorted;
    }

    private LocalDate getNextValidDate(LocalDate start, String category) {
        LocalDate date = start;
        while (true) {
            if (!CATEGORY_DAY_MAP.containsKey(category)) return date;
            if (!CATEGORY_DAY_MAP.get(category).contains(date.getDayOfWeek())) {
                date = date.plusDays(1);
                continue;
            }
            if (dailyCategoryBlock.getOrDefault(date, new HashSet<>()).contains(category)) {
                date = date.plusDays(1);
                continue;
            }
            break;
        }
        return date;
    }

    private void blockDay(LocalDate date, String category) {
        dailyCategoryBlock.computeIfAbsent(date, k -> new HashSet<>()).add(category);
    }
}
