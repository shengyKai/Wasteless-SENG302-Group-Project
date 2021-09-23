package org.seng302.datagenerator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Random;

public class Main {
    static Random random = new Random();

    /**
     * Private constructor to hide implicit public constructor.
     */
    private Main() {}

    /**
     * Connects to Marinadb production environment
     * @return the connection to the database
     */
    public static Connection connectToDatabase() throws SQLException {
        Map<String, String> properties = ExampleDataFileReader.readPropertiesFile("/generator_db.properties");
        return DriverManager.getConnection(properties.get("url"), properties.get("username"), properties.get("password"));
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

    /**
     * Generates a random instant between a lower and upper bound
     * @param start The earliest instant that can be randomly generated
     * @param end The latest instant that can be randomly generated (exclusive)
     * @return Randomly generated instant
     */
    public static Instant randomInstant(Instant start, Instant end) {
        long min = start.getEpochSecond();
        long max = end.getEpochSecond();
        int offset = random.nextInt((int)(max - min)); // Seconds since the start
        return Instant.ofEpochSecond(min + offset);
    }
}