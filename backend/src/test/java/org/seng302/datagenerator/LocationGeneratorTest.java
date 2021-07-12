package org.seng302.datagenerator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.seng302.leftovers.Main;

import java.sql.*;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@RunWith(SpringRunner.class)
@SpringBootTest(classes={Main.class})
public class LocationGeneratorTest {

  private static final String STREET_NAMES_FILE = "street-names.txt";
  private static final String CITIES_FILE = "cities.txt";
  private static final String REGIONS_FILE = "regions.txt";
  private static final String COUNTRIES_FILE = "countries.txt";
  private static final String DISTRICTS_FILE = "districts.txt";

  private Connection conn;
  // LocationGenerator is called by UserGenerator or BusinessGenerator, so it has to be called with either of them
  private UserGenerator userGenerator;

  @Mock
  private LocationGenerator locationGenerator;

  //loads all the example files which are the same as the generators
  private List<String> streetNames = ExampleDataFileReader.readExampleDataFile(STREET_NAMES_FILE);
  private List<String> cities = ExampleDataFileReader.readExampleDataFile(CITIES_FILE);
  private List<String> regions = ExampleDataFileReader.readExampleDataFile(REGIONS_FILE);
  private List<String> countries = ExampleDataFileReader.readExampleDataFile(COUNTRIES_FILE);
  private List<String> districts = ExampleDataFileReader.readExampleDataFile(DISTRICTS_FILE);

  @BeforeEach
  public void setup() throws SQLException {
    Map<String, String> properties = ExampleDataFileReader.readPropertiesFile("/application.properties");
    this.conn =  DriverManager.getConnection(properties.get("spring.datasource.url"), properties.get("spring.datasource.username"), properties.get("spring.datasource.password"));

    //Creates generators
    this.userGenerator = new UserGenerator(conn);
  }

  @AfterEach
  public void teardown() throws SQLException {
      conn.close();
  }

  /**
   * Checks that the required fields within the location table are not null using an SQL query.
   * @param userId the id of the generated user
   */
  public void checkRequiredFieldsNotNull(long userId) throws SQLException {
      PreparedStatement stmt = conn.prepareStatement(
              "SELECT COUNT(*) FROM user JOIN location ON user.address_id = location.id WHERE userid = ? AND " +
                      "city IS NOT NULL AND country IS NOT NULL AND post_code IS NOT NULL AND " +
                      "region IS NOT NULL AND street_name IS NOT NULL AND street_number IS NOT NULL"
      );
      stmt.setObject(1, userId);
      stmt.executeQuery();
      ResultSet results = stmt.getResultSet();
      results.next();
      if (results.getLong(1) != 1) {
          fail();
      }
  }

  /**
   * Queries that database to find out how many location entries are in the database
   * @return the number of location entries in the database
   */
  public long getNumLocationsInDB() throws SQLException {
    PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM location");
    stmt.executeQuery();
    ResultSet results = stmt.getResultSet();
    results.next();
    return results.getLong(1);
  }

  /**
   * Deletes the user entry associated with the given id within the database. This is part of the clean up.
   * @param userId the ids of the generated location entries
   */
  public void deleteLocationAndUserFromDB(List<Long> userIds) throws SQLException {
    for (Long userId: userIds) {
      Long locationId = getLocationIdFromDB(userId);
      PreparedStatement stmt = conn.prepareStatement("DELETE FROM user WHERE userid = ?");
      stmt.setObject(1, userId);
      stmt.executeUpdate();

      stmt = conn.prepareStatement("DELETE FROM location WHERE id = ?");
      stmt.setObject(1, locationId);
      stmt.executeUpdate();
    }
  }

  /**
   * Gets the location id from the generated user. 
   * @param userId id of the inquired user from the table
   * @return id of the location from the table
   */
  public Long getLocationIdFromDB(Long userId) throws SQLException {
    PreparedStatement stmt = conn.prepareStatement("SELECT address_id FROM user WHERE userid = ?");
    stmt.setObject(1, userId);
    stmt.executeQuery();
    ResultSet results = stmt.getResultSet();
    results.next();
    return results.getLong(1);
  }

  /**
   * Gets the specified field value of the location from the DB to check the field validity.
   * @param fieldName Specified location field that is required
   * @param locationId Location Id of the generated location
   * @return a String containing the field value of the location from the DB
   */
  public String getLocationFieldFromDB(String fieldName, Long locationId) throws SQLException {
    PreparedStatement stmt = conn.prepareStatement("SELECT " + fieldName + " FROM location WHERE id = ?");
    stmt.setObject(1, locationId);
    stmt.executeQuery();
    ResultSet results = stmt.getResultSet();
    results.next();
    return results.getString(1);
  }

  @Test
  void generateOneLocation_generateOneUser_oneLocationEntryGenerated() throws SQLException {
    List<Long> userIds = userGenerator.generateUsers(1);
    if (userIds.size() != 1) {
        fail();
    }
    long userId = userIds.get(0);

    checkRequiredFieldsNotNull(userId);
    deleteLocationAndUserFromDB(userIds);
  }

  
  @Test
  void generateMultipleLocations_generateMultipleUsers_multipleLocationEntriesGenerated() throws SQLException {
      List<Long> userIds = userGenerator.generateUsers(10);
      if (userIds.size() != 10) {
          fail();
      }

      for (long userId: userIds) {
        checkRequiredFieldsNotNull(userId);
      }

      deleteLocationAndUserFromDB(userIds);
  }

  @Test
  void generateZeroLocations_generateZeroUsers_zeroLocationEntriesGenerated() throws SQLException {
      List<Long> userIds = userGenerator.generateUsers(0);
      if (userIds.size() != 0) {
          fail();
      }
  }

  @Test
  void generateNegativeLocations_generateNegativeUsers_noLocationEntriesGenerated() throws SQLException {
      List<Long> userIds = userGenerator.generateUsers(-1);
      if (userIds.size() != 0) {
          fail();
      }
  }

  @Test
  void checkCityValidity_checkedAgainstExampleDataFile_cityIsValid() throws SQLException {
    List<Long> userIds = userGenerator.generateUsers(1);
    Long locationId = getLocationIdFromDB(userIds.get(0));
    String cityResult = getLocationFieldFromDB("city", locationId);

    assertTrue((cities.contains(cityResult)));
  }

  @Test
  void checkStreetNameValidity_checkedAgainstExampleDataFile_streetNameIsValid() throws SQLException {
    List<Long> userIds = userGenerator.generateUsers(1);
    Long locationId = getLocationIdFromDB(userIds.get(0));
    String streetNameResult = getLocationFieldFromDB("street_name", locationId);

    assertTrue((streetNames.contains(streetNameResult)));
  }

  @Test
  void checkRegionValidity_checkedAgainstExampleDataFile_regionIsValid() throws SQLException {
    List<Long> userIds = userGenerator.generateUsers(1);
    Long locationId = getLocationIdFromDB(userIds.get(0));
    String regionResult = getLocationFieldFromDB("region", locationId);

    assertTrue((regions.contains(regionResult)));
  }

  @Test
  void checkCountryValidity_checkedAgainstExampleDataFile_countryIsValid() throws SQLException {
    List<Long> userIds = userGenerator.generateUsers(1);
    Long locationId = getLocationIdFromDB(userIds.get(0));
    String countryResult = getLocationFieldFromDB("country", locationId);

    assertTrue((countries.contains(countryResult)));
  }

  @Test
  void checkDistrictValidity_checkedAgainstExampleDataFile_districtIsValid() throws SQLException {
    List<Long> userIds = userGenerator.generateUsers(1);
    Long locationId = getLocationIdFromDB(userIds.get(0));
    String districtResult = getLocationFieldFromDB("district", locationId);

    assertTrue((districts.contains(districtResult)));
  }
}
