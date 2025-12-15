package com.roboter.roboterapi.repository;

import com.roboter.roboterapi.model.Robot;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryRobotRepository {

    // Thread-safe Map to store Robots by String ID and handle concurrent access
    private final Map<String, Robot> robots = new ConcurrentHashMap<>();

    // Saves a robot to the repository (new or updated)
    public Robot save(Robot robot) {
        robots.put(robot.getId(), robot);
        return robot;
    }

    // Finds a robot by its ID
    // Optional<Robot> is a container that either contains a Robot or is empty and helps to avoid "NullPointerExceptions"
    public Optional<Robot> findById(String id) {
        return Optional.ofNullable(robots.get(id));
    }

    // Returns all robots in the repository
    public Collection<Robot> findAll() {
        return robots.values();
    }

    public void deleteAll() {
        robots.clear();
    }
}