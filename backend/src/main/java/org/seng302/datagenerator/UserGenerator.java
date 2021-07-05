package org.seng302.datagenerator;

/**
 * If you are running this function from gradle generate then it will not
 */

import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.io.File;
import java.util.concurrent.TimeUnit;

import org.seng302.datagenerator.LocationGenerator.Location;

import static org.seng302.datagenerator.Main.*;

public class UserGenerator {
    private Random random = new Random();
    private Connection conn;
    private LocationGenerator locationGenerator = LocationGenerator.getInstance();

    //predefined lists
    String[] BIOS = {"I enjoy running on the weekends", "Beaches are fun", "Got to focus on my career", "If only I went to a better university", "Read documentation yeah right", "My cats keep me going", "All I need is food"};

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
     * Creates the SQL commands required to insert the user's account into the database
     * @return the id of the account entity (userid)
     */
    private long createInsertAccountSQL(String email, String password) throws SQLException {
        String role = "user";
        PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO account (email, role, authentication_code) "
                        + "VALUES (?, ?, ?);",
                Statement.RETURN_GENERATED_KEYS
        );
        stmt.setObject(1, email);
        stmt.setObject(2, role);
        stmt.setObject(3, password);
        stmt.executeUpdate();
        ResultSet keys = stmt.getGeneratedKeys();
        keys.next();
        return keys.getLong(1);
    }

    /**
     * Creates the SQL commands required to insert the user's account into the database
     */
    private void createInsertUsersSQL(long userId, long addressId, PersonNameGenerator.FullName fullName) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO user (first_name, middle_name, last_name, nickname, ph_num, dob, bio, created, userid, address_id) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
        );
        stmt.setObject(1, fullName.getFirstName()); //first name
        stmt.setObject(2, fullName.getMiddleName()); //middle name
        stmt.setObject(3, fullName.getLastName()); //last name
        stmt.setObject(4, fullName.getNickname()); //nickname
        stmt.setObject(5, generatePhNum()); //phone number
        stmt.setObject(6, generateDOB()); //date of birth
        stmt.setObject(7, BIOS[random.nextInt(BIOS.length)]); //bio
        stmt.setObject(8, Instant.now()); //date created
        stmt.setObject(9, userId);
        stmt.setObject(10, addressId);
        stmt.executeUpdate();
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
        clear();

        List<Long> generatedUserIds = new ArrayList<>();
        try {
            PersonNameGenerator personNameGenerator = PersonNameGenerator.getInstance();
            for (int i=0; i < userCount; i++) {
                PersonNameGenerator.FullName fullName = personNameGenerator.generateName();
                String email = generateEmail(i, fullName);
                String password = generatePassword();
                Location address = locationGenerator.generateAddress(random);
                long addressId = locationGenerator.createInsertAddressSQL(address, conn);

                clear();
                System.out.println(String.format("Creating User %d / %d", i+1, userCount));
                int progress = (int) (((float)(i+1) / (float)userCount) * 100);
                System.out.println(String.format("Progress: %d%%", progress));
                long userId = createInsertAccountSQL(email, password);
                createInsertUsersSQL(userId, addressId, fullName);
                generatedUserIds.add(userId);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return generatedUserIds;
    }
}