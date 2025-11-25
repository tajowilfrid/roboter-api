# 🤖 Roboter-API (Spring Boot)

Dies ist ein Spring Boot-Projekt, das eine HATEOAS-konforme REST-API zur Steuerung von virtuellen Robotern bereitstellt. Das Projekt demonstriert modernes API-Design, Paginierung, automatisierte Tests sowie Containerisierung und Cloud-Deployment.

## ✨ Features

  * **RESTful API:** Sauberes Ressourcen-Design.
  * **HATEOAS:** Hypermedia-Links (`_links`) zur besseren Navigierbarkeit.
  * **Paginierung:** Effizienter Abruf von Listen (Aktions-Historie).
  * **Dockerized:** Multi-Stage Dockerfile für optimierte Images.
  * **Cloud Ready:** Vorbereitet für Google Cloud Run.

-----

## 📋 Voraussetzungen

Um dieses Projekt lokal auszuführen oder zu deployen, wird folgende Software benötigt:

  * **Java JDK 21** (LTS)
  * **Apache Maven** 3.8+
  * **Docker** (für Containerisierung)
  * **Google Cloud CLI** (für das Deployment)
  * **VS Code** (empfohlen) mit dem *Extension Pack for Java*

-----

## 🚀 Anwendung lokal starten (Entwicklung)

Die Anwendung ist standardmäßig so konfiguriert, dass sie auf Port `8090` läuft.

### Option 1: Über VS Code

1.  Öffnen Sie das Projektverzeichnis in Visual Studio Code.
2.  Öffnen Sie die **Spring Boot Dashboard**-Ansicht (linke Leiste).
3.  Klicken Sie beim Projekt `roboterapi` auf den **Start**-Pfeil.

### Option 2: Über das Terminal

```bash
./mvnw spring-boot:run
```

Die API ist anschließend erreichbar unter: `http://localhost:8090/robots/r1/status`

-----

## 🐳 Docker (Containerisierung)

Das Projekt enthält ein `Dockerfile` (Multi-Stage Build), das ein schlankes Image auf Basis von `eclipse-temurin:21-jre` erstellt.

### Image bauen

*Hinweis für Mac mit M1/M2 (Apple Silicon): Nutzen Sie das `--platform` Flag für Kompatibilität mit Cloud-Servern.*

```bash
# Standard Build
docker build -t roboterapi-image .

# Build für Cloud Deployment (Linux AMD64) auf einem M1/M2 Mac
docker build --platform linux/amd64 --no-cache -t roboterapi-image .
```

### Container starten

Wir mappen den Container-Port (Standard 8080) auf den lokalen Port 8090.

```bash
docker run --rm -p 8090:8080 \
  -e PORT=8080 \
  --name roboter-container \
  roboterapi-image
```

-----

## ☁️ Deployment (Google Cloud Run)

Das Deployment erfolgt Serverless auf Google Cloud Run.

### 1\. Vorbereitung & Login

```bash
gcloud auth login
gcloud config set project [DEINE_PROJECT_ID]
gcloud auth configure-docker
```

### 2\. Image taggen & pushen

```bash
# Image für Google Registry taggen
docker tag roboterapi-image gcr.io/[DEINE_PROJECT_ID]/roboterapi-image

# Image hochladen
docker push gcr.io/[DEINE_PROJECT_ID]/roboterapi-image
```

### 3\. Service starten

```bash
gcloud run deploy roboter-service \
  --image gcr.io/[DEINE_PROJECT_ID]/roboterapi-image \
  --platform managed \
  --region europe-west1 \
  --allow-unauthenticated
```

Nach erfolgreichem Deployment wird die Service-URL in der Konsole ausgegeben.

-----

## 🧪 Tests

### Automatisierte Tests

Das Projekt nutzt `JUnit 5` und `MockMvc` für Web-Layer-Tests (Slice Tests).

Ausführung über Terminal:

```bash
./mvnw test
```

### Manuelle Tests

Für manuelle API-Calls liegt die Datei `api-tests.http` im Projektverzeichnis. Diese kann mit der VS Code Extension "REST Client" ausgeführt werden.

-----

## 📡 API-Endpunkte

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