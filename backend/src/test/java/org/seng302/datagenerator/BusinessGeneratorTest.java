package org.seng302.datagenerator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

@RunWith(SpringRunner.class)
@SpringBootTest(classes={Main.class})
public class BusinessGeneratorTest {
    private Connection conn;
    private UserGenerator userGenerator;
    private BusinessGenerator businessGenerator;

    @BeforeEach
    public void setup() throws SQLException {
        //Connects to production database
        String url = "jdbc:mariadb://localhost/seng302-2021-team500-prod";
        //change password
        this.conn = DriverManager.getConnection(url, "seng302-team500", "ListenDirectly6053");

        //Creates generators
        this.userGenerator = new UserGenerator(conn);
        this.businessGenerator = new BusinessGenerator(conn);
    }

    @AfterEach
    public void teardown() throws SQLException {
        conn.close();
    }

    /**
     * Checks that the required fields within the business table are not null using an SQL query
     * @param businessId the id of the generated business
     */
    public void checkRequiredFieldsNotNull(long businessId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM business WHERE id = ? AND " +
                        "business_type IS NOT NULL AND created IS NOT NULL AND description IS NOT NULL AND " +
                        "name IS NOT NULL AND address_id IS NOT NULL AND owner_id IS NOT NULL"
        );
        stmt.setObject(1, businessId);
        stmt.executeQuery();
        ResultSet results = stmt.getResultSet();
        results.next();
        if (results.getLong(1) != 1) {
            fail();
        }
    }

    /**
     * Queries that database to find out how many businesses are in the database
     * @return the number of businesses in the database
     */
    public long getNumBusinessesInDB() throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM business");
        stmt.executeQuery();
        ResultSet results = stmt.getResultSet();
        results.next();
        return results.getLong(1);
    }

    public void deleteUsersFromDb(List<Long> userIds) throws SQLException {
        for (Long userId: userIds) {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM user WHERE userid = ?");
            stmt.setObject(1, userId);
            stmt.executeUpdate();
        }
    }

    /**
     * Deletes the generated business from the database as part of the clean up process
     * @param businessIds the ids of the generated businesses
     */
    public void deleteBusinessesFromDB(List<Long> businessIds) throws SQLException {
        for (Long businessId: businessIds) {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM business WHERE id = ?");
            stmt.setObject(1, businessId);
            stmt.executeUpdate();
        }
    }

    @Test
    void generateBusinesses_generateOneBusinessAndConsistentData_oneBusinessGenerated() throws SQLException {
        List<Long> userIds = userGenerator.generateUsers(1);
        List<Long> businessIds = businessGenerator.generateBusinesses(userIds, 1);
        if (businessIds.size() != 1) {
            fail();
        }
        long businessId = businessIds.get(0);
        checkRequiredFieldsNotNull(businessId);
        //clean up
        deleteBusinessesFromDB(businessIds);
        deleteUsersFromDb(userIds);
    }

    @Test
    void generateBusinesses_generateTwoBusinessesAndConsistentData_twoBusinessesGenerated() throws SQLException {
        List<Long> userIds = userGenerator.generateUsers(2);
        List<Long> businessIds = businessGenerator.generateBusinesses(userIds, 2);
        if (businessIds.size() != 2) {
            fail();
        }
        for (int i=0; i < businessIds.size(); i++) {
            checkRequiredFieldsNotNull(businessIds.get(i));
        }

        deleteBusinessesFromDB(businessIds);
        deleteUsersFromDb(userIds);
    }

    @Test
    void generateBusinesses_generateTenBusinessesAndConsistentData_tenBusinessesGenerated() throws SQLException {
        List<Long> userIds = userGenerator.generateUsers(10);
        List<Long> businessIds = businessGenerator.generateBusinesses(userIds, 10);
        if (businessIds.size() != 10) {
            fail();
        }
        for (int i=0; i < businessIds.size(); i++) {
            checkRequiredFieldsNotNull(businessIds.get(i));
        }

        deleteBusinessesFromDB(businessIds);
        deleteUsersFromDb(userIds);
    }

    @Test
    void generateBusinesses_generateHundredBusinessesAndConsistentData_hundredBusinessesGenerated() throws SQLException {
        List<Long> userIds = userGenerator.generateUsers(100);
        List<Long> businessIds = businessGenerator.generateBusinesses(userIds, 100);
        if (businessIds.size() != 100) {
            fail();
        }
        for (int i=0; i < businessIds.size(); i++) {
            checkRequiredFieldsNotNull(businessIds.get(i));
        }

        deleteBusinessesFromDB(businessIds);
        deleteUsersFromDb(userIds);
    }

    @Test
    void generateBusinesses_generateZeroBusinesses_noBusinessesGenerated() throws SQLException {
        List<Long> userIds = userGenerator.generateUsers(1);
        long businessesInDB = getNumBusinessesInDB();
        List<Long> businessIds = businessGenerator.generateBusinesses(userIds, 0);
        long businessesInDBAfter = getNumBusinessesInDB();
        if (businessesInDB != businessesInDBAfter) {
            fail();
        }

        deleteUsersFromDb(userIds);
    }

    @Test
    void generateBusinesses_generateNegativeOneBusinesses_noBusinessesGenerated() throws SQLException {
        List<Long> userIds = userGenerator.generateUsers(1);
        long businessesInDB = getNumBusinessesInDB();
        List<Long> businessIds = businessGenerator.generateBusinesses(userIds, -1);
        long businessesInDBAfter = getNumBusinessesInDB();
        if (businessesInDB != businessesInDBAfter) {
            fail();
        }

        deleteUsersFromDb(userIds);
    }

    @Test
    void generateBusinesses_generateNegativeTenBusinesses_noBusinessesGenerated() throws SQLException {
        List<Long> userIds = userGenerator.generateUsers(1);
        long businessesInDB = getNumBusinessesInDB();
        List<Long> businessIds = businessGenerator.generateBusinesses(userIds, -10);
        long businessesInDBAfter = getNumBusinessesInDB();
        if (businessesInDB != businessesInDBAfter) {
            fail();
        }

        deleteUsersFromDb(userIds);
    }
}
