package org.lnu.schedule.generation.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
public class EvaluatedTimetable {
    private final Lesson[] lessons;
    private final double penalty;
    private final double[] lessonPenalties;
    private final Map<Integer, Set<Integer>> lecturerLessonsMap;
    private final Map<Integer, Set<Integer>> academicGroupLessonsMap;
    private final Map<Integer, Set<Integer>> placeLessonsMap;
    private final Comparator<Integer> lessonIndexcomparator;
}
