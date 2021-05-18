package org.seng302.tools;

import net.minidev.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonToolsTest {

    @Test
    void removeNullsFromJson_jsonWithoutNullAttributes_nothingRemoved() {
        JSONObject json = new JSONObject();
        json.appendField("name", "Fred");
        json.appendField("age", 30);
        json.appendField("occupation", "teacher");
        JsonTools.removeNullsFromJson(json);
        assertTrue(json.containsKey("name"));
        assertTrue(json.containsKey("age"));
        assertTrue(json.containsKey("occupation"));
    }

    @Test
    void removeNullsFromJson_jsonWithSomeNullAttributes_nullsRemoved() {
        JSONObject json = new JSONObject();
        json.appendField("name", "Fred");
        json.appendField("age", 30);
        json.appendField("occupation", null);
        JsonTools.removeNullsFromJson(json);
        assertTrue(json.containsKey("name"));
        assertTrue(json.containsKey("age"));
        assertFalse(json.containsKey("occupation"));
    }

    @Test
    void removeNullsFromJson_jsonWithoutOnlyNullAttributes_allRemoved() {
        JSONObject json = new JSONObject();
        json.appendField("name", null);
        json.appendField("age", null);
        json.appendField("occupation", null);
        JsonTools.removeNullsFromJson(json);
        assertTrue(json.isEmpty());
    }

}