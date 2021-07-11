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
public class InventoryItemGeneratorTest {
    private Connection conn;
    private InventoryItemGenerator invItemGenerator;

    @BeforeEach
    public void setup() throws SQLException {
        //Connects to production database
        String url = "jdbc:mariadb://" + System.getenv("S302T500-DB-ADDRESS");
        Connection conn = DriverManager.getConnection(url, System.getenv("S302T500-DB-USERNAME"), System.getenv("S302T500-DB-PASSWORD"));

        //Creates generators
        this.invItemGenerator = new InventoryItemGenerator(conn);
    }

    @AfterEach
    public void teardown() throws SQLException {
        conn.close();
    }

    /**
     * Checks that the required fields within the inventory item table are not null using an SQL query.
     * I have included all the fields including ones that are not required as the inventory item generator
     * fills in every field.
     * @param invItemId the id of the generated inventory item
     */
    public void checkRequiredFieldsNotNull(long invItemId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM inventory_item WHERE id = ? AND " +
                        "best_before IS NOT NULL AND creation_date IS NOT NULL AND expires IS NOT NULL AND " +
                        "manufactured IS NOT NULL AND price_per_item IS NOT NULL AND quantity IS NOT NULL AND " +
                        "remaining_quantity IS NOT NULL AND sell_by IS NOT NULL AND total_price IS NOT NULL AND " +
                        "version IS NOT NULL AND product_id IS NOT NULL"
        );
        stmt.setObject(1, invItemId);
        stmt.executeQuery();
        ResultSet results = stmt.getResultSet();
        results.next();
        if (results.getLong(1) != 1) {
            fail();
        }
    }

    /**
     * Queries that database to find out how many inventory items are in the database
     * @return the number of inventory items in the database
     */
    public long getNumInvItemsInDB() throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM inventory_item");
        stmt.executeQuery();
        ResultSet results = stmt.getResultSet   ();
        results.next();
        return results.getLong(1);
    }


    //Does not delete created users, businesses, and products
    /**
     * Deletes the inventory item associated with the given id within the database. This is part of the clean up.
     * @param invItemIds the ids of the generated inventory items
     */
    public void deleteInvItemsFromDB(List<Long> invItemIds) throws SQLException {
        for (Long invItemId: invItemIds) {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM inventory_item WHERE id = ?");
            stmt.setObject(1, invItemId);
            stmt.executeUpdate();
        }
    }

    @Test
    void generateInvItems_generateOneInvItemAndConsistentData_oneInvItemGenerated() throws SQLException {
        List<Long> invItemIds = invItemGenerator.generateInventoryItems(1);
        if (invItemIds.size() != 1) {
            fail();
        }
        long invItemId = invItemIds.get(0);
        checkRequiredFieldsNotNull(invItemId);
        deleteInvItemsFromDB(invItemIds);
    }

    @Test
    void generateInvItems_generateTwoInvItemsAndConsistentData_twoInvItemsGenerated() throws SQLException {
        List<Long> invItemIds = invItemGenerator.generateInventoryItems(2);
        if (invItemIds.size() != 2) {
            fail();
        }
        for (long invItemId: invItemIds) {
            checkRequiredFieldsNotNull(invItemId);
        }
        deleteInvItemsFromDB(invItemIds);
    }

    @Test
    void generateInvItems_generateTenInvItemsAndConsistentData_tenInvItemsGenerated() throws SQLException {
        List<Long> invItemIds = invItemGenerator.generateInventoryItems(10);
        if (invItemIds.size() != 10) {
            fail();
        }
        for (long invItemId: invItemIds) {
            checkRequiredFieldsNotNull(invItemId);
        }
        deleteInvItemsFromDB(invItemIds);
    }

    @Test
    void generateInvItems_generateHundredInvItemsAndConsistentData_hundredInvItemsGenerated() throws SQLException {
        List<Long> invItemIds = invItemGenerator.generateInventoryItems(100);
        if (invItemIds.size() != 100) {
            fail();
        }
        for (long invItemId: invItemIds) {
            checkRequiredFieldsNotNull(invItemId);
        }
        deleteInvItemsFromDB(invItemIds);
    }

    @Test
    void generateInvItems_generateZeroInvItemsAndConsistentData_NoInvItemGenerated() throws SQLException {
        long invItemsInDB = getNumInvItemsInDB();
        List<Long> invItemIds = invItemGenerator.generateInventoryItems(0);
        long invItemsInDBAfter = getNumInvItemsInDB();
        if (invItemsInDB != invItemsInDBAfter) {
            fail();
        }
    }

    @Test
    void generateInvItems_generateNegativeOneInvItemsAndConsistentData_NoInvItemGenerated() throws SQLException {
        long invItemsInDB = getNumInvItemsInDB();
        List<Long> invItemIds = invItemGenerator.generateInventoryItems(-1);
        long invItemsInDBAfter = getNumInvItemsInDB();
        if (invItemsInDB != invItemsInDBAfter) {
            fail();
        }
    }

    @Test
    void generateInvItems_generateNegativeTenInvItemsAndConsistentData_NoInvItemGenerated() throws SQLException {
        long invItemsInDB = getNumInvItemsInDB();
        List<Long> invItemIds = invItemGenerator.generateInventoryItems(-10);
        long invItemsInDBAfter = getNumInvItemsInDB();
        if (invItemsInDB != invItemsInDBAfter) {
            fail();
        }
    }
    //TODO Add verification of date data
}
