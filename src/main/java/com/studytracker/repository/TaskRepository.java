package com.studytracker.repository;

import com.studytracker.dto.TaskDTO;
import com.studytracker.model.Task;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByScheduledDate(LocalDate date);
    List<Task> findByStatus(Task.Status status);
    List<Task> findAll();
    Task findByTitle(String title);
    @Modifying
    @Transactional
    @Query(value = "TRUNCATE TABLE task RESTART IDENTITY CASCADE", nativeQuery = true)
    void truncateTableTask();
    @Modifying
    @Transactional
    @Query(value = "TRUNCATE TABLE task_prerequisites RESTART IDENTITY CASCADE", nativeQuery = true)
    void truncateTableTaskPrerequisites();

    @Modifying
    @Transactional
    @Query("UPDATE Task t SET t.status = :status WHERE t.id = :id")
    void updateTaskStatus(@Param("id") UUID id, @Param("status") Task.Status status);

    @Modifying
    @Transactional
    @Query("""
           UPDATE Task t SET
               t.status = :#{#dto.status},
               t.scheduledDate = :#{#dto.scheduledDate},
               t.estimatedHours = :#{#dto.estimatedHours},
               t.actualHours = :#{#dto.actualHours},
               t.isRollover = :#{#dto.rollover}
           WHERE t.id = :id
           """)
    void updateTaskFields(@Param("id") UUID id, @Param("dto") TaskDTO dto);

}

