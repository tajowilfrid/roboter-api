package com.roboter.roboterapi.model;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

// @Data von Lombok erspart uns das Tippen von get/set-Methoden
@Data
public class Robot {

    // Eindeutige ID des Roboters
    private String id;
    
    // Aktuelle Position, verwendet unsere Position-Klasse
    private Position position;
    
    // Aktuelles Energielevel
    private int energy;
    
    // Das Inventar, wir speichern hier einfach die Item-IDs als Strings
    private List<String> inventory;
    
    // Eine Liste aller Aktionen, die der Roboter durchgeführt hat
    private List<String> actionHistory;

    // Konstruktor, um einen neuen Roboter mit Standardwerten zu erstellen
    public Robot(String id) {
        this.id = id;
        this.position = new Position(0, 0); // Startet bei (0,0)
        this.energy = 100; // Startet mit voller Energie
        this.inventory = new ArrayList<>();
        this.actionHistory = new ArrayList<>();
    }

    // Eine kleine Hilfsmethode, um eine Aktion zu protokollieren
    public void logAction(String action) {
        this.actionHistory.add(action);
    }
}