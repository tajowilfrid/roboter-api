package com.roboter.roboterapi.dto;

import lombok.Data;

// Diese Klasse repräsentiert das JSON: {"direction": "up"}
@Data
public class MoveRequest {
    private String direction; // "up", "down", "left", "right"
}