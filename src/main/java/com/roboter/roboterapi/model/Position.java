package com.roboter.roboterapi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 *@Data (from Lombok) automatically creates getters, setters, toString, etc.
 *@NoArgsConstructor empty constructor for JSON
 *@AllArgsConstructor all fields constructor 
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Position {
    private int x;
    private int y;
}