package org.seng302.datagenerator;

/**
 * If you are running this function from gradle generate then it will not
 */

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.datagenerator.LocationGenerator.Location;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.seng302.datagenerator.Main.randomDate;

public class UserGenerator {
    private Random random = new Random();
    private Connection conn;
    private LocationGenerator locationGenerator = LocationGenerator.getInstance();
    private PersonNameGenerator personNameGenerator = PersonNameGenerator.getInstance();
    private final DescriptionGenerator descriptionGenerator = DescriptionGenerator.getInstance();
    private final Logger logger = LogManager.getLogger(UserGenerator.class.getName());

    public UserGenerator(Connection conn) {
        this.conn = conn;
    }

    /**
     * Randomly generates the hashed password
     * @return
     */
    private String generatePassword() {
        return "ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f";
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
     * Generates the users
     * @param userCount Number of users to generate
     * @return List of generated user ids
     */
    public List<Long> generateUsers(int userCount) {
        Instant startTime = Instant.now();
        List<PersonNameGenerator.FullName> names = new ArrayList<>();
        for (int i = 0; i < userCount; i++) names.add(personNameGenerator.generateName());

        try {
            logger.info("Adding addresses");
            List<Location> addresses = new ArrayList<>();
            for (int i = 0; i < userCount; i++) addresses.add(locationGenerator.generateAddress(random));
            List<Long> addressIds = locationGenerator.createInsertAddressSQL(addresses, conn);

            logger.info("Adding accounts");
            AccountBatch accountBatch = new AccountBatch(conn);
            for (int i = 0; i<userCount; i++) {
                String email = generateEmail(i, names.get(i));
                String password = generatePassword();

                accountBatch.addAccount(email, password);
            }
            List<Long> generatedUserIds = accountBatch.execute();

            logger.info("Adding users");
            UserBatch userBatch = new UserBatch(conn);
            for (int i = 0; i<userCount; i++) userBatch.addUser(generatedUserIds.get(i), addressIds.get(i), names.get(i));
            userBatch.execute();

            Instant endTime = Instant.now();

            logger.info("Added {} users in {} ms", userCount, ChronoUnit.MILLIS.between(startTime, endTime));

            return generatedUserIds;
        } catch (Exception e) {
            logger.error(e.getStackTrace());
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

        /**
         * Randomly generates the date of birth of the user
         * @return the user's date of birth
         */
        private LocalDate generateDOB() {
            return randomDate(LocalDate.of(1900, 1, 1), LocalDate.of(2006, 1, 1));
        }

        /**
         * Randomly generates an account creation date for the user.
         * @return the account's creation date.
         */
        private LocalDate generateCreated() {
            return randomDate(LocalDate.of(2021, 1, 1), LocalDate.now());
        }

        /**
         * Randomly generates the phone number of the user
         * @return the user's phone number
         */
        private String generatePhNum() {
            return "027 " + (random.nextInt(8999999) + 1000000);
        }

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
            statement.setObject(7, descriptionGenerator.randomDescription()); //bio
            statement.setObject(8, generateCreated()); //date created
            statement.setObject(9, userId);
            statement.setObject(10, addressId);
            statement.addBatch();
        }

        public void execute() throws SQLException {
            statement.executeBatch();
        }
    }
}