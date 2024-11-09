package com.driver;

import java.util.Scanner;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class EventMonitoringServer {
	private static final int THREAD_POOL_SIZE = 5;
    private static final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    private static final CountDownLatch shutdownLatch = new CountDownLatch(1);
    private static final AtomicBoolean highMagnitudeEventDetected = new AtomicBoolean(false);

    public static void main(String[] args) {
        try {
            startServer();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            stopServer();
        }
    }
    

    private static void startServer() throws InterruptedException {
    	// your code goes here
        Thread shutdownListener = new Thread(() -> {
            try {
                waitForShutdownSignal();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        shutdownListener.start();

        // Start processing events concurrently
        int eventId = 1;
        Random random = new Random();
        while (!highMagnitudeEventDetected.get() && shutdownLatch.getCount() > 0) {
            int eventMagnitude = random.nextInt(10) + 1;
            int currentEventId = eventId++;

            executorService.submit(() -> processEvent(currentEventId, eventMagnitude));
            Thread.sleep(200); // Simulate delay between incoming events
        }

        shutdownListener.join(); // Wait for the listener thread to complete
    }


    private static void processEvent(int eventId,int magnitude) {
        // Simulate event processing in a separate thread
        executorService.submit(() -> {
            System.out.println("Event " + eventId + " processed.");
            if (eventId == 5) { // Simulate a high magnitude event at event 5
                System.out.println("High magnitude event detected!");
                highMagnitudeEventDetected.set(true);
            }
        });
    }

    private static void waitForShutdownSignal() throws InterruptedException {
    	// your code goes here
        Scanner scanner = new Scanner(System.in);
        while (shutdownLatch.getCount() > 0) {
            String userInput = getUserInput();
            if ("shutdown".equalsIgnoreCase(userInput.trim())) {
                System.out.println("Shutting down the server gracefully...");
                shutdownLatch.countDown();
                highMagnitudeEventDetected.set(true);
            }
        }
        scanner.close();
    }

    private static String getUserInput() {
    	Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }

    private static void stopServer() {
    	// your code goes here
        try {
            executorService.shutdown();
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
            System.out.println("Server has shut down.");
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
