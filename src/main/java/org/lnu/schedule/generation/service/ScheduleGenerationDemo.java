package org.lnu.schedule.generation.service;

import lombok.AllArgsConstructor;
import org.lnu.schedule.generation.model.EvaluatedTimetable;
import org.lnu.schedule.generation.model.LessonPlace;
import org.lnu.schedule.generation.model.LessonRequirements;
import org.lnu.schedule.generation.model.LessonTimeSlot;
import org.lnu.schedule.generation.model.TimetableRequirements;
import org.lnu.schedule.generation.model.TimetableRequirementsExample;
import org.lnu.schedule.generation.model.UniversityBuilding;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class ScheduleGenerationDemo {
    private static final List<LessonTimeSlot> DEFAULT_TIME_SLOTS = List.of(
            new LessonTimeSlot(LocalTime.of(8, 30), LocalTime.of(9, 50)),
            new LessonTimeSlot(LocalTime.of(10, 10), LocalTime.of(11, 30)),
            new LessonTimeSlot(LocalTime.of(11, 50), LocalTime.of(13, 10)),
            new LessonTimeSlot(LocalTime.of(13, 30), LocalTime.of(15, 50)),
            new LessonTimeSlot(LocalTime.of(15, 05), LocalTime.of(16, 25)),
            new LessonTimeSlot(LocalTime.of(16, 40), LocalTime.of(18, 00)),
            new LessonTimeSlot(LocalTime.of(18, 10), LocalTime.of(19, 30)),
            new LessonTimeSlot(LocalTime.of(19, 40), LocalTime.of(21, 00))
    );

    private final TimetableGenerationService timetableGenerationService;

    private final TimetableRequirementsGenerationService timetableRequirementsGenerationService;

    public void timetableGenerationTestExample() {
        UniversityBuilding mainBuilding = new UniversityBuilding(1L, "Main building", DEFAULT_TIME_SLOTS);

        List<LessonRequirements> lessonRequirementsList = Arrays.asList(
                new LessonRequirements(1, Set.of(1), 1),
                new LessonRequirements(2, Set.of(2), 2),
                new LessonRequirements(3, Set.of(1, 2), 1.5),
                new LessonRequirements(3, Set.of(1),2.5),
                new LessonRequirements(1, Set.of(1), 4),
                new LessonRequirements(1, Set.of(1), 4),
                new LessonRequirements(1, Set.of(1), 4),
                new LessonRequirements(2, Set.of(1), 4),
                new LessonRequirements(3, Set.of(1), 4),
                new LessonRequirements(1, Set.of(1, 2), 4)
        );

        List<LessonPlace> places = Arrays.asList(
                new LessonPlace(1L, "116", 20, mainBuilding),
                new LessonPlace(2L, "117", 20, mainBuilding),
                new LessonPlace(3L, "118", 25, mainBuilding),
                new LessonPlace(4L, "449", 60, mainBuilding),
                new LessonPlace(5L, "111", 40, mainBuilding),
                new LessonPlace(6L, "265", 35, mainBuilding)
        );

        TimetableRequirements timetableRequirements = new TimetableRequirements(lessonRequirementsList, places, DEFAULT_TIME_SLOTS);
        EvaluatedTimetable bestTimetable = timetableGenerationService.generateTimetable(timetableRequirements);
//        timetableGenerationService.printTimetable(timetableRequirements, bestTimetable);
    }

    public void runExperiment() {
        int coefficient = 1;
        int lecturersCount = coefficient * 50;
        int academicGroupsCount = coefficient * 50;
        int placesCount = coefficient * 50;
        int lessonsCountPerWeek = coefficient * 400;

        TimetableRequirementsExample timetableRequirementsExample = timetableRequirementsGenerationService.generateRandomTimetableRequirements(lecturersCount, academicGroupsCount, placesCount, lessonsCountPerWeek);

        List<LessonRequirements> lessonRequirementsList = timetableRequirementsExample.getLessonRequirementsList();

        List<LessonPlace> places = new ArrayList<>(placesCount);
        for (int i = 0; i < placesCount; ++i) {
            places.add(null);
        }

        TimetableRequirements timetableRequirements = new TimetableRequirements(lessonRequirementsList, places, DEFAULT_TIME_SLOTS);

        EvaluatedTimetable timetable = timetableGenerationService.generateTimetable(timetableRequirements);

        double penalty = timetable.getPenalty();
        System.out.println(penalty);
    }

    public void timetableGenerationExample1() {
        TimetableRequirementsExample example = timetableRequirementsGenerationService.readExampleTimetableRequirementsExample("Example1.json");
        processTimetableGenerationExample(example);
    }

    public void timetableGenerationExample2() {
        TimetableRequirementsExample example = timetableRequirementsGenerationService.readExampleTimetableRequirementsExample("Example2.json");
        processTimetableGenerationExample(example);
    }

    public void timetableGenerationExample3() {
        TimetableRequirementsExample example = timetableRequirementsGenerationService.readExampleTimetableRequirementsExample("Example3.json");
        processTimetableGenerationExample(example);
    }

    public void timetableGenerationExample4() {
        TimetableRequirementsExample example = timetableRequirementsGenerationService.readExampleTimetableRequirementsExample("Example4.json");
        processTimetableGenerationExample(example);
    }

    public void generateTimetableRequirementsExample1() {
        int lecturersCount = 10;
        int academicGroupsCount = 5;
        int placesCount = 10;
        int lessonsCountPerWeek = 200;

        timetableRequirementsGenerationService.generateAndSaveRandomTimetableRequirementsExample(
                "Example1.json", lecturersCount, academicGroupsCount, placesCount, lessonsCountPerWeek);
    }

    public void generateTimetableRequirementsExample2() {
        int lecturersCount = 20;
        int academicGroupsCount = 10;
        int placesCount = 20;
        int lessonsCountPerWeek = 400;

        timetableRequirementsGenerationService.generateAndSaveRandomTimetableRequirementsExample(
                "Example2.json", lecturersCount, academicGroupsCount, placesCount, lessonsCountPerWeek);
    }

    public void generateTimetableRequirementsExample3() {
        int lecturersCount = 40;
        int academicGroupsCount = 20;
        int placesCount = 40;
        int lessonsCountPerWeek = 800;

        timetableRequirementsGenerationService.generateAndSaveRandomTimetableRequirementsExample(
                "Example3.json", lecturersCount, academicGroupsCount, placesCount, lessonsCountPerWeek);
    }

    public void generateTimetableRequirementsExample4() {
        int lecturersCount = 80;
        int academicGroupsCount = 40;
        int placesCount = 80;
        int lessonsCountPerWeek = 1600;

        timetableRequirementsGenerationService.generateAndSaveRandomTimetableRequirementsExample(
                "Example4.json", lecturersCount, academicGroupsCount, placesCount, lessonsCountPerWeek);
    }

    private void processTimetableGenerationExample(TimetableRequirementsExample example) {
        int lecturersCount = example.getLecturersCount();
        int academicGroupsCount = example.getAcademicGroupsCount();
        int placesCount = example.getPlacesCount();
        List<LessonRequirements> lessonRequirementsList = example.getLessonRequirementsList();

        System.out.println("Lecturers count: " + lecturersCount);
        System.out.println("Academic groups count: " + academicGroupsCount);
        System.out.println("Places count: " + placesCount);

        List<LessonPlace> places = new ArrayList<>(placesCount);
        for (int i = 0; i < placesCount; ++i) {
            places.add(null);
        }

        TimetableRequirements timetableRequirements = new TimetableRequirements(lessonRequirementsList, places, DEFAULT_TIME_SLOTS);

        EvaluatedTimetable timetable = timetableGenerationService.generateTimetable(timetableRequirements);

    }
}
