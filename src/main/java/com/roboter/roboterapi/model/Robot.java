package com.roboter.roboterapi.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

// 1. Erweitert RepresentationModel, damit die Klasse "_links" enthalten kann
// 2. @EqualsAndHashCode(callSuper = false) ist wichtig, wenn man von RM erbt
// @Data von Lombok erspart uns das Tippen von get/set-Methoden
@Data
@EqualsAndHashCode(callSuper = false)
public class Robot extends RepresentationModel<Robot> {

    // Eindeutige ID des Roboters
    private String id;
    
    // Aktuelle Position, verwendet unsere Position-Klasse
    private Position position;
    
    // Aktuelles Energielevel
    private int energy;
    
    // Das Inventar, wir speichern hier einfach die Item-IDs als Strings
    private List<String> inventory;
    
    // Eine Liste aller Aktionen, die der Roboter durchgeführt hat
    // private List<String> actionHistory;

    // 3. Geändert von List<String> zu List<RobotAction>
    private List<RobotAction> actionHistory;

    // Konstruktor, um einen neuen Roboter mit Standardwerten zu erstellen
    public Robot(String id) {
        this.id = id;
        this.position = new Position(0, 0); // Startet bei (0,0)
        this.energy = 100; // Startet mit voller Energie
        this.inventory = new ArrayList<>();
        this.actionHistory = new ArrayList<>(); // initialisiert die neue Liste
    }

    // 4. logAction wurde angepasst, um strukturierte Daten zu speichern
    public void logAction(String action, String detail) {
        // Erzeugt eine neue ID (1, 2, 3...)
        long newId = this.actionHistory.isEmpty() ? 1 : 
                     this.actionHistory.get(this.actionHistory.size() - 1).getId() + 1;
        
        this.actionHistory.add(
            new RobotAction(newId, action, detail, Instant.now())
        );
    }
    
    // HIER IST DER ZWEITE FIX:
    // Diese Methode hat in Robot.java gefehlt und den Bug der
    // doppelten Links in der /status-Antwort verursacht.
    @Override
    @org.springframework.lang.NonNull
    public Robot removeLinks() {
        super.removeLinks();
        return this;
    }
}