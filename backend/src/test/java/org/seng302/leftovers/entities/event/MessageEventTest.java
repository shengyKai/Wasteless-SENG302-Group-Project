package org.seng302.leftovers.entities.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.seng302.leftovers.dto.ConversationDTO;
import org.seng302.leftovers.dto.MessageDTO;
import org.seng302.leftovers.entities.Conversation;
import org.seng302.leftovers.entities.MarketplaceCard;
import org.seng302.leftovers.entities.Message;
import org.seng302.leftovers.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
class MessageEventTest {
    @Autowired
    private ObjectMapper mapper;

    @Mock
    User buyer;
    @Mock
    User seller;
    @Mock
    User bystander;
    @Mock
    MarketplaceCard card;
    @Mock
    Message firstMessage;
    @Mock
    Message secondMessage;
    @Mock
    Conversation eventConversation;
    @Mock
    Conversation otherConversation;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        var buyerJson = new JSONObject();
        buyerJson.appendField("id", 92);
        when(buyer.constructPublicJson()).thenReturn(buyerJson);

        var sellerJson = new JSONObject();
        sellerJson.appendField("id", 38);
        when(seller.constructPublicJson()).thenReturn(sellerJson);

        var cardJson = new JSONObject();
        cardJson.appendField("id", 520);
        when(card.constructJSONObject()).thenReturn(cardJson);

        when(buyer.getUserID()).thenReturn(92L);
        when(seller.getUserID()).thenReturn(38L);
        when(bystander.getUserID()).thenReturn(171L);

        when(firstMessage.getConversation()).thenReturn(eventConversation);
        when(firstMessage.getSender()).thenReturn(seller);

        when(eventConversation.getCard()).thenReturn(card);
        when(eventConversation.getBuyer()).thenReturn(buyer);
        when(eventConversation.getCard().getCreator()).thenReturn(seller);
        when(eventConversation.getId()).thenReturn(13L);

        when(otherConversation.getId()).thenReturn(99L);
    }

    @Test
    void messageEventConstructor_notifiedUserIsSeller_participantTypeSetToSeller() {
        var messageEvent = new MessageEvent(seller, firstMessage);
        assertEquals(MessageEvent.ParticipantType.SELLER, messageEvent.getParticipantType());
    }

    @Test
    void messageEventConstructor_notifiedUserIsBuyer_participantTypeSetToBuyer() {
        var messageEvent = new MessageEvent(buyer, firstMessage);
        assertEquals(MessageEvent.ParticipantType.BUYER, messageEvent.getParticipantType());
    }

    @Test
    void messageEventConstructor_notifiedUserIsNotConversationParticipant_responseStatusExceptionThrown() {
        var exception = assertThrows(ResponseStatusException.class, () -> new MessageEvent(bystander, firstMessage));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());
        assertEquals("Notification can only be sent to buyer or seller of card", exception.getReason());
    }

    @Test
    void setMessage_messageInConversation_messageUpdated() {
        when(secondMessage.getConversation()).thenReturn(eventConversation);
        var messageEvent = new MessageEvent(seller, firstMessage);
        messageEvent.setMessage(secondMessage);
        assertEquals(secondMessage, messageEvent.getMessage());
    }

    @Test
    void setMessage_messageInConversation_createdUpdated() {
        when(secondMessage.getConversation()).thenReturn(eventConversation);
        var messageEvent = new MessageEvent(seller, firstMessage);

        var before = Instant.now();
        messageEvent.setMessage(secondMessage);
        var after = Instant.now();

        assertFalse(before.isAfter(messageEvent.getCreated()));
        assertFalse(after.isBefore(messageEvent.getCreated()));
    }

    @Test
    void setMessage_messageNotInConversation_responseStatusExceptionThrown() {
        when(secondMessage.getConversation()).thenReturn(otherConversation);
        var messageEvent = new MessageEvent(seller, firstMessage);

        var exception = assertThrows(ResponseStatusException.class, () -> messageEvent.setMessage(secondMessage));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());
        assertEquals("The message associated with a message event can only be changed to a new message in the " +
                "original conversation.", exception.getReason());
    }

    @Test
    void setMessage_messageNotInConversation_messageUnchanged() {
        when(secondMessage.getConversation()).thenReturn(otherConversation);
        var messageEvent = new MessageEvent(seller, firstMessage);

        assertThrows(ResponseStatusException.class, () -> messageEvent.setMessage(secondMessage));
        assertEquals(firstMessage, messageEvent.getMessage());
    }

    @Test
    void setMessage_messageNotInConversation_createdUnchanged() {
        when(secondMessage.getConversation()).thenReturn(otherConversation);
        var messageEvent = new MessageEvent(seller, firstMessage);
        var createdBefore = messageEvent.getCreated();

        assertThrows(ResponseStatusException.class, () -> messageEvent.setMessage(secondMessage));
        assertEquals(createdBefore, messageEvent.getCreated());
    }

    @Test
    void asDTO_jsonHasExpectedFormat() throws JsonProcessingException {
        var messageEvent = new MessageEvent(buyer, firstMessage);
        var messageEventJson = mapper.convertValue(messageEvent.asDTO(), JSONObject.class);

        assertEquals("MessageEvent", messageEventJson.getAsString("type"));
        assertEquals(messageEvent.getCreated().toString(), messageEventJson.getAsString("created"));
        assertEquals("none", messageEventJson.getAsString("tag"));
        assertEquals(messageEvent.getStatus().toString().toLowerCase(), messageEventJson.getAsString("status"));
        assertEquals(messageEvent.getId(), messageEventJson.getAsNumber("id"));

        assertEquals(
            mapper.readTree(mapper.writeValueAsString(new ConversationDTO(messageEvent.getConversation()))),
            mapper.readTree(mapper.writeValueAsString(messageEventJson.get("conversation")))
        );
        assertEquals(
            mapper.readTree(mapper.writeValueAsString(new MessageDTO(messageEvent.getMessage()))),
            mapper.readTree(mapper.writeValueAsString(messageEventJson.get("message")))
        );

        assertEquals("buyer", messageEventJson.getAsString("participantType"));
        assertEquals(messageEvent.isRead(), messageEventJson.get("read"));
        assertEquals(9, messageEventJson.size());
    }

}