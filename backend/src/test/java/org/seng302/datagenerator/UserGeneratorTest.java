package org.seng302.datagenerator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;
import org.seng302.leftovers.Main;
import org.seng302.leftovers.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.*;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@RunWith(SpringRunner.class)
@SpringBootTest(classes={Main.class})
class UserGeneratorTest {
    private Connection conn;
    private UserGenerator userGenerator;

    @Autowired
    private UserRepository userRepository;

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
        userRepository.deleteAll();
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
        assertEquals(1, results.getLong(1));
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

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 10, 100})
    void generateUsers_generateSomeUsersConsistentData_validAndExpectedNumberOfUsersGenerated(int count) throws SQLException {
        List<Long> userIds = userGenerator.generateUsers(count);
        assertEquals(count, userIds.size());
        for (Long userId : userIds) {
            checkRequiredFieldsNotNull(userId);
        }
    }

    @Test
    void generateUsers_generateZeroUsers_noUsersGeneratedRepeatPrompt() throws SQLException {
        long usersInDB = getNumUsersInDB();
        userGenerator.generateUsers(0);
        long usersInDBAfter = getNumUsersInDB();
        assertEquals(usersInDB, usersInDBAfter);
    }

    @Test
    void generateUsers_generateNegativeOneUsers_noUsersGeneratedRepeatPrompt() throws SQLException {
        long usersInDB = getNumUsersInDB();
        userGenerator.generateUsers(-1);
        long usersInDBAfter = getNumUsersInDB();
        assertEquals(usersInDB, usersInDBAfter);
    }

    @Test
    void generateUsers_generateNegativeTenUsers_noUsersGeneratedRepeatPrompt() throws SQLException {
        long usersInDB = getNumUsersInDB();
        userGenerator.generateUsers(-10);
        long usersInDBAfter = getNumUsersInDB();
        assertEquals(usersInDB, usersInDBAfter);
    }










}
