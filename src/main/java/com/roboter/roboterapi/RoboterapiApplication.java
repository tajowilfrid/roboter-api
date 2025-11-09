package com.roboter.roboterapi;

import com.roboter.roboterapi.service.RobotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class RoboterapiApplication {

    public static void main(String[] args) {
        SpringApplication.run(RoboterapiApplication.class, args);
    }

    @Autowired
    private RobotService robotService;

    // "CommandLineRunner" allows code to be executed directly after the application starts 
	// and is used here to populate the database with test robots
    @Bean
    public CommandLineRunner createInitialRobots() {
        return args -> {
            System.out.println("Create Test-Roboter...");
            robotService.createRobot("r1");
            robotService.createRobot("r2");
            robotService.createRobot("r3");
            System.out.println("Roboter r1, r2, r3 created.");
        };
    }
}