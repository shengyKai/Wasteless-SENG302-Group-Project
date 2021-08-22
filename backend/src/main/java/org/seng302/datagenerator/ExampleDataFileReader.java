package org.seng302.datagenerator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class reads data from parameter files for generating example data
 */
public class ExampleDataFileReader {

    private static final String EXAMPLE_DATA_FILE_PATH = "example-data/";
    private static final Logger logger = LogManager.getLogger(ExampleDataFileReader.class.getName());

    /**
     * Private constructor to hide implicit public constructor.
     */
    private ExampleDataFileReader() {}


    public static Map<String, String> readPropertiesFile(String resourcePath) {
        Map<String, String> properties = new HashMap<>();

        InputStream stream = ExampleDataFileReader.class.getResourceAsStream(resourcePath);
        if (stream == null) {
            return null;
        }
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                line = line.strip();
                if (line.startsWith("#") || !line.contains("=")) continue;

                int index = line.indexOf('=');

                String key = line.substring(0, index);
                String value = line.substring(index+1);

                if (properties.containsKey(key)) {
                    throw new RuntimeException("Unexpected duplicate key \"" + key + "\"");
                }

                properties.put(key, value);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }

        return properties;
    }

    /**
     * Reads a text file with one value per line and returns a list where each entry in the list is a value from the file.
     * @param filename The name of the file to be read.
     * @return A list of all values in the file.
     */
    public static List<String> readExampleDataFile(String filename) {
        List<String> values = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(ExampleDataFileReader.class.getResourceAsStream(EXAMPLE_DATA_FILE_PATH + filename)))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String value = line.strip();
                values.add(value);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
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
