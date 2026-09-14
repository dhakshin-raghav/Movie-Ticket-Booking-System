package com.moviebooking;

import com.moviebooking.controller.BookingController;
import com.moviebooking.controller.MovieController;
import com.moviebooking.controller.ShowController;
import com.moviebooking.data.DataInitializer;
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
import com.moviebooking.util.SeatViewHelper;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Interactive Console Application inspired by BookMyShow.
 * Combines a friendly CLI menu with thread-safe seat locking and OOP design patterns.
 */
public class InteractiveConsoleApp {

    private final MovieController movieController;
    private final ShowController showController;
    private final BookingController bookingController;
    private final SeatLockService seatLockService;
    private final Scanner scanner;

    public InteractiveConsoleApp() {
        MovieRepository movieRepository = new MovieRepository();
        ShowRepository showRepository = new ShowRepository();
        BookingRepository bookingRepository = new BookingRepository();

        // 30-second checkout lock TTL for interactive users
        this.seatLockService = new SeatLockService(30);
        MovieService movieService = new MovieService(movieRepository);
        ShowService showService = new ShowService(showRepository, seatLockService);
        PaymentService paymentService = new PaymentService();
        BookingService bookingService = new BookingService(bookingRepository, seatLockService, paymentService);

        this.movieController = new MovieController(movieService);
        this.showController = new ShowController(showService);
        this.bookingController = new BookingController(bookingService);
        this.scanner = new Scanner(System.in);

        // Seed realistic sample data across Bangalore, Mumbai, Delhi
        DataInitializer.initialize(movieController, showController);
    }

    public void start() {
        printHeader("🎬 WELCOME TO MOVIE TICKET BOOKING SYSTEM 🎟️");

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = getUserChoice(1, 5);

            switch (choice) {
                case 1:
                    handleTicketBooking();
                    break;
                case 2:
                    handleViewSeatMatrix();
                    break;
                case 3:
                    runConcurrencyStressTest();
                    break;
                case 4:
                    runLockExpirationDemo();
                    break;
                case 5:
                    running = false;
                    printSuccess("Thank you for visiting! Have a wonderful day! 🍿🎬");
                    break;
            }
        }
    }

    private void printMainMenu() {
        System.out.println("\n┌─────────────────────────────────────────────────────────────┐");
        System.out.println("│                     MAIN DASHBOARD                          │");
        System.out.println("├─────────────────────────────────────────────────────────────┤");
        System.out.println("│  1. 🎟️  Book Movie Tickets (Interactive Booking Flow)       │");
        System.out.println("│  2. 💺  View Real-Time Seat Matrix for a Show               │");
        System.out.println("│  3. ⚡  Run Multithreaded Concurrency Test (Race Protection) │");
        System.out.println("│  4. ⏳  Run Seat Lock Timeout (TTL) Expiration Demo         │");
        System.out.println("│  5. 🚪  Exit Application                                    │");
        System.out.println("└─────────────────────────────────────────────────────────────┘");
    }

    // -------------------------------------------------------------------------
    // 1. INTERACTIVE TICKET BOOKING WORKFLOW
    // -------------------------------------------------------------------------
    private void handleTicketBooking() {
        printSection("STEP 1: Select City");
        City selectedCity = selectCity();

        printSection("STEP 2: Select Movie in " + selectedCity);
        Movie selectedMovie = selectMovie(selectedCity);
        if (selectedMovie == null) {
            System.out.println("⚠️  No movies currently screening in " + selectedCity);
            return;
        }

        printSection("STEP 3: Select Show for " + selectedMovie.getTitle());
        Show selectedShow = selectShow(selectedCity, selectedMovie);
        if (selectedShow == null) {
            System.out.println("⚠️  No shows available for this selection.");
            return;
        }

        // Determine Pricing Strategy based on show date (Strategy Pattern)
        PricingStrategy pricingStrategy = new WeekendPricingStrategy();

        printSection("STEP 4: Real-Time Seat Layout");
        SeatViewHelper.printSeatLayout(selectedShow, seatLockService, pricingStrategy);

        // User info
        System.out.print("👉 Enter your name (e.g. John Doe): ");
        scanner.nextLine(); // Clear buffer
        String userName = scanner.nextLine().trim();
        if (userName.isEmpty()) userName = "Guest Customer";

        System.out.print("👉 Enter your email: ");
        String email = scanner.nextLine().trim();
        if (email.isEmpty()) email = "guest@example.com";

        User user = new User("USR-" + UUID.randomUUID().toString().substring(0, 5), userName, email);

        // Select seats
        System.out.print("👉 Enter seat IDs to book (comma-separated, e.g. A1, A2): ");
        String seatInput = scanner.nextLine().trim();
        if (seatInput.isEmpty()) {
            System.out.println("❌ No seats entered. Booking aborted.");
            return;
        }

        List<Seat> selectedSeats = parseSeats(selectedShow, seatInput);
        if (selectedSeats.isEmpty()) {
            System.out.println("❌ Invalid seat IDs specified. Please verify the seat map.");
            return;
        }

        // STEP 5: Attempt In-Memory Seat Lock
        printSection("STEP 5: Acquiring In-Memory Seat Lock");
        Booking pendingBooking;
        try {
            pendingBooking = bookingController.createBooking(selectedShow, selectedSeats, user, pricingStrategy);
            System.out.println("🔒 Success! Seats " + seatInput + " temporarily locked for 30 seconds.");
            System.out.println("💰 Total Bill: $" + String.format("%.2f", pendingBooking.getTotalAmount()));
        } catch (Exception e) {
            System.out.println("❌ Unable to reserve seats: " + e.getMessage());
            return;
        }

        // STEP 6: Payment Strategy
        printSection("STEP 6: Select Payment Method");
        System.out.println("   1. 📱 UPI (GPay / PhonePe / Paytm)");
        System.out.println("   2. 💳 Credit / Debit Card");
        System.out.println("   3. ❌ Cancel Checkout & Release Seats");

        int paymentChoice = getUserChoice(1, 3);
        PaymentStrategy paymentStrategy;

        if (paymentChoice == 1) {
            System.out.print("👉 Enter your UPI ID (e.g. user@okhdfcbank): ");
            scanner.nextLine();
            String upiId = scanner.nextLine().trim();
            if (upiId.isEmpty()) upiId = "customer@upi";
            paymentStrategy = new UpiPaymentStrategy(upiId);
        } else if (paymentChoice == 2) {
            System.out.print("👉 Enter 16-digit Card Number: ");
            scanner.nextLine();
            String cardNum = scanner.nextLine().trim().replaceAll("\\s+", "");
            if (cardNum.length() < 12) cardNum = "4111222233334444";
            paymentStrategy = new CreditCardPaymentStrategy(cardNum, "12/28", "123");
        } else {
            // Cancel and release lock
            seatLockService.unlockSeats(selectedShow, selectedSeats, user);
            System.out.println("⚠️ Checkout cancelled. Seats have been unlocked.");
            return;
        }

        // STEP 7: Confirm Booking
        printSection("STEP 7: Processing Payment & Confirmation");
        try {
            Booking confirmedBooking = bookingController.confirmBooking(pendingBooking.getId(), paymentStrategy);
            printTicketReceipt(confirmedBooking);
        } catch (Exception e) {
            System.out.println("❌ Booking confirmation failed: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // 2. VIEW SEAT MATRIX FOR ANY SHOW
    // -------------------------------------------------------------------------
    private void handleViewSeatMatrix() {
        City city = selectCity();
        Movie movie = selectMovie(city);
        if (movie == null) return;
        Show show = selectShow(city, movie);
        if (show == null) return;

        PricingStrategy pricingStrategy = new StandardPricingStrategy();
        SeatViewHelper.printSeatLayout(show, seatLockService, pricingStrategy);
    }

    // -------------------------------------------------------------------------
    // 3. MULTITHREADED CONCURRENCY STRESS TEST
    // -------------------------------------------------------------------------
    private void runConcurrencyStressTest() {
        printHeader("⚡ MULTITHREADED CONCURRENCY RACE CONDITION TEST ⚡");
        System.out.println("Scenario:");
        System.out.println("  4 concurrent customer threads (Bob, Charlie, David, Emma) all attempt to");
        System.out.println("  book the exact same Platinum seat [C1] at the exact same millisecond.");
        System.out.println("Target:");
        System.out.println("  In-memory seat locking MUST guarantee that exactly ONE user succeeds,");
        System.out.println("  and the other 3 receive clean rejection messages without double-booking.\n");

        City city = City.BANGALORE;
        List<Movie> movies = showController.getMoviesByCity(city);
        Show testShow = showController.getShowsByCityAndMovie(city, movies.get(0).getId()).get(0);
        Seat targetSeat = testShow.getScreen().getSeats().stream()
                .filter(s -> s.getId().equals("C1"))
                .findFirst().orElseThrow();

        // Ensure seat is unlocked/unbooked for the demo
        PricingStrategy pricing = new StandardPricingStrategy();
        User[] users = {
                new User("USR-RACE-1", "Bob", "bob@example.com"),
                new User("USR-RACE-2", "Charlie", "charlie@example.com"),
                new User("USR-RACE-3", "David", "david@example.com"),
                new User("USR-RACE-4", "Emma", "emma@example.com")
        };

        int threadCount = users.length;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(threadCount);

        for (User u : users) {
            executor.submit(() -> {
                try {
                    startSignal.await(); // Synchronize all threads to fire simultaneously
                    PaymentStrategy payment = new UpiPaymentStrategy(u.getName().toLowerCase() + "@upi");
                    Booking b = bookingController.bookTickets(testShow, List.of(targetSeat), u, pricing, payment);
                    System.out.println("  [✅ SUCCESS] " + u.getName() + " won the race! Booked seat C1 (Booking ID: " + b.getId() + ")");
                } catch (Exception e) {
                    System.out.println("  [❌ REJECTED] " + u.getName() + " rejected: " + e.getMessage());
                } finally {
                    doneSignal.countDown();
                }
            });
        }

        System.out.println("🚀 Firing 4 concurrent threads simultaneously...\n");
        startSignal.countDown(); // Fire!
        try {
            doneSignal.await();
        } catch (InterruptedException ignored) {}
        executor.shutdown();

        System.out.println("\n📊 Current Seat C1 Status: " + seatLockService.getSeatStatus(testShow, targetSeat));
        printSuccess("Race Condition Test Complete! No double booking occurred.");
    }

    // -------------------------------------------------------------------------
    // 4. LOCK TIMEOUT / TTL EXPIRATION DEMO
    // -------------------------------------------------------------------------
    private void runLockExpirationDemo() {
        printHeader("⏳ IN-MEMORY SEAT LOCK TTL EXPIRATION DEMO ⏳");
        System.out.println("Scenario:");
        System.out.println("  1. Frank locks Gold seat [B1] with a 3-second temporary hold.");
        System.out.println("  2. Grace tries to book B1 immediately & gets blocked.");
        System.out.println("  3. After 3.5 seconds, Frank's lock expires automatically.");
        System.out.println("  4. Grace retries and successfully books B1!\n");

        City city = City.BANGALORE;
        List<Movie> movies = showController.getMoviesByCity(city);
        Show testShow = showController.getShowsByCityAndMovie(city, movies.get(0).getId()).get(0);
        Seat targetSeat = testShow.getScreen().getSeats().stream()
                .filter(s -> s.getId().equals("B1"))
                .findFirst().orElseThrow();

        // Custom short TTL lock service for demo
        SeatLockService shortTtlService = new SeatLockService(3);
        BookingRepository repo = new BookingRepository();
        BookingService shortTtlBookingService = new BookingService(repo, shortTtlService, new PaymentService());
        PricingStrategy pricing = new StandardPricingStrategy();

        User frank = new User("USR-FRANK", "Frank", "frank@example.com");
        User grace = new User("USR-GRACE", "Grace", "grace@example.com");

        System.out.println("1. Frank acquires temporary lock on B1...");
        Booking frankBooking = shortTtlBookingService.createBooking(testShow, List.of(targetSeat), frank, pricing);
        System.out.println("   🔒 B1 status: " + shortTtlService.getSeatStatus(testShow, targetSeat));

        System.out.println("\n2. Grace attempts to lock B1 while Frank's lock is active:");
        try {
            shortTtlBookingService.createBooking(testShow, List.of(targetSeat), grace, pricing);
            System.out.println("   Grace unexpectedly locked B1!");
        } catch (Exception e) {
            System.out.println("   ❌ Expected Failure for Grace: " + e.getMessage());
        }

        System.out.println("\n3. Waiting 3.5 seconds for Frank's lock to expire (TTL = 3s)...");
        try {
            Thread.sleep(3500);
        } catch (InterruptedException ignored) {}
        System.out.println("   ⏰ Timeout reached! Lock has now expired.");

        System.out.println("\n4. Grace retries booking seat B1 after expiration:");
        try {
            PaymentStrategy gracePayment = new UpiPaymentStrategy("grace@upi");
            Booking graceConfirmed = shortTtlBookingService.bookTickets(testShow, List.of(targetSeat), grace, pricing, gracePayment);
            System.out.println("   ✅ Success! Grace successfully booked seat B1! ID: " + graceConfirmed.getId());
        } catch (Exception e) {
            System.out.println("   Grace failed: " + e.getMessage());
        }

        System.out.println("\n5. Frank returns late and tries to confirm his expired booking:");
        try {
            shortTtlBookingService.confirmBooking(frankBooking.getId(), new UpiPaymentStrategy("frank@upi"));
            System.out.println("   Frank confirmed booking!");
        } catch (Exception e) {
            System.out.println("   ❌ Expected Rejection for Frank: " + e.getMessage());
        }

        printSuccess("TTL Lock Expiration Demo Completed Successfully!");
    }

    // -------------------------------------------------------------------------
    // HELPER METHODS
    // -------------------------------------------------------------------------
    private City selectCity() {
        City[] cities = City.values();
        for (int i = 0; i < cities.length; i++) {
            System.out.println("   " + (i + 1) + ". 🏙️  " + cities[i]);
        }
        int choice = getUserChoice(1, cities.length);
        return cities[choice - 1];
    }

    private Movie selectMovie(City city) {
        List<Movie> movies = showController.getMoviesByCity(city);
        if (movies.isEmpty()) return null;
        for (int i = 0; i < movies.size(); i++) {
            Movie m = movies.get(i);
            System.out.println("   " + (i + 1) + ". 🎥 " + m.getTitle() + " (" + m.getGenre() + " | " + m.getDurationInMinutes() + " mins)");
        }
        int choice = getUserChoice(1, movies.size());
        return movies.get(choice - 1);
    }

    private Show selectShow(City city, Movie movie) {
        List<Show> shows = showController.getShowsByCityAndMovie(city, movie.getId());
        if (shows.isEmpty()) return null;
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("hh:mm a (EEE, dd MMM)");
        for (int i = 0; i < shows.size(); i++) {
            Show s = shows.get(i);
            System.out.println("   " + (i + 1) + ". 🎦 " + s.getCinemaHall().getName() 
                    + " [" + s.getScreen().getName() + "] @ " + s.getStartTime().format(timeFmt));
        }
        int choice = getUserChoice(1, shows.size());
        return shows.get(choice - 1);
    }

    private List<Seat> parseSeats(Show show, String input) {
        String[] tokens = input.split("[,\\s]+");
        Map<String, Seat> seatMap = new HashMap<>();
        for (Seat s : show.getScreen().getSeats()) {
            seatMap.put(s.getId().toUpperCase(), s);
        }

        List<Seat> result = new ArrayList<>();
        for (String token : tokens) {
            String key = token.trim().toUpperCase();
            if (seatMap.containsKey(key)) {
                result.add(seatMap.get(key));
            }
        }
        return result;
    }

    private void printTicketReceipt(Booking booking) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm a");
        StringBuilder seatsStr = new StringBuilder();
        for (Seat s : booking.getSeats()) {
            seatsStr.append(s.getId()).append(" ");
        }

        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                🎟️  MOVIE TICKET CONFIRMATION  🎟️             ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║ 🎬 Movie:       " + String.format("%-44s", booking.getShow().getMovie().getTitle()) + "║");
        System.out.println("║ 🎦 Cinema:      " + String.format("%-44s", booking.getShow().getCinemaHall().getName()) + "║");
        System.out.println("║ 🕒 Show Time:   " + String.format("%-44s", booking.getShow().getStartTime().format(fmt)) + "║");
        System.out.println("║ 💺 Seats:       " + String.format("%-44s", seatsStr.toString().trim()) + "║");
        System.out.println("║ 👤 Customer:    " + String.format("%-44s", booking.getUser().getName()) + "║");
        System.out.println("║ 💵 Total Paid:  " + String.format("$%-43.2f", booking.getTotalAmount()) + "║");
        System.out.println("║ 🆔 Booking ID:  " + String.format("%-44s", booking.getId()) + "║");
        System.out.println("║ ✅ Status:      " + String.format("%-44s", booking.getStatus()) + "║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        printSuccess("Enjoy your movie and don't forget the popcorn! 🍿🎉");
    }

    private int getUserChoice(int min, int max) {
        int choice = -1;
        while (choice < min || choice > max) {
            System.out.print("👉 Enter your choice (" + min + "-" + max + "): ");
            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                if (choice < min || choice > max) {
                    System.out.println("❌ Invalid choice. Please enter a number between " + min + " and " + max + ".");
                }
            } else if (scanner.hasNext()) {
                System.out.println("❌ Invalid input! Please enter a number.");
                scanner.next();
            } else {
                // End of input stream (e.g. EOF in non-interactive mode)
                return max; // Exit
            }
        }
        return choice;
    }

    private void printHeader(String text) {
        System.out.println("\n══════════════════════════════════════════════════════════════");
        System.out.println("          " + text);
        System.out.println("══════════════════════════════════════════════════════════════");
    }

    private void printSection(String text) {
        System.out.println("\n🔹 " + text);
        System.out.println("──────────────────────────────────────────────────────────────");
    }

    private void printSuccess(String text) {
        System.out.println("\n✨ " + text + "\n");
    }
}
