package com.roboter.roboterapi.model;

import lombok.Data;
import org.springframework.hateoas.Link; // Spring HATEOAS Link
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data // Lombok für Getter/Setter
public class RobotAction {

    private long id; // Eine ID für die Aktion selbst (z.B. 1, 2, 3...)
    private String action; // z.B. "Moved", "Picked up"
    private String detail; // z.B. "up", "diamant"
    private Instant timestamp; // Zeitstempel (Instant wird zu "...Z" (UTC) serialisiert)
    
    // Jede Aktion hat ihre eigene Link-Liste (für "self")
    private List<Link> links = new ArrayList<>();

    // Konstruktor
    public RobotAction(long id, String action, String detail, Instant timestamp) {
        this.id = id;
        this.action = action;
        this.detail = detail;
        this.timestamp = timestamp;
    }

    // Hilfsmethode, um Links hinzuzufügen
    public void addLink(Link link) {
        this.links.add(link);
    }
    // Leert die Link-Liste, um Duplikate zu verhindern
    public void removeLinks() {
        this.links.clear();
    }
}