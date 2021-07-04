package org.seng302.datagenerator;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.Scanner;

public class Main {
    static Scanner scanner = new Scanner(System.in);
    static Random random = new Random();

    /**
     * Connects to Marinadb production environment
     * @return the connection to the database
     */
    public static Connection connectToDatabase() throws SQLException {
        String url = "jdbc:mariadb://localhost/seng302-2021-team500-prod";
        Connection conn = DriverManager.getConnection(url, "seng302-team500", "ListenDirectly6053");
        return conn;
    }

    /**
     * Clears the console on windows and linux
     */
    public static void clear() {
        final String ANSI_CLS = "\u001b[2J";
        final String ANSI_HOME = "\u001b[H";
        System.out.print(ANSI_CLS + ANSI_HOME);
        System.out.flush();
    }

    /**
     * Asks the user how many of the object type they want to be generated (e.g. 10 users)
     * @param objectName the name of the object
     * @return the number of objects to be generated
     */
    public static int getNumObjectsFromInput(String objectName) throws InterruptedException {
        int numObjects = 0;
        int maxObjects = 2000000; //maximum objects 2 million
        while (numObjects < 0 || numObjects >= maxObjects) {
            clear();
            try {
                System.out.println("-------------------------------------------------------");
                System.out.println(String.format("How many %s do you want", objectName));
                System.out.println("generated and put into the database?");
                System.out.println("-------------------------------------------------------");
                numObjects = Integer.parseInt(scanner.nextLine());

                if (numObjects > maxObjects) {
                    System.out.println(String.format("You cannot create %d %s", numObjects, objectName));
                    System.out.println(String.format("Please enter a number below %d", maxObjects));
                } else if (numObjects < 0) {
                    System.out.println(String.format("You cannot create %d %s that does not make sense",
                            numObjects, objectName));
                    System.out.println("Please enter a number above 0");
                }
            } catch (NumberFormatException e) {
                System.out.println("You entered an invalid character. Please input a number.");
            }
        }
        return numObjects;
    }

    /**
     * Generates a random date between a lower and upper date bound
     * @param start the earliest date that can be randomly generated
     * @param end the latest date that can be randomly generated
     * @return the randomly generated date
     */
    public static LocalDate randomDate(LocalDate start, LocalDate end) {
        int days = (int) ChronoUnit.DAYS.between(start, end);
        long randomDays = random.nextInt(days);
        return start.plusDays(randomDays);
    }
}