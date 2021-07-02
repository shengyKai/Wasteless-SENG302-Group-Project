package org.seng302.datagenerator;

import java.sql.*;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Main {
    static Scanner scanner = new Scanner(System.in);

    /**
     * Connects to Marinadb production environment
     * @return the connection to the database
     */
    public static Connection connectToDatabase() throws SQLException {
        String url = "jdbc:mariadb://localhost/seng302-2021-team500-prod";
        Connection conn = DriverManager.getConnection(url, "seng302-team500", "changeMe");
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
        int defaultCreated = 10;
        int numObjects = 0;
        while (numObjects <= 0) {
            clear();
            try {
                System.out.println("-------------------------------------------------------");
                System.out.println(String.format("How many %s do you want", objectName));
                System.out.println("generated and put into the database?");
                System.out.println("-------------------------------------------------------");
                numObjects = Integer.parseInt(scanner.nextLine());
            } catch (NoSuchElementException e) {
                System.out.println("You are using the gradle generate function");
                System.out.println("This terminal does not support scanner inputs (stdin)");
                System.out.println(String.format("To input your own number of %s", objectName));
                System.out.println("Compile and run this java file in a local terminal");
                System.out.println(String.format("%d %s will be created in...", defaultCreated, objectName));
                for (int i=5; i>0; i--) {
                    TimeUnit.SECONDS.sleep(1);
                    System.out.println(i);
                }
                numObjects = defaultCreated;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number! (above 0)");
            }
        }
        return numObjects;
    }
}