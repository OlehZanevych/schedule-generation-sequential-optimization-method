package org.lnu.schedule.generation.model;

import lombok.Data;

import java.util.List;

@Data
public class TimetableRequirements {
    private List<LessonRequirements> lessonRequirementsList;
    private List<LessonPlace> lessonPlaces;
    private List<LessonTimeSlot> timeSlots;

    public TimetableRequirements(List<LessonRequirements> lessonRequirementsList, List<LessonPlace> lessonPlaces, List<LessonTimeSlot> timeSlots) {
        this.lessonRequirementsList = lessonRequirementsList;
        this.lessonPlaces = lessonPlaces;
        this.timeSlots = timeSlots;
    }
}