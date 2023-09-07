package org.lnu.schedule.generation.model;

import lombok.Data;

import java.time.LocalTime;
import java.util.Comparator;

import static java.util.Comparator.comparing;

@Data
public class LessonTimeSlot implements Comparable<LessonTimeSlot> {
    private static final Comparator<LessonTimeSlot> LESSON_TIME_SLOT_COMPARATOR = comparing(LessonTimeSlot::getStartTime)
            .thenComparing(LessonTimeSlot::getEndTime);

    private final LocalTime startTime;
    private final LocalTime endTime;

    @Override
    public String toString() {
        return startTime + " - " + endTime;
    }

    @Override
    public int compareTo(LessonTimeSlot otherLesson) {
        if (otherLesson == null) {
            return -1;
        }

        return LESSON_TIME_SLOT_COMPARATOR.compare(this, otherLesson);
    }
}
