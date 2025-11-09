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

// Importiert statische Mockito-Methoden (when, any, eq)
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

// Importiert statische MockMvc-Request-Builder (get, post, patch)
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
// Importiert statische MockMvc-Result-Matcher (status, jsonPath)
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
* Dies ist ein Web-Schicht-Test (Slice Test) für den RobotController.
*
* @WebMvcTest(RobotController.class):
* Lädt *nur* die Web-Schicht von Spring (DispatcherServlet, Controller, JSON-Konvertierung).
* Es wird kein Service, Repository oder CommandLineRunner geladen. Das macht den Test extrem schnell.
*/
@WebMvcTest(RobotController.class)
class RobotControllerTest {

    // @Autowired: Spring injiziert uns die MockMvc-Instanz.
    // MockMvc ist die "REST-Clientbibliothek", die in der Aufgabe erwähnt wird.
    // Sie simuliert HTTP-Anfragen an unseren Controller.
    @Autowired
    private MockMvc mockMvc;

    // @Autowired: Wir brauchen einen ObjectMapper, um Java-Objekte (DTOs)
    // in JSON-Strings für den Request-Body umzuwandeln.
    @Autowired
    private ObjectMapper objectMapper;

    // @MockBean: Da @WebMvcTest den echten Service nicht lädt,
    // sagen wir Spring, es soll einen "Dummy" (Mock) für den RobotService erstellen.
    // Wir kontrollieren in jedem Test, was dieser Mock zurückgibt.
    @MockBean
    private RobotService robotService;

    // Eine wiederverwendbare Test-Roboter-Instanz
    private Robot testRobot;

    // Diese Methode wird vor *jedem* einzelnen @Test ausgeführt.
    // Perfekt, um unsere Testdaten zurückzusetzen.
    @BeforeEach
    void setUp() {
        // Wir erstellen einen sauberen Roboter für jeden Test
        testRobot = new Robot("r1");
        testRobot.setPosition(new Position(10, 10)); // Setzen einer bekannten Position
        testRobot.setEnergy(100);
    }

    // Aufgabe 1: Roboter-Status abrufen
    @Test
    void whenGetStatus_thenReturns200AndRobot() throws Exception {
        // ARRANGE (Vorbereiten):
        // Definiere, was der gemockte Service tun soll.
        // WENN service.getRobotStatus("r1") aufgerufen wird, DANN gib testRobot zurück.
        when(robotService.getRobotStatus("r1")).thenReturn(Optional.of(testRobot));

        // ACT (Handeln) & ASSERT (Prüfen):
        // Führe eine simulierte GET-Anfrage an "/robots/r1/status" durch
        mockMvc.perform(get("/robots/r1/status"))
            // Erwarte HTTP-Status 200 (OK)
            .andExpect(status().isOk())
            // Erwarte, dass das zurückgegebene JSON ein Feld "id" mit dem Wert "r1" hat
            .andExpect(jsonPath("$.id").value("r1"))
            // Erwarte, dass die Energie 100 ist
            .andExpect(jsonPath("$.energy").value(100))
            // Erwarte, dass die HATEOAS-Links existieren
            .andExpect(jsonPath("$._links.self").exists())
            .andExpect(jsonPath("$._links.actions").exists());
    }

    // Test für einen nicht existierenden Roboter
    @Test
    void whenGetStatus_forMissingRobot_thenReturns404() throws Exception {
        // ARRANGE:
        // WENN service.getRobotStatus("r99") aufgerufen wird, DANN gib ein leeres Optional zurück.
        when(robotService.getRobotStatus("r99")).thenReturn(Optional.empty());

        // ACT & ASSERT:
        mockMvc.perform(get("/robots/r99/status"))
            // Erwarte HTTP-Status 404 (Not Found)
            .andExpect(status().isNotFound());
    }

    // Aufgabe 2: Roboter bewegen
    @Test
    void whenMoveRobot_thenReturns200AndUpdatedRobot() throws Exception {
        // ARRANGE:
        // 1. Das DTO (Payload), das wir als JSON senden
        MoveRequest moveDto = new MoveRequest();
        moveDto.setDirection("up");

        // 2. Das Roboter-Objekt, das wir vom Service zurückerwarten
        testRobot.setPosition(new Position(10, 11)); // Die neue Position nach "up"
        
        // 3. Mocking: WENN service.moveRobot("r1", "up") aufgerufen wird, DANN ...
        when(robotService.moveRobot("r1", "up")).thenReturn(Optional.of(testRobot));

        // ACT & ASSERT:
        mockMvc.perform(post("/robots/r1/move")
            // Setze den Content-Type der Anfrage
            .contentType(MediaType.APPLICATION_JSON)
            // Wandle das DTO in einen JSON-String um und setze es als Request-Body
            .content(objectMapper.writeValueAsString(moveDto)))
            // Erwarte Status 200 (OK)
            .andExpect(status().isOk())
            // Überprüfe, ob die Position im JSON-Response korrekt aktualisiert wurde
            .andExpect(jsonPath("$.position.y").value(11));
    }

    // Aufgabe 3: Gegenstand aufheben
    @Test
    void whenPickupItem_thenReturns200AndUpdatedInventory() throws Exception {
        // ARRANGE:
        testRobot.getInventory().add("diamant");
        when(robotService.pickupItem("r1", "diamant")).thenReturn(Optional.of(testRobot));

        // ACT & ASSERT:
        mockMvc.perform(post("/robots/r1/pickup/diamant"))
            .andExpect(status().isOk())
            // Erwarte, dass das "inventory"-Array das Element "diamant" enthält
            .andExpect(jsonPath("$.inventory[0]").value("diamant"));
    }

    // Aufgabe 4: Roboter-Zustand aktualisieren (PATCH)
    @Test
    void whenPatchState_thenReturns200AndPatchedRobot() throws Exception {
        // ARRANGE:
        // 1. Das DTO für den Patch
        PatchStateRequest patchDto = new PatchStateRequest();
        patchDto.setEnergy(80);

        // 2. Erwartetes Ergebnis
        testRobot.setEnergy(80);

        // 3. Mocking: eq("r1") und any() sind Mockito-Matcher.
        // Wir prüfen die ID exakt, aber der Inhalt des DTOs ist uns hier egal (any()).
        when(robotService.updateRobotState(eq("r1"), any(PatchStateRequest.class)))
            .thenReturn(Optional.of(testRobot));

        // ACT & ASSERT:
        mockMvc.perform(patch("/robots/r1/state")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(patchDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.energy").value(80))
            .andExpect(jsonPath("$.position.x").value(10)); // Position bleibt unverändert
    }

    // Aufgabe 5: Alle Aktionen abrufen (Paginierungs-Test)
    @Test
    void whenGetActions_thenReturns200AndPagedResponse() throws Exception {
        // ARRANGE:
        // Paginierung zu mocken ist aufwändiger:
        // 1. Erstelle eine Test-Aktion
        RobotAction action1 = new RobotAction(1L, "Moved", "up", Instant.now());
        List<RobotAction> actions = List.of(action1);

        // 2. Erstelle ein Pageable-Objekt (das, was der Controller intern erstellt)
        // Seite 1 in der API ist Seite 0 für Spring
        Pageable pageable = PageRequest.of(0, 5); 

        // 3. Erstelle das finale Page-Objekt (das, was der Service zurückgibt)
        Page<RobotAction> pagedActions = new PageImpl<>(actions, pageable, 1L); // 1L = totalElements

        // 4. Mocking: WENN service.getRobotActionsPaged("r1", beliebiges Pageable) ...
        when(robotService.getRobotActionsPaged(eq("r1"), any(Pageable.class)))
            .thenReturn(Optional.of(pagedActions));

        // ACT & ASSERT:
        // Wir rufen die API mit Seite 1 auf
        mockMvc.perform(get("/robots/r1/actions?page=1&size=5"))
            .andExpect(status().isOk())
            // Erwarte, dass die Paginierungs-Infos korrekt sind
            .andExpect(jsonPath("$.page.number").value(1)) // API ist 1-basiert
            .andExpect(jsonPath("$.page.totalElements").value(1))
            .andExpect(jsonPath("$.page.totalPages").value(1))
            // Erwarte, dass die Aktionsdaten korrekt sind
            .andExpect(jsonPath("$.actions[0].action").value("Moved"))
            // Erwarte, dass die HATEOAS-Links (für Paginierung) existieren
            .andExpect(jsonPath("$.links[?(@.rel=='self')]").exists());
    }

    // Aufgabe 6: Anderen Roboter angreifen
    @Test
    void whenAttackRobot_thenReturns200AndAttackerStatus() throws Exception {
        // ARRANGE:
        testRobot.setEnergy(95); // Der Angreifer verliert 5 Energie
        when(robotService.attackRobot("r1", "r2")).thenReturn(Optional.of(testRobot));

        // ACT & ASSERT:
        mockMvc.perform(post("/robots/r1/attack/r2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("r1"))
            // Überprüfe die neue Energie des Angreifers
            .andExpect(jsonPath("$.energy").value(95));
    }
}