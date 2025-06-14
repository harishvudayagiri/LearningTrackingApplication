package com.studytracker.controller;

import com.studytracker.service.StudyPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/studyplan")
@RequiredArgsConstructor
public class StudyPlanController {

    private final StudyPlanService studyPlanService;

    @PostMapping("/load")
    public String loadTasksFromJson() {
        return studyPlanService.loadTasksFromJson();
    }
}