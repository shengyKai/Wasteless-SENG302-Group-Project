package org.seng302.leftovers.dto.business;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RankTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getThreshold_twoSequentialRanks_validOrdering() {
        Integer previousThreshold = 0; // Initial threshold to get to 2nd rank must be greater than 0 points
        for (Rank rank : Rank.values()) {
            Integer threshold = rank.getThreshold();
            if (threshold == null) {
                // If the threshold is null, then the previous rank must have had a threshold to reach this rank
                assertNotNull(previousThreshold);
            } else {
                // Threshold to this rank must be more than threshold to previous rank
                assertTrue(threshold > previousThreshold);
            }
            previousThreshold = rank.getThreshold();
        }
        assertNull(previousThreshold); // The final threshold must be null, since there are no more ranks
    }

    @ParameterizedTest
    @EnumSource(Rank.class)
    void getName_isLowercaseVersionOfEnumName(Rank rank) {
        assertEquals(rank.name().toLowerCase(), rank.getName());
    }

    @ParameterizedTest
    @EnumSource(Rank.class)
    void serialise_expectedFormat(Rank rank) {
        var json = objectMapper.convertValue(rank, JSONObject.class);
        assertEquals(rank.getName(), json.get("name"));

        if (rank.getThreshold() == null) { // If no threshold to next rank, the threshold shouldn't be included in the json
            assertFalse(json.containsKey("threshold"));
            assertEquals(1, json.size());
        } else {
            assertEquals(rank.getThreshold(), json.get("threshold"));
            assertEquals(2, json.size());
        }
    }
}
