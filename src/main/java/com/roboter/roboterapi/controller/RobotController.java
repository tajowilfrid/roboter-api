package com.roboter.roboterapi.controller;

import com.roboter.roboterapi.dto.MoveRequest;
import com.roboter.roboterapi.dto.PatchStateRequest;
import com.roboter.roboterapi.model.Robot;
import com.roboter.roboterapi.service.RobotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // Sagt Spring, dass dies ein Controller für REST-APIs ist
@RequestMapping("/robots") // Alle URLs in dieser Klasse beginnen mit "/robots"
public class RobotController {

    @Autowired // Spring injiziert uns den Service
    private RobotService robotService;

    // Helfer-Methode, um ein Optional<T> in eine HTTP-Antwort umzuwandeln
    // Wenn das Optional leer ist -> 404 Not Found
    // Wenn es voll ist -> 200 OK mit den Daten
    private <T> ResponseEntity<T> toResponseEntity(java.util.Optional<T> optional) {
        return optional.map(ResponseEntity::ok) // .map(data -> ResponseEntity.ok(data))
                       .orElse(ResponseEntity.notFound().build());
    }

    // Aufgabe 1: Roboter-Status abrufen
    // GET /robots/{id}/status
    @GetMapping("/{id}/status")
    public ResponseEntity<Robot> getRobotStatus(@PathVariable String id) {
        return toResponseEntity(robotService.getRobotStatus(id));
    }

    // Aufgabe 2: Roboter bewegen
    // POST /robots/{id}/move
    // @RequestBody wandelt das JSON der Anfrage in ein MoveRequest-Objekt um
    @PostMapping("/{id}/move")
    public ResponseEntity<Robot> moveRobot(@PathVariable String id, @RequestBody MoveRequest moveRequest) {
        return toResponseEntity(robotService.moveRobot(id, moveRequest.getDirection()));
    }

    // Aufgabe 3: Gegenstand aufheben
    // POST /robots/{id}/pickup/{itemId}
    @PostMapping("/{id}/pickup/{itemId}")
    public ResponseEntity<Robot> pickupItem(@PathVariable String id, @PathVariable String itemId) {
        return toResponseEntity(robotService.pickupItem(id, itemId));
    }

    // Aufgabe 3 (Erweiterung): Gegenstand ablegen
    // POST /robots/{id}/putdown/{itemId}
    @PostMapping("/{id}/putdown/{itemId}")
    public ResponseEntity<Robot> putdownItem(@PathVariable String id, @PathVariable String itemId) {
        return toResponseEntity(robotService.putdownItem(id, itemId));
    }

    // Aufgabe 4: Roboter-Zustand aktualisieren
    // PATCH /robots/{id}/state
    @PatchMapping("/{id}/state")
    public ResponseEntity<Robot> patchRobotState(@PathVariable String id, @RequestBody PatchStateRequest patchRequest) {
        return toResponseEntity(robotService.updateRobotState(id, patchRequest));
    }

    // Aufgabe 5: Alle Aktionen abrufen
    // GET /robots/{id}/actions
    @GetMapping("/{id}/actions")
    public ResponseEntity<List<String>> getRobotActions(@PathVariable String id) {
        return toResponseEntity(robotService.getRobotActions(id));
    }

    // Aufgabe 6: Anderen Roboter angreifen
    // POST /robots/{id}/attack/{targetId}
    @PostMapping("/{id}/attack/{targetId}")
    public ResponseEntity<Robot> attackRobot(@PathVariable String id, @PathVariable String targetId) {
        return toResponseEntity(robotService.attackRobot(id, targetId));
    }
}