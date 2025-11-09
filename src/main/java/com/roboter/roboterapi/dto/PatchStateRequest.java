package com.roboter.roboterapi.dto;

import com.roboter.roboterapi.model.Position;
import lombok.Data;

/*
 * 
 * Represents PATCH JSON: {"energy": 80} OR {"position": {"x": 1, "y": 2}}
 * Uses wrapper types (e.g., Integer) over primitives (int) to allow null fields
 */
@Data
public class PatchStateRequest {
    private Integer energy;
    private Position position;
}