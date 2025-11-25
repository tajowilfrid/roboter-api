# Roboter-API (Spring Boot)

Dies ist ein Spring Boot-Projekt, das eine HATEOAS-konforme REST-API zur Steuerung von virtuellen Robotern bereitstellt. Das Projekt demonstriert modernes API-Design, Paginierung, automatisierte Tests sowie eine vollständige CI/CD-Pipeline mit GitLab und Google Cloud Run.

##  Schnellstart

Um das Projekt lokal einzurichten, klonen Sie das Repository:

```bash
# Über SSH
git clone git@gitlab.com:tajowilfrid/roboter-api.git

# Oder über HTTPS
git clone https://gitlab.com/tajowilfrid/roboter-api.git

cd roboter-api
```

-----

## Features

  * **RESTful API:** Sauberes Ressourcen-Design für Roboter-Interaktionen.
  * **HATEOAS:** Hypermedia-Links (`_links`) zur besseren Navigierbarkeit der API.
  * **Paginierung:** Effizienter Abruf von Listen (z.B. Aktions-Historie).
  * **Dockerized:** Optimiertes Multi-Stage Dockerfile für kleine und sichere Images.
  * **CI/CD Pipeline:** Vollautomatisches Build, Test und Deployment auf Google Cloud Run via GitLab CI.

-----

## Voraussetzungen

Für die lokale Entwicklung und Ausführung benötigen Sie:

  * **Java JDK 21** (LTS)
  * **Apache Maven** 3.8+
  * **Docker** (für Containerisierung)
  * **VS Code** (empfohlen) mit dem *Extension Pack for Java*
  * **Google Cloud CLI** (für manuelles Deployment)

-----

## Anwendung lokal ausführen

Die Anwendung ist standardmäßig so konfiguriert, dass sie auf Port `8090` läuft.

### Option 1: Über VS Code (Empfohlen)

1.  Öffnen Sie das Projektverzeichnis in Visual Studio Code.
2.  Öffnen Sie die **Spring Boot Dashboard**-Ansicht (linke Leiste).
3.  Klicken Sie beim Projekt `roboterapi` auf den **Start**-Pfeil.

### Option 2: Über das Terminal

Nutzen Sie den mitgelieferten Maven Wrapper für ein konsistentes Build-Erlebnis:

```bash
./mvnw spring-boot:run
```

Die API ist anschließend erreichbar unter: `http://localhost:8090/robots/r1/status`

-----

## Docker

Das Projekt nutzt ein **Multi-Stage Dockerfile**, das den Build-Prozess vom Laufzeit-Image trennt. Das Ergebnis ist ein schlankes Image auf Basis von `eclipse-temurin:21-jre`.

### Image bauen
*Hinweis für Mac mit M1/M2 (Apple Silicon): Nutzen Sie das `--platform` Flag für Kompatibilität mit Cloud-Servern.*
```bash
# Standard Build
docker build -t roboterapi-image .

# Build für Cloud Deployment (Linux AMD64) auf einem M1/M2 Mac
docker build --platform linux/amd64 --no-cache -t roboterapi-image .
```

### Container starten

Der Container-Port (Standard 8080) wird auf den lokalen Port 8090 gemappt.

```bash
docker run --rm -p 8090:8080 \
  -e PORT=8080 \
  --name roboter-container \
  roboterapi-image
```

-----

## CI/CD & Cloud Deployment

Das Projekt verfügt über eine `.gitlab-ci.yml` Pipeline, die bei jedem Push auf den `main`-Branch automatisch ausgeführt wird.

**Phasen der Pipeline:**

1.  **Build:** Kompiliert den Java-Code.
2.  **Test:** Führt Unit- und Integrationstests aus.
3.  **Deploy:** Baut das Docker-Image, lädt es in die **Google Artifact Registry** und deployt den Service auf **Google Cloud Run**.

### Manuelles Deployment (Optional)

Falls Sie manuell deployen möchten:

```bash
# 1. Login & Konfiguration
gcloud auth login
gcloud config set project [IHR_PROJEKT_ID]
gcloud auth configure-docker europe-west1-docker.pkg.dev

# 2. Build & Push
docker build --platform linux/amd64 -t europe-west1-docker.pkg.dev/[IHR_PROJEKT_ID]/roboter-repo/roboterapi-image .
docker push europe-west1-docker.pkg.dev/[IHR_PROJEKT_ID]/roboter-repo/roboterapi-image

# 3. Deploy
gcloud run deploy roboter-service \
  --image europe-west1-docker.pkg.dev/[IHR_PROJEKT_ID]/roboter-repo/roboterapi-image \
  --platform managed \
  --region europe-west1 \
  --allow-unauthenticated
```

-----

## Tests

### Automatisierte Tests

Das Projekt nutzt `JUnit 5` und `MockMvc` für effiziente Web-Layer-Tests (Slice Tests).
Ausführung über Terminal:

```bash
./mvnw test
```

### Manuelle Tests

Für manuelle API-Calls liegt die Datei `api-tests.http` im Projektverzeichnis bereit. Diese kann mit der VS Code Extension "REST Client" ausgeführt werden.

-----

## API-Endpunkte

Die API stellt folgende Endpunkte zur Steuerung der Roboter (Standard: `r1`, `r2`, `r3`) bereit:

| Methode | URL | Beschreibung | Beispiel-Body |
| :--- | :--- | :--- | :--- |
| `GET` | `/robots/{id}/status` | Ruft Status inkl. HATEOAS-Links ab. | - |
| `POST` | `/robots/{id}/move` | Bewegt den Roboter. | `{"direction": "up"}` |
| `POST` | `/robots/{id}/pickup/{itemId}` | Hebt einen Gegenstand auf. | - |
| `POST` | `/robots/{id}/putdown/{itemId}` | Legt einen Gegenstand ab. | - |
| `PATCH` | `/robots/{id}/state` | Aktualisiert Energie/Position. | `{"energy": 80}` |
| `GET` | `/robots/{id}/actions` | Ruft Aktionen paginiert ab. | *Query:* `?page=1&size=5` |
| `POST` | `/robots/{id}/attack/{targetId}` | Greift einen anderen Roboter an. | - |