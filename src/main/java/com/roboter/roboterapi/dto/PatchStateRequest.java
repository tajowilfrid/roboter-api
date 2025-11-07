package com.roboter.roboterapi.dto;

import com.roboter.roboterapi.model.Position;
import lombok.Data;

// Diese Klasse repräsentiert das JSON: {"energy": 80} ODER {"position": {"x": 1, "y": 2}}
// Da es ein PATCH ist, können Felder null sein (nicht gesetzt).
// Wir verwenden Integer (Objekttyp) statt int (primitiv), damit es null sein kann.
@Data
public class PatchStateRequest {
    private Integer energy;
    private Position position;
}