package org.seng302.datagenerator;

import java.sql.*;
import java.util.Map;
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
        Map<String, String> properties = ExampleDataFileReader.readPropertiesFile("/generator_db.properties");
        return DriverManager.getConnection(properties.get("url"), properties.get("username"), properties.get("password"));
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
        while (numObjects <= 0) {
            clear();
            try {
                System.out.println("-------------------------------------------------------");
                System.out.println(String.format("How many %s do you want", objectName));
                System.out.println("generated and put into the database?");
                System.out.println("-------------------------------------------------------");
                numObjects = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number! (above 0)");
            }
        }
        return numObjects;
    }
}