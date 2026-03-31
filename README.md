# event-driven-starter

[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.13-6DB33F?logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-Roosevelt%20Esteban-0A66C2?logo=linkedin&logoColor=white)](https://linkedin.com/in/roosevelt-esteban)
[![GitHub](https://img.shields.io/badge/GitHub-resteban--pe-181717?logo=github&logoColor=white)](https://github.com/resteban-pe)

Enterprise asynchronous messaging demo — Kafka + RabbitMQ with resilience patterns.

---

## 📌 What is this?

**event-driven-starter** is a portfolio project that demonstrates real-world event-driven architecture using two complementary messaging technologies: **Apache Kafka** and **RabbitMQ**. It is not a production application — it is a deliberate showcase of how a Technical Lead designs resilient, async communication between services.

The project exposes a REST API (documented via Swagger UI) that triggers events manually, making the full messaging flow observable and testable without any external dependencies beyond Docker.

Resilience patterns are implemented at the framework level: Kafka consumers use `@RetryableTopic` with configurable backoff and automatic Dead Letter Topic routing; RabbitMQ queues are configured with `x-dead-letter-exchange` to capture unprocessable messages in a dedicated DLQ.

## 🎯 Why Kafka AND RabbitMQ?

This is intentional. They are not redundant — they solve different problems and represent two distinct integration paradigms that coexist in enterprise architectures:

| | Apache Kafka | RabbitMQ |
|---|---|---|
| **Model** | Log-based, append-only | Message broker, push-based |
| **Use case here** | Order lifecycle events (durable, replayable) | Notification dispatch (transient, routed) |
| **Retention** | Persists messages regardless of consumption | Removed from queue on ACK |
| **Routing** | Topic partitions, consumer groups | Exchanges, routing keys, bindings |
| **Retry mechanism** | `@RetryableTopic` — creates separate retry topics | `x-dead-letter-exchange` — routes to DLQ on failure |

---

## 🔁 Kafka Flow — Order Events

```
REST POST /api/v1/events/kafka
         │
         ▼
 OrderEventProducer
  kafkaTemplate.send()
  key = orderId
         │
         ▼
  ┌─────────────────┐
  │   orders-topic  │
  └────────┬────────┘
           │
           ▼
  OrderEventConsumer
   @KafkaListener
   @RetryableTopic(attempts=3, backoff=3000ms)
           │
    ┌──────┴──────┐
    │             │
  success      failure
    │             │
   ACK        retry #1 → retry #2 → retry #3
                                        │
                                        ▼
                              ┌──────────────────┐
                              │    orders-dlt     │
                              └────────┬─────────┘
                                       │
                                       ▼
                               OrderEventConsumer
                                 @DltHandler
                               handleDlt() — logs
```

## 📨 RabbitMQ Flow — Notifications

```
REST POST /api/v1/events/rabbit
         │
         ▼
 NotificationPublisher
  rabbitTemplate.convertAndSend()
  exchange = notifications.exchange
  routingKey = "notification"
         │
         ▼
  ┌──────────────────────┐
  │  notifications       │  DirectExchange
  │  .exchange           │
  └──────────┬───────────┘
             │  binding: routingKey = "notification"
             ▼
  ┌──────────────────────┐
  │  notifications.queue │  x-dead-letter-exchange → ""
  └──────────┬───────────┘  x-dead-letter-routing-key → notifications.dlq
             │
             ▼
  NotificationListener
   @RabbitListener
             │
    ┌────────┴────────┐
    │                 │
  success          failure
    │                 │
   ACK               │
                      ▼
             ┌─────────────────────┐
             │  notifications.dlq  │  durable queue
             └─────────────────────┘
```

---

## 🛠️ Tech Stack

| Technology | Version | Role |
|---|---|---|
| Java | 21 (OpenJDK) | Runtime |
| Spring Boot | 3.5.13 | Application framework |
| Apache Kafka | Confluent 7.6.0 | Event streaming — order lifecycle |
| RabbitMQ | 3.13-management-alpine | Message broker — notifications |
| Spring Kafka | 3.3.x (managed) | `@KafkaListener`, `@RetryableTopic` |
| Spring AMQP | 3.x (managed) | `@RabbitListener`, `RabbitTemplate` |
| SpringDoc OpenAPI | 2.7.0 | Swagger UI |
| Lombok | managed | Boilerplate reduction |
| JUnit 5 | managed | Unit & integration tests |
| Mockito | managed | Mocking in unit tests |
| Testcontainers | managed | Real containers in integration tests |
| Maven | 3.9.14 | Build tool |
| Docker | 29.2.1 | Local infrastructure |

---

## ⚡ Quick Start

### Prerequisites
- Java 21
- Maven 3.9+
- Docker Desktop running

### Steps

**1. Clone the repository**
```bash
git clone https://github.com/resteban-pe/event-driven-starter.git
cd event-driven-starter
```

**2. Start infrastructure (Kafka + RabbitMQ)**
```bash
docker compose up -d
```

Services started:

| Service | Internal port | Host port |
|---|---|---|
| Zookeeper | 2181 | 2181 |
| Kafka (Confluent 7.6.0) | 9092 | **9093** |
| RabbitMQ AMQP | 5672 | **5673** |
| RabbitMQ Management UI | 15672 | **15673** |

> Ports 9093 / 5673 are intentionally offset to avoid conflicts with other local services.

**3. Build**
```bash
mvn clean install
```

**4. Run**
```bash
# PowerShell
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"

# Bash / Linux / macOS
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The `dev` profile is also set as default in `application.yaml`, so plain `mvn spring-boot:run` works on most shells.

**5. Open Swagger UI**
```
http://localhost:8080/swagger-ui.html
```

RabbitMQ Management UI: `http://localhost:15673` — user: `events_user` / pass: `events_pass`

---

## 📡 API Endpoints

Base path: `/api/v1/events`

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/kafka` | Publish an `OrderEvent` to Kafka topic `orders-topic` |
| `POST` | `/rabbit` | Publish a `NotificationMessage` to RabbitMQ exchange `notifications.exchange` |

### POST `/api/v1/events/kafka`

```json
{
  "orderId": "ORD-001",
  "status": "CREATED",
  "timestamp": "2026-03-30T17:00:00Z"
}
```

### POST `/api/v1/events/rabbit`

```json
{
  "id": "MSG-001",
  "type": "EMAIL",
  "payload": "Your order has been confirmed."
}
```

Both endpoints return:
```json
{
  "success": true,
  "message": "Success",
  "data": "..."
}
```

---

## 🧱 Key Patterns Implemented

| Pattern | Description | Implementation |
|---|---|---|
| **Retry Policy** | Automatic retry with backoff on consumer failure | `@RetryableTopic(attempts="3", backoff=@Backoff(delay=3000))` on `OrderEventConsumer` |
| **Dead Letter Topic (Kafka)** | Failed messages after all retries routed to isolation topic | `@DltHandler` → `orders-dlt` |
| **Dead Letter Queue (RabbitMQ)** | Unprocessable messages forwarded to isolation queue | `x-dead-letter-exchange` + `x-dead-letter-routing-key` → `notifications.dlq` |
| **Event-Driven REST trigger** | REST API as manual event injector — decouples HTTP from messaging | `EventController` → `OrderEventProducer` / `NotificationPublisher` |
| **JSON Serialization** | Type-safe message serialization for both brokers | `JsonSerializer`/`JsonDeserializer` (Kafka), `Jackson2JsonMessageConverter` (RabbitMQ) |
| **Producer-Consumer** | Explicit separation of publishing and consuming responsibilities | `OrderEventProducer` ↔ `OrderEventConsumer`, `NotificationPublisher` ↔ `NotificationListener` |

---

## 🧪 Tests

```
src/test/java/pe/resteban/events/
├── model/
│   └── OrderEventTest.java          # Pure unit — record construction & equality
├── kafka/producer/
│   └── OrderEventProducerTest.java  # Unit with Mockito — verifies KafkaTemplate.send()
└── rest/
    └── EventControllerIT.java       # Integration — real Kafka + RabbitMQ via Testcontainers
```

Run all tests:
```bash
mvn test
```

The integration test (`EventControllerIT`) spins up real Kafka (`confluentinc/cp-kafka:7.6.0`) and RabbitMQ (`rabbitmq:3.13-management-alpine`) containers via Testcontainers — no mocks, no embedded brokers.

---

## 📁 Project Structure

```
src/main/java/pe/resteban/events/
├── kafka/
│   ├── config/      KafkaConfig.java         — topics, factories, JSON serialization
│   ├── producer/    OrderEventProducer.java   — publishes to orders-topic
│   └── consumer/    OrderEventConsumer.java   — @RetryableTopic + @DltHandler
├── rabbitmq/
│   ├── config/      RabbitMQConfig.java       — queues, exchange, DLQ binding
│   ├── publisher/   NotificationPublisher.java
│   └── listener/    NotificationListener.java
├── model/
│   ├── OrderEvent.java           — record: orderId, status, timestamp
│   └── NotificationMessage.java  — record: id, type, payload
├── rest/
│   └── EventController.java     — POST /api/v1/events/{kafka,rabbit}
└── shared/response/
    └── ApiResponse.java         — generic wrapper: ok() / error()
```

---

## 💼 Portfolio Context

This project is part of the **enterprise microservices portfolio** of Roosevelt Esteban Torres. Other projects in the series explore API Gateway patterns, distributed tracing, and service mesh configurations.

The goal of this series is to demonstrate production-grade architectural thinking — not just working code, but code that reflects real trade-offs: why two messaging systems instead of one, why retry at the consumer and not the producer, why DLQ isolation matters in async systems.

---

## 👤 Author

**Roosevelt Esteban Torres**
Technical Lead & Integration Architect

[![LinkedIn](https://img.shields.io/badge/LinkedIn-roosevelt--esteban-0A66C2?logo=linkedin&logoColor=white)](https://linkedin.com/in/roosevelt-esteban)
[![GitHub](https://img.shields.io/badge/GitHub-resteban--pe-181717?logo=github&logoColor=white)](https://github.com/resteban-pe)

---

*MIT License — feel free to fork, study, and adapt.*
