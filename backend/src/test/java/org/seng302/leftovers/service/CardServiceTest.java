package org.seng302.leftovers.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.seng302.leftovers.entities.ExpiryEvent;
import org.seng302.leftovers.entities.MarketplaceCard;
import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.persistence.ExpiryEventRepository;
import org.seng302.leftovers.persistence.MarketplaceCardRepository;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

class CardServiceTest {

    CardService cardService;
    Instant mockedCurrentTime;
    MockedStatic<Instant> mockedInstant;

    @Mock
    MarketplaceCardRepository marketplaceCardRepository;
    @Mock
    EventService eventService;
    @Mock
    ExpiryEventRepository expiryEventRepository;
    @Mock
    User mockUser;
    @Mock
    MarketplaceCard mockCard1;
    @Mock
    MarketplaceCard mockCard2;
    @Mock
    MarketplaceCard mockCard3;

    @Captor
    ArgumentCaptor<Instant> instantArgumentCaptor;
    @Captor
    ArgumentCaptor<User> userArgumentCaptor;
    @Captor
    ArgumentCaptor<ExpiryEvent> expiryEventArgumentCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        cardService = new CardService(marketplaceCardRepository, eventService, expiryEventRepository);
        setUpCards();
        mockCurrentTime();
    }

    /**
     * Mock the static method Instant.now() so that when called it will return a set time.
     */
    private void mockCurrentTime() {
        String mockedCurrentTimeValue = "2021-01-01T12:00:00Z";
        mockedCurrentTime = Instant.parse(mockedCurrentTimeValue);
        mockedInstant = mockStatic(Instant.class);
        when(Instant.now()).thenReturn(mockedCurrentTime);
    }

    /**
     * Set up the ID numbers and users which will be returned by accessors in the mocked marketplace cards.
     */
    private void setUpCards() {
        when(mockCard1.getID()).thenReturn(32L);
        when(mockCard2.getID()).thenReturn(8456L);
        when(mockCard3.getID()).thenReturn(2L);
        when(mockCard1.getCreator()).thenReturn(mockUser);
        when(mockCard2.getCreator()).thenReturn(mockUser);
        when(mockCard3.getCreator()).thenReturn(mockUser);
    }

    /**
     * Use reflection to invoke the private sendCardExpiryEvents method in the CardService class.
     */
    private void invokeSendCardExpiryEvents() {
        try {
            Method sendCardExpiryEvents = CardService.class.getDeclaredMethod("sendCardExpiryEvents");
            sendCardExpiryEvents.setAccessible(true);
            sendCardExpiryEvents.invoke(cardService);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    void sendCardExpiryEvents_queriesMarketplaceRepositoryExpectedArguments() {
        invokeSendCardExpiryEvents();
        Mockito.verify(marketplaceCardRepository).getAllExpiringBefore(instantArgumentCaptor.capture());
        assertEquals(mockedCurrentTime.plus(Duration.ofDays(1)), instantArgumentCaptor.getValue());
    }

    @Test
    void sendCardExpiryEvents_queryReturnsNoCards_doesntSendEvents() {
        // Set up and empty list of marketplace cards which will be returned by the query on the mocked marketplace card repository
        List<MarketplaceCard> queryResult = new ArrayList<>();
        when(marketplaceCardRepository.getAllExpiringBefore(any())).thenReturn(queryResult);

        invokeSendCardExpiryEvents();

        // Check that events were not sent
        Mockito.verify(eventService, never()).addUserToEvent(any(), any());
    }

    @Test
    void sendCardExpiryEvents_queryReturnsOneCards_sendsOneEvent() {
        // Set up the marketplace cards which will be returned by the query on the mocked marketplace card repository
        List<MarketplaceCard> queryResult = new ArrayList<>();
        queryResult.add(mockCard1);
        when(marketplaceCardRepository.getAllExpiringBefore(any())).thenReturn(queryResult);

        invokeSendCardExpiryEvents();

        // Check that the method to create the events has been called
        Mockito.verify(eventService, times(1)).addUserToEvent(userArgumentCaptor.capture(), expiryEventArgumentCaptor.capture());

        // Check that the event was sent for the expected user and card
        assertEquals(mockUser, userArgumentCaptor.getValue());
        assertEquals(mockCard1, expiryEventArgumentCaptor.getValue().getExpiringCard());
    }

    @Test
    void sendCardExpiryEvents_queryReturnsMultipleCards_sendsMultipleEvents() {
        // Set up the marketplace cards which will be returned by the query on the mocked marketplace card repository
        List<MarketplaceCard> queryResult = new ArrayList<>(List.of(mockCard1, mockCard2, mockCard3));
        when(marketplaceCardRepository.getAllExpiringBefore(any())).thenReturn(queryResult);

        invokeSendCardExpiryEvents();

        // Check that the method to send the events has been called once for each card
        Mockito.verify(eventService, times(3)).addUserToEvent(any(), any());
    }


    @AfterEach
    void tearDown() {
        mockedInstant.close();
    }
}