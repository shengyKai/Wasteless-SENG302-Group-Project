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

    public static long parseLongFromJsonField(JSONObject json, String fieldName) {
        try {
            return Long.parseLong(json.getAsString(fieldName));
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("%s must be a number", fieldName));
        }
    }

    public static long[] parseLongArrayFromJsonField(JSONObject json, String fieldName) {
        try {
            Object object = json.get(fieldName);
            List<Integer> list = (List<Integer>) object;
            long[] array = new long[list.size()];
            for (int i = 0; i < list.size(); i++) {
                array[i] = list.get(i);
            }
            return array;
        } catch (ClassCastException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("%s must be an array of numbers", fieldName));
        }
    }
}
