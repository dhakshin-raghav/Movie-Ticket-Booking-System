# Movie Ticket Booking System - Low Level Design (LLD)

A beginner-friendly, clean, and modular **Console-Based Movie Ticket Booking System** implemented in pure Java. This project demonstrates core Object-Oriented Programming (OOP) concepts, thread-safe in-memory seat locking to avoid race conditions, the Strategy design pattern, and a layered architecture.

---

## 🏛️ Architectural Overview

The application follows the **Controller - Service - Repository** design pattern, ensuring high cohesion and loose coupling.

```
┌─────────────────────────────────────────────────────────────┐
│                      Controller Layer                       │
│      MovieController   ShowController   BookingController   │
└──────────────────────────────┬──────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────┐
│                        Service Layer                        │
│   MovieService   ShowService   BookingService  PaymentService│
│                 └───► SeatLockService (Concurrency)         │
└──────────────────────────────┬──────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────┐
│                      Repository Layer                       │
│    MovieRepository     ShowRepository    BookingRepository   │
│             (Thread-Safe ConcurrentHashMap)                 │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔑 Core Features & Patterns

### 1. Multithreading & In-Memory Seat Locking (No Race Conditions)
- **Problem**: In high-demand scenarios (e.g. blockbuster movie releases), multiple users try to book the exact same seat at the exact same millisecond.
- **Solution**:
  - `SeatLockService` uses fine-grained `ReentrantLock` per `Show` to ensure atomic check-and-lock operations.
  - Seats are temporarily held in an in-memory lock table with a configurable **Time-to-Live (TTL)**.
  - If a user completes payment within the TTL window, the seat transitions from `LOCKED` to `BOOKED`.
  - If the user abandons checkout or the lock expires, the seat automatically becomes available again for other customers.

### 2. Strategy Design Pattern
- **Dynamic Pricing Strategy (`PricingStrategy`)**:
  - `StandardPricingStrategy`: Flat rates based on seat tier (`SILVER`: $150, `GOLD`: $250, `PLATINUM`: $400).
  - `WeekendPricingStrategy`: Applies a 20% surge premium on Saturday and Sunday shows.
- **Pluggable Payment Methods (`PaymentStrategy`)**:
  - `UpiPaymentStrategy`: Handles UPI virtual payment addresses.
  - `CreditCardPaymentStrategy`: Handles credit/debit card validation and charging.

### 3. Object-Oriented Principles (OOP)
- **Encapsulation**: Models like `Seat`, `Show`, `Booking`, and `SeatLock` protect their state with private fields and controlled mutations.
- **Polymorphism**: Pluggable strategies for both payment and pricing can be swapped at runtime without changing the core `BookingService`.
- **Single Responsibility**: Each class has one focused job (e.g. `SeatLockService` only handles locking and expiration, while `BookingService` handles the booking lifecycle).

---

## 📁 Project Structure

```
movie-ticket-booking-system/
├── src/
│   └── com/
│       └── moviebooking/
│           ├── Main.java                          // Application entry point with 4 detailed demos
│           ├── model/                             // Domain entities
│           │   ├── Booking.java
│           │   ├── BookingStatus.java             // PENDING, CONFIRMED, CANCELLED, EXPIRED
│           │   ├── CinemaHall.java                // Theatre / Multiplex
│           │   ├── Movie.java
│           │   ├── Payment.java
│           │   ├── PaymentStatus.java             // SUCCESS, FAILED, PENDING, REFUNDED
│           │   ├── Screen.java
│           │   ├── Seat.java
│           │   ├── SeatLock.java                  // Temporary lock with TTL
│           │   ├── SeatStatus.java                // AVAILABLE, LOCKED, BOOKED
│           │   ├── SeatType.java                  // SILVER, GOLD, PLATINUM
│           │   ├── Show.java
│           │   └── User.java
│           ├── repository/                        // Thread-safe in-memory stores
│           │   ├── BookingRepository.java
│           │   ├── MovieRepository.java
│           │   └── ShowRepository.java
│           ├── strategy/
│           │   ├── payment/                       // Strategy Pattern: Payment
│           │   │   ├── PaymentStrategy.java
│           │   │   ├── UpiPaymentStrategy.java
│           │   │   └── CreditCardPaymentStrategy.java
│           │   └── pricing/                       // Strategy Pattern: Pricing
│           │       ├── PricingStrategy.java
│           │       ├── StandardPricingStrategy.java
│           │       └── WeekendPricingStrategy.java
│           ├── service/                           // Business Logic
│           │   ├── BookingService.java
│           │   ├── MovieService.java
│           │   ├── PaymentService.java
│           │   ├── SeatLockService.java           // Concurrency & TTL lock management
│           │   └── ShowService.java
│           └── controller/                        // API / Controller Layer
│               ├── BookingController.java
│               ├── MovieController.java
│               └── ShowController.java
└── README.md
```

---

## 🚀 How to Compile & Run

### Prerequisites
- Java Development Kit (JDK 11 or higher). Java 21 is pre-tested.

### Compilation
From the `movie-ticket-booking-system` root directory:
```bash
mkdir -p bin
javac -d bin $(find src -name "*.java")
```

### Running the Demos
```bash
java -cp bin com.moviebooking.Main
```

---

## 🧪 Scenarios Demonstrated in `Main.java`

1. **Standard Booking Flow**:
   - Customer browses available seats, selects seats, calculates price via `StandardPricingStrategy`, and completes payment via `UpiPaymentStrategy`.
2. **Multithreaded Race Condition Prevention**:
   - 4 concurrent threads (Bob, Charlie, David, Emma) all attempt to book the exact same VIP seat [C1] simultaneously using `CountDownLatch`.
   - Result: Exactly 1 thread succeeds; the remaining 3 are cleanly rejected without deadlocks or double-booking.
3. **Seat Lock Expiration & Recovery (TTL)**:
   - Frank locks seat [B1] and abandons checkout.
   - After the 2-second TTL expires, Grace retries and successfully books seat [B1].
   - When Frank returns late, his attempt to confirm is rejected.
4. **Dynamic Pricing Strategy**:
   - Comparison of standard weekday pricing vs. weekend surge pricing (+20%).
