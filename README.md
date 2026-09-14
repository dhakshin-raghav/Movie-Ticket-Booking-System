# 🎬 Movie Ticket Booking System - Low Level Design (LLD)

A clean, modular, and beginner-friendly **Interactive Console-Based Movie Ticket Booking System** implemented in pure Java. Inspired by real-world platforms like **BookMyShow**, this project demonstrates core Object-Oriented Programming (OOP) concepts, thread-safe in-memory seat locking to eliminate race conditions, dynamic Strategy design patterns, and an intuitive CLI interface.

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

### 1. 🛡️ Thread-Safe In-Memory Seat Locking (No Race Conditions)
- **Problem**: In high-demand scenarios, multiple users try to book the exact same seat at the exact same millisecond.
- **Solution**:
  - `SeatLockService` uses fine-grained `ReentrantLock` per `Show` to ensure atomic check-and-lock operations.
  - Seats are temporarily held in an in-memory lock table with a configurable **Time-to-Live (TTL)** (e.g. 30 seconds for checkout).
  - If a user completes payment within the TTL window, the seat transitions from `LOCKED` to `BOOKED`.
  - If the user abandons checkout or the lock expires, the seat automatically becomes available again for other customers.

### 2. 🧩 Strategy Design Pattern
- **Dynamic Pricing Strategy (`PricingStrategy`)**:
  - `StandardPricingStrategy`: Tier-based pricing (`SILVER`: $150, `GOLD`: $250, `PLATINUM`: $400).
  - `WeekendPricingStrategy`: Applies a 20% surge premium on Saturday and Sunday shows.
- **Pluggable Payment Methods (`PaymentStrategy`)**:
  - `UpiPaymentStrategy`: Handles UPI virtual payment addresses.
  - `CreditCardPaymentStrategy`: Handles credit/debit card validation and charging.

### 3. 🗺️ Visual ASCII Seating Grid & City Selection
- Real-time visual seat map rendering:
```
═════════════════════════════════════════════════════════════════════════════
                            🎬  SCREEN THIS WAY  🎬                          
═════════════════════════════════════════════════════════════════════════════

 Row A (SILVER   - $150):  [🟢 A1] [🟢 A2] [❌ A3] [🟢 A4] [🟢 A5] 
 Row B (GOLD     - $250):  [🟢 B1] [⏳ B2] [🟢 B3] [🟢 B4] [🟢 B5] 
 Row C (PLATINUM - $400):  [🟢 C1] [🟢 C2] [🟢 C3] [🟢 C4] [🟢 C5] 

─────────────────────────────────────────────────────────────────────────────
 Legend:  [🟢 Available]   [⏳ Locked / Checking Out]   [❌ Booked]
─────────────────────────────────────────────────────────────────────────────
```
- Multi-city support (`Bangalore`, `Mumbai`, `Delhi`) with distinct theatres, screens, and shows.

---

## 📁 Project Structure

```
movie-ticket-booking-system/
├── src/
│   └── com/
│       └── moviebooking/
│           ├── Main.java                          // Application launcher
│           ├── InteractiveConsoleApp.java         // Interactive CLI dashboard
│           ├── data/
│           │   └── DataInitializer.java          // Seeds cities, cinemas, movies, and shows
│           ├── model/                             // Domain entities
│           │   ├── Booking.java
│           │   ├── BookingStatus.java             // PENDING, CONFIRMED, CANCELLED, EXPIRED
│           │   ├── CinemaHall.java                // Theatre / Multiplex
│           │   ├── City.java                      // BANGALORE, MUMBAI, DELHI
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
│           ├── controller/                        // API / Controller Layer
│           │   ├── BookingController.java
│           │   ├── MovieController.java
│           │   └── ShowController.java
│           └── util/
│               └── SeatViewHelper.java            // Visual ASCII seat matrix renderer
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

### Launching the Interactive Application
```bash
java -cp bin com.moviebooking.Main
```

---

## 🎮 Interactive Features in the Dashboard

```
┌─────────────────────────────────────────────────────────────┐
│                     MAIN DASHBOARD                          │
├─────────────────────────────────────────────────────────────┤
│  1. 🎟️  Book Movie Tickets (Interactive Booking Flow)       │
│  2. 💺  View Real-Time Seat Matrix for a Show               │
│  3. ⚡  Run Multithreaded Concurrency Test (Race Protection) │
│  4. ⏳  Run Seat Lock Timeout (TTL) Expiration Demo         │
│  5. 🚪  Exit Application                                    │
└─────────────────────────────────────────────────────────────┘
```

1. **🎟️ Interactive Booking Flow**:
   - Step 1: Choose City (`Bangalore`, `Mumbai`, `Delhi`)
   - Step 2: Choose Movie (`Interstellar`, `Inception`, `The Dark Knight`)
   - Step 3: Choose Cinema & Show Time
   - Step 4: Real-time visual seat map display
   - Step 5: Enter seat IDs (e.g. `A1, A2`)
   - Step 6: Acquire thread-safe in-memory lock
   - Step 7: Select payment method (`UPI` / `Card`)
   - Step 8: Receive formatted Ticket Confirmation receipt!
2. **💺 View Real-Time Seat Matrix**:
   - Inspects the live status of any screen with color-coded status badges.
3. **⚡ Multithreaded Concurrency Stress Test**:
   - Runs 4 concurrent threads simultaneously competing for the exact same seat using `CountDownLatch`. Demonstrates zero race conditions.
4. **⏳ Seat Lock TTL Expiration Demo**:
   - Demonstrates locking a seat, letting the hold timeout, and another user successfully claiming it.
