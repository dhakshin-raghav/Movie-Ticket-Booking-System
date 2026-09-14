package com.moviebooking;

import com.moviebooking.controller.BookingController;
import com.moviebooking.controller.MovieController;
import com.moviebooking.controller.ShowController;
import com.moviebooking.model.*;
import com.moviebooking.repository.BookingRepository;
import com.moviebooking.repository.MovieRepository;
import com.moviebooking.repository.ShowRepository;
import com.moviebooking.service.BookingService;
import com.moviebooking.service.MovieService;
import com.moviebooking.service.PaymentService;
import com.moviebooking.service.SeatLockService;
import com.moviebooking.service.ShowService;
import com.moviebooking.strategy.payment.CreditCardPaymentStrategy;
import com.moviebooking.strategy.payment.PaymentStrategy;
import com.moviebooking.strategy.payment.UpiPaymentStrategy;
import com.moviebooking.strategy.pricing.PricingStrategy;
import com.moviebooking.strategy.pricing.StandardPricingStrategy;
import com.moviebooking.strategy.pricing.WeekendPricingStrategy;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main application class showcasing:
 * 1. Object-Oriented Architecture (Controller -> Service -> Repository)
 * 2. In-Memory Seat Locking to eliminate race conditions
 * 3. Strategy Pattern for Dynamic Pricing and Pluggable Payment Methods
 * 4. Multithreaded Concurrency Stress Test with simultaneous seat booking
 * 5. Time-To-Live (TTL) Seat Lock Expiration and Recovery
 */
public class Main {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("========================================================================");
        System.out.println("       MOVIE TICKET BOOKING SYSTEM - LOW LEVEL DESIGN (LLD)");
        System.out.println("       Java | OOP | Multithreading | In-Memory Seat Locking");
        System.out.println("========================================================================\n");

        // ---------------------------------------------------------------------
        // 1. SYSTEM INITIALIZATION & DEPENDENCY INJECTION
        // ---------------------------------------------------------------------
        // Repositories
        MovieRepository movieRepository = new MovieRepository();
        ShowRepository showRepository = new ShowRepository();
        BookingRepository bookingRepository = new BookingRepository();

        // Services
        // Configure lock TTL: 2 seconds for easy demo verification
        SeatLockService seatLockService = new SeatLockService(2);
        MovieService movieService = new MovieService(movieRepository);
        ShowService showService = new ShowService(showRepository, seatLockService);
        PaymentService paymentService = new PaymentService();
        BookingService bookingService = new BookingService(bookingRepository, seatLockService, paymentService);

        // Controllers
        MovieController movieController = new MovieController(movieService);
        ShowController showController = new ShowController(showService);
        BookingController bookingController = new BookingController(bookingService);

        // ---------------------------------------------------------------------
        // 2. SEED SAMPLE DATA (Cinema, Screens, Seats, Movies, Shows)
        // ---------------------------------------------------------------------
        CinemaHall cinema = new CinemaHall("CH-1", "PVR Cinemas, Nexus Mall", "Bengaluru");
        Screen screen1 = new Screen("SCR-1", "Audi 1 (Dolby Atmos)");

        // Add 5 Silver seats (A1-A5), 5 Gold seats (B1-B5), 5 Platinum seats (C1-C5)
        for (int i = 1; i <= 5; i++) {
            screen1.addSeat(new Seat("A" + i, "A", i, SeatType.SILVER));
            screen1.addSeat(new Seat("B" + i, "B", i, SeatType.GOLD));
            screen1.addSeat(new Seat("C" + i, "C", i, SeatType.PLATINUM));
        }
        cinema.addScreen(screen1);

        // Add Movie
        movieController.addMovie("MOV-101", "Interstellar", 169, "Sci-Fi / Adventure", "English");
        Movie movie = movieController.getMovieById("MOV-101").orElseThrow();

        // Add Shows: Show 1 (Friday evening - Weekday) and Show 2 (Saturday night - Weekend)
        LocalDateTime fridayEvening = LocalDateTime.of(2026, 9, 18, 19, 0);
        LocalDateTime saturdayNight = LocalDateTime.of(2026, 9, 19, 21, 0);

        Show weekdayShow = showController.createShow("SHOW-001", movie, cinema, screen1, fridayEvening, fridayEvening.plusMinutes(169));
        Show weekendShow = showController.createShow("SHOW-002", movie, cinema, screen1, saturdayNight, saturdayNight.plusMinutes(169));

        System.out.println("Initialized Cinema: " + cinema);
        System.out.println("Screen: " + screen1.getName() + " with " + screen1.getSeats().size() + " total seats.");
        System.out.println("Movie: " + movie);
        System.out.println("Created Shows:\n  1) " + weekdayShow + "\n  2) " + weekendShow);
        System.out.println("------------------------------------------------------------------------\n");

        // ---------------------------------------------------------------------
        // DEMO 1: SINGLE USER BOOKING (Happy Path + Pricing & Payment Strategy)
        // ---------------------------------------------------------------------
        System.out.println(">>> DEMO 1: Standard Single-User Booking Flow <<<");
        User alice = new User("USR-1", "Alice Johnson", "alice@example.com");
        PricingStrategy standardPricing = new StandardPricingStrategy();
        PaymentStrategy upiPayment = new UpiPaymentStrategy("alice@okhdfcbank");

        System.out.println("Customer: " + alice.getName() + " is viewing available seats for Show #SHOW-001...");
        List<Seat> availableForShow1 = showController.getAvailableSeats(weekdayShow);
        System.out.println("Available seats count: " + availableForShow1.size());

        // Alice chooses seats A1 and A2
        Seat seatA1 = screen1.getSeats().stream().filter(s -> s.getId().equals("A1")).findFirst().get();
        Seat seatA2 = screen1.getSeats().stream().filter(s -> s.getId().equals("A2")).findFirst().get();
        List<Seat> aliceSeats = List.of(seatA1, seatA2);

        System.out.println("\nAlice is booking seats: " + aliceSeats + " using " + upiPayment.getPaymentMethodName());
        try {
            Booking aliceBooking = bookingController.bookTickets(weekdayShow, aliceSeats, alice, standardPricing, upiPayment);
            System.out.println("\n--- Booking Succeeded! ---");
            System.out.println(aliceBooking);
        } catch (Exception e) {
            System.err.println("Booking failed: " + e.getMessage());
        }

        System.out.println("\nAvailable seats remaining for Show #SHOW-001: " + showController.getAvailableSeats(weekdayShow).size());
        System.out.println("------------------------------------------------------------------------\n");

        // ---------------------------------------------------------------------
        // DEMO 2: CONCURRENT MULTITHREADING TEST (RACE CONDITION PREVENTION)
        // ---------------------------------------------------------------------
        System.out.println(">>> DEMO 2: Multithreaded Concurrency Stress Test <<<");
        System.out.println("Scenario: 4 concurrent threads (Bob, Charlie, David, Emma) all attempt to");
        System.out.println("book the exact same VIP seat [C1] simultaneously at the exact same millisecond.");
        System.out.println("Target: Only ONE thread must succeed; 3 threads must be cleanly rejected without double-booking.\n");

        Seat highDemandSeat = screen1.getSeats().stream().filter(s -> s.getId().equals("C1")).findFirst().get();
        List<Seat> targetSeats = List.of(highDemandSeat);

        User[] competingUsers = {
                new User("USR-2", "Bob", "bob@example.com"),
                new User("USR-3", "Charlie", "charlie@example.com"),
                new User("USR-4", "David", "david@example.com"),
                new User("USR-5", "Emma", "emma@example.com")
        };

        int threadCount = competingUsers.length;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(threadCount);

        for (User user : competingUsers) {
            executor.submit(() -> {
                try {
                    // Wait until all threads are created and ready to fire at the exact same instant
                    startSignal.await();
                    PaymentStrategy payment = new CreditCardPaymentStrategy("4111222233334444", "12/28", "123");
                    Booking b = bookingController.bookTickets(weekendShow, targetSeats, user, standardPricing, payment);
                    System.out.println(" [SUCCESS] Thread " + Thread.currentThread().getName() + " -> " 
                            + user.getName() + " successfully booked seat " + highDemandSeat.getId() 
                            + "! Booking ID: " + b.getId());
                } catch (Exception e) {
                    System.out.println(" [REJECTED] Thread " + Thread.currentThread().getName() + " -> " 
                            + user.getName() + " failed to book: " + e.getMessage());
                } finally {
                    doneSignal.countDown();
                }
            });
        }

        // Fire all threads at once!
        startSignal.countDown();
        doneSignal.await();
        executor.shutdown();

        System.out.println("\nVerification: Is seat C1 marked as booked in Show #SHOW-002? " + weekendShow.isSeatBooked("C1"));
        System.out.println("------------------------------------------------------------------------\n");

        // ---------------------------------------------------------------------
        // DEMO 3: SEAT LOCK EXPIRATION & RECOVERY (TTL CHECKOUT TIMEOUT)
        // ---------------------------------------------------------------------
        System.out.println(">>> DEMO 3: In-Memory Seat Lock Expiration & Recovery <<<");
        System.out.println("Scenario: Frank locks seat [B1] but abandons checkout (does NOT pay).");
        System.out.println("Lock TTL is set to 2 seconds. After timeout, Grace should be able to book it.\n");

        User frank = new User("USR-6", "Frank", "frank@example.com");
        User grace = new User("USR-7", "Grace", "grace@example.com");
        Seat seatB1 = screen1.getSeats().stream().filter(s -> s.getId().equals("B1")).findFirst().get();

        // Frank reserves the seat (creates PENDING booking with temporary lock)
        System.out.println("1. Frank acquires temporary lock on seat B1...");
        Booking frankPendingBooking = bookingController.createBooking(weekdayShow, List.of(seatB1), frank, standardPricing);
        System.out.println("   Seat B1 locked by Frank. Pending Booking ID: " + frankPendingBooking.getId());

        // Immediately, Grace tries to book the same seat B1
        System.out.println("\n2. Grace attempts to lock seat B1 immediately while Frank's lock is active:");
        try {
            bookingController.createBooking(weekdayShow, List.of(seatB1), grace, standardPricing);
            System.out.println("   Grace unexpectedly locked seat B1!");
        } catch (Exception e) {
            System.out.println("   Expected Failure for Grace: " + e.getMessage());
        }

        // Wait for lock to expire (TTL is 2 seconds, we sleep 2.5 seconds)
        System.out.println("\n3. Waiting 2.5 seconds for Frank's lock to expire...");
        Thread.sleep(2500);
        System.out.println("   TTL expired! Frank's lock is now stale.");

        // Grace tries to book seat B1 again
        System.out.println("\n4. Grace retries booking seat B1 after lock expiration:");
        try {
            PaymentStrategy graceCard = new CreditCardPaymentStrategy("5500111122223333", "08/29", "456");
            Booking graceBooking = bookingController.bookTickets(weekdayShow, List.of(seatB1), grace, standardPricing, graceCard);
            System.out.println("   Success! Grace successfully locked and booked seat B1!");
            System.out.println("   Grace's Booking ID: " + graceBooking.getId() + " | Status: " + graceBooking.getStatus());
        } catch (Exception e) {
            System.err.println("   Grace failed: " + e.getMessage());
        }

        // Now Frank tries to confirm his expired booking
        System.out.println("\n5. Frank returns late and tries to confirm his expired booking:");
        try {
            PaymentStrategy frankUpi = new UpiPaymentStrategy("frank@okaxis");
            bookingController.confirmBooking(frankPendingBooking.getId(), frankUpi);
            System.out.println("   Frank confirmed booking!");
        } catch (Exception e) {
            System.out.println("   Expected Failure for Frank: " + e.getMessage());
        }
        System.out.println("------------------------------------------------------------------------\n");

        // ---------------------------------------------------------------------
        // DEMO 4: STRATEGY PATTERN (DYNAMIC PRICING COMPARISON)
        // ---------------------------------------------------------------------
        System.out.println(">>> DEMO 4: Strategy Pattern (Dynamic Weekend Pricing) <<<");
        PricingStrategy weekendPricing = new WeekendPricingStrategy();
        Seat platinumSeat = screen1.getSeats().stream().filter(s -> s.getId().equals("C2")).findFirst().get();

        double weekdayPrice = standardPricing.calculatePrice(weekdayShow, platinumSeat);
        double weekendPrice = weekendPricing.calculatePrice(weekendShow, platinumSeat);

        System.out.println("Seat: " + platinumSeat);
        System.out.println("Weekday Show (" + weekdayShow.getStartTime().getDayOfWeek() + ") Base Price: $" + weekdayPrice);
        System.out.println("Weekend Show (" + weekendShow.getStartTime().getDayOfWeek() + ") Surge Price (+20%): $" + weekendPrice);

        System.out.println("\n========================================================================");
        System.out.println("                   ALL LLD DEMONSTRATIONS COMPLETED!");
        System.out.println("========================================================================");
    }
}
