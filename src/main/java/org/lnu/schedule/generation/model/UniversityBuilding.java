package org.lnu.schedule.generation.model;

import lombok.Data;

import java.util.List;

@Data
public class UniversityBuilding {
    private final Long id;
    private final String name;
    private final List<LessonTimeSlot> timeSlots;
}
