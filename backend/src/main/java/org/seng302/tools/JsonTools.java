package org.seng302.tools;

import net.minidev.json.JSONObject;

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
}
