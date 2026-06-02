# Distributed Chat Microservices Platform

A modern, scalable distributed chat application built with Java, Spring Boot, gRPC, and RabbitMQ. This project demonstrates a robust microservices architecture featuring the Outbox Pattern, GraphQL data aggregation, and event-driven automation.

## 🏗️ Architecture Overview

The system consists of six specialized microservices and a shared infrastructure layer:

### 🚀 Core Services
*   **BFF (Backend-for-Frontend):**
    *   **GraphQL API:** Aggregates data from User and Chat services into a unified schema.
    *   **REST Proxy:** Provides direct, authenticated access to downstream services.
    *   **Security Gateway:** Manages OAuth2 login and JWT distribution.
*   **UserService:**
    *   Full CRUD for user profiles.
    *   Persistent storage in `users_db`.
    *   High-performance gRPC endpoint for internal profile lookups.
*   **ChatService (Message Engine):**
    *   Handles message persistence and history.
    *   **Outbox Pattern:** Ensures atomic database updates and event publishing.
    *   gRPC Server for message operations.
*   **ChatAPI:**
    *   A REST-to-gRPC bridge that translates web requests for the ChatService.
*   **BotService:**
    *   Reactive consumer that listens for chat events on RabbitMQ.
    *   Generates automated support responses and injects them back via gRPC.
*   **NotificationService:**
    *   Asynchronous consumer that simulates real-time push/email notifications.
*   **AuthService:**
    *   OAuth2 Authorization Server using Spring Authorization Server.
    *   Database-backed authentication against the shared `users_db`.

## 🛡️ Security
*   **OAuth2 / OpenID Connect:** Standardized login flow handled by the BFF.
*   **JWT Customization:** Tokens include a custom `user_id` claim, providing a stable, immutable identifier across the cluster.
*   **Zero-Trust Networking:** Every internal service acts as a Resource Server, performing independent stateless validation of JWTs.

## 📡 Messaging & Integration
*   **gRPC:** Used for all synchronous, internal service-to-service communication for maximum performance.
*   **RabbitMQ:** Powering the asynchronous, event-driven architecture.
*   **Outbox Pattern:** Guarantees that a message is only "published" if it has been successfully saved to the database.

## 🛠️ Tech Stack
*   **Language:** Java 25/26
*   **Framework:** Spring Boot 4.x / Spring Cloud
*   **API:** GraphQL, REST, gRPC
*   **Database:** MySQL 8.0
*   **Broker:** RabbitMQ
*   **Orchestration:** Kubernetes (Ingress, Services, ConfigMaps)
*   **Containerization:** Multi-stage Dockerfiles

## 🚀 Getting Started

### Prerequisites
*   Docker & Docker Desktop (with Kubernetes enabled)
*   Maven 3.9+
*   `kubectl`

### Local Infrastructure
1.  Initialize the databases:
    ```bash
    # init-db/init.sql contains all necessary CREATE DATABASE statements
    ```
2.  Start infrastructure via Docker Compose:
    ```bash
    docker-compose up -d mysql rabbitmq
    ```

### Kubernetes Deployment
1.  Build the images (repeat for all services):
    ```bash
    docker build -t <service-name>:latest .
    ```
2.  Apply the manifests:
    ```bash
    kubectl apply -f k8s/infrastructure.yaml
    kubectl apply -f k8s/services.yaml
    kubectl apply -f k8s/ingress.yaml
    ```
3.  Access the platform:
    *   **BFF:** `http://bff.localtest.me`
    *   **Auth:** `http://auth.localtest.me`

## 🧪 Testing
Each service contains a suite of unit and integration tests. Run them using:
```bash
./mvnw test
```

## 📝 Roadmap History
- **Phase 1:** Chat Domain & gRPC implementation.
- **Phase 2:** User Service & BFF GraphQL integration.
- **Phase 3:** Event-Driven Notifications (Outbox Pattern).
- **Phase 4:** Advanced BFF Aggregation & REST Proxying.
- **Phase 5:** Kubernetes Manifests & Containerization.
- **Phase 6:** Automated Bot Service & Persistent User Lifecycle.
