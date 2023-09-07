package org.lnu.schedule.generation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.lnu.schedule.generation.model.LessonRequirements;
import org.lnu.schedule.generation.model.TimetableRequirementsExample;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Service
@AllArgsConstructor
public class TimetableRequirementsGenerationService {

    private final String EXAMPLES_FOLDER = "timetable-requirements-examples";

    private static final Random random = new Random();

    private final ObjectMapper objectMapper;

    public void generateAndSaveRandomTimetableRequirementsExample(String fileName, int lecturersCount,
                                                                  int academicGroupsCount, int placesCount,
                                                                  int lessonsCountPerWeek) {

        TimetableRequirementsExample timetableRequirementsExample
                = generateRandomTimetableRequirements(lecturersCount, academicGroupsCount, placesCount, lessonsCountPerWeek);

        try {
            String timetableRequirementsExampleStr = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(timetableRequirementsExample);

            Path pathToFile = Path.of(EXAMPLES_FOLDER,fileName);
            Files.createDirectories(pathToFile.getParent());
            Files.writeString(pathToFile, timetableRequirementsExampleStr);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public TimetableRequirementsExample readExampleTimetableRequirementsExample(String fileName) {
        try {
            Path pathToFile = Path.of(EXAMPLES_FOLDER,fileName);
            return objectMapper.readValue(pathToFile.toFile(), TimetableRequirementsExample.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public TimetableRequirementsExample generateRandomTimetableRequirements(int lecturersCount, int academicGroupsCount,
                                                    int placesCount, int lessonsCountPerWeek) {

        List<LessonRequirements> lessonRequirementsList = new ArrayList<>(lessonsCountPerWeek);
        double unusedLessonsCount = lessonsCountPerWeek;
        while (unusedLessonsCount > 0) {
            double currentLessonsCountPerWeek = random.nextInt(3);

            if (random.nextDouble() < 0.25) {
                currentLessonsCountPerWeek += 0.5;
            }

            if (currentLessonsCountPerWeek == 0) {
                currentLessonsCountPerWeek = 0.5;
            }

            if (currentLessonsCountPerWeek > unusedLessonsCount) {
                currentLessonsCountPerWeek = unusedLessonsCount;
            }

            int lecturerIndex = random.nextInt(lecturersCount);

            Set<Integer> academicGroupIndexes = new HashSet<>();
            int lessonAcademicGroupsCount = 1 + random.nextInt(5);
            while (lessonAcademicGroupsCount > 0) {
                int academicGroupIndex = random.nextInt(academicGroupsCount);
                if (academicGroupIndexes.add(academicGroupIndex)) {
                    academicGroupIndexes.add(academicGroupIndex);
                    --lessonAcademicGroupsCount;
                }
            }

            lessonRequirementsList.add(new LessonRequirements(lecturerIndex, academicGroupIndexes, currentLessonsCountPerWeek));

            unusedLessonsCount -= currentLessonsCountPerWeek;
        }

        return new TimetableRequirementsExample(lecturersCount, academicGroupsCount, placesCount, lessonRequirementsList);
    }
}
