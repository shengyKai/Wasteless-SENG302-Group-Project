package org.seng302.leftovers.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.seng302.leftovers.exceptions.ValidationResponseException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
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
                Arguments.of(json, "egg", "egg must be a number"),
                Arguments.of(json, "fig", "fig must be a number"),
                Arguments.of(json, "gherkin", "gherkin must be a number"),
                Arguments.of(json, "hamburger", "hamburger is not present")
        );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidArgsForParseLongFromJsonField")
    void parseLongFromJsonField_fieldInvalid_shouldThrowException(JSONObject json, String field, String message) {
        var exception = assertThrows(ValidationResponseException.class, () -> JsonTools.parseLongFromJsonField(json, field));
        assertEquals(message, exception.getMessage());
    }

    private static Stream<Arguments> provideValidArgsForParseLongArrayFromJsonField() {
        JSONObject json = new JSONObject();
        json.appendField("apple", new int[0]);
        json.appendField("banana", new long[] {3L, -77L, 18L});
        json.appendField("carrot", new int[] {43255});
        json.appendField("donut", new int[100]);
        json.appendField("egg", Arrays.asList(1, 3, 9));
        json.appendField("fig", new ArrayList<Integer>());
        json.appendField("gherkin", new ArrayList<>(Arrays.asList(88L, 44L, 22L, 11L)));
        return Stream.of(
                Arguments.of(json, "apple", new long[0]),
                Arguments.of(json, "banana", new long[] {3L, -77L, 18L}),
                Arguments.of(json, "carrot", new long[] {43255}),
                Arguments.of(json, "donut", new long[100]),
                Arguments.of(json, "egg", new long[] {1L, 3L, 9L}),
                Arguments.of(json, "fig", new long[0]),
                Arguments.of(json, "gherkin", new long[] {88L, 44L, 22L, 11L})
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
                Arguments.of(json, "egg", "egg must be an array of numbers"),
                Arguments.of(json, "fig", "fig must be an array of numbers"),
                Arguments.of(json, "gherkin", "gherkin must be an array of numbers"),
                Arguments.of(json, "hamburger", "hamburger is not present")
        );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidArgsForParseLongArrayFromJsonField")
    void parseLongArrayFromJsonField_fieldInvalid_shouldThrowException(JSONObject json, String field, String message) {
        var exception = assertThrows(ValidationResponseException.class, () -> JsonTools.parseLongArrayFromJsonField(json, field));
        assertEquals(message, exception.getMessage());
    }

    private <T> Page<T> createPage(List<T> items, long totalItems) {
        return new PageImpl<>(items, Pageable.unpaged(), totalItems);
    }

    @Test
    void constructPageJSON_emptyPage_emptyPageRepresentation() throws JsonProcessingException {
        Page<JSONObject> page = createPage(List.of(), 0);

        JSONObject expectedJSON = new JSONObject();
        expectedJSON.put("count", 0);
        expectedJSON.put("results", new JSONArray());

        JSONObject actualJSON = JsonTools.constructPageJSON(page);

        ObjectMapper objectMapper = new ObjectMapper();
        assertEquals(objectMapper.readTree(expectedJSON.toJSONString()), objectMapper.readTree(actualJSON.toJSONString()));
    }

    @Test
    void constructPageJSON_pageWithItems_pageWithItemsRepresentation() throws JsonProcessingException {
        JSONObject object1 = new JSONObject();
        object1.put("id", 1);
        object1.put("name", "Free");
        JSONObject object2 = new JSONObject();
        object2.put("id", 4);
        object2.put("name", "Seven");
        List<JSONObject> objects = List.of(object1, object2);

        Page<JSONObject> page = createPage(objects, 99);

        JSONObject expectedJSON = new JSONObject();
        expectedJSON.put("count", 99);
        expectedJSON.put("results", objects);

        JSONObject actualJSON = JsonTools.constructPageJSON(page);

        ObjectMapper objectMapper = new ObjectMapper();
        assertEquals(objectMapper.readTree(expectedJSON.toJSONString()), objectMapper.readTree(actualJSON.toJSONString()));
    }

    @Test
    void constructPageJSON_mappedItems_pageWithMappedRepresentation() throws JsonProcessingException {
        List<Long> items = List.of(3L, 5L);

        Function<Long, JSONObject> mapping = value -> {
            JSONObject result = new JSONObject();
            result.put("value", value);
            result.put("squared", value*value);
            return result;
        };

        Page<Long> page = createPage(items, 70);

        JSONObject expectedJSON = new JSONObject();
        expectedJSON.put("count", 70);
        expectedJSON.put("results", items.stream().map(mapping).collect(Collectors.toList()));

        JSONObject actualJSON = JsonTools.constructPageJSON(page.map(mapping));

        ObjectMapper objectMapper = new ObjectMapper();
        assertEquals(objectMapper.readTree(expectedJSON.toJSONString()), objectMapper.readTree(actualJSON.toJSONString()));
    }
}