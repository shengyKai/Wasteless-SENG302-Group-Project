package org.seng302.leftovers.entities.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.seng302.leftovers.dto.conversation.ConversationDTO;
import org.seng302.leftovers.dto.conversation.MessageDTO;
import org.seng302.leftovers.entities.*;
import org.seng302.leftovers.exceptions.InternalErrorResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
class MessageEventTest {
    @Autowired
    private ObjectMapper mapper;

    @Mock
    private User buyer;
    @Mock
    private Location address;
    @Mock
    private User seller;
    @Mock
    private User bystander;
    @Mock
    private MarketplaceCard card;
    @Mock
    private Message firstMessage;
    @Mock
    private Message secondMessage;
    @Mock
    private Conversation eventConversation;
    @Mock
    private Conversation otherConversation;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(buyer.getAddress()).thenReturn(address);
        when(seller.getAddress()).thenReturn(address);

        when(card.getID()).thenReturn(520L);

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
    void messageEventConstructor_notifiedUserIsNotConversationParticipant_InternalErrorResponseExceptionThrown() {
        var exception = assertThrows(InternalErrorResponseException.class, () -> new MessageEvent(bystander, firstMessage));
        assertEquals("Notification can only be sent to buyer or seller of card", exception.getMessage());
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
    void setMessage_messageNotInConversation_InternalErrorResponseExceptionThrown() {
        when(secondMessage.getConversation()).thenReturn(otherConversation);
        var messageEvent = new MessageEvent(seller, firstMessage);

        var exception = assertThrows(InternalErrorResponseException.class, () -> messageEvent.setMessage(secondMessage));
        assertEquals("The message associated with a message event can only be changed to a new message in the " +
                "original conversation.", exception.getMessage());
    }

    @Test
    void setMessage_messageNotInConversation_messageUnchanged() {
        when(secondMessage.getConversation()).thenReturn(otherConversation);
        var messageEvent = new MessageEvent(seller, firstMessage);

        assertThrows(InternalErrorResponseException.class, () -> messageEvent.setMessage(secondMessage));
        assertEquals(firstMessage, messageEvent.getMessage());
    }

    @Test
    void setMessage_messageNotInConversation_createdUnchanged() {
        when(secondMessage.getConversation()).thenReturn(otherConversation);
        var messageEvent = new MessageEvent(seller, firstMessage);
        var createdBefore = messageEvent.getCreated();

        assertThrows(InternalErrorResponseException.class, () -> messageEvent.setMessage(secondMessage));
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
        assertEquals(messageEvent.getLastModified().toString(), messageEventJson.get("lastModified"));
        assertEquals(10, messageEventJson.size());
    }

}