package org.seng302.datagenerator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.leftovers.entities.Business;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.seng302.datagenerator.Main.*;

public class BusinessGenerator {
    private Random random = new Random();
    private Connection conn;
    private final LocationGenerator locationGenerator = LocationGenerator.getInstance();
    private final DescriptionGenerator descriptionGenerator = DescriptionGenerator.getInstance();
    private final CommerceNameGenerator commerceNameGenerator = CommerceNameGenerator.getInstance();
    private final Logger logger = LogManager.getLogger(BusinessGenerator.class.getName());

    public BusinessGenerator(Connection conn) { this.conn = conn; }

    /**
     * Creates and inserts the business into the database
     * @param addressId the id associated with the location entity representing the business's address
     * @param ownerId the id associated with the user entity representing the user who owns the business
     * @return the if of the business that was generated
     */
    private long createInsertBusinessSQL(long addressId, long ownerId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO business (business_type, created, description, name, address_id, owner_id)" +
                            "VALUES (?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            )) {
            stmt.setObject(1, Business.getBusinessTypes().get(random.nextInt(Business.getBusinessTypes().size())));
            stmt.setObject(2, Instant.now()); //date created
            stmt.setObject(3, descriptionGenerator.randomDescription());
            stmt.setObject(4, commerceNameGenerator.randomBusinessName());
            stmt.setObject(5, addressId);
            stmt.setObject(6, ownerId);
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            keys.next();
            return keys.getLong(1);
        }
    }

    /**
     * Inserts the admin of the business into the database
     * @param businessId the id associated with the business
     * @param adminId the id associated with the user who is an administrator of the business
     */
    private void addAdminToBusiness(long businessId, long adminId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO business_admins (business_id, user_id)" +
                            "VALUES (?, ?)"
            )) {
            stmt.setObject(1, businessId);
            stmt.setObject(2, adminId);
            stmt.executeUpdate();
        }
    }

    /**
     * Main program
     * @param args no arguments should be provided
     */
    public static void main(String[] args) throws SQLException, InterruptedException {
        Connection conn = connectToDatabase();
        var userGenerator = new UserGenerator(conn);
        var businessGenerator = new BusinessGenerator(conn);

        int userCount = getNumObjectsFromInput("users");
        List<Long> userIds = userGenerator.generateUsers(userCount);

        int businessCount = getNumObjectsFromInput("businesses");
        businessGenerator.generateBusinesses(userIds, businessCount);
    }

    /**
     * Generates the businesses
     * @param userIds Users to select owners and admins from
     * @param businessCount Number of businesses to generate
     * @return List of generated business ids
     */
    public List<Long> generateBusinesses(List<Long> userIds, int businessCount) {
        List<Long> generatedBusinessIds = new ArrayList<>();
        try {
            for (int i=0; i < businessCount; i++) {
                clear();
                long ownerId = userIds.get(random.nextInt(userIds.size()));

                LocationGenerator.Location businessLocation = locationGenerator.generateAddress(random);
                long addressId = locationGenerator.createInsertAddressSQL(businessLocation, conn);

                if (i % 10 == 0) {
                    logger.info("Creating Business {} / {}", i + 1, businessCount);
                    int progress = (int) (((float) (i + 1) / (float) businessCount) * 100);
                    logger.info("Progress: {}%", progress);
                }
                long businessId = createInsertBusinessSQL(addressId, ownerId);

                //check if an admin needs to be added to the business
                if (random.nextBoolean() && userIds.size() > 1) {
                    long adminId = ownerId;
                    while (adminId == ownerId) {
                        adminId = userIds.get(random.nextInt(userIds.size()));
                    }
                    addAdminToBusiness(businessId, adminId);
                }

                generatedBusinessIds.add(businessId);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return generatedBusinessIds;
    }
}
