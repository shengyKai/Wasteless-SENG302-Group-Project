package org.seng302.datagenerator;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Random;

/**
 * This class randomly generates names relating to a business, consisting of business, manufacturer and product names.
 */
public class BusinessNameGenerator {

    private static BusinessNameGenerator instance;

    private static final String BUSINESS_SUFFIX_FILE = "business-suffixes.txt";
    private static final String MANUFACTURER_SUFFIX_FILE = "manufacturer-suffixes.txt";
    private static final String PRODUCT_ADJECTIVE_FILE = "product-adjectives.txt";
    private static final String PRODUCT_NOUN_FILE = "product-nouns.txt";

    private final List<String> businessSuffixes;
    private final List<String> manufacturerSuffixes;
    private final List<String> productAdjectives;
    private final List<String> productNouns;

    private final PersonNameGenerator personNameGenerator;
    private final LocationGenerator locationGenerator;
    private final Random random;

    /**
     * Private constructor. Reads data for generating business, manufacturer and product names into lists. Initializes
     * personNameGenerator for generating last names, and random for generating random numbers.
     */
    private BusinessNameGenerator() {
        businessSuffixes = ExampleDataFileReader.readExampleDataFile(BUSINESS_SUFFIX_FILE);
        manufacturerSuffixes = ExampleDataFileReader.readExampleDataFile(MANUFACTURER_SUFFIX_FILE);
        productAdjectives = ExampleDataFileReader.readExampleDataFile(PRODUCT_ADJECTIVE_FILE);
        productNouns = ExampleDataFileReader.readExampleDataFile(PRODUCT_NOUN_FILE);
        personNameGenerator = PersonNameGenerator.getInstance();
        locationGenerator = LocationGenerator.getInstance();
        random = new Random();
    }

    /**
     * Generates a name for a business by combining a randomly selected street name with a randomly selected suffix.
     * @return A randomly generated business name.
     */
    public String randomBusinessName() {
        String street = locationGenerator.randomStreetName();
        int suffixIndex = random.nextInt(businessSuffixes.size());
        return String.format("%s %s", street, businessSuffixes.get(suffixIndex));
    }

    /**
     * Generates a name for a manufacturer by combining a randomly selected last name with a randomly selected suffix.
     * @return A randomly generated manufacturer name.
     */
    public String randomManufacturerName() {
        String lastName = personNameGenerator.randomLastName();
        int suffixIndex = random.nextInt(manufacturerSuffixes.size());
        return String.format("%s %s", lastName, manufacturerSuffixes.get(suffixIndex));
    }

    /**
     * Generates a name for a product by combining a randomly selected adjective with a randomly selected noun.
     * @return A randomly generated product name.
     */
    public String randomProductName() {
        int adjIndex = random.nextInt(productAdjectives.size());
        int nounIndex = random.nextInt(productNouns.size());
        return String.format("%s %s", productAdjectives.get(adjIndex), productNouns.get(nounIndex));
    }

    /**
     * Creates (if necessary) and returns the singleton instance of the BusinessNameGenerator class.
     * @return the singleton instance of the BusinessNameGenerator.
     */
    public static BusinessNameGenerator getInstance() {
        if (instance == null) {
            instance = new BusinessNameGenerator();
        }
        return instance;
    }


    //Todo delete before merging to dev
    public static void main(String[] args) {
        BusinessNameGenerator businessNameGenerator = BusinessNameGenerator.getInstance();
        businessNameGenerator.printTest();
    }

    //Todo delete before merging to dev
    public void printTest() {

        Instant startTime = Instant.now();
        for (int i = 0; i < 10000; i++) {
            randomBusinessName();
            randomManufacturerName();
            randomProductName();
        }
        Instant endTime = Instant.now();
        System.out.println("Exectution time for generating 10000 business, manufacturer and product names: " + Duration.between(startTime, endTime).getNano());

        for (int i = 0; i < 5; i++) {
            System.out.println("Random business name: " + randomBusinessName());
        }
        System.out.println();
        for (int i = 0; i < 5; i++) {
            System.out.println("Random manufacturer name: " + randomManufacturerName());
        }
        System.out.println();
        for (int i = 0; i < 5; i++) {
            System.out.println("Random product name: " + randomProductName());
        }
    }

}
