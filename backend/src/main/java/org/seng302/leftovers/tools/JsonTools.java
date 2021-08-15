package org.seng302.leftovers.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

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
            if (json.containsKey(fieldName)) {
                return Long.parseLong(json.getAsString(fieldName));
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("%s is not present", fieldName));
            }
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("%s must be a number", fieldName));
        }
    }

    /**
     * Attempts to fetch the contents of the given field as a string.
     * Throws a Bad Request if the field is not present
     * @param json The JSON to parse
     * @param fieldName The name of the field to return
     * @return String containing value of given field
     */
    public static String parseStringFromJsonField(JSONObject json, String fieldName) {
        if (json.containsKey(fieldName)) {
            return json.getAsString(fieldName);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("%s is not present", fieldName));
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
        ResponseStatusException invalidFormatException = new ResponseStatusException(HttpStatus.BAD_REQUEST,
                String.format("%s must be an array of numbers", fieldName));
        ResponseStatusException notPresentException = new ResponseStatusException(HttpStatus.BAD_REQUEST,
                String.format("%s is not present", fieldName));

        try {
            final ObjectNode node = new ObjectMapper().readValue(json.toJSONString(), ObjectNode.class);
            if (node.has(fieldName)) {
                JsonNode value = node.get(fieldName);
                if (value.getNodeType() != JsonNodeType.ARRAY) {
                    throw invalidFormatException;
                }
                int arrayLength = value.size();
                long[] longArray = new long[arrayLength];
                for (int i = 0; i < arrayLength; i++) {
                    if (value.get(i).getNodeType() != JsonNodeType.NUMBER) {
                        throw invalidFormatException;
                    }
                    longArray[i] = value.get(i).asLong();
                }
                return longArray;
            } else {
                throw notPresentException;
            }
        } catch (JsonProcessingException e) {
            throw invalidFormatException;
        }
    }

    /**
     * Converts a page to the standard page representation.
     * E.g.
     * {
     *     "count": 100,
     *     "results": [{...}, {...}]
     * }
     * Users of this function are expected to use Page.map to convert their initial entities into a Page<JSONObject>
     * @param page Page to convert
     * @return Page as a JSONObject
     */
    public static JSONObject constructPageJSON(Page<JSONObject> page) {
        JSONArray resultArray = new JSONArray();
        for (JSONObject item : page) {
            resultArray.appendElement(item);
        }

        JSONObject json = new JSONObject();
        json.put("count", page.getTotalElements());
        json.put("results", resultArray);
        return json;
    }
}
