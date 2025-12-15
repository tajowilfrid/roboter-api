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

    private <T> ResponseEntity<T> toResponseEntity(Optional<T> optional) {
        return optional.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.notFound().build());
    }

    // Add standard HATEOAS links to a Robot, fixing a bug by first clearing existing links
    @SuppressWarnings("null")
    private void addLinksToRobot(Robot robot) {
        // Remove all links before adding new ones.
        robot.removeLinks(); 
        
        robot.add(linkTo((Object) methodOn(RobotController.class)
            .getRobotStatus(robot.getId())).withSelfRel());

        String actionsUri = linkTo((Object) methodOn(RobotController.class)
            .getRobotActions(robot.getId(), 1, 5))
            .toUriComponentsBuilder()
            .build().toUriString();
        
        // Use page=1 (API-based) instead of 0 (Spring-internal)
        robot.add(Link.of(actionsUri.replace("page=0", "page=1"), "actions"));
    }


    // GET /robots/{id}/status
    @GetMapping("/{id}/status")
    public ResponseEntity<Robot> getRobotStatus(@PathVariable String id) {
        Optional<Robot> robotOpt = robotService.getRobotStatus(id);
        robotOpt.ifPresent(this::addLinksToRobot);
        return toResponseEntity(robotOpt);
    }

    // POST /robots/{id}/move
    // @RequestBody converts the JSON of the request into a MoveRequest object
    @PostMapping("/{id}/move")
    public ResponseEntity<Robot> moveRobot(@PathVariable String id, @RequestBody MoveRequest moveRequest) {
        Optional<Robot> robotOpt = robotService.moveRobot(id, moveRequest.getDirection());
        // Add HATEOAS links to the POST response
        robotOpt.ifPresent(this::addLinksToRobot);
        return toResponseEntity(robotOpt);
    }

    // POST /robots/{id}/pickup/{itemId}
    @PostMapping("/{id}/pickup/{itemId}")
    public ResponseEntity<Robot> pickupItem(@PathVariable String id, @PathVariable String itemId) {
        Optional<Robot> robotOpt = robotService.pickupItem(id, itemId);
        robotOpt.ifPresent(this::addLinksToRobot);
        return toResponseEntity(robotOpt);
    }

    // POST /robots/{id}/putdown/{itemId}
    @PostMapping("/{id}/putdown/{itemId}")
    public ResponseEntity<Robot> putdownItem(@PathVariable String id, @PathVariable String itemId) {
        Optional<Robot> robotOpt = robotService.putdownItem(id, itemId);
        robotOpt.ifPresent(this::addLinksToRobot);
        return toResponseEntity(robotOpt);
    }

    // PATCH /robots/{id}/state
    @PatchMapping("/{id}/state")
    public ResponseEntity<Robot> patchRobotState(@PathVariable String id, @RequestBody PatchStateRequest patchRequest) {
        Optional<Robot> robotOpt = robotService.updateRobotState(id, patchRequest);
        robotOpt.ifPresent(this::addLinksToRobot);
        return toResponseEntity(robotOpt);
    }


    // GET /robots/{id}/actions
    @SuppressWarnings("null")
    @GetMapping("/{id}/actions")
    public ResponseEntity<PagedActionsResponse> getRobotActions(
            @PathVariable String id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        // Ensure the API is 1-based and inputs 0 or 1 map to the first page (index 0)
        int pageNumber = Math.max(0, page - 1); 
        Pageable pageable = PageRequest.of(pageNumber, size);

        Optional<Page<RobotAction>> pageOpt = robotService.getRobotActionsPaged(id, pageable);

        if (pageOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Page<RobotAction> actionsPage = pageOpt.get();
        PageInfo pageInfo = PageInfo.fromPage(actionsPage);

        // Add "self" links to every single action
        List<RobotAction> actionsWithLinks = actionsPage.getContent().stream()
            .peek(action -> {
                action.removeLinks(); 
                action.addLink(linkTo((Object) methodOn(RobotController.class)
                    .getRobotActionById(id, action.getId()))
                    .withSelfRel());
            })
            .toList();

        // Create "next" and "previous" links for the overall response
        List<Link> rootLinks = new ArrayList<>();
        
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
        robotOpt.ifPresent(this::addLinksToRobot);
        return toResponseEntity(robotOpt);
    }

    // POST /robots/reset
    @PostMapping("/reset")
    public ResponseEntity<Void> resetAll() {
        robotService.resetSystem();
        return ResponseEntity.ok().build();
    }
}