package org.lnu.schedule.generation.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimetableRequirementsExample {
    private int lecturersCount;
    private int academicGroupsCount;
    private int placesCount;
    private List<LessonRequirements> lessonRequirementsList;
}
