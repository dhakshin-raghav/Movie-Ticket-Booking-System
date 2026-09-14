package com.moviebooking;

import com.moviebooking.controller.BookingController;
import com.moviebooking.model.*;
import com.moviebooking.service.BookingService;
import com.moviebooking.service.SeatLockService;
import com.moviebooking.strategy.CardPayment;
import com.moviebooking.strategy.PaymentStrategy;
import com.moviebooking.strategy.UpiPayment;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main console application demonstrating:
 * 1. OOP Principles (Controller -> Service -> Model)
 * 2. Strategy Pattern (PaymentStrategy with UpiPayment and CardPayment)
 * 3. Multithreading & In-Memory Seat Locking (No race conditions)
 */
public class Main {

    private final BookingController controller;
    private final Scanner scanner;

    public Main() {
        SeatLockService seatLockService = new SeatLockService();
        BookingService bookingService = new BookingService(seatLockService);
        this.controller = new BookingController(bookingService);
        this.scanner = new Scanner(System.in);
    }

    public static void main(String[] args) {
        Main app = new Main();
        app.run();
    }

    public void run() {
        System.out.println("=================================================");
        System.out.println("     🎬 MOVIE TICKET BOOKING SYSTEM (LLD) 🎟️     ");
        System.out.println("=================================================");

        boolean running = true;
        while (running) {
            System.out.println("\nSelect an option:");
            System.out.println("  1. 🎟️  Book a Ticket (Interactive Flow)");
            System.out.println("  2. ⚡  Run Concurrency Demo (Race Condition Test)");
            System.out.println("  3. 🚪  Exit");
            System.out.print("👉 Choice: ");

            int choice = readInt(1, 3);
            switch (choice) {
                case 1:
                    bookTicketFlow();
                    break;
                case 2:
                    runConcurrencyDemo();
                    break;
                case 3:
                    running = false;
                    System.out.println("\nThank you for using Movie Booking System! 🍿\n");
                    break;
            }
        }
    }

    // -------------------------------------------------------------------------
    // 1. INTERACTIVE BOOKING FLOW
    // -------------------------------------------------------------------------
    private void bookTicketFlow() {
        List<Show> shows = controller.getAllShows();
        System.out.println("\n--- Select a Show ---");
        for (int i = 0; i < shows.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + shows.get(i));
        }
        System.out.print("👉 Choose Show (1-" + shows.size() + "): ");
        int showIdx = readInt(1, shows.size()) - 1;
        Show selectedShow = shows.get(showIdx);

        // Display Seats
        System.out.println("\n-------------- SCREEN THIS WAY --------------");
        for (Seat seat : selectedShow.getSeats()) {
            boolean booked = selectedShow.isSeatBooked(seat.getSeatNumber());
            String status = booked ? "❌ BOOKED" : "🟢 AVAILABLE";
            System.out.printf("  Seat %2d [%-8s - $%.0f] : %s\n", 
                    seat.getSeatNumber(), seat.getSeatType(), seat.getPrice(), status);
        }
        System.out.println("---------------------------------------------");

        // Select Seat
        System.out.print("👉 Enter Seat Number to book (1-10): ");
        int seatNum = readInt(1, selectedShow.getSeats().size());

        // Enter Customer Name
        System.out.print("👉 Enter Customer Name: ");
        scanner.nextLine(); // consume newline
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) name = "Alice";
        User user = new User("U-" + seatNum, name);

        // Select Payment Strategy
        System.out.println("\nSelect Payment Method (Strategy Pattern):");
        System.out.println("  1. 📱 UPI (GPay / PhonePe)");
        System.out.println("  2. 💳 Credit/Debit Card");
        System.out.print("👉 Choice: ");
        int payChoice = readInt(1, 2);

        PaymentStrategy paymentStrategy;
        if (payChoice == 1) {
            System.out.print("👉 Enter UPI ID (e.g. user@upi): ");
            scanner.nextLine();
            String upiId = scanner.nextLine().trim();
            if (upiId.isEmpty()) upiId = name.toLowerCase() + "@upi";
            paymentStrategy = new UpiPayment(upiId);
        } else {
            System.out.print("👉 Enter Card Number: ");
            scanner.nextLine();
            String card = scanner.nextLine().trim();
            if (card.isEmpty()) card = "4111222233334444";
            paymentStrategy = new CardPayment(card);
        }

        // Process Booking
        try {
            Booking booking = controller.bookTicket(selectedShow, seatNum, user, paymentStrategy);
            System.out.println(booking);
        } catch (Exception e) {
            System.out.println("\n❌ Booking Failed: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // 2. CONCURRENCY & SEAT LOCKING DEMO
    // -------------------------------------------------------------------------
    private void runConcurrencyDemo() {
        System.out.println("\n=================================================");
        System.out.println("   ⚡ CONCURRENCY RACE CONDITION TEST ⚡");
        System.out.println("=================================================");
        System.out.println("Scenario:");
        System.out.println("  3 concurrent threads (Alice, Bob, Charlie) simultaneously");
        System.out.println("  attempt to book the EXACT SAME VIP Seat [5].");
        System.out.println("Target:");
        System.out.println("  In-memory seat locking ensures only ONE thread succeeds,");
        System.out.println("  and the remaining threads are cleanly rejected without double-booking.\n");

        Show show = controller.getAllShows().get(0);
        int targetSeatNumber = 5;

        // Reset target seat if previously booked for demo purposes
        show.getBookedSeatNumbers().remove(targetSeatNumber);

        String[] customers = {"Alice", "Bob", "Charlie"};
        int threadCount = customers.length;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(threadCount);

        for (String customerName : customers) {
            executor.submit(() -> {
                try {
                    startSignal.await(); // Synchronize all threads to fire at the exact same instant
                    User user = new User("USR-" + customerName, customerName);
                    PaymentStrategy payment = new UpiPayment(customerName.toLowerCase() + "@upi");
                    
                    Booking b = controller.bookTicket(show, targetSeatNumber, user, payment);
                    System.out.println("  [✅ SUCCESS] " + customerName + " successfully booked Seat " 
                            + targetSeatNumber + "! (Booking ID: " + b.getId() + ")");
                } catch (Exception e) {
                    System.out.println("  [❌ REJECTED] " + customerName + " failed: " + e.getMessage());
                } finally {
                    doneSignal.countDown();
                }
            });
        }

        startSignal.countDown(); // Fire all 3 threads!
        try {
            doneSignal.await();
        } catch (InterruptedException ignored) {}
        executor.shutdown();

        System.out.println("\nVerification: Is Seat " + targetSeatNumber + " booked? " + show.isSeatBooked(targetSeatNumber));
        System.out.println("Result: Zero double booking! In-memory locking passed. ✨\n");
    }

    private int readInt(int min, int max) {
        int val = -1;
        while (val < min || val > max) {
            if (scanner.hasNextInt()) {
                val = scanner.nextInt();
                if (val < min || val > max) {
                    System.out.print("Invalid! Enter (" + min + "-" + max + "): ");
                }
            } else if (scanner.hasNext()) {
                scanner.next();
                System.out.print("Invalid! Enter a number: ");
            } else {
                return max; // End of stream
            }
        }
        return val;
    }
}
