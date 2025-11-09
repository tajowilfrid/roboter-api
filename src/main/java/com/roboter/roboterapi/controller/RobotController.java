package com.roboter.roboterapi.controller;

import com.roboter.roboterapi.dto.MoveRequest;
import com.roboter.roboterapi.dto.PageInfo;
import com.roboter.roboterapi.dto.PagedActionsResponse;
import com.roboter.roboterapi.dto.PatchStateRequest;
import com.roboter.roboterapi.model.Robot;
import com.roboter.roboterapi.model.RobotAction;
import com.roboter.roboterapi.service.RobotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/robots")
public class RobotController {

    @Autowired
    private RobotService robotService;

    // Helfer-Methode, um ein Optional<T> in eine HTTP-Antwort umzuwandeln
    private <T> ResponseEntity<T> toResponseEntity(Optional<T> optional) {
        return optional.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.notFound().build());
    }

    // Diese Methode fügt einem Roboter-Objekt die Standard-HATEOAS-Links hinzu
    // und behebt den Bug, indem sie vorher alle alten Links entfernt.
    @SuppressWarnings("null")
    private void addLinksToRobot(Robot robot) {
        // Entfernt alle Links, bevor neue hinzugefügt werden
        robot.removeLinks(); 
        
        // 1. "self" Link
        robot.add(linkTo((Object) methodOn(RobotController.class)
            .getRobotStatus(robot.getId())).withSelfRel());

        // 2. "actions" Link
        String actionsUri = linkTo((Object) methodOn(RobotController.class)
            .getRobotActions(robot.getId(), 1, 5)) // Standard auf Seite 1, Größe 5
            .toUriComponentsBuilder()
            .build().toUriString();
        
        // Wir verwenden page=1 (API-basiert) statt 0 (Spring-intern)
        robot.add(Link.of(actionsUri.replace("page=0", "page=1"), "actions"));
    }


    // GET /robots/{id}/status
    @GetMapping("/{id}/status")
    public ResponseEntity<Robot> getRobotStatus(@PathVariable String id) {
        Optional<Robot> robotOpt = robotService.getRobotStatus(id);
        // HATEOAS-Teil wird jetzt von der Helfermethode erledigt
        robotOpt.ifPresent(this::addLinksToRobot);
        return toResponseEntity(robotOpt);
    }

    // POST /robots/{id}/move
    // @RequestBody wandelt das JSON der Anfrage in ein MoveRequest-Objekt um
    @PostMapping("/{id}/move")
    public ResponseEntity<Robot> moveRobot(@PathVariable String id, @RequestBody MoveRequest moveRequest) {
        Optional<Robot> robotOpt = robotService.moveRobot(id, moveRequest.getDirection());
        // Fügt HATEOAS-Links auch zur POST-Antwort hinzu
        robotOpt.ifPresent(this::addLinksToRobot);
        return toResponseEntity(robotOpt);
    }

    // POST /robots/{id}/pickup/{itemId}
    @PostMapping("/{id}/pickup/{itemId}")
    public ResponseEntity<Robot> pickupItem(@PathVariable String id, @PathVariable String itemId) {
        Optional<Robot> robotOpt = robotService.pickupItem(id, itemId);
        // Fügt HATEOAS-Links auch zur POST-Antwort hinzu
        robotOpt.ifPresent(this::addLinksToRobot);
        return toResponseEntity(robotOpt);
    }

    // POST /robots/{id}/putdown/{itemId}
    @PostMapping("/{id}/putdown/{itemId}")
    public ResponseEntity<Robot> putdownItem(@PathVariable String id, @PathVariable String itemId) {
        Optional<Robot> robotOpt = robotService.putdownItem(id, itemId);
        // Fügt HATEOAS-Links auch zur POST-Antwort hinzu
        robotOpt.ifPresent(this::addLinksToRobot);
        return toResponseEntity(robotOpt);
    }

    // PATCH /robots/{id}/state
    @PatchMapping("/{id}/state")
    public ResponseEntity<Robot> patchRobotState(@PathVariable String id, @RequestBody PatchStateRequest patchRequest) {
        Optional<Robot> robotOpt = robotService.updateRobotState(id, patchRequest);
        // Fügt HATEOAS-Links auch zur PATCH-Antwort hinzu
        robotOpt.ifPresent(this::addLinksToRobot);
        return toResponseEntity(robotOpt);
    }


    // GET /robots/{id}/actions (Bugfix bei Paginierung-Link)
    @SuppressWarnings("null")
    @GetMapping("/{id}/actions")
    public ResponseEntity<PagedActionsResponse> getRobotActions(
            @PathVariable String id,
            @RequestParam(defaultValue = "1") int page, // Standard auf 1
            @RequestParam(defaultValue = "5") int size
    ) {
        // Wir stellen sicher, dass die API 1-basiert ist
        // Wenn der Benutzer 0 oder 1 sendet, meinen wir die erste Seite (Index 0)
        int pageNumber = Math.max(0, page - 1); 
        Pageable pageable = PageRequest.of(pageNumber, size);

        Optional<Page<RobotAction>> pageOpt = robotService.getRobotActionsPaged(id, pageable);

        if (pageOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Page<RobotAction> actionsPage = pageOpt.get();
        PageInfo pageInfo = PageInfo.fromPage(actionsPage);

        // 2. Füge "self" Links zu JEDER EINZELNEN Aktion hinzu
        List<RobotAction> actionsWithLinks = actionsPage.getContent().stream()
            .peek(action -> {
                // Alte Links entfernen
                action.removeLinks(); 
                action.addLink(linkTo((Object) methodOn(RobotController.class)
                    .getRobotActionById(id, action.getId()))
                    .withSelfRel());
            })
            .toList();

        // 3. Erstelle "next" und "previous" Links für die Gesamt-Antwort
        List<Link> rootLinks = new ArrayList<>();
        
        // Aktuelle Seite (API-basiert, z.B. 1)
        int currentPage = pageInfo.getNumber(); 

        rootLinks.add(linkTo((Object) methodOn(RobotController.class).getRobotActions(id, currentPage, size)).withSelfRel());

        if (actionsPage.hasNext()) {
            rootLinks.add(linkTo((Object) methodOn(RobotController.class)
                .getRobotActions(id, currentPage + 1, size))
                .withRel("next"));
        }
        if (actionsPage.hasPrevious()) {
            rootLinks.add(linkTo((Object) methodOn(RobotController.class)
                .getRobotActions(id, currentPage - 1, size))
                .withRel("previous"));
        }
        
        PagedActionsResponse response = new PagedActionsResponse(pageInfo, actionsWithLinks, rootLinks);
        return ResponseEntity.ok(response);
    }

    // GET /robots/{id}/actions/{actionId}
    @SuppressWarnings("null")
    @GetMapping("/{id}/actions/{actionId}")
    public ResponseEntity<RobotAction> getRobotActionById(@PathVariable String id, @PathVariable long actionId) {
        Optional<RobotAction> actionOpt = robotService.getRobotActionById(id, actionId);
        actionOpt.ifPresent(action -> {
            // Alte Links entfernen
            action.removeLinks(); 
            action.addLink(linkTo((Object) methodOn(RobotController.class)
                .getRobotActionById(id, actionId))
                .withSelfRel());
        });
        return toResponseEntity(actionOpt);
    }

    // POST /robots/{id}/attack/{targetId}
    @PostMapping("/{id}/attack/{targetId}")
    public ResponseEntity<Robot> attackRobot(@PathVariable String id, @PathVariable String targetId) {
        Optional<Robot> robotOpt = robotService.attackRobot(id, targetId);
        // Fügt HATEOAS-Links auch zur POST-Antwort hinzu
        robotOpt.ifPresent(this::addLinksToRobot);
        return toResponseEntity(robotOpt);
    }
}