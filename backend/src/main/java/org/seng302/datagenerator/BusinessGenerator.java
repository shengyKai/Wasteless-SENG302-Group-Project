package org.seng302.datagenerator;

import org.seng302.entities.Business;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    String[] BUSINESSTYPES = {};
    String[] DESCRIPTIONS = {};
    String[] NAMES = {};

    public BusinessGenerator(Connection conn) { this.conn = conn; }

    /**
     * Creates and inserts the buiness into the database
     * @param addressId the id associated with the location entity representing the business's address
     * @param ownerId the id associated with the user entity representing the user who owns the business
     * @return the id of the business that got created
     */
    private long createInsertBusinessSQL(long addressId, long ownerId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
            "INSERT INTO business (business_type, created, description, name, address_id, owner_id)"
                + "VALUES (?, ?, ?, ?, ?, ?)"
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

        clear();



    }
}
