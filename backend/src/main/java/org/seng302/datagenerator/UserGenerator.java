package org.seng302.datagenerator;

/**
 * If you are running this function from gradle generate then it will not
 */

import java.sql.*;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.io.File;
import java.util.concurrent.TimeUnit;

import lombok.Data;
import org.seng302.datagenerator.LocationGenerator.Location;

import static org.seng302.datagenerator.Main.*;

public class UserGenerator {
    private Random random = new Random();
    private Connection conn;
    private LocationGenerator locationGenerator = LocationGenerator.getInstance();
    private PersonNameGenerator personNameGenerator = PersonNameGenerator.getInstance();

    //predefined lists
    private static final String[] BIOS = {"I enjoy running on the weekends", "Beaches are fun", "Got to focus on my career", "If only I went to a better university", "Read documentation yeah right", "My cats keep me going", "All I need is food"};

    public UserGenerator(Connection conn) {
        this.conn = conn;
    }

    /**
     * Randomly generates the date of birth of the user
     * @return the user's date of birth
     */
    private String generateDOB() {
        String day = String.valueOf(random.nextInt(27) + 1); // +1 as the day cannot be zero
        String month = String.valueOf(random.nextInt(11) + 1); // +1 as the month cannot be zero
        //year must be more than a year in the past
        String year = String.valueOf(random.nextInt(2006) + 1); // +1 as the year cannot be zero
        while (year.length() < 4) year = "0" + year;

        return year +"-"+ month +"-"+ day;
    }

    /**
     * Randomly generates the phone number of the user
     * @return the user's phone number
     */
    private String generatePhNum() {
        return "027" + String.valueOf(random.nextInt(8999999) + 1000000);
    }

    /**
     * Randomly generates the hashed password
     * @return
     */
    private String generatePassword() {
        return "0".repeat(64);
    }

    /**
     * Randomly generates the email of the user. Appends an number representing
     * the current user being generate to keep each individual email unique
     * @return the user's email
     */
    private String generateEmail(int counter, PersonNameGenerator.FullName fullName) {
        String[] suffixes = {"@gmail.com", "@hotmail.com", "@yahoo.com", "@uclive.ac.nz", "@xtra.co.nz"};
        String emailStart = fullName.getFirstName() + fullName.getLastName();
        String counterStr = String.valueOf(counter);
        String suffix = suffixes[random.nextInt(suffixes.length)];
        return emailStart + counterStr + suffix;
    }

    /**
     * Main program
     * @param args no arguments should be provided
     */
    public static void main(String[] args) throws SQLException, InterruptedException {
        Connection conn = connectToDatabase();
        var generator = new UserGenerator(conn);
        int userCount = getNumObjectsFromInput("users");
        generator.generateUsers(userCount);
    }

    /**
     * Generates the users
     * @param userCount Number of users to generate
     * @return List of generated user ids
     */
    public List<Long> generateUsers(int userCount) {
        Instant startTime = Instant.now();
        List<PersonNameGenerator.FullName> names = new ArrayList<>();
        for (int i = 0; i < userCount; i++) names.add(personNameGenerator.generateName());


        try {
            System.out.println("Adding addresses");
            List<Location> addresses = new ArrayList<>();
            for (int i = 0; i < userCount; i++) addresses.add(locationGenerator.generateAddress(random));
            List<Long> addressIds = locationGenerator.createInsertAddressSQL(addresses, conn);

            System.out.println("Adding accounts");
            AccountBatch accountBatch = new AccountBatch(conn);
            for (int i = 0; i<userCount; i++) {
                String email = generateEmail(i, names.get(i));
                String password = generatePassword();

                accountBatch.addAccount(email, password);
            }
            List<Long> generatedUserIds = accountBatch.execute();

            System.out.println("Adding users");
            UserBatch userBatch = new UserBatch(conn);
            for (int i = 0; i<userCount; i++) userBatch.addUser(generatedUserIds.get(i), addressIds.get(i), names.get(i));
            userBatch.execute();

            Instant endTime = Instant.now();

            System.out.println("Added " + userCount + " users in " + ChronoUnit.MILLIS.between(startTime, endTime) + "ms");

            return generatedUserIds;
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public class AccountBatch {
        private final PreparedStatement statement;

        public AccountBatch(Connection connection) throws SQLException {
            statement = connection.prepareStatement(
                    "INSERT INTO account (email, role, authentication_code) "
                            + "VALUES (?, ?, ?);",
                    Statement.RETURN_GENERATED_KEYS
            );
        }

        public void addAccount(String email, String password) throws SQLException {
            String role = "user";
            statement.setObject(1, email);
            statement.setObject(2, role);
            statement.setObject(3, password);
            statement.addBatch();
        }

        public List<Long> execute() throws SQLException {
            statement.executeBatch();

            List<Long> keys = new ArrayList<>();
            ResultSet keyResults = statement.getGeneratedKeys();
            while (keyResults.next()) keys.add(keyResults.getLong(1));
            return keys;
        }
    }

    public class UserBatch {
        private final PreparedStatement statement;

        public UserBatch(Connection connection) throws SQLException {
            statement = connection.prepareStatement(
                    "INSERT INTO user (first_name, middle_name, last_name, nickname, ph_num, dob, bio, created, userid, address_id) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            );
        }

        public void addUser(long userId, long addressId, PersonNameGenerator.FullName fullName) throws SQLException {
            statement.setObject(1, fullName.getFirstName()); //first name
            statement.setObject(2, fullName.getMiddleName()); //middle name
            statement.setObject(3, fullName.getLastName()); //last name
            statement.setObject(4, fullName.getNickname()); //nickname
            statement.setObject(5, generatePhNum()); //phone number
            statement.setObject(6, generateDOB()); //date of birth
            statement.setObject(7, BIOS[random.nextInt(BIOS.length)]); //bio
            statement.setObject(8, Instant.now()); //date created
            statement.setObject(9, userId);
            statement.setObject(10, addressId);
            statement.addBatch();
        }

        public void execute() throws SQLException {
            statement.executeBatch();
        }
    }
}