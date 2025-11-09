package com.roboter.roboterapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roboter.roboterapi.dto.MoveRequest;
import com.roboter.roboterapi.dto.PatchStateRequest;
import com.roboter.roboterapi.model.Position;
import com.roboter.roboterapi.model.Robot;
import com.roboter.roboterapi.model.RobotAction;
import com.roboter.roboterapi.service.RobotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is a web layer (slice) test for the RobotController
 *
 * @WebMvcTest(RobotController.class):
 * Loads *only* the Spring web layer (DispatcherServlet, Controller, JSON conversion)
 * No Service, Repository, or CommandLineRunner is loaded. This makes the test extremely fast
 */
@WebMvcTest(RobotController.class)
class RobotControllerTest {

    // @Autowired: Spring injects the MockMvc instance.
    // MockMvc simulates HTTP requests to our controller.
    @Autowired
    private MockMvc mockMvc;

    // @Autowired: Needed to convert Java objects (DTOs)
    // into JSON strings for request bodies.
    @Autowired
    private ObjectMapper objectMapper;

    // @MockBean: Since @WebMvcTest doesn't load the real service,
    // Spring to create a mock for RobotService and control what this mock returns in each test
    @MockBean
    private RobotService robotService;

    // A reusable test robot instance
    private Robot testRobot;

    // This method runs before *each* single @Test.
    @BeforeEach
    void setUp() {
        // create a clean robot for each test
        testRobot = new Robot("r1");
        testRobot.setPosition(new Position(10, 10)); // Set a known position
        testRobot.setEnergy(100);
    }

    // Task 1: Get robot status
    @Test
    void whenGetStatus_thenReturns200AndRobot() throws Exception {
        // WHEN service.getRobotStatus("r1") is called, THEN return testRobot
        when(robotService.getRobotStatus("r1")).thenReturn(Optional.of(testRobot));

        // Perform a simulated GET request to "/robots/r1/status"
        mockMvc.perform(get("/robots/r1/status"))
                // Expect HTTP 200 (OK)
                .andExpect(status().isOk())
                // Expect JSON field "id" to be "r1"
                .andExpect(jsonPath("$.id").value("r1"))
                // Expect energy to be 100
                .andExpect(jsonPath("$.energy").value(100))
                // Expect HATEOAS links to exist
                .andExpect(jsonPath("$._links.self").exists())
                .andExpect(jsonPath("$._links.actions").exists());
    }

    // Test for a non-existent robot
    @Test
    void whenGetStatus_forMissingRobot_thenReturns404() throws Exception {
        // WHEN service.getRobotStatus("r99") is called, THEN return an empty Optional
        when(robotService.getRobotStatus("r99")).thenReturn(Optional.empty());

        mockMvc.perform(get("/robots/r99/status"))
                // Expect HTTP 404 (Not Found)
                .andExpect(status().isNotFound());
    }

    // Task 2: Move robot
    @Test
    void whenMoveRobot_thenReturns200AndUpdatedRobot() throws Exception {
        // 1. The DTO (payload) to send as JSON
        MoveRequest moveDto = new MoveRequest();
        moveDto.setDirection("up");

        // 2. The robot object expected from the service
        testRobot.setPosition(new Position(10, 11)); // The new position after "up"
        
        // 3. Mocking: WHEN service.moveRobot("r1", "up") is called, THEN...
        when(robotService.moveRobot("r1", "up")).thenReturn(Optional.of(testRobot));

        mockMvc.perform(post("/robots/r1/move")
                // Set the request Content-Type
                .contentType(MediaType.APPLICATION_JSON)
                // Convert DTO to JSON and set as request body
                .content(objectMapper.writeValueAsString(moveDto)))
                // Expect status 200 (OK)
                .andExpect(status().isOk())
                // Check if the position in the JSON response is updated
                .andExpect(jsonPath("$.position.y").value(11));
    }

    // Task 3: Pick up item
    @Test
    void whenPickupItem_thenReturns200AndUpdatedInventory() throws Exception {
        testRobot.getInventory().add("diamant");
        when(robotService.pickupItem("r1", "diamant")).thenReturn(Optional.of(testRobot));

        mockMvc.perform(post("/robots/r1/pickup/diamant"))
                .andExpect(status().isOk())
                // Expect the "inventory" array to contain "diamant"
                .andExpect(jsonPath("$.inventory[0]").value("diamant"));
    }

    // Task 4: Update robot state (PATCH)
    @Test
    void whenPatchState_thenReturns200AndPatchedRobot() throws Exception {
        // 1. The DTO for the patch
        PatchStateRequest patchDto = new PatchStateRequest();
        patchDto.setEnergy(80);

        // 2. Expected result
        testRobot.setEnergy(80);

        // 3. Mocking: eq("r1") and any() are Mockito matchers.
        when(robotService.updateRobotState(eq("r1"), any(PatchStateRequest.class)))
                .thenReturn(Optional.of(testRobot));

        mockMvc.perform(patch("/robots/r1/state")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.energy").value(80))
                .andExpect(jsonPath("$.position.x").value(10)); // Position remains unchanged
    }

    // Task 5: Get all actions (Pagination test)
    @Test
    void whenGetActions_thenReturns200AndPagedResponse() throws Exception {

        // 1. Create a test action
        RobotAction action1 = new RobotAction(1L, "Moved", "up", Instant.now());
        List<RobotAction> actions = List.of(action1);

        // 2. Create a Pageable object (what the controller creates internally)
        Pageable pageable = PageRequest.of(0, 5); 

        // 3. Create the final Page object (what the service returns)
        Page<RobotAction> pagedActions = new PageImpl<>(actions, pageable, 1L); // 1L = totalElements

        // 4. Mocking: WHEN service.getRobotActionsPaged("r1", any Pageable)...
        when(robotService.getRobotActionsPaged(eq("r1"), any(Pageable.class)))
                .thenReturn(Optional.of(pagedActions));

        // call the API with page 1
        mockMvc.perform(get("/robots/r1/actions?page=1&size=5"))
                .andExpect(status().isOk())
                // Expect pagination info to be correct
                .andExpect(jsonPath("$.page.number").value(1)) // API is 1-based
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andExpect(jsonPath("$.page.totalPages").value(1))
                // Expect action data to be correct
                .andExpect(jsonPath("$.actions[0].action").value("Moved"))
                // Expect HATEOAS links (for pagination) to exist
                .andExpect(jsonPath("$.links[?(@.rel=='self')]").exists());
    }

    // Task 6: Attack other robot
    @Test
    void whenAttackRobot_thenReturns200AndAttackerStatus() throws Exception {

        testRobot.setEnergy(95); // The attacker loses 5 energy
        when(robotService.attackRobot("r1", "r2")).thenReturn(Optional.of(testRobot));

        mockMvc.perform(post("/robots/r1/attack/r2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("r1"))
                // Check the attacker's new energy
                .andExpect(jsonPath("$.energy").value(95));
    }
}