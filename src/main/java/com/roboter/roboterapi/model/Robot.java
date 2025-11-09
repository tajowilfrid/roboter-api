package com.roboter.roboterapi.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class Robot extends RepresentationModel<Robot> {

    // Unique ID of the robot
    private String id;
    
    // Current position using our Position class
    private Position position;
    
    // Current energy level
    private int energy;
    
    // The inventory stores the item IDs as strings
    private List<String> inventory;
    
    // A list of all actions the robot has performed
    // private List<String> actionHistory;

    // From List<String> to List<RobotAction>
    private List<RobotAction> actionHistory;

    // Constructor to create a new robot with default values
    public Robot(String id) {
        this.id = id;
        this.position = new Position(0, 0); // Start at (0,0)
        this.energy = 100; // Starts with full energy
        this.inventory = new ArrayList<>();
        this.actionHistory = new ArrayList<>(); // initializes the new list
    }

    // logAction is been adapted to store structured data
    public void logAction(String action, String detail) {
        // Creates a new ID (1, 2, 3...)
        long newId = this.actionHistory.isEmpty() ? 1 : 
                     this.actionHistory.get(this.actionHistory.size() - 1).getId() + 1;
        
        this.actionHistory.add(
            new RobotAction(newId, action, detail, Instant.now())
        );
    }
    
    // method to remove all links from Robot to fix duplicate links bug
    @Override
    @org.springframework.lang.NonNull
    public Robot removeLinks() {
        super.removeLinks();
        return this;
    }
}