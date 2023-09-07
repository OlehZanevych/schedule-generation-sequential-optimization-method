package org.lnu.schedule.generation.service;

import org.lnu.schedule.generation.model.Day;
import org.lnu.schedule.generation.model.EvaluatedTimetable;
import org.lnu.schedule.generation.model.Lesson;
import org.lnu.schedule.generation.model.LessonPeriodicity;
import org.lnu.schedule.generation.model.LessonPlace;
import org.lnu.schedule.generation.model.LessonRequirements;
import org.lnu.schedule.generation.model.LessonTimeSlot;
import org.lnu.schedule.generation.model.TimetableRequirements;
import org.lnu.schedule.generation.util.LessonTimeSlotUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

@Service
public class TimetableGenerationService {
    private static final Random random = new Random();
    private static final Day[] days = Day.values();

    private static final Comparator<Lesson> LESSON_COMPARATOR = comparing(Lesson::getDay)
            .thenComparing(Lesson::getTimeSlot)
            .thenComparing(Lesson::getPeriodicity);

    private static final Comparator<EvaluatedTimetable> TIMETABLE_COMPARATOR = comparing(EvaluatedTimetable::getPenalty);

    private final LessonTimeSlotUtil lessonTimeSlotUtil;

    private final int iterationsMaxCount;

    private final double lecturerConflictTimeSlotPenalty;
    private final double lecturerConflictTimeSlotPenaltyPower;
    private final double lecturerTimeWindowPenalty;
    private final double lecturerTimeWindowPenaltyPower;
    private final double lecturerTimeWindowPenaltyDayPower;

    private final double academicGroupConflictTimeSlotPenalty;
    private final double academicGroupConflictTimeSlotPenaltyPower;
    private final double academicGroupTimeWindowPenalty;
    private final double academicGroupTimeWindowPenaltyPower;
    private final double academicGroupTimeWindowPenaltyDayPower;

    private final double placeConflictTimeSlotPenalty;
    private final double placeConflictTimeSlotPenaltyPower;

    public TimetableGenerationService(
            LessonTimeSlotUtil lessonTimeSlotUtil,

            @Value("${iterations.max-count}") int iterationsMaxCount,

            @Value("${penalty.lecturer.conflict_time_slot}") double lecturerConflictTimeSlotPenalty,
            @Value("${penalty.lecturer.conflict_time_slot.power}") double lecturerConflictTimeSlotPenaltyPower,
            @Value("${penalty.lecturer.time_window}") double lecturerTimeWindowPenalty,
            @Value("${penalty.lecturer.time_window.power}") double lecturerTimeWindowPenaltyPower,
            @Value("${penalty.lecturer.time_window.power.day}") double lecturerTimeWindowPenaltyDayPower,

            @Value("${penalty.academic_group.conflict_time_slot}") double academicGroupConflictTimeSlotPenalty,
            @Value("${penalty.academic_group.conflict_time_slot.power}") double academicGroupConflictTimeSlotPenaltyPower,
            @Value("${penalty.academic_group.time_window}") double academicGroupTimeWindowPenalty,
            @Value("${penalty.academic_group.time_window.power}") double academicGroupTimeWindowPenaltyPower,
            @Value("${penalty.academic_group.time_window.power.day}") double academicGroupTimeWindowPenaltyDayPower,

            @Value("${penalty.place.conflict_time_slot}") double placeConflictTimeSlotPenalty,
            @Value("${penalty.place.conflict_time_slot.power}") double placeConflictTimeSlotPenaltyPower
    ) {
        this.lessonTimeSlotUtil = lessonTimeSlotUtil;

        this.iterationsMaxCount = iterationsMaxCount;

        this.lecturerConflictTimeSlotPenalty = lecturerConflictTimeSlotPenalty;
        this.lecturerConflictTimeSlotPenaltyPower = lecturerConflictTimeSlotPenaltyPower;
        this.lecturerTimeWindowPenalty = lecturerTimeWindowPenalty;
        this.lecturerTimeWindowPenaltyPower = lecturerTimeWindowPenaltyPower;
        this.lecturerTimeWindowPenaltyDayPower = lecturerTimeWindowPenaltyDayPower;

        this.academicGroupConflictTimeSlotPenalty = academicGroupConflictTimeSlotPenalty;
        this.academicGroupConflictTimeSlotPenaltyPower = academicGroupConflictTimeSlotPenaltyPower;
        this.academicGroupTimeWindowPenalty = academicGroupTimeWindowPenalty;
        this.academicGroupTimeWindowPenaltyPower = academicGroupTimeWindowPenaltyPower;
        this.academicGroupTimeWindowPenaltyDayPower = academicGroupTimeWindowPenaltyDayPower;

        this.placeConflictTimeSlotPenalty = placeConflictTimeSlotPenalty;
        this.placeConflictTimeSlotPenaltyPower = placeConflictTimeSlotPenaltyPower;
    }

    public EvaluatedTimetable generateTimetable(TimetableRequirements timetableRequirements) {
        Lesson[] lessons = generateInitialTimetable(timetableRequirements);
        EvaluatedTimetable timetable = evaluateTimetable(timetableRequirements, lessons);

        timetable = optimizeSchedule(timetableRequirements, timetable);

        return timetable;
    }

    private Lesson[] generateInitialTimetable(TimetableRequirements timetableRequirements) {
        List<LessonRequirements> lessonRequirementsList = timetableRequirements.getLessonRequirementsList();
        List<LessonTimeSlot> timeSlots = timetableRequirements.getTimeSlots();
        List<LessonPlace> lessonPlaces = timetableRequirements.getLessonPlaces();

        List<Lesson> lessons = new ArrayList<>();
        for (int i = 0; i < lessonRequirementsList.size(); ++i) {
            LessonRequirements lessonRequirements = lessonRequirementsList.get(i);
            double numberOfClassesPerWeek = lessonRequirements.getLessonsCountPerWeek();

            while (numberOfClassesPerWeek > 0) {
                Day day = getRandomDay();
                LessonTimeSlot timeSlot = getRandomTimeSlot(timeSlots);
                int lessonPlaceIndex = getRandomPlace(lessonPlaces);

                LessonPeriodicity lessonPeriodicity = getRandomLessonPeriodicity(numberOfClassesPerWeek);

                Lesson lesson = Lesson.builder()
                        .requirementsIndex(i)
                        .day(day)
                        .timeSlot(timeSlot)
                        .placeIndex(lessonPlaceIndex)
                        .periodicity(lessonPeriodicity)
                        .build();

                lessons.add(lesson);

                --numberOfClassesPerWeek;
            }
        }

        return lessons.toArray(Lesson[]::new);
    }

    private EvaluatedTimetable evaluateTimetable(TimetableRequirements timetableRequirements, Lesson[] lessons) {
        List<LessonRequirements> lessonRequirementsList = timetableRequirements.getLessonRequirementsList();

        Map<Integer, Set<Integer>> lecturerLessonsMap = new HashMap<>();
        Map<Integer, Set<Integer>> academicGroupLessonsMap = new HashMap<>();
        Map<Integer, Set<Integer>> placeLessonsMap = new HashMap<>();

        Comparator<Integer> lessonIndexcomparator = (index1, index2) -> {
            int comparisonResult = LESSON_COMPARATOR.compare(lessons[index1], lessons[index2]);
            return comparisonResult == 0 ? index1 - index2 : comparisonResult;
        };

        for (int i = 0; i < lessons.length; ++i) {
            Lesson lesson = lessons[i];

            LessonRequirements lessonRequirements = lessonRequirementsList.get(lesson.getRequirementsIndex());
            int lecturerIndex = lessonRequirements.getLecturerIndex();
            int placeIndex = lesson.getPlaceIndex();
            Set<Integer> academicGroupIndexes = lessonRequirements.getAcademicGroupIndexes();


            Set<Integer> lecturerLessonIndexes = lecturerLessonsMap.get(lecturerIndex);
            if (lecturerLessonIndexes == null) {
                lecturerLessonIndexes = new TreeSet<>(lessonIndexcomparator);
                lecturerLessonsMap.put(lecturerIndex, lecturerLessonIndexes);
            }
            lecturerLessonIndexes.add(i);

            for (int academicGroupIndex : academicGroupIndexes) {
                Set<Integer> academicGroupLessonIndexes = academicGroupLessonsMap.get(academicGroupIndex);
                if (academicGroupLessonIndexes == null) {
                    academicGroupLessonIndexes = new TreeSet<>(lessonIndexcomparator);
                    academicGroupLessonsMap.put(academicGroupIndex, academicGroupLessonIndexes);
                }
                academicGroupLessonIndexes.add(i);
            }


            Set<Integer> placeLessonIndexes = placeLessonsMap.get(placeIndex);
            if (placeLessonIndexes == null) {
                placeLessonIndexes = new TreeSet<>(lessonIndexcomparator);
                placeLessonsMap.put(placeIndex, placeLessonIndexes);
            }
            placeLessonIndexes.add(i);
        }

        double penalty = 0;
        double[] lessonPenalties = new double[lessons.length];

        for (Entry<Integer, Set<Integer>> lecturerLessonsEntry : lecturerLessonsMap.entrySet()) {
            int lecturerIndex = lecturerLessonsEntry.getKey();
            Set<Integer> lessonIndexes = lecturerLessonsEntry.getValue();

            double conflictTimeSlotsCount = 0;
            double timeWindowCount = 0;

            Iterator<Integer> lessonIndexIterator = lessonIndexes.iterator();
            Lesson prevLesson = null;
            int firstLessonIndex = 0;
            List<Integer> conflictTimeSlotLessonIndexes = new ArrayList<>();
            List<Integer> timeWindowLessonIndexes = new ArrayList<>();
            while (lessonIndexIterator.hasNext()) {
                double timeWindowDayCount = 0;

                List<Integer> dayLessonIndexes = new ArrayList<>();
                if (firstLessonIndex != 0) {
                    dayLessonIndexes.add(firstLessonIndex);
                }

                while (lessonIndexIterator.hasNext()) {
                    int lessonIndex = lessonIndexIterator.next();
                    Lesson lesson = lessons[lessonIndex];
                    LessonTimeSlot lessonTimeSlot = lesson.getTimeSlot();
                    LessonPeriodicity lessonPeriodicity = lesson.getPeriodicity();

                    boolean isNewDay = false;
                    if (prevLesson != null) {
                        if (lesson.getDay() == prevLesson.getDay()) {
                            dayLessonIndexes.add(lessonIndex);

                            LessonTimeSlot prevLessonTimeSlot = prevLesson.getTimeSlot();
                            LessonPeriodicity prevLessonPeriodicity = prevLesson.getPeriodicity();

                            if (lessonTimeSlotUtil.isConflict(prevLessonTimeSlot, lessonTimeSlot)) {
                                if (lessonPeriodicity == prevLessonPeriodicity) {
                                    if (lessonPeriodicity == LessonPeriodicity.WEEKLY) {
                                        ++conflictTimeSlotsCount;
                                        conflictTimeSlotLessonIndexes.add(lessonIndex);
                                    } else {
                                        conflictTimeSlotsCount += 0.5;
                                        conflictTimeSlotLessonIndexes.add(lessonIndex);
                                    }
                                } else if (lessonPeriodicity == LessonPeriodicity.WEEKLY || prevLessonPeriodicity == LessonPeriodicity.WEEKLY) {
                                    conflictTimeSlotsCount += 0.5;
                                    conflictTimeSlotLessonIndexes.add(lessonIndex);
                                }
                            }

                            if (lessonTimeSlotUtil.isWindow(prevLessonTimeSlot, lessonTimeSlot)) {
                                ++timeWindowDayCount;
                            }
                        } else {
                            isNewDay = true;
                            firstLessonIndex = lessonIndex;
                        }
                    }

                    prevLesson = lesson;
                    if (isNewDay) {
                        break;
                    }
                }

                if (timeWindowDayCount > 0) {
                    timeWindowDayCount = Math.pow(timeWindowDayCount, lecturerTimeWindowPenaltyDayPower);

                    timeWindowCount += timeWindowDayCount;

                    timeWindowLessonIndexes.addAll(dayLessonIndexes);
                }
            }

            if (conflictTimeSlotsCount > 0) {
                conflictTimeSlotsCount = Math.pow(conflictTimeSlotsCount, lecturerConflictTimeSlotPenaltyPower);

                double conflictTimeSlotPenalty = conflictTimeSlotsCount * lecturerConflictTimeSlotPenalty;
                penalty += conflictTimeSlotPenalty;

                double conflictTimeSlotPenaltyPerLesson = conflictTimeSlotPenalty / conflictTimeSlotLessonIndexes.size();
                for (int lessonIndex : conflictTimeSlotLessonIndexes) {
                    lessonPenalties[lessonIndex] += conflictTimeSlotPenaltyPerLesson;
                }
            }

            if (timeWindowCount > 0) {
                timeWindowCount = Math.pow(timeWindowCount, lecturerTimeWindowPenaltyPower);

                double timeWindowPenalty = timeWindowCount * lecturerTimeWindowPenalty;
                penalty += timeWindowPenalty;

                double timeWindowPenaltyPerLesson = timeWindowPenalty / timeWindowLessonIndexes.size();
                for (int lessonIndex : timeWindowLessonIndexes) {
                    lessonPenalties[lessonIndex] += timeWindowPenaltyPerLesson;
                }
            }
        }

        for (Entry<Integer, Set<Integer>> academicGroupLessonsEntry : academicGroupLessonsMap.entrySet()) {
            int academicGroupIndex = academicGroupLessonsEntry.getKey();
            Set<Integer> lessonIndexes = academicGroupLessonsEntry.getValue();

            double conflictTimeSlotsCount = 0;
            double timeWindowCount = 0;

            Iterator<Integer> lessonIndexIterator = lessonIndexes.iterator();
            Lesson prevLesson = null;
            int firstLessonIndex = 0;
            List<Integer> conflictTimeSlotLessonIndexes = new ArrayList<>();
            List<Integer> timeWindowLessonIndexes = new ArrayList<>();
            while (lessonIndexIterator.hasNext()) {
                double timeWindowDayCount = 0;

                List<Integer> dayLessonIndexes = new ArrayList<>();
                if (firstLessonIndex != 0) {
                    dayLessonIndexes.add(firstLessonIndex);
                }

                while (lessonIndexIterator.hasNext()) {
                    int lessonIndex = lessonIndexIterator.next();
                    Lesson lesson = lessons[lessonIndex];
                    LessonTimeSlot lessonTimeSlot = lesson.getTimeSlot();
                    LessonPeriodicity lessonPeriodicity = lesson.getPeriodicity();

                    boolean isNewDay = false;
                    if (prevLesson != null) {
                        if (lesson.getDay() == prevLesson.getDay()) {
                            dayLessonIndexes.add(lessonIndex);

                            LessonTimeSlot prevLessonTimeSlot = prevLesson.getTimeSlot();
                            LessonPeriodicity prevLessonPeriodicity = prevLesson.getPeriodicity();

                            if (lessonTimeSlotUtil.isConflict(prevLessonTimeSlot, lessonTimeSlot)) {
                                if (lessonPeriodicity == prevLessonPeriodicity) {
                                    if (lessonPeriodicity == LessonPeriodicity.WEEKLY) {
                                        ++conflictTimeSlotsCount;
                                        conflictTimeSlotLessonIndexes.add(lessonIndex);
                                    } else {
                                        conflictTimeSlotsCount += 0.5;
                                        conflictTimeSlotLessonIndexes.add(lessonIndex);
                                    }
                                } else if (lessonPeriodicity == LessonPeriodicity.WEEKLY || prevLessonPeriodicity == LessonPeriodicity.WEEKLY) {
                                    conflictTimeSlotsCount += 0.5;
                                    conflictTimeSlotLessonIndexes.add(lessonIndex);
                                }
                            }

                            if (lessonTimeSlotUtil.isWindow(prevLessonTimeSlot, lessonTimeSlot)) {
                                ++timeWindowDayCount;
                            }
                        } else {
                            isNewDay = true;
                            firstLessonIndex = lessonIndex;
                        }
                    }

                    prevLesson = lesson;
                    if (isNewDay) {
                        break;
                    }
                }

                if (timeWindowDayCount > 0) {
                    timeWindowDayCount = Math.pow(timeWindowDayCount, academicGroupTimeWindowPenaltyDayPower);

                    timeWindowCount += timeWindowDayCount;

                    timeWindowLessonIndexes.addAll(dayLessonIndexes);
                }
            }

            if (conflictTimeSlotsCount > 0) {
                conflictTimeSlotsCount = Math.pow(conflictTimeSlotsCount, academicGroupConflictTimeSlotPenaltyPower);

                double conflictTimeSlotPenalty = conflictTimeSlotsCount * academicGroupConflictTimeSlotPenalty;
                penalty += conflictTimeSlotPenalty;

                double conflictTimeSlotPenaltyPerLesson = conflictTimeSlotPenalty / conflictTimeSlotLessonIndexes.size();
                for (int lessonIndex : conflictTimeSlotLessonIndexes) {
                    lessonPenalties[lessonIndex] += conflictTimeSlotPenaltyPerLesson;
                }
            }

            if (timeWindowCount > 0) {
                timeWindowCount = Math.pow(timeWindowCount, academicGroupTimeWindowPenaltyPower);

                double timeWindowPenalty = timeWindowCount * academicGroupTimeWindowPenalty;
                penalty += timeWindowPenalty;

                double timeWindowPenaltyPerLesson = timeWindowPenalty / timeWindowLessonIndexes.size();
                for (int lessonIndex : timeWindowLessonIndexes) {
                    lessonPenalties[lessonIndex] += timeWindowPenaltyPerLesson;
                }
            }
        }

        for (Entry<Integer, Set<Integer>> placeLessonsEntry : placeLessonsMap.entrySet()) {
            int placeIndex = placeLessonsEntry.getKey();
            Set<Integer> lessonIndexes = placeLessonsEntry.getValue();

            double conflictTimeSlotsCount = 0;

            Iterator<Integer> lessonIndexIterator = lessonIndexes.iterator();
            Lesson prevLesson = null;
            List<Integer> conflictTimeSlotLessonIndexes = new ArrayList<>();
            while (lessonIndexIterator.hasNext()) {
                while (lessonIndexIterator.hasNext()) {
                    int lessonIndex = lessonIndexIterator.next();
                    Lesson lesson = lessons[lessonIndex];
                    LessonTimeSlot lessonTimeSlot = lesson.getTimeSlot();
                    LessonPeriodicity lessonPeriodicity = lesson.getPeriodicity();

                    boolean isNewDay = false;
                    if (prevLesson != null) {
                        LessonTimeSlot prevLessonTimeSlot = prevLesson.getTimeSlot();

                        if (lesson.getDay() == prevLesson.getDay()) {
                            LessonPeriodicity prevLessonPeriodicity = prevLesson.getPeriodicity();

                            if (lessonTimeSlotUtil.isConflict(prevLessonTimeSlot, lessonTimeSlot)) {
                                if (lessonPeriodicity == prevLessonPeriodicity) {
                                    if (lessonPeriodicity == LessonPeriodicity.WEEKLY) {
                                        ++conflictTimeSlotsCount;
                                        conflictTimeSlotLessonIndexes.add(lessonIndex);
                                    } else {
                                        conflictTimeSlotsCount += 0.5;
                                        conflictTimeSlotLessonIndexes.add(lessonIndex);
                                    }
                                } else if (lessonPeriodicity == LessonPeriodicity.WEEKLY || prevLessonPeriodicity == LessonPeriodicity.WEEKLY) {
                                    conflictTimeSlotsCount += 0.5;
                                    conflictTimeSlotLessonIndexes.add(lessonIndex);
                                }
                            }
                        } else {
                            isNewDay = true;
                        }
                    }

                    prevLesson = lesson;
                    if (isNewDay) {
                        break;
                    }
                }
            }

            if (conflictTimeSlotsCount > 0) {
                conflictTimeSlotsCount = Math.pow(conflictTimeSlotsCount, placeConflictTimeSlotPenaltyPower);

                double conflictTimeSlotPenalty = conflictTimeSlotsCount * placeConflictTimeSlotPenalty;
                penalty += conflictTimeSlotPenalty;

                double conflictTimeSlotPenaltyPerLesson = conflictTimeSlotPenalty / conflictTimeSlotLessonIndexes.size();
                for (int lessonIndex : conflictTimeSlotLessonIndexes) {
                    lessonPenalties[lessonIndex] += conflictTimeSlotPenaltyPerLesson;
                }
            }
        }

        return new EvaluatedTimetable(lessons, penalty, lessonPenalties, lecturerLessonsMap, academicGroupLessonsMap, placeLessonsMap, lessonIndexcomparator);
//        return new EvaluatedTimetable(lessons, penalty, lessonPenalties);
    }


    private EvaluatedTimetable optimizeSchedule(TimetableRequirements timetableRequirements, EvaluatedTimetable schedule) {
        System.out.println("Initial penalty: " + schedule.getPenalty());

        EvaluatedTimetable optimizedSchedule = schedule;
        int iterNum = 0;
        while (optimizedSchedule.getPenalty() > 0 && iterNum < iterationsMaxCount) {
            System.out.println("Iteration: " + iterNum);

            optimizedSchedule = optimizeScheduleByDayAndTimeSlot(timetableRequirements, optimizedSchedule);
            System.out.println(optimizedSchedule.getPenalty());
            if (optimizedSchedule.getPenalty() == 0) {
                break;
            }

            optimizedSchedule = optimizeScheduleByPlace(timetableRequirements, optimizedSchedule);
            System.out.println(optimizedSchedule.getPenalty());

            ++iterNum;
        }
        ;

        return optimizedSchedule;
    }

    private EvaluatedTimetable optimizeScheduleByDayAndTimeSlot(TimetableRequirements timetableRequirements, EvaluatedTimetable timetable) {
        List<LessonRequirements> lessonRequirementsList = timetableRequirements.getLessonRequirementsList();

        Lesson[] lessons = timetable.getLessons();
        double[] lessonPenalties = timetable.getLessonPenalties();
        Map<Integer, Set<Integer>> lecturerLessonsMap = timetable.getLecturerLessonsMap();
        Map<Integer, Set<Integer>> academicGroupLessonsMap = timetable.getAcademicGroupLessonsMap();
        Map<Integer, Set<Integer>> placeLessonsMap = timetable.getPlaceLessonsMap();

        Comparator<Integer> lessonPenaltiesComparator = (lessonIndex1, lessonIndex2) ->
                Double.compare(lessonPenalties[lessonIndex2], lessonPenalties[lessonIndex1]);

        int lessonsCount = lessons.length;

        Integer[] lessonIndexes = new Integer[lessonsCount];
        for (int i = 0; i < lessonIndexes.length; ++i) {
            lessonIndexes[i] = i;
        }
        Arrays.sort(lessonIndexes, lessonPenaltiesComparator);

        for (int lessonIndex : lessonIndexes) {
            if (lessonPenalties[lessonIndex] == 0) {
                break;
            }

            Lesson lesson = lessons[lessonIndex];
            LessonRequirements lessonRequirements = lessonRequirementsList.get(lesson.getRequirementsIndex());

            int lecturerIndex = lessonRequirements.getLecturerIndex();
            Set<Integer> academicGroupIndexes = lessonRequirements.getAcademicGroupIndexes();
            int placeIndex = lesson.getPlaceIndex();

            Set<Integer> lecturerLessonIndexes = lecturerLessonsMap.get(lecturerIndex);
            List<Set<Integer>> academicGroupLessonIndexesList = academicGroupIndexes.stream()
                    .map(academicGroupIndex -> academicGroupLessonsMap.get(academicGroupIndex))
                    .collect(Collectors.toList());
            Set<Integer> placeLessonIndexes = placeLessonsMap.get(placeIndex);

            optimizeLessonDayAndTimeSlot(timetableRequirements, lessons, lessonIndex, lecturerLessonIndexes, academicGroupLessonIndexesList,
                    placeLessonIndexes);
        }

        EvaluatedTimetable repairedTimetable = evaluateTimetable(timetableRequirements, lessons);

        return repairedTimetable;
    }

    private EvaluatedTimetable optimizeScheduleByPlace(TimetableRequirements timetableRequirements, EvaluatedTimetable timetable) {
        List<LessonRequirements> lessonRequirementsList = timetableRequirements.getLessonRequirementsList();

        Lesson[] lessons = timetable.getLessons();
        double[] lessonPenalties = timetable.getLessonPenalties();
        Map<Integer, Set<Integer>> lecturerLessonsMap = timetable.getLecturerLessonsMap();
        Map<Integer, Set<Integer>> academicGroupLessonsMap = timetable.getAcademicGroupLessonsMap();

        Comparator<Integer> lessonPenaltiesComparator = (lessonIndex1, lessonIndex2) ->
                Double.compare(lessonPenalties[lessonIndex2], lessonPenalties[lessonIndex1]);

        int lessonsCount = lessons.length;

        Integer[] lessonIndexes = new Integer[lessonsCount];
        for (int i = 0; i < lessonIndexes.length; ++i) {
            lessonIndexes[i] = i;
        }
        Arrays.sort(lessonIndexes, lessonPenaltiesComparator);

        for (int lessonIndex : lessonIndexes) {
            if (lessonPenalties[lessonIndex] == 0) {
                break;
            }

            Lesson lesson = lessons[lessonIndex];
            LessonRequirements lessonRequirements = lessonRequirementsList.get(lesson.getRequirementsIndex());

            int lecturerIndex = lessonRequirements.getLecturerIndex();
            Set<Integer> academicGroupIndexes = lessonRequirements.getAcademicGroupIndexes();

            Set<Integer> lecturerLessonIndexes = lecturerLessonsMap.get(lecturerIndex);
            List<Set<Integer>> academicGroupLessonIndexesList = academicGroupIndexes.stream()
                    .map(academicGroupIndex -> academicGroupLessonsMap.get(academicGroupIndex))
                    .collect(Collectors.toList());

            optimizeLessonPlace(timetableRequirements, timetable, lessonIndex, lecturerLessonIndexes, academicGroupLessonIndexesList);

//            System.out.println("lessonIndex: " + lessonIndex);
//            ++count;
//            if (count % 100 == 0) {
//                System.out.println(count);
//            }

        }
//        System.out.println("Work");

        return evaluateTimetable(timetableRequirements, lessons);
    }

    private void optimizeLessonDayAndTimeSlot(TimetableRequirements timetableRequirements, Lesson[] lessons, int lessonIndex,
                                              Set<Integer> lecturerLessonIndexes, List<Set<Integer>> academicGroupLessonIndexesList,
                                              Set<Integer> placeLessonIndexes) {

        Lesson lesson = lessons[lessonIndex];

        List<LessonTimeSlot> timeSlots = timetableRequirements.getTimeSlots();

        double minPenalty = Double.MAX_VALUE;
        Day minDay = Day.MONDAY;
        LessonTimeSlot minTimeSlot = timeSlots.get(0);

        dayLoop:
        for (Day day : days) {
            for (LessonTimeSlot timeSlot : timeSlots) {
                changeTime(lecturerLessonIndexes, academicGroupLessonIndexesList, placeLessonIndexes, lesson,
                        lessonIndex, day, timeSlot);

                double penalty = calcLocalPenalty(lecturerLessonIndexes, academicGroupLessonIndexesList,
                        placeLessonIndexes, lessons);

                if (penalty < minPenalty) {
                    minDay = day;
                    minTimeSlot = timeSlot;

                    minPenalty = penalty;
                }

                if (penalty == 0) {
                    break dayLoop;
                }
            }
        }

        changeTime(lecturerLessonIndexes, academicGroupLessonIndexesList, placeLessonIndexes, lesson,
                lessonIndex, minDay, minTimeSlot);
    }

    private void optimizeLessonPlace(TimetableRequirements timetableRequirements, EvaluatedTimetable timetable, int lessonIndex,
                                     Set<Integer> lecturerLessonIndexes, List<Set<Integer>> academicGroupLessonIndexesList) {

        Lesson[] lessons = timetable.getLessons();
        Map<Integer, Set<Integer>> placeLessonsMap = timetable.getPlaceLessonsMap();
        Comparator<Integer> lessonIndexcomparator = timetable.getLessonIndexcomparator();

        Lesson lesson = lessons[lessonIndex];

        int placesCount = timetableRequirements.getLessonPlaces().size();


        int minPlaceIndex = lesson.getPlaceIndex();
        Set<Integer> placeLessonIndexes = placeLessonsMap.get(minPlaceIndex);
        double minPenalty = calcLocalPenalty(lecturerLessonIndexes, academicGroupLessonIndexesList,
                placeLessonIndexes, lessons);
        int prevPlaceIndex = minPlaceIndex;

        for (int placeIndex = 0; placeIndex < placesCount; ++placeIndex) {
            if (placeIndex == minPlaceIndex) {
                continue;
            }

            placeLessonIndexes = changePlace(placeLessonsMap, lesson, lessonIndex, prevPlaceIndex, placeIndex, lessonIndexcomparator);

            double penalty = calcLocalPenalty(lecturerLessonIndexes, academicGroupLessonIndexesList,
                    placeLessonIndexes, lessons);

            if (penalty < minPenalty) {
                minPlaceIndex = placeIndex;
                minPenalty = penalty;
            }

            prevPlaceIndex = placeIndex;

            if (penalty == 0) {
                break;
            }
        }

        if (minPlaceIndex != prevPlaceIndex) {
            changePlace(placeLessonsMap, lesson, lessonIndex, prevPlaceIndex, minPlaceIndex, lessonIndexcomparator);
        }
    }


    private double calcLocalPenalty(Set<Integer> lecturerLessonIndexes, List<Set<Integer>> academicGroupLessonIndexesList,
                                    Set<Integer> placeLessonIndexes, Lesson[] lessons) {
        double penalty = 0;

        {
            double conflictTimeSlotsCount = 0;
            double timeWindowCount = 0;

            Iterator<Integer> lessonIndexIterator = lecturerLessonIndexes.iterator();
            Lesson prevLesson = null;
            while (lessonIndexIterator.hasNext()) {
                double timeWindowDayCount = 0;

                while (lessonIndexIterator.hasNext()) {
                    int lessonIndex = lessonIndexIterator.next();
                    Lesson lesson = lessons[lessonIndex];
                    LessonTimeSlot lessonTimeSlot = lesson.getTimeSlot();
                    LessonPeriodicity lessonPeriodicity = lesson.getPeriodicity();

                    boolean isNewDay = false;
                    if (prevLesson != null) {
                        if (lesson.getDay() == prevLesson.getDay()) {
                            LessonTimeSlot prevLessonTimeSlot = prevLesson.getTimeSlot();
                            LessonPeriodicity prevLessonPeriodicity = prevLesson.getPeriodicity();

                            if (lessonTimeSlotUtil.isConflict(prevLessonTimeSlot, lessonTimeSlot)) {
                                if (lessonPeriodicity == prevLessonPeriodicity) {
                                    if (lessonPeriodicity == LessonPeriodicity.WEEKLY) {
                                        ++conflictTimeSlotsCount;
                                    } else {
                                        conflictTimeSlotsCount += 0.5;
                                    }
                                } else if (lessonPeriodicity == LessonPeriodicity.WEEKLY || prevLessonPeriodicity == LessonPeriodicity.WEEKLY) {
                                    conflictTimeSlotsCount += 0.5;
                                }
                            }

                            if (lessonTimeSlotUtil.isWindow(prevLessonTimeSlot, lessonTimeSlot)) {
                                ++timeWindowDayCount;
                            }
                        } else {
                            isNewDay = true;
                        }
                    }

                    prevLesson = lesson;
                    if (isNewDay) {
                        break;
                    }
                }

                if (timeWindowDayCount > 0) {
                    timeWindowDayCount = Math.pow(timeWindowDayCount, lecturerTimeWindowPenaltyDayPower);

                    timeWindowCount += timeWindowDayCount;
                }
            }

            if (conflictTimeSlotsCount > 0) {
                conflictTimeSlotsCount = Math.pow(conflictTimeSlotsCount, lecturerConflictTimeSlotPenaltyPower);

                double conflictTimeSlotPenalty = conflictTimeSlotsCount * lecturerConflictTimeSlotPenalty;
                penalty += conflictTimeSlotPenalty;
            }

            if (timeWindowCount > 0) {
                timeWindowCount = Math.pow(timeWindowCount, lecturerTimeWindowPenaltyPower);

                double timeWindowPenalty = timeWindowCount * lecturerTimeWindowPenalty;
                penalty += timeWindowPenalty;
            }
        }

        for (Set<Integer> lessonIndexes : academicGroupLessonIndexesList) {
            double conflictTimeSlotsCount = 0;
            double timeWindowCount = 0;

            Iterator<Integer> lessonIndexIterator = lessonIndexes.iterator();
            Lesson prevLesson = null;
            while (lessonIndexIterator.hasNext()) {
                double timeWindowDayCount = 0;

                while (lessonIndexIterator.hasNext()) {
                    int lessonIndex = lessonIndexIterator.next();
                    Lesson lesson = lessons[lessonIndex];
                    LessonTimeSlot lessonTimeSlot = lesson.getTimeSlot();
                    LessonPeriodicity lessonPeriodicity = lesson.getPeriodicity();

                    boolean isNewDay = false;
                    if (prevLesson != null) {
                        if (lesson.getDay() == prevLesson.getDay()) {
                            LessonTimeSlot prevLessonTimeSlot = prevLesson.getTimeSlot();
                            LessonPeriodicity prevLessonPeriodicity = prevLesson.getPeriodicity();

                            if (lessonTimeSlotUtil.isConflict(prevLessonTimeSlot, lessonTimeSlot)) {
                                if (lessonPeriodicity == prevLessonPeriodicity) {
                                    if (lessonPeriodicity == LessonPeriodicity.WEEKLY) {
                                        ++conflictTimeSlotsCount;
                                    } else {
                                        conflictTimeSlotsCount += 0.5;
                                    }
                                } else if (lessonPeriodicity == LessonPeriodicity.WEEKLY || prevLessonPeriodicity == LessonPeriodicity.WEEKLY) {
                                    conflictTimeSlotsCount += 0.5;
                                }
                            }

                            if (lessonTimeSlotUtil.isWindow(prevLessonTimeSlot, lessonTimeSlot)) {
                                ++timeWindowDayCount;
                            }
                        } else {
                            isNewDay = true;
                        }
                    }

                    prevLesson = lesson;
                    if (isNewDay) {
                        break;
                    }
                }

                if (timeWindowDayCount > 0) {
                    timeWindowDayCount = Math.pow(timeWindowDayCount, academicGroupTimeWindowPenaltyDayPower);

                    timeWindowCount += timeWindowDayCount;
                }
            }

            if (conflictTimeSlotsCount > 0) {
                conflictTimeSlotsCount = Math.pow(conflictTimeSlotsCount, academicGroupConflictTimeSlotPenaltyPower);

                double conflictTimeSlotPenalty = conflictTimeSlotsCount * academicGroupConflictTimeSlotPenalty;
                penalty += conflictTimeSlotPenalty;
            }

            if (timeWindowCount > 0) {
                timeWindowCount = Math.pow(timeWindowCount, academicGroupTimeWindowPenaltyPower);

                double timeWindowPenalty = timeWindowCount * academicGroupTimeWindowPenalty;
                penalty += timeWindowPenalty;
            }
        }

        {
            double conflictTimeSlotsCount = 0;

            Iterator<Integer> lessonIndexIterator = placeLessonIndexes.iterator();
            Lesson prevLesson = null;
            while (lessonIndexIterator.hasNext()) {
                while (lessonIndexIterator.hasNext()) {
                    int lessonIndex = lessonIndexIterator.next();
                    Lesson lesson = lessons[lessonIndex];
                    LessonTimeSlot lessonTimeSlot = lesson.getTimeSlot();
                    LessonPeriodicity lessonPeriodicity = lesson.getPeriodicity();

                    boolean isNewDay = false;
                    if (prevLesson != null) {
                        LessonTimeSlot prevLessonTimeSlot = prevLesson.getTimeSlot();

                        if (lesson.getDay() == prevLesson.getDay()) {
                            LessonPeriodicity prevLessonPeriodicity = prevLesson.getPeriodicity();

                            if (lessonTimeSlotUtil.isConflict(prevLessonTimeSlot, lessonTimeSlot)) {
                                if (lessonPeriodicity == prevLessonPeriodicity) {
                                    if (lessonPeriodicity == LessonPeriodicity.WEEKLY) {
                                        ++conflictTimeSlotsCount;
                                    } else {
                                        conflictTimeSlotsCount += 0.5;
                                    }
                                } else if (lessonPeriodicity == LessonPeriodicity.WEEKLY || prevLessonPeriodicity == LessonPeriodicity.WEEKLY) {
                                    conflictTimeSlotsCount += 0.5;
                                }
                            }
                        } else {
                            isNewDay = true;
                        }
                    }

                    prevLesson = lesson;
                    if (isNewDay) {
                        break;
                    }
                }
            }

            if (conflictTimeSlotsCount > 0) {
                conflictTimeSlotsCount = Math.pow(conflictTimeSlotsCount, placeConflictTimeSlotPenaltyPower);

                double conflictTimeSlotPenalty = conflictTimeSlotsCount * placeConflictTimeSlotPenalty;
                penalty += conflictTimeSlotPenalty;
            }
        }

        return penalty;
    }

    public void printTimetable(TimetableRequirements timetableRequirements, EvaluatedTimetable timetable) {
        for (Lesson lesson : timetable.getLessons()) {
            System.out.printf("%3d | %9s | %s | %3d | %s \n",
                    lesson.getRequirementsIndex(),
                    lesson.getDay(),
                    lesson.getTimeSlot(),
                    lesson.getPlaceIndex(),
                    lesson.getPeriodicity()
            );
        }
    }

    private void changeTime(Set<Integer> lecturerLessonIndexes, List<Set<Integer>> academicGroupLessonIndexesList,
                            Set<Integer> placeLessonIndexes, Lesson lesson, int lessonIndex, Day day, LessonTimeSlot timeSlot) {
        lecturerLessonIndexes.remove(lessonIndex);
        academicGroupLessonIndexesList.forEach(academicGroupLessonIndexes -> {
            academicGroupLessonIndexes.remove(lessonIndex);
        });
        placeLessonIndexes.remove(lessonIndex);

        lesson.setDay(day);
        lesson.setTimeSlot(timeSlot);

        lecturerLessonIndexes.add(lessonIndex);
        academicGroupLessonIndexesList.forEach(academicGroupLessonIndexes -> {
            academicGroupLessonIndexes.add(lessonIndex);
        });
        placeLessonIndexes.add(lessonIndex);
    }

    private Set<Integer> changePlace(Map<Integer, Set<Integer>> placeLessonsMap, Lesson lesson, int lessonIndex,
                                     int prevPlaceIndex, int placeIndex, Comparator<Integer> lessonIndexcomparator) {

        placeLessonsMap.get(prevPlaceIndex).remove(lessonIndex);

        lesson.setPlaceIndex(placeIndex);

        Set<Integer> placeLessonIndexes = placeLessonsMap.get(placeIndex);
        if (placeLessonIndexes == null) {
            placeLessonIndexes = new TreeSet<>(lessonIndexcomparator);
            placeLessonsMap.put(placeIndex, placeLessonIndexes);
        }

        placeLessonIndexes.add(lessonIndex);

        return placeLessonIndexes;
    }

    private LessonPeriodicity getRandomLessonPeriodicity(double numberOfClassesPerWeek) {
        if (numberOfClassesPerWeek >= 1) {
            return LessonPeriodicity.WEEKLY;
        }

        return getRandomLessonPeriodicity();
    }

    private Day getRandomDay() {
        return days[random.nextInt(days.length)];
    }

    private LessonTimeSlot getRandomTimeSlot(List<LessonTimeSlot> timeSlots) {
        return timeSlots.get(random.nextInt(timeSlots.size()));
    }

    private int getRandomPlace(List<LessonPlace> lessonPlaces) {
        return random.nextInt(lessonPlaces.size());
    }

    private LessonPeriodicity getRandomLessonPeriodicity() {
        return random.nextBoolean() ? LessonPeriodicity.NUMERATOR : LessonPeriodicity.DENOMINATOR;
    }
}
