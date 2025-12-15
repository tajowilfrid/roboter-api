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

@Service // Marks this as a Service, contains business logic
public class RobotService {

    // Spring auto-injects the repository (Dependency Injection).
    @Autowired
    private InMemoryRobotRepository robotRepository;

    // Helper for attack randomness
    private final Random random = new Random();

    // Method for: GET /robots/{id}/status
    public Optional<Robot> getRobotStatus(String id) {
        // Ask the repository for the robot
        return robotRepository.findById(id);
    }

    // Method for: POST /robots/{id}/move
    public Optional<Robot> moveRobot(String id, String direction) {
        Optional<Robot> robotOpt = robotRepository.findById(id);
        
        // .ifPresent(...) only executes if the robot exists
        robotOpt.ifPresent(robot -> {
            // Only perform actions if energy > 0
            if (robot.getEnergy() <= 0) return; 

            Position pos = robot.getPosition();
            switch (direction.toLowerCase()) {
                case "up": pos.setY(pos.getY() + 1); break;
                case "down": pos.setY(pos.getY() - 1); break;
                case "right": pos.setX(pos.getX() + 1); break;
                case "left": pos.setX(pos.getX() - 1); break;
            }
            // Log with "action" and "detail"
            robot.logAction("Moved", direction);
            robotRepository.save(robot); // Save the updated state
        });
        
        return robotOpt;
    }

    // Method for: POST /robots/{id}/pickup/{itemId}
    public Optional<Robot> pickupItem(String id, String itemId) {
        Optional<Robot> robotOpt = robotRepository.findById(id);
        robotOpt.ifPresent(robot -> {
            if (robot.getEnergy() <= 0) return;
            robot.getInventory().add(itemId); // Add item to inventory
            robot.logAction("Picked up", itemId);
            robotRepository.save(robot);
        });
        return robotOpt;
    }

    // Method for: POST /robots/{id}/putdown/{itemId}
    public Optional<Robot> putdownItem(String id, String itemId) {
        Optional<Robot> robotOpt = robotRepository.findById(id);
        robotOpt.ifPresent(robot -> {
            if (robot.getEnergy() <= 0) return;
            // Check if item is in inventory before dropping
            if (robot.getInventory().remove(itemId)) {
                robot.logAction("Put down", itemId);
                robotRepository.save(robot);
            }
            // Could throw an error here if item was missing
        });
        return robotOpt;
    }

    // Method for: PATCH /robots/{id}/state
    public Optional<Robot> updateRobotState(String id, PatchStateRequest patchRequest) {
        Optional<Robot> robotOpt = robotRepository.findById(id);
        robotOpt.ifPresent(robot -> {
            boolean patched = false; // To log only once
            // Patch energy (if set in request)
            if (patchRequest.getEnergy() != null) {
                // Cap energy at 0
                robot.setEnergy(Math.max(0, patchRequest.getEnergy()));
                patched = true;
            }
            // Patch position (if set in request)
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

    // Get actions as a "Page"
    public Optional<Page<RobotAction>> getRobotActionsPaged(String id, Pageable pageable) {
        Optional<Robot> robotOpt = robotRepository.findById(id);
        if (robotOpt.isEmpty()) {
            return Optional.empty(); // Robot not found
        }

        List<RobotAction> allActions = robotOpt.get().getActionHistory();

        // Manual pagination of the in-memory list
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allActions.size());

        if (start > end) {
            // If start is out of bounds, return an empty page
            return Optional.of(Page.empty(pageable));
        }

        List<RobotAction> pageContent = allActions.subList(start, end);
        
        // Create a "Page" object containing pagination info
        return Optional.of(new PageImpl<>(pageContent, pageable, allActions.size()));
    }

    // Get a single action by its ID
    // (Needed for action "self" links)
    public Optional<RobotAction> getRobotActionById(String id, long actionId) {
        return robotRepository.findById(id)
                .flatMap(robot -> robot.getActionHistory().stream() // Use flatMap
                        .filter(action -> action.getId() == actionId)
                        .findFirst());
    }

    // Method for: GET /robots/{id}/actions
    // public Optional<List<String>> getRobotActions(String id) {
        // Use ".map" to transform Optional<Robot> to Optional<List<String>>
        // return robotRepository.findById(id).map(Robot::getActionHistory);
    //}

    // Method for: POST /robots/{id}/attack/{targetId}
    public Optional<Robot> attackRobot(String id, String targetId) {
        // Prevent self-attack
        if (id.equals(targetId)) {
            return Optional.empty(); // Or log "Attack failed: self" and return attacker
        }

        Optional<Robot> attackerOpt = robotRepository.findById(id);
        Optional<Robot> targetOpt = robotRepository.findById(targetId);

        // Both robots must exist, otherwise abort
        if (attackerOpt.isPresent() && targetOpt.isPresent()) {
            Robot attacker = attackerOpt.get();
            Robot target   = targetOpt.get();

            // If attacker has no energy, abort
            if (attacker.getEnergy() <= 0) {
                return Optional.empty(); // Or return attackerOpt / log error
            }

            // Attacker energy cannot drop below 0
            int newAttackerEnergy = Math.max(0, attacker.getEnergy() - 5);
            attacker.setEnergy(newAttackerEnergy);

            // 5% chance (0.05) to damage target
            if (random.nextDouble() <= 0.05) {
                // Target energy cannot drop below 0
                int newTargetEnergy = Math.max(0, target.getEnergy() - 20);
                target.setEnergy(newTargetEnergy);
                
                attacker.logAction("Attacked", targetId + " (HIT)");
                target.logAction("Was attacked by", id + " (HIT)");
                robotRepository.save(target); // Save target
            } else {
                attacker.logAction("Attacked", targetId + " (MISS)");
            }
            robotRepository.save(attacker); // Save attacker
            return attackerOpt;
        }
        return Optional.empty(); // One or both robots not found
    }

    // create a robot (for startup)
    public void createRobot(String id) {
        robotRepository.save(new Robot(id));
    }

    /**
     * Resets the entire system by clearing all robots from the repository
     */
    public void resetSystem() {
        //robotRepository.findAll().clear();
        robotRepository.deleteAll();
        createRobot("r1");
        createRobot("r2");
        createRobot("r3");
    }
}