package com.roboter.roboterapi.service;

import com.roboter.roboterapi.dto.PatchStateRequest;
import com.roboter.roboterapi.model.Position;
import com.roboter.roboterapi.model.Robot;
import com.roboter.roboterapi.model.RobotAction;
import com.roboter.roboterapi.repository.InMemoryRobotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
            // Aktionen nur ausführen, wenn Energie > 0
            if (robot.getEnergy() <= 0) return; 

            Position pos = robot.getPosition();
            switch (direction.toLowerCase()) {
                case "up": pos.setY(pos.getY() + 1); break;
                case "down": pos.setY(pos.getY() - 1); break;
                case "right": pos.setX(pos.getX() + 1); break;
                case "left": pos.setX(pos.getX() - 1); break;
            }
            // Loggen mit "action" und "detail"
            robot.logAction("Moved", direction);
            robotRepository.save(robot); // Den geänderten Zustand speichern
        });
        
        return robotOpt;
    }

    // Methode für: POST /robots/{id}/pickup/{itemId}
    public Optional<Robot> pickupItem(String id, String itemId) {
        Optional<Robot> robotOpt = robotRepository.findById(id);
        robotOpt.ifPresent(robot -> {
            if (robot.getEnergy() <= 0) return;
            robot.getInventory().add(itemId); // Item zum Inventar hinzufügen
            robot.logAction("Picked up", itemId);
            robotRepository.save(robot);
        });
        return robotOpt;
    }

    // Methode für: POST /robots/{id}/putdown/{itemId}
    public Optional<Robot> putdownItem(String id, String itemId) {
        Optional<Robot> robotOpt = robotRepository.findById(id);
        robotOpt.ifPresent(robot -> {
            if (robot.getEnergy() <= 0) return;
            // Prüfen, ob das Item im Inventar ist, bevor wir es ablegen
            if (robot.getInventory().remove(itemId)) {
                robot.logAction("Put down", itemId);
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
            boolean patched = false; // Um nur einmal zu loggen
            // Patch für Energie (nur wenn im Request gesetzt)
            if (patchRequest.getEnergy() != null) {
                // Energie auf 0 begrenzen
                robot.setEnergy(Math.max(0, patchRequest.getEnergy()));
                patched = true;
            }
            // Patch für Position (nur wenn im Request gesetzt)
            if (patchRequest.getPosition() != null) {
                robot.setPosition(patchRequest.getPosition());
                patched = true;
            }
            
            if (patched) {
                robot.logAction("State patched", null);
                robotRepository.save(robot);
            }
        });
        return robotOpt;
    }

    // Holt die Aktionen als "Page" (Seite)
    public Optional<Page<RobotAction>> getRobotActionsPaged(String id, Pageable pageable) {
        Optional<Robot> robotOpt = robotRepository.findById(id);
        if (robotOpt.isEmpty()) {
            return Optional.empty(); // Roboter nicht gefunden
        }

        List<RobotAction> allActions = robotOpt.get().getActionHistory();

        // Manuelle Paginierung der In-Memory-Liste
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allActions.size());

        if (start > end) {
            // Falls die Startseite außerhalb des Bereichs liegt, leere Seite zurückgeben
            return Optional.of(Page.empty(pageable));
        }

        List<RobotAction> pageContent = allActions.subList(start, end);
        
        // Erstellt ein "Page"-Objekt, das die Paginierungs-Infos enthält
        return Optional.of(new PageImpl<>(pageContent, pageable, allActions.size()));
    }

    // Holt eine einzelne Aktion anhand ihrer ID
    // (Wird für die "self"-Links der Aktionen benötigt)
    public Optional<RobotAction> getRobotActionById(String id, long actionId) {
        return robotRepository.findById(id)
            .flatMap(robot -> robot.getActionHistory().stream() // findFlatMap
                .filter(action -> action.getId() == actionId)
                .findFirst());
    }

    // Methode für: GET /robots/{id}/actions
    // public Optional<List<String>> getRobotActions(String id) {
        // Wir nutzen ".map", um das Optional<Robot> in ein Optional<List<String>> umzuwandeln
        // return robotRepository.findById(id).map(Robot::getActionHistory);
    //}

    // Methode für: POST /robots/{id}/attack/{targetId}
    public Optional<Robot> attackRobot(String id, String targetId) {
        // Selbstangriff sofort verhindern
        if (id.equals(targetId)) {
            return Optional.empty(); // oder: attackerOpt.map(a -> a) + "Attack failed: self" loggen
        }

        Optional<Robot> attackerOpt = robotRepository.findById(id);
        Optional<Robot> targetOpt = robotRepository.findById(targetId);

        // Wir brauchen beide Roboter, sonst bricht die Aktion ab
        if (attackerOpt.isPresent() && targetOpt.isPresent()) {
            Robot attacker = attackerOpt.get();
            Robot target   = targetOpt.get();

            // Wenn der Angreifer keine Energie hat, bricht die Aktion ab
            if (attacker.getEnergy() <= 0) {
                return Optional.empty(); // Oder: attackerOpt zurückgeben / Fehler loggen
            }

            // Energie des Angreifers kann nicht unter 0 fallen
            int newAttackerEnergy = Math.max(0, attacker.getEnergy() - 5);
            attacker.setEnergy(newAttackerEnergy);

            // 5% Chance (0.05) auf Schaden beim Ziel
            if (random.nextDouble() <= 0.05) {
                // Ziel-Energie kann nicht unter 0 fallen
                int newTargetEnergy = Math.max(0, target.getEnergy() - 20);
                target.setEnergy(newTargetEnergy);
                
                attacker.logAction("Attacked", targetId + " (HIT)");
                target.logAction("Was attacked by", id + " (HIT)");
                robotRepository.save(target); // Ziel speichern
            } else {
                attacker.logAction("Attacked", targetId + " (MISS)");
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