package org.lnu.schedule.generation.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class Lesson {
    private int requirementsIndex;
    private Day day;
    private LessonTimeSlot timeSlot;
    private int placeIndex;
    private LessonPeriodicity periodicity;

    public Lesson clone() {
        return this.toBuilder().build();
    }
}
