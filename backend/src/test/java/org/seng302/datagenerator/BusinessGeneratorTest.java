package org.seng302.datagenerator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.seng302.leftovers.Main;
import org.seng302.leftovers.persistence.BusinessRepository;
import org.seng302.leftovers.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.*;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;

@RunWith(SpringRunner.class)
@SpringBootTest(classes={Main.class})
public class BusinessGeneratorTest {
    private Connection conn;
    private UserGenerator userGenerator;
    private BusinessGenerator businessGenerator;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BusinessRepository businessRepository;

    @BeforeEach
    public void setup() throws SQLException {
        Map<String, String> properties = ExampleDataFileReader.readPropertiesFile("/application.properties");
        if (properties.get("spring.datasource.url") == null || properties.get("spring.datasource.username") == null || properties.get("spring.datasource.password") == null) {
            fail("The url/username/password is not found");
        }
        this.conn =  DriverManager.getConnection(properties.get("spring.datasource.url"), properties.get("spring.datasource.username"), properties.get("spring.datasource.password"));

        //Creates Generators
        this.userGenerator = new UserGenerator(conn);
        this.businessGenerator = new BusinessGenerator(conn);
    }

    @AfterEach
    public void teardown() throws SQLException {
        businessRepository.deleteAll();
        userRepository.deleteAll();
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

    @Test
    void generateBusinesses_generateOneBusinessAndConsistentData_oneBusinessGenerated() throws SQLException {
        List<Long> userIds = userGenerator.generateUsers(1);
        List<Long> businessIds = businessGenerator.generateBusinesses(userIds, 1);
        if (businessIds.size() != 1) {
            fail();
        }
        long businessId = businessIds.get(0);
        checkRequiredFieldsNotNull(businessId);
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
    }
}
