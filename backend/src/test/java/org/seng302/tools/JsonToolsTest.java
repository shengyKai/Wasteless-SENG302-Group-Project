package org.seng302.tools;

import net.minidev.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

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

    private static Stream<Arguments> provideValidArgsForParseLongFromJsonField() {
        JSONObject json = new JSONObject();
        json.appendField("apple", 99907);
        json.appendField("banana", -33);
        json.appendField("carrot", 0);
        json.appendField("donut", 5L);
        return Stream.of(
                Arguments.of(json, "apple", 99907L),
                Arguments.of(json, "banana", -33L),
                Arguments.of(json, "carrot", 0L),
                Arguments.of(json, "donut", 5L)
        );
    }

    @ParameterizedTest
    @MethodSource("provideValidArgsForParseLongFromJsonField")
    void parseLongFromJsonField_fieldHasNumber_shouldReturnFieldValue(JSONObject json, String field, long expected) {
        assertEquals(expected, JsonTools.parseLongFromJsonField(json, field));
    }

    private static Stream<Arguments> provideInvalidArgsForParseLongFromJsonField() {
        JSONObject json = new JSONObject();
        json.appendField("egg", "egg");
        json.appendField("fig", new int[0]);
        json.appendField("gherkin", null);
        return Stream.of(
                Arguments.of(json, "egg"),
                Arguments.of(json, "fig"),
                Arguments.of(json, "gherkin"),
                Arguments.of(json, "hamburger")
        );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidArgsForParseLongFromJsonField")
    void parseLongFromJsonField_fieldInvalid_shouldThrowException(JSONObject json, String field) {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> JsonTools.parseLongFromJsonField(json, field));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(String.format("%s must be a number", field), exception.getReason());
    }

    private static Stream<Arguments> provideValidArgsForParseLongArrayFromJsonField() {
        JSONObject json = new JSONObject();
        json.appendField("apple", new int[0]);
        json.appendField("banana", new long[] {3L, -77L, 18L});
        json.appendField("carrot", new int[] {43255});
        json.appendField("donut", new int[100]);
        return Stream.of(
                Arguments.of(json, "apple", new long[0]),
                Arguments.of(json, "banana", new long[] {3L, -77L, 18L}),
                Arguments.of(json, "carrot", new long[] {43255}),
                Arguments.of(json, "donut", new long[100])
        );
    }

    @ParameterizedTest
    @MethodSource("provideValidArgsForParseLongArrayFromJsonField")
    void parseLongArrayFromJsonField_fieldHasNumberArray_shouldReturnFieldValue(JSONObject json, String field, long[] expected) {
        assertArrayEquals(expected, JsonTools.parseLongArrayFromJsonField(json, field));
    }

    private static Stream<Arguments> provideInvalidArgsForParseLongArrayFromJsonField() {
        JSONObject json = new JSONObject();
        json.appendField("egg", "egg");
        json.appendField("fig", 13);
        json.appendField("gherkin", null);
        return Stream.of(
                Arguments.of(json, "egg"),
                Arguments.of(json, "fig"),
                Arguments.of(json, "gherkin"),
                Arguments.of(json, "hamburger")
        );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidArgsForParseLongArrayFromJsonField")
    void parseLongArrayFromJsonField_fieldInvalid_shouldThrowException(JSONObject json, String field) {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> JsonTools.parseLongArrayFromJsonField(json, field));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(String.format("%s must be an array of numbers", field), exception.getReason());
    }



}