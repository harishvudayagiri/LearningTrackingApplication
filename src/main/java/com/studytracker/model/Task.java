package com.studytracker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String title;
    private String category;
    private LocalDate scheduledDate;
    @Column(nullable = false)
    @JsonProperty("isRollover")
    private boolean isRollover = false;

    @Column(nullable = false)
    private Double estimatedHours = 0.0;

    @Column(nullable = false)
    private Double actualHours = 0.0;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> prerequisites;

    public enum Status {
        NOT_STARTED, IN_PROGRESS, COMPLETED, PENDING
    }
}
