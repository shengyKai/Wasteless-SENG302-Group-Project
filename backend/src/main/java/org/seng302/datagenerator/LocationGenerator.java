package org.seng302.datagenerator;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import lombok.Data;

import java.io.BufferedReader;
import java.io.FileReader;

public class LocationGenerator {

    private static LocationGenerator instance;

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
      streetNames = ExampleDataFileReader.readExampleDataFile(STREET_NAMES_FILE);
      cities = ExampleDataFileReader.readExampleDataFile(CITIES_FILE);
      regions = ExampleDataFileReader.readExampleDataFile(REGIONS_FILE);
      countries = ExampleDataFileReader.readExampleDataFile(COUNTRIES_FILE);
      districts = ExampleDataFileReader.readExampleDataFile(DISTRICTS_FILE);
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
     * Randomly generates the address of the user/business
     * @return the elements of a location object (user/business's address) in a Location object
     */
    public Location generateAddress(Random random) {
      String streetNum = String.valueOf(random.nextInt(998) + 1);
      String streetName = streetNames.get(random.nextInt(streetNames.size()));
      String city = cities.get(random.nextInt(cities.size()));
      String region = regions.get(random.nextInt(regions.size()));
      String country = countries.get(random.nextInt(countries.size()));
      String postcode = String.valueOf(random.nextInt(98999) + 1000);
      String district = districts.get(random.nextInt(districts.size()));
      return Location.of(streetNum, streetName, city, region, country, postcode, district);
    }

    public long createInsertAddressSQL(Location address, Connection conn) throws SQLException {
        return createInsertAddressSQL(List.of(address), conn).get(0);
    }

    /**
     * Creates the SQL commands required to insert the user/business's address into the database
     * @return the id of the location entity (addressid)
     */
    public List<Long> createInsertAddressSQL(List<Location> addresses, Connection conn) throws SQLException {
      PreparedStatement stmt = conn.prepareStatement(
              "INSERT INTO location (street_number, street_name, city, region, country, post_code, district) "
                      + "VALUES (?, ?, ?, ?, ?, ?, ?);",
              Statement.RETURN_GENERATED_KEYS
      );
      for (Location address : addresses) {
          stmt.setObject(1, address.streetNum);
          stmt.setObject(2, address.streetName);
          stmt.setObject(3, address.city);
          stmt.setObject(4, address.region);
          stmt.setObject(5, address.country);
          stmt.setObject(6, address.postcode);
          stmt.setObject(7, address.district);
          stmt.addBatch();
      }
      stmt.executeBatch();
      ResultSet keys = stmt.getGeneratedKeys();

      List<Long> locationIds = new ArrayList<>();
      while (keys.next()) {
          locationIds.add(keys.getLong(1));
      }
      return locationIds;
    }

    /**
     * Randomly select and return a street name from the list of street names.
     * @return The randomly selected street name.
     */
    public String randomStreetName() {
        return streetNames.get(random.nextInt(streetNames.size()));
    }

    /**
     * A Location object to make the addresses more readable.
     */
    @Data(staticConstructor="of")
    static class Location {
      private final String streetNum;
      private final String streetName;
      private final String city;
      private final String region;
      private final String country;
      private final String postcode;
      private final String district;
    }
}