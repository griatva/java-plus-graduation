# Explore With Me Plus — Event Platform & Recommendation System

**Explore With Me (EWM)** is a backend application for managing and discovering events.  
It represents an event listing platform where users can publish events, browse available activities,
interact with events, and receive personalized recommendations based on their behavior.

The project was developed as a **graduation project**, based on a prior **team learning project**.
All architectural changes, refactoring, and additional functionality in this version
were implemented by me independently.

---

## Short Overview

This project demonstrates backend development skills with a strong focus on
**microservice architecture**, **service interaction**, and **real-time data processing**.

### Key features
- Event listing and management platform (events, users, comments, participation requests)
- Separation of concerns via independent domain microservices
- Centralized configuration, service discovery, and API gateway
- Real-time recommendation pipeline using Kafka and gRPC
- Fault-tolerant inter-service communication

### Tech Stack (Core)
**Java 21 · Spring Boot · Spring Cloud (Config, Eureka, Gateway) ·  
PostgreSQL · Apache Kafka · gRPC · OpenFeign · Resilience4j · Docker Compose**

---

## About the Project

Explore With Me is designed as an **event discovery application** (“афиша”):
users can create events, search and filter available activities,
interact with events (views, registrations, likes),
and receive recommendations based on their interaction history.

The system is split into multiple services responsible for:
- core event and user management
- statistics collection
- real-time recommendation calculations

The project focuses on **architecture and system design** rather than UI or frontend features.

---

## Project Background

This graduation project is based on the original group project:
https://github.com/Maxim2777/java-explore-with-me-plus

All modifications, refactoring, service decomposition, and new components
described below were implemented by me as part of the graduation work.

### Original Group Project Contributors
- https://github.com/Maxim2777
- https://github.com/Akmula
- https://github.com/griatva
- https://github.com/ChakNikolay87

---

## Implemented Changes and Extensions

### Stage 1 — Cloud-Ready Architecture

The original project was adapted for a cloud-oriented microservice environment.

Configured Spring Cloud components:
- Spring Cloud Config Server (centralized configuration)
- Spring Cloud Eureka (service discovery)
- Spring Cloud Gateway (single entry point for external requests)

The statistics client (`StatClient`) was integrated with Eureka using:
- `DiscoveryClient`
- `RetryTemplate`

---

### Stage 2 — Service Decomposition

The monolithic `main-service` was split into independent domain services:
- `event-service`
- `user-service`
- `comment-service`
- `request-service`

A shared library **interaction-api** was introduced, containing:
- common DTOs
- exceptions
- Feign clients
- fallback implementations
- shared contracts

Inter-service communication was implemented using:
- Spring Cloud OpenFeign
- Spring Cloud Circuit Breaker (Resilience4j)

---

### Stage 3 — Recommendation System

A real-time recommendation pipeline was implemented using **Apache Kafka** and **gRPC**.

The pipeline consists of three services:
- **Collector**
- **Aggregator**
- **Analyzer**

Each service:
- retrieves configuration via service discovery
- runs on a dynamically assigned gRPC port
- is deployed together with Kafka using Docker Compose

---

## Architecture Overview

The system consists of the following services:

- **discovery-server**  
  Service registry (Eureka) for dynamic discovery.

- **config-server**  
  Centralized configuration storage for all services.

- **gateway**  
  API Gateway handling routing and basic authorization.

- **event-service**  
  Event management service (`event-db`).

- **user-service**  
  User management service (`user-db`).

- **comment-service**  
  Event comments management (`comment-db`).

- **request-service**  
  Participation requests management (`request-db`).

- **stats-server**  
  Collection and aggregation of view statistics (`stats-db`).

---

## High-Level Project Structure

```
java-plus-graduation/
├── pom.xml                  # Parent Maven configuration
├── docker-compose.yml       # Local infrastructure
│
├── core/                    # Core domain services
│   ├── event-service/
│   ├── user-service/
│   ├── comment-service/
│   ├── interaction-api/    # Shared API contracts & DTOs
│   └── request-service/
│
├── stats/                   # Statistics & recommendations
│   ├── stats-client/
│   ├── collector/
│   ├── aggregator/
│   ├── serialization/
│   └── analyzer/
│      
└── infra/                   # Infrastructure services
    ├── config-server/
    ├── discovery-server/
    └── gateway-server/
```
---

## Recommendation Pipeline

- **Collector**  
  gRPC service receiving user actions (view, register, like)  
  Publishes Avro messages to `stats.user-actions.v1`.

- **Aggregator**  
  Consumes user actions, maintains a user–event weight matrix,  
  incrementally computes cosine similarity between events,  
  and publishes results to `stats.events-similarity.v1`.

- **Analyzer**  
  Consumes both Kafka topics and exposes gRPC APIs to:
  - retrieve similar events
  - provide personalized recommendations
  - calculate aggregated interaction weights

---

## Service Interaction

- All services register in **discovery-server**
- Configuration is loaded from **config-server**
- External requests go through **gateway**
- Internal communication uses **Feign**, **gRPC**, and **Kafka**

---

## API & Database

- OpenAPI JSON specifications are included in the repository:
  - `ewm-main-service-spec.json`
  - `ewm-stats-service-spec.json`

  These specifications are based on the original group project and may not fully
  reflect all changes and extensions introduced in this graduation project
  (e.g. additional endpoints or updated endpoint behavior).

- PostgreSQL schemas are defined in `schema.sql` for each core service and analyzer.

---

## Docker Compose

The `docker-compose.yml` file starts the following infrastructure components:
- **5 PostgreSQL databases** (one database per service)
- **Apache Kafka**

---

## Internal API

The diagram below illustrates internal microservice communication within the project:

![internal-microservice-interaction.jpg](internal-microservice-interaction.jpg)

### Event Service
(A dedicated controller is used for internal endpoints: `InternalEventController`)

- `GET /internal/events/{id}`  
  Retrieves an event by its ID (used by Comment Service and Request Service)  
  **Returns:** `EventFullDto`

---

### User Service
(Uses endpoints from the external API)

- `GET /admin/users`  
  Retrieves a list of users by their IDs (used by Event Service)  
  **Returns:** `List<UserDto>`

- `GET /admin/users/{userId}`  
  Retrieves a single user by ID (used by Event, Comment, and Request services)  
  **Returns:** `UserShortDto`

---

### Stats Server
(All endpoints are intended for internal use)

- `POST /hit`  
  Stores statistics data (used by Event Service)  
  **Returns:** `void`

- `GET /stats`  
  Retrieves aggregated statistics (used by Event Service)  
  **Returns:** `List<ViewStatsDto>`

---

### Participation Request Service
(A dedicated controller is used for internal endpoints: `InternalRequestController`)

- `GET /internal/events/requests/byEventId`  
  Retrieves all participation requests for a specific event  
  **Returns:** `List<ParticipationRequestDto>`

- `GET /internal/events/requests/byEventIdsAndStatus`  
  Retrieves participation requests for multiple events filtered by request status  
  **Returns:** `List<ParticipationRequestDto>`

- `GET /internal/events/requests/count`  
  Retrieves the number of participation requests with a specific status for an event  
  **Returns:** `long`

- `PUT /internal/events/requests`  
  Updates participation request data (e.g., request status)  
  **Returns:** `Integer`

---

## Recommendation Services API

### Collector Service
(gRPC server for receiving user actions)

- **RPC** `CollectUserAction(UserActionProto) returns (google.protobuf.Empty)`

  **UserActionProto**
  - `int64 user_id`
  - `int64 event_id`
  - `ActionTypeProto action_type` (_VIEW, REGISTER, LIKE_)
  - `google.protobuf.Timestamp timestamp`

Publishes Avro messages (`UserActionAvro`) to the Kafka topic  
`stats.user-actions.v1`.

---

### Aggregator Service
(Communicates exclusively via Kafka; no external HTTP or gRPC endpoints)

- Consumes messages from `stats.user-actions.v1`
- Incrementally computes event similarities
- Publishes Avro messages (`EventSimilarityAvro`) to  
  `stats.events-similarity.v1`

---

### Analyzer Service
(gRPC server providing recommendations)

- **RPC** `GetSimilarEvents(SimilarEventsRequestProto) returns (stream RecommendedEventProto)`
  - **SimilarEventsRequestProto**
    - `int64 event_id`
    - `int64 user_id`
    - `int32 max_results`
  - Returns N similar events the user has not interacted with

- **RPC** `GetRecommendationsForUser(UserPredictionsRequestProto) returns (stream RecommendedEventProto)`
  - **UserPredictionsRequestProto**
    - `int64 user_id`
    - `int32 max_results`
  - Returns personalized recommendations based on interaction history and similarity

- **RPC** `GetInteractionsCount(InteractionsCountRequestProto) returns (stream RecommendedEventProto)`
  - **InteractionsCountRequestProto**
    - `repeated int64 event_id`
  - Returns aggregated interaction weights per event

- **RecommendedEventProto**
  - `int64 event_id`
  - `double score`

---

### Feign Clients

- **EventClient**  
  Used by Comment Service and Request Service to verify event existence and retrieve event data.

- **UserClient**  
  Used by Event, Comment, and Request services to retrieve user information.

- **StatsClient**  
  Used by Event Service to store and retrieve statistics.

- **ParticipationRequestClient**  
  Used by Event Service to retrieve participation requests for one or multiple events,  
  count approved requests, and update request statuses.

---

### gRPC Clients

- **CollectorClient**  
  Used in Event Service and Request Service to send user actions (`view`, `like`, `register`)  
  to the Collector Service via `CollectUserAction(UserActionProto)`.

- **RecommendationsClient**  
  Used in Event Service to request from Analyzer Service:
  - aggregated interaction weights (`GetInteractionsCount`)
  - similar events (`GetSimilarEvents`)
  - personalized recommendations (`GetRecommendationsForUser`)

---

### Kafka Topics

1. **stats.user-actions.v1**
  - **Producer:** Collector Service (Avro `UserActionAvro`)
  - **Consumers:**
    - Aggregator Service (event similarity computation)
    - Analyzer Service (interaction history updates)

2. **stats.events-similarity.v1**
  - **Producer:** Aggregator Service (Avro `EventSimilarityAvro`)
  - **Consumer:** Analyzer Service (similarity persistence)

---

### Fallback Mechanism

Fault tolerance is implemented using **Spring Cloud Circuit Breaker (Resilience4j)**  
in combination with **Feign clients**.

Implementation details:
- Each Feign client has a dedicated fallback class
- On remote call failure, the fallback method is invoked automatically
- The `fallback` attribute is used in the `@FeignClient` annotation

---

## External API References

- Recommendation service tester:  
  https://github.com/yandex-praktikum/java-plus-graduation/blob/ci/Readme.md

- Main service Postman collection:  
  https://github.com/yandex-praktikum/java-plus-graduation/tree/ci/.github/workflows/stuff/postman

---

## How to Run Locally

```bash
mvn clean package
docker-compose up
```
All services start via Docker Compose.

External access is provided through the API Gateway.

---

## Project Status

This project was developed as a graduation project and is not under active development.  
The focus was on system architecture, service interaction,  
and real-time data processing.

---

## About

This repository demonstrates practical experience with **Spring Boot**, **Spring Cloud**,  
**microservice architecture**, **inter-service communication**, and **real-time data pipelines**  
using **Apache Kafka** and **gRPC** in a backend-focused environment.
