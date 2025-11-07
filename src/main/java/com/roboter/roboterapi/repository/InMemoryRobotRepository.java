package com.roboter.roboterapi.repository;

import com.roboter.roboterapi.model.Robot;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component // Spring, bitte erstelle ein Objekt dieser Klasse beim Start
public class InMemoryRobotRepository {

    // Unsere "Datenbank".
    // Eine ConcurrentHashMap ist threadsicher, falls mehrere Anfragen gleichzeitig kommen.
    // Wir speichern Roboter unter ihrer ID (String).
    private final Map<String, Robot> robots = new ConcurrentHashMap<>();

    // Speichert einen Roboter (neu oder Update)
    public Robot save(Robot robot) {
        robots.put(robot.getId(), robot);
        return robot;
    }

    // Findet einen Roboter anhand seiner ID.
    // Optional<Robot> ist ein Container, der entweder einen Roboter enthält ODER leer ist.
    // Das hilft, "NullPointerExceptions" zu vermeiden.
    public Optional<Robot> findById(String id) {
        return Optional.ofNullable(robots.get(id));
    }

    // Gibt alle Roboter zurück (brauchen wir nicht für die Aufgabe, aber nützlich)
    public Collection<Robot> findAll() {
        return robots.values();
    }
}