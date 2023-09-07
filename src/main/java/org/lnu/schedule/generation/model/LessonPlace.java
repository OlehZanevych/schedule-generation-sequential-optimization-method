package org.lnu.schedule.generation.model;

import lombok.Data;

@Data
public class LessonPlace {
    private final long id;
    private final String name;
    private final int capacity;
    private final UniversityBuilding building;
}
