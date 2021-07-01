package org.seng302.datagenerator;

/**
 * If you are running this function from gradle generate then it will not
 */

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Random;
import java.io.File;
import java.util.concurrent.TimeUnit;

import static org.seng302.datagenerator.Main.*;

public class UserGenerator {
    private Random random = new Random();
    private Connection conn;
    static Scanner scanner = new Scanner(System.in);

    public ArrayList<Long> userIds = new ArrayList<Long>();
    public long addressId;

    //predefined lists
    String[] BIOS = {"I enjoy running on the weekends", "Beaches are fun", "Got to focus on my career", "If only I went to a better university", "Read documentation yeah right", "My cats keep me going", "All I need is food"};

    //predefined list of location elements
    String[] STREETNAMES = {"Hillary Cresenct", "Elizabeth Street", "Alice Avenue", "Racheal Road", "Peveral Street", "Moorhouse Avenue", "Riccarton Road", "Clyde Road", "Angelic Avenue"};
    String[] CITIES = {"Dunedin", "Nightcaps", "Gore", "Tapanui", "Wellington", "Christchurch", "Auckland", "Melbourne", "Brisbance", "Sydeny", "Perth", "Darwin", "Alice Springs"};
    String[] REGIONS = {"Otago", "Southland", "Canterbury", "Victoria", "Tasman", "Upper Hutt"};
    String[] COUNTRIES = {"New Zealand", "Zealand", "Australia", "England", "United Kingdom", "Japan", "Korea", "Singapore", "France", "Germany", "Norway"};
    String[] DISTRICTS = {"Alpha", "Beta", "Charlie", "Delta", "Echo", "Foxtrot"};

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
        String year = String.valueOf(random.nextInt(9998) + 1); // +1 as the year cannot be zero
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
     * Randomly generates the address of the user
     * @return the elements of a location object (user's address) in an array
     */
    private String[] generateAddress() {
        String streetNum = String.valueOf(random.nextInt(998) + 1);
        String streetName = STREETNAMES[random.nextInt(STREETNAMES.length)];
        String city = CITIES[random.nextInt(CITIES.length)];
        String region = REGIONS[random.nextInt(REGIONS.length)];
        String country = COUNTRIES[random.nextInt(COUNTRIES.length)];
        String postcode = String.valueOf(random.nextInt(98999) + 1000);
        String district = DISTRICTS[random.nextInt(DISTRICTS.length)];
        String[] address = {streetNum, streetName, city, region, country, postcode, district};
        return address;
    }

    /**
     * Creates the SQL commands required to insert the user's address into the database
     * @return the id of the location entity (addressid)
     */
    private long createInsertAddressSQL(String[] address) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO location (street_number, street_name, city, region, country, post_code, district) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?);",
                Statement.RETURN_GENERATED_KEYS
        );
        for (int i=0; i<7; i++) {
            stmt.setObject(i+1, address[i]);
        }
        stmt.executeUpdate();
        ResultSet keys = stmt.getGeneratedKeys();
        keys.next();
        return keys.getLong(1);
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
        stmt.setObject(10, this.addressId);
        stmt.executeUpdate();
    }

    /**
     * Asks the user how many users they want generated
     * @return the number of users to be generated
     */
    private int getUsersFromInput() throws InterruptedException {
        int users = 0; //Change users
        while (users <= 0) {
            clear();
            try {
                System.out.println("------------------------------------");
                System.out.println("How many users do you want generated");
                System.out.println("and put into the database?");
                System.out.println("------------------------------------");
                users = Integer.parseInt(scanner.nextLine());
            } catch (NoSuchElementException e) {
                System.out.println("You are using the gradle generate function");
                System.out.println("This console does not support scanner inputs");
                System.out.println("To input your own number of users");
                System.out.println("Compile and run this java file in a local terminal");
                System.out.println("10 users will be creating in...");
                for (int i=5; i>0; i--) {
                    TimeUnit.SECONDS.sleep(1);
                    System.out.println(i);
                }
                users = 10;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number! (above 0)");
            }
        }
        return users;
    }

    /**
     * Main program
     * @param args no arguments should be provided
     */
    public static void main(String[] args) throws SQLException, InterruptedException {
        Connection conn = connectToDatabase();
        var generator = new UserGenerator(conn);
        generator.generateUsers(null);
    }

    /**
     * Generates the users
     */
    public void generateUsers(Integer users) throws InterruptedException {
        if (users == null) {
            users = getUsersFromInput();
        }
        clear();

        try {
            String[] address = generateAddress();
            PersonNameGenerator personNameGenerator = PersonNameGenerator.getInstance();
            this.addressId = createInsertAddressSQL(address);

            for (int i=0; i < users; i++) {
                PersonNameGenerator.FullName fullName = personNameGenerator.generateName();
                String email = generateEmail(i, fullName);
                String password = generatePassword();

                clear();
                System.out.println(String.format("Creating User %d / %d", i+1, users));
                int progress = (int) (((float)(i+1) / (float)users) * 100);
                System.out.println(String.format("Progress: %d%%", progress));
                long userId = createInsertAccountSQL(email, password);
                createInsertUsersSQL(userId, fullName);
                this.userIds.add(userId);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        scanner.close();
    }

    public long getAddressId() {
        return this.addressId;
    }

    public ArrayList<Long> getUserIds() {
        return this.userIds;
    }
}