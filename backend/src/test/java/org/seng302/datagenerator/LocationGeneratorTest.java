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
public class LocationGeneratorTest {
  private Connection conn;
  // LocationGenerator is called by UserGenerator or BusinessGenerator, so it does not have to be called on its own.
  private UserGenerator userGenerator;
  
  @BeforeEach
  public void setup() throws SQLException {
      //Connects to production database
      String url = "jdbc:mariadb://localhost/seng302-2021-team500-prod";
      //change password
      this.conn = DriverManager.getConnection(url, "seng302-team500", "ListenDirectly6053");

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

  public Long getLocationIdFromDB(Long userId) throws SQLException {
    PreparedStatement stmt = conn.prepareStatement("SELECT address_id FROM user WHERE userid = ?");
    stmt.setObject(1, userId);
    stmt.executeQuery();
    ResultSet results = stmt.getResultSet();
    results.next();
    return results.getLong(1);
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

}
