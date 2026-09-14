# 🎬 Movie Ticket Booking System - Low Level Design (LLD)

A clean **Movie Ticket Booking System** in pure Java designed to showcase **Object-Oriented Programming (OOP)**, **Concurrency / In-Memory Seat Locking**, and the **Strategy Pattern**.

---

## 🎯 Highlights
- **100% Pure Java**: No complex frameworks or external dependencies.
- **Multithreading & Concurrency**: In-memory seat locking prevents race conditions and double-booking.
- **Strategy Design Pattern**: Pluggable payment strategies (`UpiPayment`, `CardPayment`).
- **Clean Architecture**: Controller &rarr; Service &rarr; Model layers.
- **Interactive Console Demo**: Simple menu to book tickets and run live concurrency tests.

---

## 🏛️ Project Structure

```
movie-ticket-booking-system/
└── src/
    └── com/
        └── moviebooking/
            ├── Main.java                      // Interactive console menu
            ├── model/                         // Domain entities
            │   ├── Movie.java                 // Movie details
            │   ├── Seat.java                  // Seat number, type, price
            │   ├── Show.java                  // Movie show & booked seats
            │   ├── Booking.java               // Ticket booking receipt
            │   └── User.java                  // Customer
            ├── strategy/                      // Strategy Pattern
            │   ├── PaymentStrategy.java       // Payment interface
            │   ├── UpiPayment.java            // UPI strategy (GPay/PhonePe)
            │   └── CardPayment.java           // Credit/Debit Card strategy
            ├── service/                       // Business logic
            │   ├── SeatLockService.java       // Thread-safe in-memory seat locking
            │   └── BookingService.java        // Booking & payment coordination
            └── controller/                    // API / Controller layer
                └── BookingController.java     // Controller exposing booking operations
```

---

## 🔑 How In-Memory Seat Locking Prevents Race Conditions

When multiple users try to book the exact same seat simultaneously:

```java
public boolean lockSeat(Show show, int seatNumber, String userName) {
    mutex.lock(); // Atomically check and lock
    try {
        if (show.isSeatBooked(seatNumber)) return false;
        if (lockedSeats.containsKey(show.getId() + ":" + seatNumber)) return false;
        
        lockedSeats.put(show.getId() + ":" + seatNumber, userName);
        return true;
    } finally {
        mutex.unlock();
    }
}
```

1. **Atomic Check-and-Lock**: Only one thread can enter the critical section at a time.
2. **First-Come, First-Served**: The winning thread locks the seat, pays, and permanently confirms the booking.
3. **Safe Rejection**: Competing threads are cleanly informed that the seat is already locked or booked without any double-booking.

---

## 🚀 How to Run

### Compilation
```bash
mkdir -p bin
javac -d bin $(find src -name "*.java")
```

### Run Interactive Console
```bash
java -cp bin com.moviebooking.Main
```

---

## 🎮 Features in Interactive Menu

```
=================================================
     🎬 MOVIE TICKET BOOKING SYSTEM (LLD) 🎟️     
=================================================

Select an option:
  1. 🎟️  Book a Ticket (Interactive Flow)
  2. ⚡  Run Concurrency Demo (Race Condition Test)
  3. 🚪  Exit
```

1. **🎟️ Book a Ticket**:
   - Choose Movie/Show (*Interstellar* or *Inception*).
   - View visual seat layout with tiers and prices.
   - Enter seat number and customer name.
   - Select payment method (`1. UPI` or `2. Card`).
   - Generates a formatted ticket receipt.

2. **⚡ Concurrency Demo**:
   - Spawns 3 concurrent threads (*Alice*, *Bob*, *Charlie*) simultaneously competing for the exact same seat.
   - Demonstrates that only 1 user succeeds and the remaining 2 are rejected with zero race conditions.
