package org.seng302.datagenerator;

import java.util.Random;

public class DescriptionGenerator {
    private static DescriptionGenerator instance;

    private static final String DESCRIPTIONS_FILE = "lorem-ipsum.txt";
    private String descriptions;

    private Random random;

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
        int end = start + random.nextInt(200) + 1;  // 200 characters is max length of descriptions
        if (end > descriptions.length()) end = descriptions.length();
        return descriptions.substring(start, end-1);
    }
}
