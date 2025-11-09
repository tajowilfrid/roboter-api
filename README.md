# 🤖 Roboter-API (Spring Boot)

Dies ist ein Spring Boot-Projekt, das eine HATEOAS-konforme REST-API zur Steuerung von virtuellen Robotern bereitstellt. Das Projekt wurde im Rahmen einer Übung erstellt und umfasst API-Design, Paginierung und automatisierte Tests.

---

## 📋 Voraussetzungen

Um dieses Projekt auszuführen, wird folgende Software benötigt:

* **Java JDK 21** (oder neuer)
* **Apache Maven** 3.8+
* **VS Code** (empfohlen) mit dem **Extension Pack for Java**
* (Optional) **REST Client** VS Code Extension (zum Testen der `api-tests.http`-Datei)

---

## Anwendung starten

Die Anwendung ist so konfiguriert, dass sie auf Port `8090` läuft.

### Option 1: Über VS Code (Empfohlen)

1.  Öffne das Projektverzeichnis in Visual Studio Code.
2.  Warte, bis die Java-Erweiterungen das Projekt geladen haben.
3.  Öffne die "Spring Boot Dashboard"-Ansicht (Sechseck-Icon in der linken Leiste).
4.  Finde das `roboterapi`-Projekt und klicke auf das "Start"-Symbol (Play-Button).
5.  Die API läuft, sobald im Terminal "Started RoboterapiApplication..." und "Tomcat started on port(s): 8090" erscheint.

### Option 2: Über das Terminal (Maven Wrapper)

1.  Öffne ein Terminal im Projekt-Hauptverzeichnis (`roboterapi/`).
2.  Führe den Maven Wrapper aus:
    ```bash
    ./mvnw spring-boot:run
    ```
3.  Die API läuft auf `http://localhost:8090`.

---

## Testen der API

Das Projekt enthält sowohl automatisierte Tests als auch eine Datei für manuelle Anfragen.

### 1. Automatisierte Tests (Slice Tests)

Die Tests überprüfen die Controller-Schicht und mocken den Service.

1.  Öffne die Datei `src/test/java/com/roboter/roboterapi/controller/RobotControllerTest.java` in VS Code.
2.  Klicke auf das "Play"-Symbol neben dem Klassennamen `RobotControllerTest`, um alle Tests auszuführen.
3.  Die Ergebnisse werden im Tab "Test Runner for Java" angezeigt (alle Tests sollten grün sein).

### 2. Manuelle Endpunkt-Tests (REST Client)

Für manuelle Tests mit einem "echten" Server:

1.  Stelle sicher, dass die Anwendung läuft (siehe "Anwendung starten").
2.  Öffne die Datei `api-tests.http` in VS Code.
3.  Klicke auf den `Send Request`-Link über einer beliebigen HTTP-Anfrage, um diese an die laufende Anwendung zu senden. Die Antwort erscheint in einem neuen Fenster.

---

## API-Endpunkte

Die API stellt die folgenden Endpunkte zur Steuerung der Roboter `r1`, `r2` und `r3` (Standard-Roboter) bereit.

| Methode | URL | Beschreibung | Body (Beispiel) |
| :--- | :--- | :--- | :--- |
| `GET` | `/robots/{id}/status` | Ruft den Status (inkl. `_links`) eines Roboters ab. | |
| `POST` | `/robots/{id}/move` | Bewegt einen Roboter. | `{"direction": "up"}` |
| `POST` | `/robots/{id}/pickup/{itemId}` | Roboter hebt einen Gegenstand auf. | |
| `POST` | `/robots/{id}/putdown/{itemId}` | Roboter legt einen Gegenstand ab. | |
| `PATCH` | `/robots/{id}/state` | Aktualisiert Energie oder Position. | `{"energy": 80}` |
| `GET` | `/robots/{id}/actions` | Ruft Aktionen paginiert ab (z.B. `?page=1&size=2`). | |
| `POST` | `/robots/{id}/attack/{targetId}` | Lässt einen Roboter einen anderen angreifen. | |