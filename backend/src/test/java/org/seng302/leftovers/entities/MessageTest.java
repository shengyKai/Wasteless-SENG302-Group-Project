package org.seng302.leftovers.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MessageTest {

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
        var exception = assertThrows(ResponseStatusException.class, () -> new Message(null, user, "Hello!"));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Message conversation cannot be null", exception.getReason());
    }

    @Test
    void createMessage_nullUser_400Thrown() {
        var exception = assertThrows(ResponseStatusException.class, () -> new Message(conversation, null, "Hello!"));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Message sender cannot be null", exception.getReason());
    }


    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   ", "\n", "\t"})
    void createMessage_noContent_400Thrown(String content) {
        var exception = assertThrows(ResponseStatusException.class, () -> new Message(conversation, user, content));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Message cannot be empty", exception.getReason());
    }

    @Test
    void createMessage_messageTooLong_400Thrown() {
        String content = "a".repeat(201);

        var exception = assertThrows(ResponseStatusException.class, () -> new Message(conversation, user, content));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Message must be 200 characters or less", exception.getReason());
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
}
