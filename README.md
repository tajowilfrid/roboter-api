# Robot API (Spring Boot)

This is a Spring Boot project providing a HATEOAS-compliant REST API for controlling virtual robots. The project demonstrates modern API design, pagination, automated testing, and a complete CI/CD pipeline using GitLab and Google Cloud Run.using GitLab, Google Cloud Run, and Cloud Storage.

**Live Demo:** [https://roboter-service-820842100826.europe-west1.run.app/](https://roboter-service-820842100826.europe-west1.run.app/)

## Quick Start

To set up the project locally, clone the repository:

```bash
# Via SSH
git clone git@gitlab.com:tajowilfrid/roboter-api.git

# Or via HTTPS
git clone https://gitlab.com/tajowilfrid/roboter-api.git

cd roboter-api
```

**Live Demo:** [https://roboter-service-820842100826.europe-west1.run.app/](https://roboter-service-820842100826.europe-west1.run.app/)

-----

## Features

  * **RESTful API:** Clean resource design for robot interactions.
  * **HATEOAS:** Hypermedia links (`_links`) for better API navigability.
  * **Pagination:** Efficient retrieval of lists (e.g., action history).
  * **Dockerized:** Optimized multi-stage Dockerfile for small and secure images.
  * **CI/CD Pipeline:** Fully automated build, test, and deployment to Google Cloud Run via GitLab CI.
  * **Web Components Frontend:** A modern, framework-free SPA (Single Page Application) using native Web Components and Shadow DOM.
  * **Micro Frontend Architecture:** The frontend is decoupled, bundled via Vite and served via Google Cloud Storage (CDN).



-----

## Prerequisites

For local development and execution, you need:

  * **Java JDK 21** (LTS)
  * **Apache Maven** 3.8+
  * **Docker** (for containerization)
  * **VS Code** (recommended) with the *Extension Pack for Java*
  * **Node.js & npm** (for building the frontend bundle)
  * **Google Cloud CLI** (for manual deployment)

-----

## Running the Application Locally

The application is configured by default to run on port `8090`.

### Option 1: Via VS Code (Recommended)

1.  Open the project directory in Visual Studio Code.
2.  Open the **Spring Boot Dashboard** view (left sidebar).
3.  Click the **Start** arrow next to the `roboterapi` project.

### Option 2: Via Terminal

Use the included Maven Wrapper for a consistent build experience:

```bash
./mvnw spring-boot:run
```

The API will be accessible at: `http://localhost:8090/robots/r1/status`

-----

## Docker

The project uses a **Multi-Stage Dockerfile** that separates the build process from the runtime image. The result is a slim image based on `eclipse-temurin:21-jre`.

### Build Image

*Note for Mac with M1/M2 (Apple Silicon): Use the `--platform` flag for compatibility with cloud servers.*

```bash
# Standard Build
docker build -t roboterapi-image .

# Build for Cloud Deployment (Linux AMD64) on an M1/M2 Mac
docker build --platform linux/amd64 --no-cache -t roboterapi-image .
```

### Start Container

The container port (Standard 8080) is mapped to the local port 8090.

```bash
docker run --rm -p 8090:8080 \
  -e PORT=8080 \
  --name roboter-container \
  roboterapi-image
```

### Access the Frontend

Open the browser and navigate to: [http://localhost:8090/index.html](https://www.google.com/search?q=http://localhost:8090/index.html)

You will see the **Robot Mission Control System** where you can control the robot, view logs, and change settings.

-----

## Frontend Architecture (Micro Frontends)

This project implements a **Micro Frontend** approach. The frontend logic is decoupled from the backend and can be embedded into any website.

### 1\. Build Process (Vite)

The frontend source code located in `frontend-build/` is bundled into a single JavaScript file.

```bash
cd frontend-build
npm install
npm run build
```

*Output:* `dist/robot-components.es.js`

### 2\. Cloud Storage Deployment

The bundled file is uploaded to a public Google Cloud Storage bucket to serve as a CDN.

```bash
# Upload bundle
gcloud storage cp dist/robot-components.es.js gs://roboter-frontend-assets/

# Make public
gcloud storage objects update gs://roboter-frontend-assets/robot-components.es.js --add-acl-grant=entity=AllUsers,role=READER
```

### 3\. Integration Example

To verify the portability, the file `test-integration.html` demonstrates how to embed the robot controls into an external 3rd-party website using a simple script tag:

```html
<script type="module" src="https://storage.googleapis.com/roboter-frontend-assets/robot-components.es.js"></script>
```

-----

## CI/CD & Cloud Deployment

The project includes a `.gitlab-ci.yml` pipeline that runs automatically on every push to the `main` branch.

**Pipeline Stages:**

1.  **Build:** Compiles the Java code.
2.  **Test:** Executes unit and integration tests.
3.  **Deploy:** Builds the Docker image, pushes it to the **Google Artifact Registry**, and deploys the service to **Google Cloud Run**.

### Manual Deployment (Optional)

If you wish to deploy manually:

```bash
# 1. Login & Configuration
gcloud auth login
gcloud config set project [YOUR_PROJECT_ID]
gcloud auth configure-docker europe-west1-docker.pkg.dev

# 2. Build & Push
docker build --platform linux/amd64 -t europe-west1-docker.pkg.dev/[YOUR_PROJECT_ID]/roboter-repo/roboterapi-image .
docker push europe-west1-docker.pkg.dev/[YOUR_PROJECT_ID]/roboter-repo/roboterapi-image

# 3. Deploy
gcloud run deploy roboter-service \
  --image europe-west1-docker.pkg.dev/[YOUR_PROJECT_ID]/roboter-repo/roboterapi-image \
  --platform managed \
  --region europe-west1 \
  --allow-unauthenticated
```

-----

## Tests

### Automated Tests

The project uses `JUnit 5` and `MockMvc` for efficient web-layer tests (Slice Tests).
Execute via terminal:

```bash
./mvnw test
```

### Manual Tests

For manual API calls, the file `api-tests.http` is available in the project directory. This can be executed using the VS Code extension "REST Client".

-----

## API Endpoints

The API provides the following endpoints to control the robots (Default: `r1`, `r2`, `r3`):

| Method | URL | Description | Example Body |
| :--- | :--- | :--- | :--- |
| `GET` | `/robots/{id}/status` | Retrieves status incl. HATEOAS links. | - |
| `POST` | `/robots/{id}/move` | Moves the robot. | `{"direction": "up"}` |
| `POST` | `/robots/{id}/pickup/{itemId}` | Picks up an item. | - |
| `POST` | `/robots/{id}/putdown/{itemId}` | Puts down an item. | - |
| `PATCH` | `/robots/{id}/state` | Updates energy/position. | `{"energy": 80}` |
| `GET` | `/robots/{id}/actions` | Retrieves paginated actions. | *Query:* `?page=1&size=5` |
| `POST` | `/robots/{id}/attack/{targetId}` | Attacks another robot. | - |
