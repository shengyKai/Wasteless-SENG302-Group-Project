package org.seng302.leftovers.entities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.seng302.leftovers.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CreateKeywordEventTest {

    @Autowired
    KeywordRepository keywordRepository;
    @Autowired
    EventRepository eventRepository;
    Keyword keyword;

    @BeforeEach
    void setUp() {
        keyword = new Keyword("Test");
        keyword = keywordRepository.save(keyword);
    }

    @AfterEach
    void tearDown() {
        eventRepository.deleteAll();
        keywordRepository.deleteAll();
    }

    @Test
    void constructJSONObject_jsonHasExpectedFormat() throws JsonProcessingException {
        CreateKeywordEvent event = new CreateKeywordEvent(keyword);
        event = eventRepository.save(event);

        String expectedJsonString = String.format(
                        "{\"id\":%d," +
                        "\"created\":\"%s\"," +
                        "\"type\":\"CreateKeywordEvent\"," +
                        "\"keyword\":%s}",
                event.getId(),
                event.getCreated(),
                keyword.constructJSONObject().toJSONString());
        String actualJsonString = event.constructJSONObject().toJSONString();
        ObjectMapper mapper = new ObjectMapper();
        assertEquals(mapper.readTree(expectedJsonString), mapper.readTree(actualJsonString));
    }

}