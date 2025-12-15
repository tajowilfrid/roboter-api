package com.roboter.roboterapi.model;

import lombok.Data;
import org.springframework.hateoas.Link;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
public class RobotAction {

    private long id;
    private String action;
    private String detail;
    private Instant timestamp;
    
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