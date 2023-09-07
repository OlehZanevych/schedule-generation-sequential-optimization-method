package org.lnu.schedule.generation;

import lombok.AllArgsConstructor;
import org.lnu.schedule.generation.service.ScheduleGenerationDemo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@AllArgsConstructor
@SpringBootApplication
public class ScheduleGenerationApp implements CommandLineRunner {

    private final ScheduleGenerationDemo scheduleGenerationDemo;

    public static void main(String[] args) {
        SpringApplication.run(ScheduleGenerationApp.class, args);
    }

    @Override
    public void run(String... args) {
        scheduleGenerationDemo.runExperiment();
//        scheduleGenerationDemo.generateScheduleRequirementsExample1();
//        scheduleGenerationDemo.generateScheduleRequirementsExample2();
//        scheduleGenerationDemo.generateScheduleRequirementsExample3();
//        scheduleGenerationDemo.generateScheduleRequirementsExample4();

//        scheduleGenerationDemo.scheduleGenerationExample1();
//        scheduleGenerationDemo.scheduleGenerationExample2();
//        scheduleGenerationDemo.scheduleGenerationExample3();
//        scheduleGenerationDemo.scheduleGenerationExample4();
    }
}
