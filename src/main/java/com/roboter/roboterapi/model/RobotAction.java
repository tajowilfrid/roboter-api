package com.roboter.roboterapi.model;

import lombok.Data;
import org.springframework.hateoas.Link; // Spring HATEOAS Link
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data // Lombok for Getter/Setter
public class RobotAction {

    private long id; // Unique ID for each action
    private String action; // e.g. "Moved", "Picked up"
    private String detail; // e.g. "up", "diamant"
    private Instant timestamp; // When the action occurred
    
    private List<Link> links = new ArrayList<>();

    // Constructor
    public RobotAction(long id, String action, String detail, Instant timestamp) {
        this.id = id;
        this.action = action;
        this.detail = detail;
        this.timestamp = timestamp;
    }

    // Method to add a link
    public void addLink(Link link) {
        this.links.add(link);
    }
    // Clear all links list to avoid duplicates
    public void removeLinks() {
        this.links.clear();
    }
}