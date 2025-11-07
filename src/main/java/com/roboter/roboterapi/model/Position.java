package com.roboter.roboterapi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// @Data (von Lombok) erstellt automatisch Getter, Setter, toString, etc.
// @NoArgsConstructor erstellt einen leeren Konstruktor (wichtig für JSON)
// @AllArgsConstructor erstellt einen Konstruktor mit allen Feldern
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Position {
    private int x;
    private int y;
}