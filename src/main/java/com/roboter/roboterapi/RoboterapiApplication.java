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
    private RobotService robotService; // Wir holen uns den Service

    // Ein "CommandLineRunner" ist Code, der direkt nach dem Start der App ausgeführt wird.
    // Wir nutzen ihn, um unsere "Datenbank" mit Test-Robotern zu füllen.
    @Bean
    public CommandLineRunner createInitialRobots() {
        return args -> {
            System.out.println("Erstelle Test-Roboter...");
            robotService.createRobot("r1");
            robotService.createRobot("r2");
            robotService.createRobot("r3");
            System.out.println("Roboter r1, r2, r3 erstellt.");
        };
    }
}