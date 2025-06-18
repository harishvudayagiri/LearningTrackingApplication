package com.studytracker.controller;

import com.studytracker.dto.TaskDTO;
import com.studytracker.model.Task;
import com.studytracker.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @PostMapping("/load")
    public String loadTasksFromJson() {
        return taskService.loadTasksFromJson();
    }

    @GetMapping("/date/{date}")
    public List<TaskDTO> getTasksByDate(@PathVariable String date) {
        return taskService.getTasksForDate(LocalDate.parse(date));
    }

    @PutMapping("/{id}/status")
    public TaskDTO updateStatus(@PathVariable UUID id, @RequestParam String status) {
        return taskService.updateTaskStatus(id, Task.Status.valueOf(status.toUpperCase()));
    }

    @PutMapping("/{id}")
    public TaskDTO updateTask(@PathVariable UUID id, @RequestBody TaskDTO updatedTask) {
        return taskService.updateTask(id, updatedTask);
    }

    @PostMapping
    public TaskDTO createTask(@RequestBody TaskDTO dto) {
        return taskService.createTask(dto);
    }
}