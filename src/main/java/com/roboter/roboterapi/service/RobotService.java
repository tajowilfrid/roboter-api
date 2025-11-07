package com.roboter.roboterapi.service;

import com.roboter.roboterapi.dto.PatchStateRequest;
import com.roboter.roboterapi.model.Position;
import com.roboter.roboterapi.model.Robot;
import com.roboter.roboterapi.repository.InMemoryRobotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service // Markiert diese Klasse als "Service", hier lebt die Business-Logik
public class RobotService {

    // Spring "injiziert" uns automatisch das Repository, das es beim Start erstellt hat.
    // (Dependency Injection)
    @Autowired
    private InMemoryRobotRepository robotRepository;

    // Hilfsobjekt für den Zufall beim Angreifen
    private final Random random = new Random();

    // Methode für: GET /robots/{id}/status
    public Optional<Robot> getRobotStatus(String id) {
        // Wir fragen einfach das Repository nach dem Roboter
        return robotRepository.findById(id);
    }

    // Methode für: POST /robots/{id}/move
    public Optional<Robot> moveRobot(String id, String direction) {
        Optional<Robot> robotOpt = robotRepository.findById(id);
        
        // .ifPresent(...) wird nur ausgeführt, wenn der Roboter existiert
        robotOpt.ifPresent(robot -> {
            Position pos = robot.getPosition();
            switch (direction.toLowerCase()) {
                case "up": pos.setY(pos.getY() + 1); break;
                case "down": pos.setY(pos.getY() - 1); break;
                case "right": pos.setX(pos.getX() + 1); break;
                case "left": pos.setX(pos.getX() - 1); break;
            }
            robot.logAction("Moved " + direction);
            robotRepository.save(robot); // Den geänderten Zustand speichern
        });
        
        return robotOpt;
    }

    // Methode für: POST /robots/{id}/pickup/{itemId}
    public Optional<Robot> pickupItem(String id, String itemId) {
        Optional<Robot> robotOpt = robotRepository.findById(id);
        robotOpt.ifPresent(robot -> {
            robot.getInventory().add(itemId); // Item zum Inventar hinzufügen
            robot.logAction("Picked up item " + itemId);
            robotRepository.save(robot);
        });
        return robotOpt;
    }

    // Methode für: POST /robots/{id}/putdown/{itemId}
    public Optional<Robot> putdownItem(String id, String itemId) {
        Optional<Robot> robotOpt = robotRepository.findById(id);
        robotOpt.ifPresent(robot -> {
            // Prüfen, ob das Item im Inventar ist, bevor wir es ablegen
            if (robot.getInventory().remove(itemId)) {
                robot.logAction("Put down item " + itemId);
                robotRepository.save(robot);
            }
            // (Hier könnte man einen Fehler werfen, wenn das Item nicht da war)
        });
        return robotOpt;
    }

    // Methode für: PATCH /robots/{id}/state
    public Optional<Robot> updateRobotState(String id, PatchStateRequest patchRequest) {
        Optional<Robot> robotOpt = robotRepository.findById(id);
        robotOpt.ifPresent(robot -> {
            // Patch für Energie (nur wenn im Request gesetzt)
            if (patchRequest.getEnergy() != null) {
                robot.setEnergy(patchRequest.getEnergy());
            }
            // Patch für Position (nur wenn im Request gesetzt)
            if (patchRequest.getPosition() != null) {
                robot.setPosition(patchRequest.getPosition());
            }
            robot.logAction("State patched");
            robotRepository.save(robot);
        });
        return robotOpt;
    }

    // Methode für: GET /robots/{id}/actions
    public Optional<List<String>> getRobotActions(String id) {
        // Wir nutzen ".map", um das Optional<Robot> in ein Optional<List<String>> umzuwandeln
        return robotRepository.findById(id).map(Robot::getActionHistory);
    }

    // Methode für: POST /robots/{id}/attack/{targetId}
    public Optional<Robot> attackRobot(String id, String targetId) {
        Optional<Robot> attackerOpt = robotRepository.findById(id);
        Optional<Robot> targetOpt = robotRepository.findById(targetId);

        // Wir brauchen beide Roboter, sonst bricht die Aktion ab
        if (attackerOpt.isPresent() && targetOpt.isPresent()) {
            Robot attacker = attackerOpt.get();
            Robot target = targetOpt.get();

            // 5% Energieverlust für den Angreifer
            attacker.setEnergy(attacker.getEnergy() - 5);
            
            // 5% Chance (0.05) auf Schaden beim Ziel
            if (random.nextDouble() <= 0.05) {
                // Sagen wir, ein Treffer kostet 20 Energie
                target.setEnergy(target.getEnergy() - 20);
                attacker.logAction("Attacked " + targetId + " (HIT)");
                target.logAction("Was attacked by " + id + " (HIT)");
                robotRepository.save(target); // Ziel speichern
            } else {
                attacker.logAction("Attacked " + targetId + " (MISS)");
            }
            robotRepository.save(attacker); // Angreifer speichern
            return attackerOpt;
        }
        return Optional.empty(); // Einer der Roboter wurde nicht gefunden
    }

    // Nützliche Methode, um einen Roboter zu erstellen (für den Start)
    public void createRobot(String id) {
        robotRepository.save(new Robot(id));
    }
}