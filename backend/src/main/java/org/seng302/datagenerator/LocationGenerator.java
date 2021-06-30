package org.seng302.datagenerator;

import java.sql.*;
import java.util.Random;

public class LocationGenerator {
    //predefined list of location elements
    String[] STREETNAMES = {"Hillary Cresenct", "Elizabeth Street", "Alice Avenue", "Racheal Road", "Peveral Street", "Moorhouse Avenue", "Riccarton Road", "Clyde Road", "Angelic Avenue", "Henley Road", "Sandspit Road", "Dinglebank Road", "Waipa Street", "Ranui Avenue", "Marywil Crescent"};
    String[] CITIES = {"Dunedin", "Nightcaps", "Gore", "Tapanui", "Wellington", "Christchurch", "Auckland", "Melbourne", "Brisbance", "Sydeny", "Perth", "Darwin", "Alice Springs", "Tokyo", "London"};
    String[] REGIONS = {"Otago", "Southland", "Canterbury", "Victoria", "Tasman", "Upper Hutt", "Alaska", "California", "Florida", "Hubei Province", "Fujian Province", "Hainan Province", "Munster", "Ulster", "Leinster"};
    String[] COUNTRIES = {"New Zealand", "Malaysia", "Australia", "England", "United Kingdom", "Japan", "Korea", "Singapore", "France", "Germany", "Norway", "Ireland", "Belgium", "Iceland", "Thailand"};
    //The district can be left empty, thus there is a null as an option
    String[] DISTRICTS = {null, "Kaipara District", "New Plymouth District", "Carterton District", "Beaver County", "Big Lakes County", "Camrose County", "Cardston County", "Gambir", "Menteng", "Senen", "Johor Bahru District", "Jeli District", "Raub District", "Ruapehu District"};
  
    /**
     * Randomly generates the address of the user/business
     * @return the elements of a location object (user/business's address) in an array
     */
    public String[] generateAddress(Random random) {
      String streetNum = String.valueOf(random.nextInt(998) + 1);
      String streetName = STREETNAMES[random.nextInt(STREETNAMES.length)];
      String city = CITIES[random.nextInt(CITIES.length)];
      String region = REGIONS[random.nextInt(REGIONS.length)];
      String country = COUNTRIES[random.nextInt(COUNTRIES.length)];
      String postcode = String.valueOf(random.nextInt(98999) + 1000);
      String district = DISTRICTS[random.nextInt(DISTRICTS.length)];
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
