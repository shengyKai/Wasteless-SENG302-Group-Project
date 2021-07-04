package org.seng302.datagenerator;

import org.seng302.leftovers.entities.Location;

import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.seng302.datagenerator.Main.*;

public class BusinessGenerator {

    private Random random = new Random();
    private Connection conn;
    private LocationGenerator locationGenerator = LocationGenerator.getInstance();

    //predefined lists
    String[] BUSINESSTYPES = {"Accommodation and Food Services", "Retail Trade", "Charitable organisation", "Non-profit organisation"};
    String[] DESCRIPTIONS = {"This is a Japanese restaurant, the best Ramen and Sake.", "We are non-profit organisation focused on bringing New Zealand's extreme housing unaffordability down to a managable unaffordable housing market.",
    "We are a non-profit focused on making sure all SENG302 students get enough sleep"};
    String[] NAMES = {"Japan Food", "Sleep Saviour", "Ed Sheeran Church", "Unaffordable Housing"};

    public BusinessGenerator(Connection conn) { this.conn = conn; }

    /**
     * Creates and inserts the buiness into the database
     * @param addressId the id associated with the location entity representing the business's address
     * @param ownerId the id associated with the user entity representing the user who owns the business
     * @return the if of the business that was generated
     */
    private long createInsertBusinessSQL(long addressId, long ownerId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
            "INSERT INTO business (business_type, created, description, name, address_id, owner_id)"
                + "VALUES (?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
        );
        stmt.setObject(1, BUSINESSTYPES[random.nextInt(BUSINESSTYPES.length)]); //business type
        stmt.setObject(2, Instant.now()); //date created
        stmt.setObject(3, DESCRIPTIONS[random.nextInt(DESCRIPTIONS.length)]); //description
        stmt.setObject(4, NAMES[random.nextInt(NAMES.length)]);
        stmt.setObject(5, addressId);
        stmt.setObject(6, ownerId);
        stmt.executeUpdate();
        ResultSet keys = stmt.getGeneratedKeys();
        keys.next();
        return keys.getLong(1);
    }

    /**
     * Inserts the admin of the business into the database
     * @param businessId the id associated with the business
     * @param adminId the id associated with the user who is an administrator of the business
     */
    private void addAdminToBusiness(long businessId, long adminId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO business_admins (business_id, user_id)"
                + "VALUES (?, ?)"
        );
        stmt.setObject(1, businessId);
        stmt.setObject(2, adminId);
        stmt.executeUpdate();
    }

    /**
     * Main program
     * @param args no arguments should be provided
     */
    public static void main(String[] args) throws SQLException, InterruptedException {
        Connection conn = connectToDatabase();
        var generator = new BusinessGenerator(conn);

        int businessCount = getNumObjectsFromInput("businesses");
        generator.generateBusinesses(businessCount);
    }

    /**
     * Generates the businesses
     * @param businessCount Number of businesses to generate
     * @return List of generated business ids
     */
    public List<Long> generateBusinesses(int businessCount) throws InterruptedException {
        var userGenerator = new UserGenerator(conn);
        List<Long> generatedBusinessIds = new ArrayList<>();
        try {
            for (int i=0; i < businessCount; i++) {
                clear();
                int usersGenerated = random.nextInt(2) + 1; // between 1 and 2 users will be generated
                //if two users are generated, the second is a business admin
                List<Long> userIds = userGenerator.generateUsers(usersGenerated);
                long ownerId = userIds.get(0);

                LocationGenerator.Location businessLocation = locationGenerator.generateAddress(random);
                long addressId = locationGenerator.createInsertAddressSQL(businessLocation, conn);

                System.out.println(String.format("Creating Business %d / %d", i+1, businessCount));
                int progress = (int) (((float)(i+1) / (float)businessCount) * 100);
                System.out.println(String.format("Progress: %d%%", progress));
                long businessId = createInsertBusinessSQL(addressId, ownerId);

                //check if an admin needs to be added to the business
                if (userIds.size() == 2) {
                    long adminId = userIds.get(1);
                    addAdminToBusiness(businessId, adminId);
                }

                generatedBusinessIds.add(businessId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return generatedBusinessIds;
    }
}
