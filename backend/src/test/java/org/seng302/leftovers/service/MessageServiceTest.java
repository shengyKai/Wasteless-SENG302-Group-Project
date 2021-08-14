package org.seng302.leftovers.service;

import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.seng302.leftovers.entities.Conversation;
import org.seng302.leftovers.entities.Message;
import org.seng302.leftovers.entities.MessageEvent;
import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.persistence.MessageEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class MessageServiceTest {

    @Autowired
    MessageService messageService;
    @MockBean
    EventService eventService;
    @MockBean
    MessageEventRepository messageEventRepository;
    @Captor
    ArgumentCaptor<MessageEvent> messageEventArgumentCaptor;
    @Mock
    Conversation eventConversation;
    @Mock
    Conversation otherConversation;
    @Mock
    Message validMessage;
    @Mock
    Message invalidMessage;
    @Mock
    User buyer;
    @Mock
    User seller;
    @Mock
    User bystander;

    @Before
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(validMessage.getConversation()).thenReturn(eventConversation);
        when(invalidMessage.getConversation()).thenReturn(otherConversation);

    }

    @Test
    void notifyConversationParticipants_newConversation_messageSentToBuyer() {
        fail("Not yet implemented");
    }

    @Test
    void notifyConversationParticipants_newConversation_messageSentToSeller() {
        fail("Not yet implemented");
    }

    @Test
    void notifyConversationParticipants_existingConversation_buyerMessageUpdated() {
        fail("Not yet implemented");
    }

    @Test
    void notifyConversationParticipants_existingConversation_sellerMessageUpdated() {
        fail("Not yet implemented");
    }

    @Test
    void notifyConversationParticipants_messageEventConstructorThrowsException_eventNotSent() {
        fail("Not yet implemented");
    }

    @Test
    void notifyConversationParticipants_messageEventSetMessageThrowsException_eventNotSent() {
        fail("Not yet implemented");
    }

}