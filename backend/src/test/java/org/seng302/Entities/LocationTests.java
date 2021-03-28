package org.seng302.Entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Main test class. Testing overall application sanity
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class LocationTests {

  private Location testLocation = new Location();
  private Location.Builder locationBuilder;

  @BeforeEach
  public void setUp() {
    locationBuilder = new Location.Builder().atStreetNumber(1).onStreet("Elizabeth Street").inSuburb("Riccarton").inCity("Christchurch")
            .inRegion("Canterbury").inCountry("New Zealand").withZipCode("8041");
  }

  /**
   * Checks all integers between 1 and 999 return true when passed into the checkValidStreetNumber method
   */
  @Test
  public void checkValidStreetNumberValidNumbers() {
    for (int i = 1; i <= 999; i++) {
      assertTrue(testLocation.checkValidStreetNumber(i));
    }
  }

  /**
   * Checks several large integers above 999 return false when passed into the checkValidStreetNumber method
   */
  @Test
  public void checkValidStreetNumberTooLargeNumbers() {
    int[] streetNumbers = new int[]{ 1000, 1001, 1002, 1003, 2000, 3000, 4000, 5000, 12345, 123456, 1234567, 12345678,
                                      123456789, 1234567890 };
    for (int streetNumber : streetNumbers) {
      assertFalse(testLocation.checkValidStreetNumber(streetNumber));
    }
  }

  /**
   * Checks the integer zero fails when passed into the checkValidStreetNumber method
   */
  @Test
  public void checkValidStreetNumberZero() {
    int streetNumber = 0;
    assertFalse(testLocation.checkValidStreetNumber(streetNumber));
  }

  /**
   * Checks negative integers fail when passed into the checkValidStreetNumber method
   */
  @Test
  public void checkValidStreetNumberNegativeNumbers() {
    int[] streetNumbers = new int[]{ -1000, -1001, -1002, -1003, -2000, -3000, -4000, -5000, -12345, -123456, -1234567,
                                      -12345678, -123456789, -1234567890 };
    for (int streetNumber : streetNumbers) {
      assertFalse(testLocation.checkValidStreetNumber(streetNumber));
    }
  }

  /**
   * Checks several names pass return true when passed into the checkValidStreetName method
   */
  @Test
  public void checkValidStreetNameLetters() {
    String[] streetNames = new String[]{ "Abby Park Street", "Barn Street", "California Street", "Danish Avenue",
                                          "Eastern Cesta", "Farmers Lane", "Galghard Road", "Hazlett Avenue" };
    for (String streetName : streetNames) {
      assertTrue(testLocation.checkValidStreetName(streetName));
    }
  }

  /**
   * Checks several names with numbers in them fail when passed into the checkValidStreetName method
   */
  @Test
  public void checkValidStreetNameNumbers() {
    String[] streetNames = new String[]{ "Over 9000 Avenue", "69th Street", "0 Lane", "333 Road" };
    for (String streetName : streetNames) {
      assertFalse(testLocation.checkValidStreetName(streetName));
    }
  }

  /**
   * Checks several names with characters in them fail when passed into the checkValidStreetName method
   */
  @Test
  public void checkValidStreetNameCharacters() {
    String[] streetNames = new String[]{ "Hashtag # Lane", "Dollar $ Avenue", "Me & The Bois Road", "@ Me Street" };
    for (String streetName : streetNames) {
      assertFalse(testLocation.checkValidStreetName(streetName));
    }
  }

  /**
   * Checks several names with over 100 characters fail when passed into the checkValidStreetName method
   */
  @Test
  public void checkValidStreetNameOverHundred() {
    String[] streetNames = new String[]{
            "abcdefghijklmnopqrstuvwxyz abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz",
            "helphelphelphelphelphelphelphelphelphelphelphelphelphelphelphelphelphelphelphelphelphelphelphelphelp",
            "idontneedit idontneedit idontneedit idontneedit idontneeditidontneeditidontneeditidontneeditidontneedit" };
    for (String streetName : streetNames) {
      assertFalse(testLocation.checkValidStreetName(streetName));
    }
  }

  /**
   * Check an empty string fail when passed into the checkValidStreetName method
   */
  @Test
  public void checkValidStreetNameEmpty() {
    assertFalse(testLocation.checkValidStreetName(""));
  }

  /**
   * Checks several names pass return true when passed into the checkValidSuburb method
   */
  @Test
  public void checkValidSuburbLetters() {
    String[] suburbs = new String[]{ "Addington", "Beckenham", "Casebrook", "Dallington", "Edgeware", "Fendalton",
                                      "Halswell", "Ilam", "Kainga", "Linwood" };
    for (String suburb : suburbs) {
      assertTrue(testLocation.checkValidSuburb(suburb));
    }
  }

  /**
   * Checks several names with numbers in them fail when passed into the checkValidSuburb method
   */
  @Test
  public void checkValidSuburbNumbers() {
    String[] suburbs = new String[]{ "Add1ngt0n", "B3ck3nham", "Cas3br00k", "Dall1ngt0n", "3dg3war3", "F3ndalt0n",
                                      "Halsw3ll", "1lam", "Ka1nga", "L1nw00d" };
    for (String suburb : suburbs) {
      assertFalse(testLocation.checkValidSuburb(suburb));
    }
  }

  /**
   * Checks several names with characters in them fail when passed into the checkValidSuburb method
   */
  @Test
  public void checkValidSuburbCharacters() {
    String[] suburbs = new String[]{ "@dd!ngton", "Bec&enh@m", "C@$ebroo&", "D@ll!ngton", "Edgew@re", "Fend@lton",
                                      "H@lswell", "!l@m", "K@!ng@", "L!nwood" };
    for (String suburb : suburbs) {
      assertFalse(testLocation.checkValidSuburb(suburb));
    }
  }

  /**
   * Checks several names with over 50 characters fail when passed into the checkValidSuburb method
   */
  @Test
  public void checkValidSuburbOverFifty() {
    String[] suburbs = new String[]{ "new old oldie new old old oldie oldie oldie old old",
                                      "helpppppppppppppppppppppppppppppppppppppppppppppppppppppp",
                                      "this suburb name is exactly fifty characters longg"};
    for (String suburb : suburbs) {
      assertFalse(testLocation.checkValidSuburb(suburb));
    }
  }

  /**
   * Check an empty string fail when passed into the checkValidSuburb method
   */
  @Test
  public void checkValidSuburbEmpty() {
    assertFalse(testLocation.checkValidSuburb(""));
  }

  /**
   * Checks several names pass return true when passed into the checkValidCity method
   */
  @Test
  public void checkValidCityLetters() {
    String[] cities = new String[]{ "Christchurch", "Dunedin", "Nelson", "Wellington", "Hamilton", "Auckland",
                                    "Nightcaps", "Sendai", "Tokyo", "Osaka", "Akihabara", "New York", "London" };
    for (String city : cities) {
      assertTrue(testLocation.checkValidCity(city));
    }
  }

  /**
   * Checks several names with numbers in them fail when passed into the checkValidCity method
   */
  @Test
  public void checkValidCityNumbers() {
    String[] cities = new String[]{ "Chr1stchurch", "Dun3d1n", "N3ls0n", "W3ll1ngt0n", "Ham1lt0n", "Auck1and",
                                    "N1ghtcaps", "S3nda1", "T0ky0", "0saka", "Ak1habara", "N3w Y0rk", "L0nd0n" };
    for (String city : cities) {
      assertFalse(testLocation.checkValidCity(city));
    }
  }

  /**
   * Checks several names with characters in them fail when passed into the checkValidCity method
   */
  @Test
  public void checkValidCityCharacters() {
    String[] cities = new String[]{ "Chr!stchurch", "Duned!n", "Ne!son", "Well!ngton", "H@m!lton", "@uck!@nd",
                                    "N!ghtc@p$", "$end@!", "To&yo", "Os@&@", "@k!h@b@r@", "New Yor&", "Lon)on" };
    for (String city : cities) {
      assertFalse(testLocation.checkValidCity(city));
    }
  }

  /**
   * Checks several names with over 50 characters fail when passed into the checkValidCity method
   */
  @Test
  public void checkValidCityOverFifty() {
    String[] cities = new String[]{ "this city string contains exactly fifty characters",
                                    "helpppppppppppppppppppppppppppppppppppppppppppppppppppppp",
                                    "This is a city name that resembles the name of city" };
    for (String city : cities) {
      assertFalse(testLocation.checkValidCity(city));
    }
  }

  /**
   * Check an empty string fail when passed into the checkValidCity method
   */
  @Test
  public void checkValidCityEmpty() {
    assertFalse(testLocation.checkValidCity(""));
  }

  /**
   * Checks several names pass return true when passed into the checkValidRegion method
   */
  @Test
  public void checkValidRegionLetters() {
    String[] regions = new String[]{ "Southland", "Otago", "West Coast", "Cantebury", "Clutha", "Marlborough", "Selwyn",
                                      "Tasman", "Waimakariri", "Waimate"};
    for (String region : regions) {
      assertTrue(testLocation.checkValidRegion(region));
    }
  }

  /**
   * Checks several names with numbers in them fail when passed into the checkValidRegion method
   */
  @Test
  public void checkValidRegionNumbers() {
    String[] regions = new String[]{ "S0uthland", "0tago", "W3st C0ast", "Cant3bury", "C!utha", "Marlb0r0ugh", "S3lwyn",
                                      "5asman", "Wa1makar1r1", "Wa1mat3"};
    for (String region : regions) {
      assertFalse(testLocation.checkValidRegion(region));
    }
  }

  /**
   * Checks several names with characters in them fail when passed into the checkValidRegion method
   */
  @Test
  public void checkValidRegionCharacters() {
    String[] regions = new String[]{ "Southl@nd", "Ot@go", "We$t Co@st", "C@ntebury", "Cluth@", "M@rlborough", "Se!wyn",
                                      "T@sm@n", "W@!m@kar!r!", "W@!m@te" };
    for (String region : regions) {
      assertFalse(testLocation.checkValidRegion(region));
    }
  }

  /**
   * Checks several names with over 50 characters fail when passed into the checkValidRegion method
   */
  @Test
  public void checkValidRegionOverFifty() {
    String[] regions = new String[]{ "this region issssssssssssssssssss fifty characters",
                                      "helppppppppppppppppppppppppppppppppppppppppppppppp",
                                      "this region is full of surprises you should come have a look" };
    for (String region : regions) {
      assertFalse(testLocation.checkValidRegion(region));
    }
  }

  /**
   * Check an empty string fail when passed into the checkValidRegion method
   */
  @Test
  public void checkValidRegionEmpty() {
    assertFalse(testLocation.checkValidRegion(""));
  }

  /**
   * Checks several names pass return true when passed into the checkValidCountry method
   */
  @Test
  public void checkValidCountryLetters() {
    String[] countries = new String[]{ "New Zealand", "Australia", "Fiji", "Tonga", "Japan", "Korea", "United Kingdom",
                                        "Great Britan", "England", "Scotland" };
    for (String country : countries) {
      assertTrue(testLocation.checkValidRegion(country));
    }
  }

  /**
   * Checks several names with numbers in them fail when passed into the checkValidCountry method
   */
  @Test
  public void checkValidCountryNumbers() {
    String[] countries = new String[]{ "N3w Z3aland", "Austral1a", "F1j1", "T0nga", "Ja9an", "K0r3a", "Un1t3d K1ngd0m",
                                        "Gr3at Br1tan", "3ngland", "Sc0tland" };
    for (String country : countries) {
      assertFalse(testLocation.checkValidRegion(country));
    }
  }

  /**
   * Checks several names with characters in them fail when passed into the checkValidCountry method
   */
  @Test
  public void checkValidCountryCharacters() {
    String[] countries = new String[]{ "New Ze@l@nd", "@ustr@l!@", "F!j!", "Tong@", "J@p@n", "K0r3@", "Un!ted K!ngdom",
                                        "Gre@t Br!t@n", "Engl@nd", "Scotl@nd" };
    for (String country : countries) {
      assertFalse(testLocation.checkValidRegion(country));
    }
  }

  /**
   * Checks several names with over 50 characters fail when passed into the checkValidCountry method
   */
  @Test
  public void checkValidCountryOverFifty() {
    String[] countries = new String[]{ "This country is exactlyyyyyy fifty characters long",
                                        "helppppppppppppppppppppppppppppppppppppppppppppppp",
                                        "This country has a lot going for it we are coronavirus free" };
    for (String country : countries) {
      assertFalse(testLocation.checkValidRegion(country));
    }
  }

  /**
   * Check an empty string fail when passed into the checkValidCountry method
   */
  @Test
  public void checkValidCountryEmpty() {
    assertFalse(testLocation.checkValidCountry(""));
  }

  /**
   * Checks several numeric zipcodes return true when passed into the checkValidZipCode method
   */
  @Test
  public void checkValidZipCodeNumeric() {
    String[] zipcodes = new String[]{ "1", "2", "3", "4", "5", "10", "20", "30", "40", "100", "1000", "12345", "123456",
                                      "1234567", "12345678", "123456789" };
    for (String zipcode : zipcodes) {
      assertTrue(testLocation.checkValidZipCode(zipcode));
    }
  }

  /**
   * Checks several numeric zipcodes return true when passed into the checkValidZipCode method
   */
  @Test
  public void checkValidZipCodeAlphaNumeric() {
    String[] zipcodes = new String[]{ "a1", "b2", "c3", "d4", "e5", "f10", "g20", "h30", "i40", "j100", "k1000",
                                      "l12345", "m123456", "o1234567", "p12345678", "q12345678" };
    for (String zipcode : zipcodes) {
      assertTrue(testLocation.checkValidZipCode(zipcode));
    }
  }

  /**
   * Checks several zipcodes with characters in them that fail when passed into the checkValidZipCode method
   */
  @Test
  public void checkValidZipCodeCharacters() {
    String[] zipcodes = new String[]{ "!1", "@2", "#3", "$4", "%5", "^10", "&20", "*30", "(40", ")100", "=1000",
                                      "_12345", "[123456", "]1234567", "{12345678", "}123456789" };
    for (String zipcode : zipcodes) {
      assertFalse(testLocation.checkValidZipCode(zipcode));
    }
  }

  /**
   * Checks several zipcodes over ten characters fail when passed into the checkValidZipCode method
   */
  @Test
  public void checkValidZipCodeOverTen() {
    String[] zipcodes = new String[]{ "1234567890", "123456789123456789", "123456789000000" };
    for (String zipcode : zipcodes) {
      assertFalse(testLocation.checkValidZipCode(zipcode));
    }
  }

  /**
   * Checks an empty string fail when passed into the checkValidZipCode method
   */
  @Test
  public void checkValidZipCodeEmpty() {
    assertFalse(testLocation.checkValidZipCode(""));
  }

  /**
   * Checks a Location object with all valid parameters returns true when passed into the
   * checkValidAllLocationParameters method
   */
  @Test
  public void checkValidAllLocationParametersEverythingValid() {
    Location location = new Location.Builder().atStreetNumber(1).onStreet("Elizabeth Street").inSuburb("Riccarton").inCity("Christchurch")
                                      .inRegion("Canterbury").inCountry("New Zealand").withZipCode("8041").build();
    assertTrue(testLocation.checkValidAllLocationParameters(location));
  }

  /**
   * Checks a Location object with an invalid street number parameter fails when passed into the
   * checkValidAllLocationParameters method
   */
  @Test
  public void checkValidAllLocationParametersStreetNumberInvalid() {
    assertThrows(ResponseStatusException.class, () -> {
      Location location = new Location.Builder().atStreetNumber(12345).onStreet("Elizabeth Street").inSuburb("Riccarton").inCity("Christchurch")
                      .inRegion("Canterbury").inCountry("New Zealand").withZipCode("8041").build();
    });
  }

  /**
   * Checks a Location object with an invalid street name parameter fails when passed into the
   * checkValidAllLocationParameters method
   */
  @Test
  public void checkValidAllLocationParametersStreetNameInvalid() {
    assertThrows(ResponseStatusException.class, () -> {
      Location location = new Location.Builder().atStreetNumber(1).onStreet("Eliz@beth Str33t").inSuburb("Riccarton").inCity("Christchurch")
            .inRegion("Canterbury").inCountry("New Zealand").withZipCode("8041").build();
    });
  }

  /**
   * Checks a Location object with an invalid suburb parameter fails when passed into the
   * checkValidAllLocationParameters method
   */
  @Test
  public void checkValidAllLocationParametersSuburbInvalid() {
    assertThrows(ResponseStatusException.class, () -> {
      Location location = new Location.Builder().atStreetNumber(1).onStreet("Elizabeth Street").inSuburb("R!cc@rton").inCity("Christchurch")
              .inRegion("Canterbury").inCountry("New Zealand").withZipCode("8041").build();
    });
  }

  /**
   * Checks a Location object with an invalid city parameter fails when passed into the
   * checkValidAllLocationParameters method
   */
  @Test
  public void checkValidAllLocationParametersCityInvalid() {
    assertThrows(ResponseStatusException.class, () -> {
      Location location = new Location.Builder().atStreetNumber(1).onStreet("Elizabeth Street").inSuburb("Riccarton").inCity("Chr!$stchurch")
              .inRegion("Canterbury").inCountry("New Zealand").withZipCode("8041").build();
    });
  }

  /**
   * Checks a Location object with an invalid region parameter fails when passed into the
   * checkValidAllLocationParameters method
   */
  @Test
  public void checkValidAllLocationParametersRegionInvalid() {
    assertThrows(ResponseStatusException.class, () -> {
      Location location = new Location.Builder().atStreetNumber(1).onStreet("Elizabeth Street").inSuburb("Riccarton").inCity("Christchurch")
              .inRegion("C@nt3rbury").inCountry("New Zealand").withZipCode("8041").build();
    });
  }

  /**
   * Checks a Location object with an invalid country parameter fails when passed into the
   * checkValidAllLocationParameters method
   */
  @Test
  public void checkValidAllLocationParametersCountryInvalid() {
    assertThrows(ResponseStatusException.class, () -> {
      Location location = new Location.Builder().atStreetNumber(1).onStreet("Elizabeth Street").inSuburb("Riccarton").inCity("Christchurch")
              .inRegion("Canterbury").inCountry("N3w Z3@l@nd").withZipCode("8041").build();
    });
  }

  /**
   * Checks a Location object with an invalid zip code parameter fails when passed into the
   * checkValidAllLocationParameters method
   */
  @Test
  public void checkValidAllLocationParametersZipCodeInvalid() {
    assertThrows(ResponseStatusException.class, () -> {
      Location location = new Location.Builder().atStreetNumber(1).onStreet("Elizabeth Street").inSuburb("Riccarton").inCity("Christchurch")
                      .inRegion("Canterbury").inCountry("New Zealand").withZipCode("80999999999999941").build();
    });
  }

  /**
   * Verify that Location.Builder.build() throws a ResponseStatusException when it is called before street number has
   * been set.
   */
  @Test
  public void buildWithoutStreetNumberTest() {
    Location.Builder locationBuilder = new Location.Builder()
            .onStreet("Elizabeth Street")
            .inSuburb("Riccarton")
            .inCity("Christchurch")
            .inRegion("Canterbury")
            .inCountry("New Zealand")
            .withZipCode("8041");
    assertThrows(ResponseStatusException.class, locationBuilder::build);
  }

  /**
   * Verify that Location.Builder.build() throws a ResponseStatusException when it is called before street name has
   * been set.
   */
  @Test
  public void buildWithoutStreetNameTest() {
    Location.Builder locationBuilder = new Location.Builder()
            .atStreetNumber(1)
            .inSuburb("Riccarton")
            .inCity("Christchurch")
            .inRegion("Canterbury")
            .inCountry("New Zealand")
            .withZipCode("8041");
    assertThrows(ResponseStatusException.class, locationBuilder::build);
  }

  /**
   * Verify that Location.Builder.build() throws a ResponseStatusException when it is called before suburb has
   * been set.
   */
  @Test
  public void buildWithoutSuburbTest() {
    Location.Builder locationBuilder = new Location.Builder()
            .atStreetNumber(1)
            .onStreet("Elizabeth Street")
            .inCity("Christchurch")
            .inRegion("Canterbury")
            .inCountry("New Zealand")
            .withZipCode("8041");
    assertThrows(ResponseStatusException.class, locationBuilder::build);
  }

  /**
   * Verify that Location.Builder.build() throws a ResponseStatusException when it is called before city has been set.
   */
  @Test
  public void buildWithoutCityTest() {
    Location.Builder locationBuilder = new Location.Builder()
            .atStreetNumber(1)
            .onStreet("Elizabeth Street")
            .inSuburb("Riccarton")
            .inRegion("Canterbury")
            .inCountry("New Zealand")
            .withZipCode("8041");
    assertThrows(ResponseStatusException.class, locationBuilder::build);
  }

  /**
   * Verify that Location.Builder.build() throws a ResponseStatusException when it is called before region has
   * been set.
   */
  @Test
  public void buildWithoutRegionTest() {
    Location.Builder locationBuilder = new Location.Builder()
            .atStreetNumber(1)
            .onStreet("Elizabeth Street")
            .inSuburb("Riccarton")
            .inCity("Christchurch")
            .inCountry("New Zealand")
            .withZipCode("8041");
    assertThrows(ResponseStatusException.class, locationBuilder::build);
  }

  /**
   * Verify that Location.Builder.build() throws a ResponseStatusException when it is called before country has
   * been set.
   */
  @Test
  public void buildWithoutCountryTest() {
    Location.Builder locationBuilder = new Location.Builder()
            .atStreetNumber(1)
            .onStreet("Elizabeth Street")
            .inSuburb("Riccarton")
            .inCity("Christchurch")
            .inRegion("Canterbury")
            .withZipCode("8041");
    assertThrows(ResponseStatusException.class, locationBuilder::build);
  }

  /**
   * Verify that Location.Builder.build() throws a ResponseStatusException when it is called before zip code has
   * been set.
   */
  @Test
  public void buildWithoutZipCodeTest() {
    Location.Builder locationBuilder = new Location.Builder()
            .atStreetNumber(1)
            .onStreet("Elizabeth Street")
            .inSuburb("Riccarton")
            .inCity("Christchurch")
            .inRegion("Canterbury")
            .inCountry("New Zealand");
    assertThrows(ResponseStatusException.class, locationBuilder::build);
  }

  /**
   * Verify that when Location.Builder.build() is called with all parameters set, the street number of the resulting
   * location is the same as value set for the builder.
   */
  @Test
  public void buildStreetNumberTest() {
    Location location = locationBuilder.build();
    assertEquals(1, location.getStreetNumber());
  }

  /**
   * Verify that when Location.Builder.build() is called with all parameters set, the street name of the resulting
   * location is the same as value set for the builder.
   */
  @Test
  public void buildStreetNameTest() {
    Location location = locationBuilder.build();
    assertEquals("Elizabeth Street", location.getStreetName());
  }

  /**
   * Verify that when Location.Builder.build() is called with all parameters set, the suburb of the resulting
   * location is the same as value set for the builder.
   */
  @Test
  public void buildSuburbTest() {
    Location location = locationBuilder.build();
    assertEquals("Riccarton", location.getSuburb());
  }

  /**
   * Verify that when Location.Builder.build() is called with all parameters set, the city of the resulting
   * location is the same as value set for the builder.
   */
  @Test
  public void buildCityTest() {
    Location location = locationBuilder.build();
    assertEquals("Christchurch", location.getCity());
  }

  /**
   * Verify that when Location.Builder.build() is called with all parameters set, the region of the resulting
   * location is the same as value set for the builder.
   */
  @Test
  public void buildRegionTest() {
    Location location = locationBuilder.build();
    assertEquals("Canterbury", location.getRegion());
  }

  /**
   * Verify that when Location.Builder.build() is called with all parameters set, the country of the resulting
   * location is the same as value set for the builder.
   */
  @Test
  public void buildCountryTest() {
    Location location = locationBuilder.build();
    assertEquals("New Zealand", location.getCountry());
  }
}