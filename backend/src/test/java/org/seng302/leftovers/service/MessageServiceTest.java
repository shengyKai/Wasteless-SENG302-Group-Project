package org.seng302.leftovers.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.seng302.leftovers.entities.Conversation;
import org.seng302.leftovers.entities.MarketplaceCard;
import org.seng302.leftovers.entities.Message;
import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.entities.event.MessageEvent;
import org.seng302.leftovers.persistence.MessageEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
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
    Message firstMessage;
    @Mock
    Message secondMessage;
    @Mock
    Message otherConversationMessage;
    @Mock
    User buyer;
    @Mock
    User seller;
    @Mock
    User bystander;
    @Mock
    MarketplaceCard card;

    MessageEvent buyerMessageEvent;
    MessageEvent sellerMessageEvent;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(firstMessage.getConversation()).thenReturn(eventConversation);
        when(secondMessage.getConversation()).thenReturn(eventConversation);
        when(otherConversationMessage.getConversation()).thenReturn(otherConversation);

        when(buyer.getUserID()).thenReturn(1L);
        when(seller.getUserID()).thenReturn(2L);
        when(bystander.getUserID()).thenReturn(3L);

        when(eventConversation.getId()).thenReturn(4L);
        when(eventConversation.getBuyer()).thenReturn(buyer);
        when(eventConversation.getCard()).thenReturn(card);
        when(card.getCreator()).thenReturn(seller);

        buyerMessageEvent = new MessageEvent(buyer, firstMessage);
        sellerMessageEvent = new MessageEvent(seller, firstMessage);
    }

    @Test
    void notifyConversationParticipants_newConversation_messagesSentToParticipants() {
        when(messageEventRepository.findByNotifiedUserAndConversation(buyer, eventConversation)).thenReturn(Optional.empty());
        when(messageEventRepository.findByNotifiedUserAndConversation(seller, eventConversation)).thenReturn(Optional.empty());

        messageService.notifyConversationParticipants(firstMessage, buyer, seller);

        Mockito.verify(eventService, times(2)).saveEvent(messageEventArgumentCaptor.capture());

        List<MessageEvent> capturedEvents = messageEventArgumentCaptor.getAllValues();
        Set<User> notifiedUsers = capturedEvents.stream().map(MessageEvent::getNotifiedUser).collect(Collectors.toSet());
        assertTrue(notifiedUsers.contains(buyer));
        assertTrue(notifiedUsers.contains(seller));
    }

    @Test
    void notifyConversationParticipants_existingConversation_messagesSentToParticipants() {
        when(messageEventRepository.findByNotifiedUserAndConversation(buyer, eventConversation)).thenReturn(Optional.of(buyerMessageEvent));
        when(messageEventRepository.findByNotifiedUserAndConversation(seller, eventConversation)).thenReturn(Optional.of(sellerMessageEvent));

        messageService.notifyConversationParticipants(secondMessage, buyer, seller);

        Mockito.verify(eventService, times(2)).saveEvent(messageEventArgumentCaptor.capture());

        List<MessageEvent> capturedEvents = messageEventArgumentCaptor.getAllValues();
        Set<User> notifiedUsers = capturedEvents.stream().map(MessageEvent::getNotifiedUser).collect(Collectors.toSet());
        assertTrue(notifiedUsers.contains(buyer));
        assertTrue(notifiedUsers.contains(seller));
    }

    @Test
    void notifyConversationParticipants_invalidParticipant_eventNotSent() {
        when(messageEventRepository.findByNotifiedUserAndConversation(buyer, eventConversation)).thenReturn(Optional.empty());
        when(messageEventRepository.findByNotifiedUserAndConversation(seller, eventConversation)).thenReturn(Optional.empty());

        messageService.notifyConversationParticipants(firstMessage, bystander, seller);

        Mockito.verify(eventService, times(1)).saveEvent(messageEventArgumentCaptor.capture());

        List<MessageEvent> capturedEvents = messageEventArgumentCaptor.getAllValues();
        Set<User> notifiedUsers = capturedEvents.stream().map(MessageEvent::getNotifiedUser).collect(Collectors.toSet());
        assertFalse(notifiedUsers.contains(bystander));
        assertTrue(notifiedUsers.contains(seller));
    }

}