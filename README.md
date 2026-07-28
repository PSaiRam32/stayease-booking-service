# 📅 StayEase Booking Service

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen)
![Spring Security](https://img.shields.io/badge/Spring%20Security-Enabled-success)
![MySQL](https://img.shields.io/badge/Database-MySQL-blue)
![Spring Data JPA](https://img.shields.io/badge/Spring%20Data%20JPA-Hibernate-success)
![OpenFeign](https://img.shields.io/badge/OpenFeign-Service%20Communication-orange)
![Resilience4j](https://img.shields.io/badge/Resilience4j-Circuit%20Breaker-success)
![Gradle](https://img.shields.io/badge/Build-Gradle-blueviolet)
![License](https://img.shields.io/badge/License-MIT-green)

---

# 📖 Overview

The **StayEase Booking Service** is responsible for managing the complete booking lifecycle within the StayEase microservices ecosystem.

As the central reservation domain of the platform, the Booking Service coordinates booking creation, room availability validation, payment initiation, booking confirmation, cancellation, check-in, check-out, rescheduling, and booking completion while collaborating with multiple business services.

The service integrates with the Property Service to validate properties and room availability, the Payment Service to initiate and manage payment workflows, the User Service to retrieve customer information, and the Notification Service to deliver booking-related notifications.

Designed using Spring Boot and Spring Data JPA, the Booking Service follows enterprise microservices principles by maintaining dedicated ownership of reservation data while orchestrating business operations across multiple services without duplicating domain information.

---

# 🎯 Business Problem

Managing reservations in a hostel and PG booking platform requires significantly more than simply storing booking records.

The platform must ensure that:

- Rooms are available for the requested stay period.
- Booking dates are valid.
- Booking amounts are calculated accurately.
- Payments are initiated before confirmation.
- Booking status transitions follow business rules.
- Check-in and check-out occur in the correct order.
- Cancellations follow defined workflows.
- Availability is calculated dynamically from bookings.
- Notifications are sent after important booking events.
- Booking information is shared with other services without duplicating business data.

Without a dedicated Booking Service, reservation logic would become tightly coupled with property or payment management, resulting in duplicated business rules, inconsistent booking states, and poor scalability.

---

# 💡 Business Solution

The StayEase Booking Service centralizes all reservation-related business functionality into a dedicated microservice while collaborating with other domain services through OpenFeign.

The service is responsible for:

- Booking Creation
- Booking Retrieval
- Booking Confirmation
- Booking Cancellation
- Booking Rescheduling
- Check-In Management
- Check-Out Management
- Booking Completion
- Dynamic Availability Validation
- Booking Amount Calculation
- Payment Service Integration
- Notification Service Integration
- Owner Booking Dashboard
- User Booking Dashboard
- Booking Summary APIs

This separation enables booking management to evolve independently while maintaining strong domain ownership and providing scalable, enterprise-grade reservation management.

---

# 🏢 Enterprise Concepts Demonstrated

This project demonstrates several enterprise backend engineering concepts commonly adopted in production systems.

- Database per Service
- Booking Lifecycle Management
- Layered Architecture
- OpenFeign Client Communication
- Dynamic Availability Validation
- Payment Orchestration
- Booking State Management
- Spring Data JPA
- Bean Validation
- Global Exception Handling
- Centralized Logging
- Service-to-Service Communication
- Externalized Configuration
- Transaction Management
- Resilience4j Retry
- Resilience4j Circuit Breaker
- Domain-Driven Service Separation

---

# 🎯 Project Objectives

The Booking Service has been designed with the following objectives:

- Centralize reservation management.
- Maintain complete ownership of booking data.
- Validate room availability dynamically.
- Coordinate payment workflows.
- Support booking lifecycle transitions.
- Provide booking dashboards for users and owners.
- Integrate seamlessly with Property, Payment, User, and Notification Services.
- Demonstrate enterprise-grade booking orchestration.

---

# ✨ Features

## 📅 Booking Management

- Booking Creation
- Booking Retrieval
- Booking Confirmation
- Booking Cancellation
- Booking Rescheduling
- Booking Completion
- Booking History
- Booking Summary APIs

---

## 🛏 Availability Management

- Dynamic Room Availability Validation
- Date Validation
- Capacity Validation
- Booking Conflict Detection

---

## 💳 Payment Integration

- Payment Order Creation
- Payment Verification
- Failed Payment Handling
- Refund Integration
- Payment Status Synchronization

---

## 🚪 Stay Management

- Check-In
- Check-Out
- Booking Status Tracking
- Booking Lifecycle Management

---

## 👤 Dashboard Management

- User Booking Dashboard
- Owner Booking Dashboard
- Booking Statistics
- Booking History

---

## 🔄 Service Communication

- Property Service Integration
- Payment Service Integration
- User Service Integration
- Notification Service Integration
- OpenFeign-Based Communication

---

## 🚀 Reliability

- Resilience4j Retry
- Resilience4j Circuit Breaker
- Global Exception Handling
- Bean Validation
- Structured Logging
- Transaction Management

---

# 🛠 Technology Stack

| Category | Technology |
|-----------|------------|
| Language | Java 21 |
| Framework | Spring Boot 3 |
| Security | Spring Security |
| Database | MySQL |
| ORM | Spring Data JPA |
| Service Communication | OpenFeign |
| Fault Tolerance | Resilience4j |
| Validation | Bean Validation |
| Build Tool | Gradle |

---

# 🏛 High-Level Architecture

```text
                    Client Applications
                             │
                             ▼
                   Booking Controller
                             │
                             ▼
                    Booking Service
                             │
      ┌──────────────┬──────────────┬──────────────┬──────────────┐
      ▼              ▼              ▼              ▼
 Repository     Property      Payment        Notification
                  Client        Client          Client
                     │              │               │
                     ▼              ▼               ▼
              Property Service Payment Service Notification Service
                             │
                             ▼
                      User Service
```

---

# 📅 Booking Service Responsibilities

The Booking Service acts as the central reservation management service for the StayEase platform.

Its primary responsibilities include:

- Managing booking information.
- Managing booking lifecycle transitions.
- Validating room availability.
- Coordinating payment workflows.
- Managing check-in and check-out.
- Managing booking cancellations.
- Managing booking rescheduling.
- Providing booking dashboards.
- Integrating with Property, Payment, User, and Notification Services.
- Providing booking summary information to other services.

By isolating all reservation-related business functionality into a dedicated microservice, the StayEase platform maintains clear domain ownership while enabling independent scalability and long-term maintainability.

---

# 🌟 Why a Dedicated Booking Service?

Separating booking management into its own microservice provides several enterprise advantages.

- Clear Separation of Concerns
- Independent Database Ownership
- Centralized Reservation Domain
- Dynamic Availability Management
- Payment Orchestration
- Booking Lifecycle Management
- Reduced Service Coupling
- Scalable Reservation Processing

---

# 📂 Project Structure

```text
stayease-booking-service
│
├── gradle/
│
├── logs/
│
├── src
│   ├── main
│   │
│   ├── java
│   │   └── com
│   │       └── stayease
│   │           └── booking_service
│   │
│   │               ├── config
│   │               │   ├── AsyncConfig.java
│   │               │   ├── FeignConfig.java
│   │               │   ├── NotificationClient.java
│   │               │   ├── PaymentClient.java
│   │               │   ├── PropertyClient.java
│   │               │   └── UserClient.java
│   │               │
│   │               ├── controller
│   │               │   └── BookingController.java
│   │               │
│   │               ├── dto
│   │               │   ├── request
│   │               │   └── response
│   │               │
│   │               ├── entity
│   │               │   ├── Booking.java
│   │               │   ├── BookingStatus.java
│   │               │   ├── PropertyStatus.java
│   │               │   └── WashroomType.java
│   │               │
│   │               ├── exception
│   │               │   ├── BusinessException.java
│   │               │   ├── GlobalExceptionHandler.java
│   │               │   ├── PaymentFailedException.java
│   │               │   └── ResourceNotFoundException.java
│   │               │
│   │               ├── repository
│   │               │   └── BookingRepository.java
│   │               │
│   │               ├── security
│   │               │   ├── HeaderAuthenticationFilter.java
│   │               │   └── SecurityConfig.java
│   │               │
│   │               ├── service
│   │               │   ├── BookingService.java
│   │               │   └── BookingServiceImpl.java
│   │               │
│   │               └── BookingServiceApplication.java
│   │
│   ├── resources
│   │   └── application.yaml
│   │
│   └── test
│
├── .gitattributes
├── .gitignore
├── build.gradle
├── gradlew
├── gradlew.bat
├── settings.gradle
└── README.md
```

---

# 📦 Package Responsibilities

| Package | Responsibility |
|----------|----------------|
| **config** | Configures asynchronous processing, OpenFeign clients, inter-service communication, and application infrastructure. |
| **controller** | Exposes REST APIs for booking management, booking lifecycle, dashboards, booking history, cancellations, rescheduling, check-in, and check-out operations. |
| **dto** | Contains request and response models exchanged between the Booking Service, API Gateway, and other microservices. |
| **entity** | Represents the booking domain model and supporting enumerations such as booking status, property status, and washroom type. |
| **exception** | Provides centralized exception handling together with business-specific exceptions for booking validation, payment failures, and missing resources. |
| **repository** | Performs persistence operations using Spring Data JPA while abstracting database access from the service layer. |
| **security** | Implements Header Authentication Filter and Spring Security configuration to secure internal and external booking APIs. |
| **service** | Implements the complete booking lifecycle including booking creation, payment orchestration, availability validation, dashboards, cancellation, rescheduling, check-in, check-out, and notification coordination. |
| **resources** | Stores Spring Boot configuration, logging configuration, and environment-specific application settings. |
| **test** | Contains unit and integration tests for controllers, services, repositories, and security components. |

---

# 🏗 Layered Architecture

The Booking Service follows a layered architecture where every layer owns a clearly defined responsibility while collaborating with multiple business services through OpenFeign.

```text
                     Client Request
                           │
                           ▼
                 Booking Controller
                           │
                           ▼
                  Booking Service
                           │
      ┌─────────────┬──────────────┬──────────────┬──────────────┐
      ▼             ▼              ▼              ▼
 Repository   Property Client  Payment Client Notification Client
      │
      ▼
 MySQL Database
      │
      ├────────► Property Service
      ├────────► Payment Service
      ├────────► User Service
      └────────► Notification Service
```

Each layer focuses on a single responsibility:

- **Controller Layer** handles incoming HTTP requests and response generation.
- **Service Layer** contains the complete reservation business logic.
- **Repository Layer** manages data persistence.
- **Feign Clients** communicate with external microservices.
- **Security Layer** protects APIs and validates authenticated requests.

This separation improves maintainability, scalability, testability, and loose coupling across the Booking Service.

---

# 📚 Package Overview

The Booking Service follows a modular package structure where each package owns a specific responsibility within the reservation domain.

---

## 📁 config

Responsible for configuring the application's infrastructure and service integrations.

Includes:

- Asynchronous Processing
- OpenFeign Configuration
- Property Service Client
- Payment Service Client
- User Service Client
- Notification Service Client

---

## 📁 controller

Acts as the entry point for all booking-related REST APIs.

Responsibilities include:

- Booking Creation
- Booking Retrieval
- Booking Confirmation
- Booking Failure Handling
- Booking Cancellation
- Booking Rescheduling
- Check-In
- Check-Out
- Booking Completion
- User Dashboard APIs
- Owner Dashboard APIs
- Booking History APIs
- Revenue Summary APIs
- Occupied Room Count APIs

---

## 📁 dto

Contains request and response models exchanged between clients and services.

Examples include:

- Booking Requests
- Booking Responses
- Dashboard Responses
- Revenue Summary Responses
- Occupied Room Responses
- Owner Booking Responses
- User Booking Responses

DTOs provide a clean separation between internal entities and externally exposed API contracts.

---

## 📁 entity

Represents the booking domain.

Current persistent entity:

- Booking

Supporting Enumerations:

- BookingStatus
- PropertyStatus
- WashroomType

These classes model reservation state while ensuring strong typing and well-defined lifecycle transitions.

---

## 📁 repository

Provides database access using Spring Data JPA.

Repositories include:

- BookingRepository

The repository layer abstracts persistence logic and enables clean separation from business rules.

---

## 📁 security

Responsible for protecting booking APIs.

Responsibilities include:

- Header Authentication
- Spring Security Configuration
- Request Filtering
- Role-Based Authorization
- Internal Service Authentication

This layer ensures that only authenticated users and trusted internal services can access protected booking operations.

---

## 📁 service

Contains the core business logic of the Booking Service.

Major responsibilities include:

- Booking Creation
- Booking Retrieval
- Booking Confirmation
- Booking Failure Handling
- Booking Cancellation
- Booking Rescheduling
- Booking Completion
- Check-In Management
- Check-Out Management
- Booking History
- Upcoming Bookings
- Completed Bookings
- Owner Booking Dashboard
- User Booking Dashboard
- Revenue Summary
- Occupied Room Statistics
- Availability Validation
- Payment Coordination
- Notification Coordination

The service layer orchestrates the complete reservation lifecycle while collaborating with Property, Payment, User, and Notification Services.

---

## 📁 exception

Provides centralized exception handling across the application.

Business exceptions include:

- BusinessException
- PaymentFailedException
- ResourceNotFoundException

The GlobalExceptionHandler converts application exceptions into standardized API responses, ensuring consistent error handling throughout the Booking Service.

---

# 🔄 Booking Request Lifecycle

Every booking request follows a structured validation and processing pipeline before being persisted and coordinated with other microservices.

```text
Customer Request
        │
        ▼
Booking Controller
        │
        ▼
Input Validation
        │
        ▼
Booking Service
        │
        ▼
Property & Room Validation
        │
        ▼
Availability Validation
        │
        ▼
Booking Amount Calculation
        │
        ▼
Create Booking
        │
        ▼
Initiate Payment
        │
        ▼
Persist Booking
        │
        ▼
Send Notification
        │
        ▼
Return Booking Response
```

Each stage is responsible for validating a specific business rule before the booking progresses to the next phase, ensuring reservation consistency and preventing invalid bookings.

---

# 📅 Booking Lifecycle

The Booking Service manages reservations through a controlled lifecycle to ensure that every booking follows valid business transitions.

```text
Booking Created
       │
       ▼
PENDING
       │
       ▼
CONFIRMED
       │
       ▼
CHECKED_IN
       │
       ▼
CHECKED_OUT
       │
       ▼
COMPLETED
```

Alternative lifecycle transitions:

### ❌ Payment Failure

```text
PENDING
    │
    ▼
FAILED
```

### ❌ Booking Cancellation

```text
CONFIRMED
      │
      ▼
CANCELLATION_IN_PROGRESS
      │
      ▼
CANCELLED
```

### 🔄 Booking Rescheduling

```text
CONFIRMED
      │
      ▼
RESCHEDULED
      │
      ▼
CONFIRMED
```

The Booking Service enforces these state transitions to maintain data integrity and prevent invalid operations such as checking in before confirmation or checking out before check-in.

---

# 🏠 Booking Creation Workflow

Creating a booking involves validating business rules across multiple services before the reservation is successfully created.

```text
Customer
      │
      ▼
Create Booking Request
      │
      ▼
Validate Request
      │
      ▼
Fetch Property Details
      │
      ▼
Validate Property Status
      │
      ▼
Validate Room Availability
      │
      ▼
Calculate Booking Amount
      │
      ▼
Create Booking (PENDING)
      │
      ▼
Initiate Payment
      │
      ▼
Return Booking Details
```

During booking creation, the Booking Service collaborates with the Property Service to verify property details, room information, pricing, and availability before persisting the reservation.

---

# 💳 Payment Processing Workflow

The Booking Service delegates all payment responsibilities to the Payment Service while maintaining ownership of the booking lifecycle.

```text
Booking Created
        │
        ▼
Create Payment Order
        │
        ▼
Payment Service
        │
        ▼
Payment Processing
        │
   ┌───────────────┐
   ▼               ▼
SUCCESS         FAILURE
   │               │
   ▼               ▼
Confirm Booking   Mark Booking Failed
   │               │
   ▼               ▼
Notify User     Notify User
```

The Booking Service never manages payment transactions directly. Instead, it coordinates payment processing through the Payment Service and updates the booking status based on the payment outcome.

---

# 🔗 Booking Service Orchestration

The Booking Service acts as the reservation orchestrator within the StayEase platform.

```text
                   Booking Service
                          │
      ┌───────────┬────────────┬─────────────┬─────────────┐
      ▼           ▼            ▼             ▼
 Property      Payment       User      Notification
  Service       Service      Service      Service
```

The service coordinates multiple business domains while maintaining ownership only of reservation data.

Responsibilities include:

- Validating property and room information
- Checking room availability
- Coordinating payment initiation
- Managing booking lifecycle transitions
- Sending booking notifications
- Providing booking dashboards for users and owners

This orchestration approach keeps domain ownership isolated while allowing services to collaborate efficiently through OpenFeign.

---

# 🚪 Check-In Workflow

The Booking Service manages customer check-in by validating the booking state before allowing guests to occupy the reserved accommodation.

```text
Confirmed Booking
        │
        ▼
Check-In Request
        │
        ▼
Validate Booking
        │
        ▼
Verify Booking Status
        │
        ▼
Update Booking Status
        │
        ▼
CHECKED_IN
        │
        ▼
Notify Customer
```

During the check-in process, the Booking Service ensures:

- The booking exists.
- The booking has been confirmed.
- The booking has not been cancelled.
- The booking has not already been checked in.
- The booking status is updated to **CHECKED_IN** after successful validation.

---

# 🚪 Check-Out Workflow

The Booking Service manages customer check-out after verifying that the booking has already been checked in.

```text
Checked-In Booking
        │
        ▼
Check-Out Request
        │
        ▼
Validate Booking
        │
        ▼
Verify Booking Status
        │
        ▼
Update Booking Status
        │
        ▼
CHECKED_OUT
        │
        ▼
Complete Booking
        │
        ▼
COMPLETED
```

During the check-out process, the Booking Service ensures:

- The booking exists.
- The customer has already checked in.
- Invalid state transitions are prevented.
- The booking progresses through **CHECKED_OUT** before being marked **COMPLETED**.

---

# ❌ Booking Cancellation Workflow

Booking cancellation follows a controlled workflow to maintain booking consistency and coordinate refund processing.

```text
Confirmed Booking
        │
        ▼
Cancellation Request
        │
        ▼
Business Validation
        │
        ▼
Update Status
        │
        ▼
CANCELLATION_IN_PROGRESS
        │
        ▼
Initiate Refund
        │
        ▼
Update Booking
        │
        ▼
CANCELLED
        │
        ▼
Notify Customer
```

The cancellation workflow performs several business validations before cancelling a reservation.

These validations include:

- Booking exists.
- Booking is eligible for cancellation.
- Booking has not already been cancelled.
- Booking has not been completed.
- Refund request is coordinated with the Payment Service.
- Customer notification is triggered after successful cancellation.

---

# 🔄 Booking Rescheduling Workflow

The Booking Service allows customers to reschedule bookings by validating the newly requested stay period before updating the reservation.

```text
Existing Booking
        │
        ▼
Reschedule Request
        │
        ▼
Validate New Dates
        │
        ▼
Validate Property
        │
        ▼
Validate Room
        │
        ▼
Check Availability
        │
        ▼
Update Booking
        │
        ▼
RESCHEDULED
        │
        ▼
Notify Customer
```

The rescheduling workflow ensures:

- The booking exists.
- The requested dates are valid.
- The selected room is available.
- Property information remains valid.
- Booking information is updated successfully.
- The customer is notified after rescheduling.

---

# 📊 Booking Dashboard Workflow

The Booking Service provides dedicated dashboards for both customers and property owners.

```text
Dashboard Request
        │
        ▼
Booking Service
        │
        ▼
Fetch Booking Records
        │
        ▼
Aggregate Booking Data
        │
        ▼
Generate Dashboard
        │
        ▼
Return Dashboard Response
```

Dashboard capabilities include:

## 👤 User Dashboard

- Booking History
- Upcoming Bookings
- Completed Bookings
- Cancelled Bookings
- Booking Summary

## 🏠 Owner Dashboard

- Property Bookings
- Booking History
- Revenue Summary
- Occupied Room Statistics
- Active Reservations

These dashboards provide a consolidated view of reservation activities without exposing internal booking implementation details.

---

# 🌐 Inter-Service Communication Workflow

The Booking Service collaborates with multiple business services to complete reservation processing.

```text
                 Booking Service
                        │
        ┌───────────────┼───────────────┐
        ▼               ▼               ▼
 Property Service   Payment Service   User Service
        │
        ▼
Notification Service
```

### Property Service

Used for:

- Property Validation
- Room Validation
- Availability Verification

### Payment Service

Used for:

- Payment Order Creation
- Refund Processing
- Payment Status Updates

### User Service

Used for:

- User Information
- Customer Details
- Dashboard Data

### Notification Service

Used for:

- Booking Confirmation Notifications
- Cancellation Notifications
- Rescheduling Notifications
- Booking Status Updates

By collaborating through OpenFeign clients, each microservice maintains ownership of its own business domain while participating in end-to-end reservation workflows.

---

# 🎯 Why a Dedicated Booking Service?

Reservation management is one of the most business-critical domains within the StayEase platform.

Separating booking management into its own microservice provides several enterprise advantages.

- Centralized Reservation Ownership
- Independent Booking Database
- Complete Booking Lifecycle Management
- Dynamic Availability Validation
- Payment Orchestration
- Controlled Booking State Transitions
- User and Owner Dashboards
- Reduced Service Coupling
- Independent Scalability
- Clear Domain Boundaries
- Enterprise-Grade Maintainability

The Booking Service acts as the orchestration layer for reservations by coordinating property validation, payment processing, customer notifications, and booking lifecycle transitions while maintaining ownership only of reservation data.

This clear separation of responsibilities enables the StayEase platform to evolve each business domain independently, resulting in a scalable, maintainable, and production-ready microservices architecture.

---

# 🏛 Booking Management Strategy

The Booking Service is the central reservation domain within the StayEase platform. It owns the complete booking lifecycle while collaborating with other microservices to validate business rules and coordinate reservation processing.

Instead of embedding booking logic into the Property or Payment Services, all reservation-related operations are centralized within a dedicated Booking Service.

This strategy provides several enterprise advantages:

- Single Source of Truth for reservations
- Independent booking lifecycle management
- Centralized booking validations
- Independent database ownership
- Reduced service coupling
- Easier business rule evolution
- Improved scalability

By isolating reservation management into its own microservice, the StayEase platform maintains clear domain boundaries while allowing each business service to evolve independently.

---

# 🛏 Dynamic Availability Strategy

Rather than storing mutable room availability inside the Property Service, StayEase calculates room availability dynamically using booking records.

```text
Property Service
        │
        ▼
Static Room Information
(Capacity, Price, Amenities)
        │
        ▼
Booking Service
        │
        ▼
Existing Bookings
        │
        ▼
Calculate Occupied Beds
        │
        ▼
Available Capacity
```

This strategy ensures that availability always reflects the current reservation state.

### Benefits

- No duplicated availability data
- Eliminates synchronization problems
- Prevents inconsistent room counts
- Availability is always calculated from the latest bookings
- Supports concurrent reservation processing

This follows the principle that **Property Service owns static room information**, while **Booking Service owns reservation state and availability calculations**.

---

# 💳 Payment Orchestration Strategy

The Booking Service coordinates payment processing but never owns payment transactions.

```text
Booking Service
       │
       ▼
Create Payment Order
       │
       ▼
Payment Service
       │
       ▼
Payment Result
       │
       ▼
Update Booking Status
```

The Payment Service remains the single source of truth for financial transactions.

The Booking Service only:

- Creates payment requests
- Receives payment outcomes
- Updates booking status
- Coordinates refunds
- Triggers customer notifications

This separation follows Domain-Driven Design by keeping financial operations isolated from reservation management.

---

# 🔄 Booking Lifecycle Strategy

Every reservation follows a controlled lifecycle.

```text
PENDING
   │
   ▼
CONFIRMED
   │
   ▼
CHECKED_IN
   │
   ▼
CHECKED_OUT
   │
   ▼
COMPLETED
```

Alternative transitions include:

```text
PENDING
    │
    ▼
FAILED
```

```text
CONFIRMED
      │
      ▼
CANCELLED
```

```text
CONFIRMED
      │
      ▼
RESCHEDULED
```

Restricting state transitions ensures that invalid operations such as checking out before check-in or confirming cancelled bookings cannot occur.

---

# 🌐 Service Communication Strategy

The Booking Service collaborates with multiple business services through OpenFeign.

```text
                Booking Service
                       │
      ┌────────────────┼────────────────┐
      ▼                ▼                ▼
 Property         Payment         User
  Service          Service       Service
                       │
                       ▼
               Notification Service
```

### Property Service

Responsible for:

- Property validation
- Room validation
- Room information
- Pricing details

### Payment Service

Responsible for:

- Payment processing
- Refund processing
- Payment status

### User Service

Responsible for:

- Customer information
- User profile information

### Notification Service

Responsible for:

- Booking confirmation notifications
- Cancellation notifications
- Reschedule notifications
- Booking lifecycle notifications

Using OpenFeign enables each service to maintain its own business domain while collaborating through well-defined APIs.

---

# ⚡ Asynchronous Processing Strategy

The Booking Service uses asynchronous processing for operations that do not need to block the client's request.

Examples include:

- Sending booking notifications
- Processing non-critical background tasks

```text
Booking Completed
        │
        ▼
Return Response
        │
        ▼
Async Task
        │
        ▼
Notification Service
```

Benefits include:

- Faster API responses
- Improved throughput
- Better scalability
- Reduced request latency
- Loose coupling between business operations

Asynchronous processing improves the user experience while keeping the reservation workflow responsive.

---

# 🔐 Security Strategy

The Booking Service is protected using Spring Security together with a custom Header Authentication Filter.

Security responsibilities include:

- Request authentication
- Internal service authentication
- Role-based authorization
- Protected REST endpoints
- Secure inter-service communication

This ensures that only authenticated users and trusted internal services can perform booking operations.

---

# 🗄 Database Design Strategy

The Booking Service follows the **Database per Service** pattern.

```text
Booking Service
       │
       ▼
Booking Database
```

Only reservation-related information is stored within this database.

External information such as:

- Property details
- User information
- Payment transactions

is retrieved from their respective services whenever required.

This strategy prevents data duplication while preserving clear ownership of each business domain.

---

# ⚠ Exception Handling Strategy

The Booking Service implements centralized exception handling using `@RestControllerAdvice`.

Business exceptions include:

- ResourceNotFoundException
- BusinessException
- PaymentFailedException

Benefits include:

- Consistent API responses
- Cleaner controller implementation
- Centralized error handling
- Better maintainability
- Simplified debugging

All exceptions are transformed into standardized error responses before being returned to clients.

---

# 🌱 Spring Profiles Strategy

The Booking Service uses Spring Profiles to support multiple deployment environments while keeping environment-specific configurations isolated from the application code.

Supported environments include:

- Local Development
- Testing
- Production

Each profile can maintain independent configurations for:

- Database Connection
- Logging Levels
- External Service URLs
- Security Configuration
- Third-Party Integrations

Using Spring Profiles enables seamless deployment across different environments without requiring code modifications.

---

# ⚙ Externalized Configuration Strategy

The Booking Service follows the Twelve-Factor App principle by externalizing application configuration.

Configuration includes:

- Database Credentials
- Server Port
- Feign Client URLs
- Logging Configuration
- Security Properties
- Environment Variables

Benefits include:

- Easier deployment
- Environment-specific configuration
- Improved security
- Better maintainability
- Simplified DevOps workflows

This approach keeps sensitive configuration outside the application codebase while supporting multiple deployment environments.

---

# 📝 Logging Strategy

The Booking Service uses structured logging to improve observability and simplify troubleshooting.

Logging is performed across the application lifecycle, including:

- Incoming API Requests
- Booking Creation
- Payment Coordination
- Availability Validation
- Booking Confirmation
- Booking Cancellation
- Check-In / Check-Out
- External Service Communication
- Business Exceptions

Structured logging provides several advantages:

- Faster debugging
- Better production monitoring
- Easier issue diagnosis
- Improved operational visibility
- Simplified auditing of booking activities

Logs are generated at appropriate log levels (INFO, DEBUG, WARN, and ERROR) to provide meaningful operational insights while minimizing unnecessary noise.

---

# 📦 DTO Design Strategy

The Booking Service separates API models from persistence models using dedicated Data Transfer Objects (DTOs).

```text
Client Request
        │
        ▼
Request DTO
        │
        ▼
Business Logic
        │
        ▼
Entity
        │
        ▼
Response DTO
        │
        ▼
Client Response
```

This separation provides several enterprise benefits:

- Prevents direct entity exposure
- Improves API stability
- Supports independent API evolution
- Simplifies validation
- Enhances maintainability

DTOs also facilitate clean communication between the Booking Service and other microservices through OpenFeign.

---

# 🔄 Transaction Management Strategy

Booking operations often involve multiple business steps that must execute reliably.

The Booking Service uses Spring's transaction management to ensure that critical database operations are executed atomically.

Typical transactional operations include:

- Booking Creation
- Booking Confirmation
- Booking Cancellation
- Booking Rescheduling
- Check-In
- Check-Out
- Booking Completion

Using transactions ensures:

- Data consistency
- Atomic operations
- Automatic rollback on failures
- Prevention of partial updates
- Reliable booking state transitions

This guarantees that reservation data remains consistent even when unexpected failures occur during business processing.

---

# 🛡 Validation Strategy

The Booking Service validates requests at multiple layers before processing business operations.

Validation includes:

### Request Validation

- Mandatory Fields
- Date Validation
- Booking Rules
- Input Constraints

### Business Validation

- Booking Existence
- Booking Status
- Property Validation
- Room Validation
- Availability Validation
- Payment Validation

### Service Validation

- Property Service Verification
- User Service Verification
- Payment Service Coordination

Multi-layer validation prevents invalid requests from entering the booking lifecycle while ensuring that all reservation operations satisfy business requirements.

---

# 🚀 Production Readiness

The Booking Service incorporates several enterprise practices that prepare it for production deployment.

Current capabilities include:

- Layered Architecture
- Database per Service
- Spring Security
- OpenFeign Communication
- Global Exception Handling
- Bean Validation
- Transaction Management
- Structured Logging
- Asynchronous Processing
- Environment-Based Configuration
- Centralized Booking Lifecycle
- Payment Orchestration
- Dynamic Availability Validation

These capabilities provide a solid foundation for scalable and maintainable production deployments.

---

# 🔮 Future Enhancements

The Booking Service has been designed with extensibility in mind.

Potential future enhancements include:

- Apache Kafka Event Publishing
- Event-Driven Booking Processing
- Distributed Caching with Redis
- Distributed Locking for Concurrent Reservations
- Scheduled Booking Expiration
- Automated Refund Processing
- WebSocket Notifications
- Observability with Micrometer and Prometheus
- Distributed Tracing with Zipkin
- Docker Containerization
- Kubernetes Deployment
- CI/CD Automation

The current architecture enables these capabilities to be introduced with minimal impact on existing business logic.

---

# 📐 Enterprise Design Principles

The Booking Service follows several enterprise software engineering principles.

### Separation of Concerns

Each layer focuses on a single responsibility.

### Single Responsibility Principle

Controllers, services, repositories, and Feign clients each own a distinct responsibility.

### Loose Coupling

Inter-service communication occurs through OpenFeign interfaces rather than direct dependencies.

### High Cohesion

Booking-related business logic is centralized within the Booking Service.

### Database per Service

The Booking Service exclusively owns reservation data while retrieving external information from other domain services.

### Domain-Driven Design

Each microservice owns its respective business domain:

- Booking Service → Reservations
- Property Service → Properties & Rooms
- Payment Service → Payments
- User Service → User Profiles
- Notification Service → Notifications

This clear separation promotes maintainability, scalability, and independent service evolution.

---

# 🏆 Enterprise Design Summary

The StayEase Booking Service demonstrates modern enterprise backend development practices by combining robust booking lifecycle management with scalable microservices architecture.

Key architectural highlights include:

- Centralized Reservation Management
- Independent Booking Database
- Layered Architecture
- Dynamic Availability Validation
- Payment Orchestration
- OpenFeign-Based Service Communication
- Spring Security
- Bean Validation
- Global Exception Handling
- Structured Logging
- Asynchronous Processing
- Transaction Management
- Environment-Based Configuration
- Domain-Driven Service Separation

By isolating reservation management into a dedicated microservice, the StayEase platform achieves a scalable, maintainable, and production-ready architecture capable of supporting future business growth while maintaining clean separation of responsibilities across services.

---

# 🚀 Getting Started

Follow the steps below to set up and run the Booking Service locally.

---

# 📋 Prerequisites

Ensure the following software is installed before running the application.

| Software | Version |
|----------|---------|
| Java | 21+ |
| Gradle | 8.x+ |
| MySQL | 8.x |
| Git | Latest |
| IntelliJ IDEA / Eclipse | Recommended |

---

# 📥 Clone the Repository

```bash
git clone https://github.com/PSaiRam32/stayease-booking-service.git

cd stayease-booking-service
```

---

# ⚙ Configure the Application

Update the `application.yaml` file with your local database and service configurations.

Example:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/stayease_booking
    username: root
    password: your_password

server:
  port: 8085
```

Also configure the URLs for dependent services:

- Property Service
- Payment Service
- User Service
- Notification Service

---

# 🗄 Database Setup

Create a MySQL database.

```sql
CREATE DATABASE stayease_booking;
```

The Booking Service will automatically create the required tables when the application starts (depending on the configured JPA settings).

---

# ▶ Running the Application

Using Gradle:

```bash
./gradlew bootRun
```

Or build the project:

```bash
./gradlew clean build
```

---

# 🌐 REST API Overview

The Booking Service exposes APIs for managing the complete reservation lifecycle.

Major API categories include:

## 📅 Booking Management

- Create Booking
- Get Booking
- Booking History
- Booking Summary

---

## 💳 Payment Coordination

- Confirm Booking
- Handle Failed Booking
- Refund Coordination

---

## 🚪 Stay Management

- Check-In
- Check-Out
- Complete Booking

---

## ❌ Reservation Management

- Cancel Booking
- Reschedule Booking

---

## 👤 User APIs

- User Dashboard
- Upcoming Bookings
- Completed Bookings

---

## 🏠 Owner APIs

- Property Bookings
- Booking History
- Revenue Summary
- Occupied Room Statistics

---

# 🧪 Testing

The Booking Service supports multiple testing strategies.

### Unit Testing

Tests individual business components in isolation.

Examples:

- Booking Service
- Repository Layer
- Validation Logic

---

### Integration Testing

Tests interactions between:

- Controller
- Service
- Repository
- Database

---

### API Testing

REST APIs can be tested using:

- Postman
- Swagger UI
- IntelliJ HTTP Client

---

# 📊 Monitoring

The Booking Service uses structured logging to simplify monitoring and debugging.

Operational visibility includes:

- Booking Requests
- Booking Status Updates
- Payment Coordination
- Availability Validation
- External Service Calls
- Business Exceptions

These logs help developers monitor reservation processing and diagnose production issues efficiently.

---

# ⚡ Performance Considerations

The Booking Service has been designed with scalability and responsiveness in mind.

Performance strategies include:

- Layered Architecture
- Database per Service
- OpenFeign Communication
- Asynchronous Notification Processing
- Transaction Management
- Dynamic Availability Calculation
- Reduced Data Duplication
- Optimized Business Validation

These practices enable the service to efficiently manage reservation workloads while maintaining data consistency.

---
# 🔐 Security Best Practices

The Booking Service follows several enterprise security practices to protect reservation data and secure inter-service communication.

Current security measures include:

- Spring Security
- Header-Based Authentication
- Role-Based Authorization
- Protected REST Endpoints
- Secure Service-to-Service Communication
- Input Validation
- Global Exception Handling

Recommended production enhancements include:

- HTTPS/TLS Encryption
- API Gateway Authentication
- Rate Limiting
- OAuth2 / JWT Authentication
- Secrets Management
- Security Auditing

These practices help ensure that booking operations remain secure, reliable, and protected from unauthorized access.

---

# 🤝 Contributing

Contributions are always welcome!

If you'd like to improve the Booking Service:

1. Fork the repository.
2. Create a feature branch.

```bash
git checkout -b feature/your-feature
```

3. Commit your changes.

```bash
git commit -m "Add new booking feature"
```

4. Push your branch.

```bash
git push origin feature/your-feature
```

5. Open a Pull Request.

Please ensure that all new features follow the existing project structure, coding standards, and architectural principles.

---

# 📄 License

This project is licensed under the **MIT License**.

See the **LICENSE** file for complete details.

---

# 👨‍💻 Author

**Sai Ram Paidipati**

Java Backend Developer

- GitHub: https://github.com/PSaiRam32
- LinkedIn: https://www.linkedin.com/in/sairam-paidipati/

---

# 💬 Support

If you find this project useful, consider:

- ⭐ Starring the repository
- 🍴 Forking the project
- 🐛 Reporting issues
- 💡 Suggesting improvements
- 🤝 Contributing new features

Your feedback helps improve the project and supports continuous learning.

---

# 🎓 Learning Outcomes

This project demonstrates practical implementation of modern backend engineering concepts, including:

- Enterprise Microservices Architecture
- Booking Lifecycle Management
- Dynamic Availability Validation
- Payment Orchestration
- OpenFeign Communication
- Spring Security
- Spring Data JPA
- Bean Validation
- Layered Architecture
- Repository Pattern
- DTO Pattern
- Global Exception Handling
- Structured Logging
- Transaction Management
- Asynchronous Processing
- Database per Service
- Domain-Driven Design
- RESTful API Design
- Production-Oriented Service Design

This service showcases how reservation management can be implemented as an independent, scalable, and maintainable microservice within a distributed system.

---

# 📚 References

The following technologies and resources were used while building this project:

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- Hibernate
- OpenFeign
- MySQL
- Gradle
- Bean Validation (Jakarta Validation)
- SLF4J Logging
- REST API Design Principles

---

# 🏆 Project Summary

The **StayEase Booking Service** is the central reservation management component of the StayEase microservices platform.

It is responsible for managing the complete booking lifecycle, including booking creation, confirmation, cancellation, rescheduling, check-in, check-out, booking completion, dashboard generation, payment coordination, and dynamic availability validation.

By collaborating with the Property, Payment, User, and Notification Services through OpenFeign, the Booking Service orchestrates reservation workflows while maintaining clear ownership of booking data.

The architecture emphasizes:

- Clean Separation of Concerns
- Independent Database Ownership
- Domain-Driven Design
- Layered Architecture
- Service-to-Service Communication
- Centralized Booking Lifecycle
- Dynamic Availability Calculation
- Enterprise Security
- Production Readiness

These architectural decisions result in a scalable, maintainable, and production-oriented reservation management service capable of supporting future business growth.

---

# 🙏 Acknowledgements

This project was built as part of the **StayEase Backend Microservices** ecosystem to explore enterprise-grade reservation management using modern Java and Spring technologies.

During the development of the Booking Service, the following concepts were implemented and practiced:

- Booking Lifecycle Management
- Dynamic Room Availability Validation
- Payment Orchestration
- Booking Cancellation & Rescheduling
- Check-In & Check-Out Management
- User & Owner Booking Dashboards
- OpenFeign-Based Service Communication
- Layered Architecture
- Spring Data JPA
- Bean Validation
- Global Exception Handling
- Structured Logging
- Transaction Management
- Asynchronous Processing
- Database per Service
- Domain-Driven Design
- Production-Oriented Microservice Design

This project served as a practical implementation of enterprise reservation management concepts while reinforcing clean architecture, scalable system design, and distributed microservices development.

Thank you for exploring this repository. I hope it provides valuable insights into building secure, maintainable, and enterprise-grade backend systems with Spring Boot and Java.

---
