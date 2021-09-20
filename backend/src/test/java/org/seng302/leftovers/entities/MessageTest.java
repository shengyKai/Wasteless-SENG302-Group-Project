package org.seng302.leftovers.entities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.seng302.leftovers.dto.MessageDTO;
import org.seng302.leftovers.exceptions.ValidationResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MessageTest {
    @Autowired
    private ObjectMapper mapper;

    @Mock
    private User user;

    @Mock
    private Conversation conversation;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createMessage_nullConversation_400Thrown() {
        var exception = assertThrows(ValidationResponseException.class, () -> new Message(null, user, "Hello!"));
        assertEquals("Message conversation cannot be null", exception.getMessage());
    }

    @Test
    void createMessage_nullUser_400Thrown() {
        var exception = assertThrows(ValidationResponseException.class, () -> new Message(conversation, null, "Hello!"));
        assertEquals("Message sender cannot be null", exception.getMessage());
    }


    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   ", "\n", "\t"})
    void createMessage_noContent_400Thrown(String content) {
        var exception = assertThrows(ValidationResponseException.class, () -> new Message(conversation, user, content));
        assertEquals("Message cannot be empty", exception.getMessage());
    }

    @Test
    void createMessage_messageTooLong_400Thrown() {
        String content = "a".repeat(201);

        var exception = assertThrows(ValidationResponseException.class, () -> new Message(conversation, user, content));
        assertEquals("Message must be 200 characters or less", exception.getMessage());
    }

    @Test
    void createMessage_validContent_noException() {
        String content = "a".repeat(200);
        var user = mock(User.class);
        assertDoesNotThrow(() -> new Message(conversation, user, content));
    }

    @Test
    void createMessage_validParameters_setsValidCreationTime() {
        var before = Instant.now();
        var message = new Message(conversation, user, "Hello world!");
        var after = Instant.now();

        assertFalse(before.isAfter(message.getCreated()));
        assertFalse(after.isBefore(message.getCreated()));
    }

    @Test
    void asDTO_jsonHasExpectedFormat() throws JsonProcessingException {
        JSONObject userJson = new JSONObject();
        userJson.appendField("id", 967);
        when(conversation.getId()).thenReturn(44L);
        Message message = new Message(conversation, user, "Hello world!");


        var json = mapper.convertValue(new MessageDTO(message), JSONObject.class);
        assertEquals(message.getId(), json.getAsNumber("id"));
        assertEquals(message.getSender().getUserID(), (Long) json.getAsNumber("senderId"));
        assertEquals(message.getCreated().toString(), json.getAsString("created"));
        assertEquals(message.getContent(), json.getAsString("content"));
        assertEquals(4, json.size());
    }
}
