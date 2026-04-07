# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build
./mvnw clean package

# Run (development profile)
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# Run built JAR
java -jar target/FintrackerGateway-0.0.1.jar --spring.profiles.active=dev

# Build Docker image (build JAR first)
./mvnw clean package
docker build -t fintrackergateway:latest .
```

No tests are currently implemented in this project.

## Architecture

This is a **Spring Cloud Gateway** (reactive/WebFlux) acting as the API gateway for the FinTracker microservices ecosystem.

### Request Flow

1. Incoming request hits a route defined in `application.yaml`
2. If the route requires auth (determined by `AuthServiceRouteValidator`), `AuthConfigGatewayFilter` runs
3. The filter extracts `accessToken` and `refreshToken` from HTTP cookies
4. It calls the User Auth Service to validate the access token
5. On expiry, it calls the auth service to get a new access token using the refresh token, then retries
6. On success, it injects `userEmail` and `userId` headers into the downstream request
7. If auth fails, a `BadException` is thrown → `GlobalExceptionHandler` returns 400

### Key Classes

- **`AuthConfigGatewayFilter`** — Core gateway filter implementing JWT validation and token refresh logic. Uses `WebClient` to call the auth service reactively.
- **`AuthServiceRouteValidator`** — Defines which routes are public (no auth required): `/oauth2/**`, `/userauth/api/**`, `/swagger/**`, `/actuator/**`, `/fallback`.
- **`fallback`** (controller) — Returns a plain string response when circuit breaker trips.

### Routes (defined in `application.yaml`)

| Route | Upstream |
|---|---|
| `/personal/**` | `${PERSONAL_SERVICE_URL}` (requires auth filter) |
| `/oauth2/**` | `${USER_AUTH_SERVICE_URL}` (circuit breaker) |
| `/userauth/api/**` | `${USER_AUTH_SERVICE_URL}` (circuit breaker) |
| `/swagger/**` | Aggregated OpenAPI docs from backend services |
| `/actuator/**` | Local actuator endpoints |

### Profiles

- **`dev`**: Port 8081, debug logging, CORS for `http://localhost:5173`, auth service at `http://localhost:8084`
- **`prod`**: Port 8080, auth service at `http://userauthenticationservice:8080`, CORS for `https://frontend.vrajpatelproject.software`, Eureka disabled

### External Service Dependencies

- **User Auth Service** — Called at `/userauth/api/auth/validate` and `/userauth/api/auth/getNewAccessToken`; URL configured via `USER_AUTH_SERVICE_URL` env var (prod) or `http://localhost:8084` (dev)
- **Personal Expense Service** — URL from `PERSONAL_SERVICE_URL` env var
- **Jaeger** — Distributed tracing via gRPC to `http://jaeger:4317`
- **Prometheus** — Metrics scraped from `/actuator/prometheus`

### Deployment

Deployed to GKE (`fintracker` namespace) via Kubernetes manifests in `k8s/`. The ingress exposes the gateway at `fintracker.vrajpatelproject.software` with a GCP-managed TLS certificate. Docker image published to `us-central1-docker.pkg.dev/fintracker-466022/fintracker/gateway/fintrackergateway`.
