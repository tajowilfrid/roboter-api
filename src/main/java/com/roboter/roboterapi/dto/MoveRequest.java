package com.roboter.roboterapi.dto;

import lombok.Data;

@Data
public class MoveRequest {
    private String direction; // "up", "down", "left", "right"
}