package com.studytracker.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudyPlanInput {
    private List<CategoryWrapper> studyPlan;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryWrapper {
        private String category;
        private List<Topic> topics;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Topic {
        private String title;
        private double estimatedHours;
        private List<String> prerequisites;
    }
}