package org.lnu.schedule.generation.util;

import org.lnu.schedule.generation.model.LessonTimeSlot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class LessonTimeSlotUtil {
    private final int timeSlotMinBreak;
    private final int timeWindowMinDuration;

    public LessonTimeSlotUtil(
            @Value("${time_slot_min_break}") int timeSlotMinBreak,
            @Value("${time_window_min_duration}") int timeWindowMinDuration
    ) {
        this.timeSlotMinBreak = timeSlotMinBreak;
        this.timeWindowMinDuration = timeWindowMinDuration;
    }

    public boolean isConflict(LessonTimeSlot timeSlot1, LessonTimeSlot timeSlot2) {
        int comparisonResult = timeSlot1.compareTo(timeSlot2);

        if (comparisonResult == 0) {
            return true;
        }

        if (comparisonResult > 0) {
            LessonTimeSlot temp = timeSlot1;
            timeSlot1 = timeSlot2;
            timeSlot2 = temp;
        }

        return Duration.between(timeSlot1.getEndTime(), timeSlot2.getStartTime()).toMinutes() < timeSlotMinBreak;
    }

    public boolean isWindow(LessonTimeSlot timeSlot1, LessonTimeSlot timeSlot2) {
        int comparisonResult = timeSlot1.compareTo(timeSlot2);

        if (comparisonResult == 0) {
            return false;
        }

        if (comparisonResult > 0) {
            LessonTimeSlot temp = timeSlot1;
            timeSlot1 = timeSlot2;
            timeSlot2 = temp;
        }

        return Duration.between(timeSlot1.getEndTime(), timeSlot2.getStartTime()).toMinutes() >= timeWindowMinDuration;
    }
}
