package org.seng302.datagenerator;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import static org.seng302.datagenerator.Main.clear;
import static org.seng302.datagenerator.Main.connectToDatabase;

public class BusinessGenerator {
    private Random random = new Random();
    private Connection conn;
    static Scanner scanner = new Scanner(System.in);

    //predefined lists
    String[] BUSINESSTYPES = {"Accommodation and Food Services", "Retail Trade", "Charitable organisation", "Non-profit organisation"};
    String[] DESCRIPTIONS = {"This is a Japanese restaurant, the best Ramen and Sake.", "We are non-profit organisation focused on bringing New Zealand's extreme housing unaffordability down to a managable unaffordable housing market.",
    "We are a non-profit focused on making sure all SENG302 students get enough sleep"};
    String[] NAMES = {"Japan Food", "Sleep Saviour", "Ed Sheeran Church", "Unaffordable Housing"};

    public long businessId;

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
     * @param adminId the id associated with the user who is an administrator of the business
     */
    private void addAdminToBusiness(long adminId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO business_admins (business_id, user_id)"
                + "VALUES (?, ?)"
        );
        stmt.setObject(1, this.businessId);
        stmt.setObject(2, adminId);
        stmt.executeUpdate();
    }

    /**
     * Asks the user how many businesses they want generated
     * @return the number of businesses to be generated
     */
    private int getBusinessesFromInput() throws InterruptedException {
        int businesses = 0; //Change businesses
        while (businesses <= 0) {
            clear();
            try {
                System.out.println("-----------------------------------------");
                System.out.println("How many businesses do you want generated");
                System.out.println("and put into the database?");
                System.out.println("----------------------------------------");
                businesses = Integer.parseInt(scanner.nextLine());
            } catch (NoSuchElementException e) {
                System.out.println("You are using the gradle generate function");
                System.out.println("This console does not support scanner inputs");
                System.out.println("To input your own number of users");
                System.out.println("Compile and run this java file in a local terminal");
                System.out.println("10 businesses will be creating in...");
                for (int i=5; i>0; i--) {
                    TimeUnit.SECONDS.sleep(1);
                    System.out.println(i);
                }
                businesses = 10;
            } catch (Exception e) {
                System.out.println("Please enter a number! (above 0)");
            }
        }
        return businesses;
    }

    /**
     * Main program
     * @param args no arguments should be provided
     */
    public static void main(String[] args) throws SQLException, InterruptedException {
        Connection conn = connectToDatabase();
        var generator = new BusinessGenerator(conn);
        generator.generateBusinesses();
    }

    /**
     * Generates the businesses
     */
    private void generateBusinesses() throws InterruptedException {
        int businesses = getBusinessesFromInput();
        var userGenerator = new UserGenerator(conn);

        try {
            for (int i=0; i < businesses; i++) {
                clear();
                int usersGenerated = random.nextInt(1) + 1; // between 1 and 2 users will be generated
                //if two users are generated, the second is a business admin
                userGenerator.generateUsers(usersGenerated);
                ArrayList<Long> userIds = userGenerator.getUserIds();
                long ownerId = userIds.get(0);
                long addressId = userGenerator.getAddressId();
                System.out.println(String.format("Creating Business %d / %d", i+1, businesses));
                int progress = (int) (((float)(i+1) / (float)businesses) * 100);
                System.out.println(String.format("Progress: %d%%", progress));
                this.businessId = createInsertBusinessSQL(addressId, ownerId);

                //check if an admin needs to be added to the business
                if (userIds.size() == 2) {
                    long adminId = userIds.get(1);
                    addAdminToBusiness(adminId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        scanner.close();
    }

    public long getBusinessId() { return this.businessId; }
}
