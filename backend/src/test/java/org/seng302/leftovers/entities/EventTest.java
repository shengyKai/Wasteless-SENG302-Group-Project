package org.seng302.leftovers.entities;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EventTest {

    @Test
    void createEvent_any_creationTimeCorrect() {
        var before = Instant.now();
        Event event = new EventSubclass();
        var after = Instant.now();

        assertFalse(event.getCreated().isBefore(before));
        assertFalse(event.getCreated().isAfter(after));
    }

    // Event is static so we need to subclass
    static class EventSubclass extends Event {}
}
