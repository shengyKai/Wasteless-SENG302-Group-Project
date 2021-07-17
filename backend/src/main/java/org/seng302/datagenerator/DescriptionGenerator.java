package org.seng302.datagenerator;

import java.util.Random;

public class DescriptionGenerator {
    private static DescriptionGenerator instance;

    private static final String DESCRIPTIONS_FILE = "lorem-ipsum.txt";
    private final String descriptions;

    private final Random random;

    /**
     * Instance constructor for description generator, loads file of descriptions to be selected from
     * Description file is all one line so description is String of first in List
     */
    private DescriptionGenerator() {
        descriptions = ExampleDataFileReader.readExampleDataFile(DESCRIPTIONS_FILE).get(0);
        random = new Random();
    }

    /**
     * Gets an exert from the lorem ipsum file
     * @return a random description lorem ipsum placeholder
     */
    public String randomDescription() {
        int start = random.nextInt(descriptions.length());
        int end = start + random.nextInt(190) + 10;  // 10-199 char
        if (end > descriptions.length()) end = descriptions.length();
        String desc = descriptions.substring(start, end-1).trim() + ".";
        return desc.substring(0,1).toUpperCase() + desc.substring(1);
    }

    /**
     * Description singleton
     * @return an instance of the description generator class
     */
    public static DescriptionGenerator getInstance() {
        if (instance == null) instance = new DescriptionGenerator();
        return instance;
    }
}
