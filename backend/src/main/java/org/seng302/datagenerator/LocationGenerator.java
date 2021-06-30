package org.seng302.datagenerator;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.io.BufferedReader;
import java.io.FileReader;

public class LocationGenerator {

    private static LocationGenerator instance;

    private static final String EXAMPLE_DATA_FILE_PATH = "backend/example-data/";
    private static final String STREET_NAMES_FILE = "street-names.txt";
    private static final String CITIES_FILE = "cities.txt";
    private static final String REGIONS_FILE = "regions.txt";
    private static final String COUNTRIES_FILE = "countries.txt";
    private static final String DISTRICTS_FILE = "districts.txt";

    private List<String> streetNames;
    private List<String> cities;
    private List<String> regions;
    private List<String> countries;
    private List<String> districts;

    private Random random;

    private LocationGenerator() {
      streetNames = readLocationFile(STREET_NAMES_FILE);
      cities = readLocationFile(CITIES_FILE);
      regions = readLocationFile(REGIONS_FILE);
      countries = readLocationFile(COUNTRIES_FILE);
      districts = readLocationFile(DISTRICTS_FILE);
      random = new Random();
    }

    /**
     * This method creates (if necessary) and returns the singleton instance of the LocationGenerator class.
     * @return The LocationGenerator singleton.
     */
    public static LocationGenerator getInstance() {
      if (instance == null) {
          instance = new LocationGenerator();
      }
      return instance;
    }


    /**
     * Reads a text file with a list of a part of a location and returns a list where each entry in the list is a part
     * of location from the file.
     * @param filename The name of the file to be read.
     * @return A list of all the names of the part of the location in the file.
     */
    private List<String> readLocationFile(String filename) {
      List<String> locations = new ArrayList<>();
      try (BufferedReader bufferedReader = new BufferedReader(new FileReader(EXAMPLE_DATA_FILE_PATH + filename))) {
          String line;
          while ((line = bufferedReader.readLine()) != null) {
              String location = line.strip();
              locations.add(location);
          }
      } catch (Exception e) {
          e.printStackTrace();
      }
      return locations;
  }


    /**
     * Randomly generates the address of the user/business
     * @return the elements of a location object (user/business's address) in an array
     */
    public String[] generateAddress(Random random) {
      String streetNum = String.valueOf(random.nextInt(998) + 1);
      String streetName = streetNames.get(random.nextInt(streetNames.size()));
      String city = cities.get(random.nextInt(cities.size()));
      String region = regions.get(random.nextInt(regions.size()));
      String country = countries.get(random.nextInt(countries.size()));
      String postcode = String.valueOf(random.nextInt(98999) + 1000);
      String district = districts.get(random.nextInt(districts.size()));
      String[] address = {streetNum, streetName, city, region, country, postcode, district};
      return address;
    }

    /**
     * Creates the SQL commands required to insert the user/business's address into the database
     * @return the id of the location entity (addressid)
     */
    public long createInsertAddressSQL(String[] address, Connection conn) throws SQLException {
      PreparedStatement stmt = conn.prepareStatement(
              "INSERT INTO location (street_number, street_name, city, region, country, post_code, district) "
                      + "VALUES (?, ?, ?, ?, ?, ?, ?);",
              Statement.RETURN_GENERATED_KEYS
      );
      for (int i=0; i<7; i++) {
          stmt.setObject(i+1, address[i]);
      }
      stmt.executeUpdate();
      ResultSet keys = stmt.getGeneratedKeys();
      keys.next();
      return keys.getLong(1);
    }
}