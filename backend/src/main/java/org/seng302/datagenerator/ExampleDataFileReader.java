package org.seng302.datagenerator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * This class reads data from parameter files for generating example data
 */
public class ExampleDataFileReader {

    private static final String EXAMPLE_DATA_FILE_PATH = "example-data/";


    /**
     * Reads a text file with one value per line and returns a list where each entry in the list is a value from the file.
     * @param filename The name of the file to be read.
     * @return A list of all values in the file.
     */
    public static List<String> readExampleDataFile(String filename) {
        List<String> values = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(EXAMPLE_DATA_FILE_PATH + filename))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String value = line.strip();
                values.add(value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return values;
    }

    /**
     * @return A string representing the path to the directory containing the example data
     */
    public static String getExampleDataFilePath() {
        return EXAMPLE_DATA_FILE_PATH;
    }
}
