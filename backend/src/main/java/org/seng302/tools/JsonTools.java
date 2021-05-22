package org.seng302.tools;

import net.minidev.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonTools {

    private JsonTools() {}

    /**
     * This method identifies all the keys in the JSON object for which the value is null removes those key-value pairs
     * from the JSONObject.
     * @param json A JSON to remove key-value pairs with null values from.
     */
    public static void removeNullsFromJson(JSONObject json) {
        List<String> keysToRemove = new ArrayList<>();
        for (Map.Entry<String, Object> entry : json.entrySet()) {
            if (entry.getValue() == null) {
                keysToRemove.add(entry.getKey());
            }
        }
        for (String key : keysToRemove) {
            json.remove(key);
        }
    }

    /**
     * This method will return the field with the given name from the given json as a long if it can be converted to
     * that format, or will throw a bad request exception if the field cannot be converted to that format.
     * @param json The JSONObject to retrieve the field from.
     * @param fieldName The name of the field to retrieve.
     * @return The value from the field
     */
    public static long parseLongFromJsonField(JSONObject json, String fieldName) {
        try {
            return Long.parseLong(json.getAsString(fieldName));
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("%s must be a number", fieldName));
        }
    }

    /**
     * This method will return the field with the given name from the given json as a long[] if it can be converted to
     * that format, or will throw a bad request exception if the field cannot be converted to that format.
     * @param json The JSONObject to retrieve the field from.
     * @param fieldName The name of the field to retrieve.
     * @return The value from the field
     */
    public static long[] parseLongArrayFromJsonField(JSONObject json, String fieldName) {
        try {
            Object object = json.get(fieldName);
            if (object instanceof long[]) {
                return (long[]) object;
            }
            if (object instanceof int[]) {
                int[] intArray = (int[]) object;
                long[] longArray = new long[intArray.length];
                for (int i = 0; i < intArray.length; i++) {
                    longArray[i] = intArray[i];
                }
                return longArray;
            }
            List<Integer> list = (List<Integer>) object;
            long[] array = new long[list.size()];
            for (int i = 0; i < list.size(); i++) {
                array[i] = list.get(i);
            }
            return array;
        } catch (ClassCastException | NullPointerException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("%s must be an array of numbers", fieldName));
        }
    }
}
