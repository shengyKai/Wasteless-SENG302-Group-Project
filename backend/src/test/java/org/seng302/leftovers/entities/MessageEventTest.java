package org.seng302.leftovers.entities;

import net.minidev.json.JSONObject;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
class MessageEventTest {

    @Mock
    User mockUser;
    @Mock
    MarketplaceCard mockCard;
    @Mock
    Message mockMessage;
    @Mock
    Conversation mockConversation;


    MessageEvent messageEvent;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        var userJson = new JSONObject();
        userJson.appendField("id", 92);
        when(mockUser.constructPublicJson()).thenReturn(userJson);

        var messageJson = new JSONObject();
        messageJson.appendField("id", 20);
        when(mockMessage.constructJSONObject()).thenReturn(messageJson);

        var cardJson = new JSONObject();
        cardJson.appendField("id", 520);
        when(mockCard.constructJSONObject()).thenReturn(cardJson);

        when(mockMessage.getConversation()).thenReturn(mockConversation);
        when(mockConversation.getCard()).thenReturn(mockCard);

        messageEvent = new MessageEvent(mockUser, mockMessage);
    }

    @Test
    void constructJSONObject_jsonHasExpectedFormat() {
        var messageEventJson = messageEvent.constructJSONObject();
        assertEquals("MessageEvent", messageEventJson.getAsString("type"));
        assertEquals(messageEvent.getCreated().toString(), messageEventJson.getAsString("created"));
        assertEquals("none", messageEventJson.getAsString("tag"));
        assertEquals(messageEvent.getId(), messageEventJson.getAsNumber("id"));
        assertEquals(mockCard.constructJSONObject().toJSONString(), messageEventJson.getAsString("card"));
        assertEquals(mockMessage.constructJSONObject().toJSONString(), messageEventJson.getAsString("message"));
        assertEquals(6, messageEventJson.size());
    }

}