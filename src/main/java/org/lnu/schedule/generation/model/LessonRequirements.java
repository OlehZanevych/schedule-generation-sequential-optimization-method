package org.lnu.schedule.generation.model;

import lombok.Data;

import java.util.Set;

@Data
public class LessonRequirements {
    private final int lecturerIndex;
    private final Set<Integer> academicGroupIndexes;
    private final double lessonsCountPerWeek;
}
