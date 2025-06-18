package com.studytracker.repository;

import com.studytracker.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByScheduledDate(LocalDate date);
    List<Task> findByStatus(Task.Status status);
    List<Task> findAll();
    Task findByTitle(String title);
}

