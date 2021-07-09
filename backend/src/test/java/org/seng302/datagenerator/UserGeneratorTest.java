package org.seng302.datagenerator;

import org.junit.jupiter.api.*;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

@RunWith(SpringRunner.class)
@SpringBootTest(classes={Main.class})
public class UserGeneratorTest {
    private Connection conn;
    private UserGenerator generator;

    @BeforeEach
    public void setup() {
        try {
            //Connects to production database
            String url = "jdbc:mariadb://localhost/seng302-2021-team500-prod";
            //change password
            this.conn = DriverManager.getConnection(url, "seng302-team500", "ListenDirectly6053");

            //Creates generator
            this.generator = new UserGenerator(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

    @Test
    void generateUsers_generateOneUserAndConsistentData_oneUserGenerated() {
        List<Long> userIds = generator.generateUsers(1);
        if (userIds.size() != 1) {
            fail();
        }
        long userId = userIds.get(0);
        try {
            checkRequiredFieldsNotNull(userId);
        } catch (SQLException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void generateUsers_generateTwoUsersConsistentData_twoUsersGenerated() {
        List<Long> userIds = generator.generateUsers(2);
        if (userIds.size() != 2) {
            fail();
        }
        try {
            for (int i=0; i < userIds.size(); i++) {
                checkRequiredFieldsNotNull(userIds.get(i));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            fail();
        }
        //How to check if data is consistent?
    }

    @Test
    void generateUsers_generateTenUsersConsistentData_tenUsersGenerated() {
        List<Long> userIds = generator.generateUsers(10);
        if (userIds.size() != 10) {
            fail();
        }
        try {
            for (int i=0; i < userIds.size(); i++) {
                checkRequiredFieldsNotNull(userIds.get(i));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void generateUsers_generateHundredUsersConsistentData_hundredUsersGenerated() {
        List<Long> userIds = generator.generateUsers(100);
        if (userIds.size() != 100) {
            fail();
        }
        try {
            for (int i=0; i < userIds.size(); i++) {
                checkRequiredFieldsNotNull(userIds.get(i));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void generateUsers_generateZeroUsers_noUsersGeneratedRepeatPrompt() {
        try {
            long usersInDB = getNumUsersInDB();
            generator.generateUsers(0);
            long usersInDBAfter = getNumUsersInDB();
            if (usersInDB != usersInDBAfter) {
                fail();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void generateUsers_generateNegativeOneUsers_noUsersGeneratedRepeatPrompt() {
        try {
            long usersInDB = getNumUsersInDB();
            generator.generateUsers(-1);
            long usersInDBAfter = getNumUsersInDB();
            if (usersInDB != usersInDBAfter) {
                fail();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void generateUsers_generateNegativeTenUsers_noUsersGeneratedRepeatPrompt() {
        try {
            long usersInDB = getNumUsersInDB();
            generator.generateUsers(-10);
            long usersInDBAfter = getNumUsersInDB();
            if (usersInDB != usersInDBAfter) {
                fail();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            fail();
        }
    }










}
