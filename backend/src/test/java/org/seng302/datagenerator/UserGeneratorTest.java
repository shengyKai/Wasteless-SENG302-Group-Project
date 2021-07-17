package org.seng302.datagenerator;
import org.seng302.leftovers.Main;

import org.junit.jupiter.api.*;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.*;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;

@RunWith(SpringRunner.class)
@SpringBootTest(classes={Main.class})
public class UserGeneratorTest {
    private Connection conn;
    private UserGenerator userGenerator;

    @BeforeEach
    public void setup() throws SQLException {
        Map<String, String> properties = ExampleDataFileReader.readPropertiesFile("/application.properties");
        if (properties.get("spring.datasource.url") == null || properties.get("spring.datasource.username") == null || properties.get("spring.datasource.password") == null) {
            fail("The url/username/password is not found");
        }
        this.conn =  DriverManager.getConnection(properties.get("spring.datasource.url"), properties.get("spring.datasource.username"), properties.get("spring.datasource.password"));

        //Creates userGenerators
        this.userGenerator = new UserGenerator(conn);
    }

    @AfterEach
    public void teardown() throws SQLException {
        conn.close();
    }

    /**
     * Checks that the required fields within the user table are not null through SQL queries
     * @param userId the id of the user that was generated
     */
    public void checkRequiredFieldsNotNull(long userId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM user WHERE userid = ? AND "
                + "created IS NOT NULL AND dob IS NOT NULL AND first_name IS NOT NULL AND last_name IS NOT NULL AND " +
                        "address_id IS NOT NULL"
        );
        stmt.setObject(1, userId);
        stmt.executeQuery();
        ResultSet results = stmt.getResultSet();
        results.next();
        if (results.getLong(1) != 1) {
            fail();
        }
    }

    /**
     * Queries the database to find out how many users are in the database
     * @return the number of users in the database
     */
    public long getNumUsersInDB() throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM user;");
        stmt.executeQuery();
        ResultSet results = stmt.getResultSet();
        results.next();
        return results.getLong(1);
    }

    /**
     * Deletes the generated users from the database as part of the clean up process
     * @param userIds the ids of the generated users
     */
    public void deleteUsersFromDB(List<Long> userIds) throws SQLException {
        for (Long userId: userIds) {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM user WHERE userid = ?");
            stmt.setObject(1, userId);
            stmt.executeUpdate();
        }
    }

    @Test
    void generateUsers_generateOneUserAndConsistentData_oneUserGenerated() throws SQLException {
        List<Long> userIds = userGenerator.generateUsers(1);
        if (userIds.size() != 1) {
            fail();
        }
        long userId = userIds.get(0);
        checkRequiredFieldsNotNull(userId);


    }

    @Test
    void generateUsers_generateTwoUsersConsistentData_twoUsersGenerated() throws SQLException {
        List<Long> userIds = userGenerator.generateUsers(2);
        if (userIds.size() != 2) {
            fail();
        }
        for (int i=0; i < userIds.size(); i++) {
            checkRequiredFieldsNotNull(userIds.get(i));
        }
        //How to check if data is consistent?

        deleteUsersFromDB(userIds);
    }

    @Test
    void generateUsers_generateTenUsersConsistentData_tenUsersGenerated() throws SQLException {
        List<Long> userIds = userGenerator.generateUsers(10);
        if (userIds.size() != 10) {
            fail();
        }
        for (int i=0; i < userIds.size(); i++) {
            checkRequiredFieldsNotNull(userIds.get(i));
        }

        deleteUsersFromDB(userIds);
    }

    @Test
    void generateUsers_generateHundredUsersConsistentData_hundredUsersGenerated() throws SQLException {
        List<Long> userIds = userGenerator.generateUsers(100);
        if (userIds.size() != 100) {
            fail();
        }
        for (int i=0; i < userIds.size(); i++) {
            checkRequiredFieldsNotNull(userIds.get(i));
        }

        deleteUsersFromDB(userIds);
    }

    @Test
    void generateUsers_generateZeroUsers_noUsersGeneratedRepeatPrompt() throws SQLException {
        long usersInDB = getNumUsersInDB();
        userGenerator.generateUsers(0);
        long usersInDBAfter = getNumUsersInDB();
        if (usersInDB != usersInDBAfter) {
            fail();
        }
    }

    @Test
    void generateUsers_generateNegativeOneUsers_noUsersGeneratedRepeatPrompt() throws SQLException {
        long usersInDB = getNumUsersInDB();
        userGenerator.generateUsers(-1);
        long usersInDBAfter = getNumUsersInDB();
        if (usersInDB != usersInDBAfter) {
            fail();
        }
    }

    @Test
    void generateUsers_generateNegativeTenUsers_noUsersGeneratedRepeatPrompt() throws SQLException {
        long usersInDB = getNumUsersInDB();
        userGenerator.generateUsers(-10);
        long usersInDBAfter = getNumUsersInDB();
        if (usersInDB != usersInDBAfter) {
            fail();
        }
    }










}
