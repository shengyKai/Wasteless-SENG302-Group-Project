import java.util.Random;
public class LocationGenerator {
    /**
     * Randomly generates the address of the user/business
     * @return the elements of a location object (user's/business's address) in an array
     */
    public static String[] GenerateAddress(Random random) {
      //predefined lists for the location components
      String[] streetNames = {"Hillary Cresenct", "Elizabeth Street", "Alice Avenue", "Racheal Road", "Peveral Street", "Moorhouse Avenue", "Riccarton Road", "Clyde Road", "Angelic Avenue", "Henley Road", "Sandspit Road", "Dinglebank Road", "Waipa Street", "Ranui Avenue", "Marywil Crescent"};
      String[] cities = {"Dunedin", "Nightcaps", "Gore", "Tapanui", "Wellington", "Christchurch", "Auckland", "Melbourne", "Brisbance", "Sydeny", "Perth", "Darwin", "Alice Springs", "Tokyo", "London"};
      String[] regions = {"Otago", "Southland", "Canterbury", "Victoria", "Tasman", "Upper Hutt", "Alaska", "California", "Florida", "Hubei Province", "Fujian Province", "Hainan Province", "Munster", "Ulster", "Leinster"};
      String[] countries = {"New Zealand", "Malaysia", "Australia", "England", "United Kingdom", "Japan", "Korea", "Singapore", "France", "Germany", "Norway", "Ireland", "Belgium", "Iceland", "Thailand"};
      //add a choice for the randomizer to select an empty district since district can be empty too
      String[] districts = {"", "Kaipara District", "New Plymouth District", "Carterton District", "Beaver County", "Big Lakes County", "Camrose County", "Cardston County", "Gambir", "Menteng", "Senen", "Johor Bahru District", "Jeli District", "Raub District", "Ruapehu District"};

      String streetNum = String.valueOf(random.nextInt(998) + 1);
      String streetName = streetNames[random.nextInt(streetNames.length)];
      String city = cities[random.nextInt(cities.length)];
      String region = regions[random.nextInt(regions.length)];
      String country = countries[random.nextInt(countries.length)];
      String postcode = String.valueOf(random.nextInt(98999) + 1000);
      String district = districts[random.nextInt(districts.length)];
      String[] address = {streetNum, streetName, city, region, country, postcode, district};
      return address;
  }
}