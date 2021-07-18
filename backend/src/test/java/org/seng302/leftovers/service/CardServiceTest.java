package org.seng302.leftovers.service;

import org.apache.catalina.filters.ExpiresFilter;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
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
import java.util.Optional;

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
    SessionFactory sessionFactory;
    @Mock
    Session mockSession;
    @Mock
    User mockUser;
    @Mock
    MarketplaceCard mockCard1;
    @Mock
    MarketplaceCard mockCard2;
    @Mock
    MarketplaceCard mockCard3;
    @Mock
    ExpiryEvent expiryEvent1;
    @Mock
    ExpiryEvent expiryEvent2;
    @Mock
    ExpiryEvent expiryEvent3;

    @Captor
    ArgumentCaptor<Instant> instantArgumentCaptor;
    @Captor
    ArgumentCaptor<User> userArgumentCaptor;
    @Captor
    ArgumentCaptor<ExpiryEvent> expiryEventArgumentCaptor;
    @Captor
    ArgumentCaptor<MarketplaceCard> marketplaceCardArgumentCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        cardService = new CardService(marketplaceCardRepository, eventService, expiryEventRepository, sessionFactory);
        setUpCards();
        mockCurrentTime();
        when(sessionFactory.openSession()).thenReturn(mockSession);

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
        when(mockSession.find(MarketplaceCard.class, 32L)).thenReturn(mockCard1);
        when(mockSession.find(MarketplaceCard.class, 8456L)).thenReturn(mockCard2);
        when(mockSession.find(MarketplaceCard.class, 2L)).thenReturn(mockCard3);
    }

    /**
     * Use reflection to invoke the private sendCardExpiryEvents method in the CardService class.
     */
    private void invokeInitiateCardCheckEvents() {
        try {
            Method initiateCardCheckEvents = CardService.class.getDeclaredMethod("initiateCardCheckEvents");
            initiateCardCheckEvents.setAccessible(true);
            initiateCardCheckEvents.invoke(cardService);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    void sendCardExpiryEvents_queriesMarketplaceRepositoryExpectedArguments() {
        invokeInitiateCardCheckEvents();
        Mockito.verify(marketplaceCardRepository).getAllExpiringBeforeWithoutEvent(instantArgumentCaptor.capture());
        assertEquals(mockedCurrentTime.plus(Duration.ofDays(1)), instantArgumentCaptor.getValue());
    }

    @Test
    void sendCardExpiryEvents_queryReturnsNoCards_doesntSendEvents() {
        // Set up and empty list of marketplace cards which will be returned by the query on the mocked marketplace card repository
        List<MarketplaceCard> queryResult = new ArrayList<>();
        when(marketplaceCardRepository.getAllExpiringBeforeWithoutEvent(any())).thenReturn(queryResult);

        invokeInitiateCardCheckEvents();

        // Check that events were not sent
        Mockito.verify(eventService, never()).addUserToEvent(any(), any());
    }

    @Test
    void sendCardExpiryEvents_queryReturnsOneCards_sendsOneEvent() {
        // Set up the marketplace cards which will be returned by the query on the mocked marketplace card repository
        List<MarketplaceCard> queryResult = new ArrayList<>();
        queryResult.add(mockCard1);
        when(marketplaceCardRepository.getAllExpiringBeforeWithoutEvent(any())).thenReturn(queryResult);

        invokeInitiateCardCheckEvents();

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
        when(marketplaceCardRepository.getAllExpiringBeforeWithoutEvent(any())).thenReturn(queryResult);

        invokeInitiateCardCheckEvents();

        // Check that the method to send the events has been called once for each card
        Mockito.verify(eventService, times(3)).addUserToEvent(any(), any());
    }

    @Test
    void cardExpiredWithExpiryEvent_queryReturnsOneCardAndExpiryEvent_deletesOneCardAndExpiryEvent() {
        List<MarketplaceCard> queryResult = new ArrayList<>(List.of(mockCard1));
        when(marketplaceCardRepository.getAllExpiredBefore(any())).thenReturn(queryResult);
        when(expiryEventRepository.getByExpiringCard(any())).thenReturn(Optional.of(expiryEvent1));
        
        invokeInitiateCardCheckEvents();

        Mockito.verify(expiryEventRepository, times(1)).delete(expiryEventArgumentCaptor.capture());

        Mockito.verify(marketplaceCardRepository, times(1)).delete(marketplaceCardArgumentCaptor.capture());
    }

    @Test
    void cardExpiredWithNoExpiryEvent_queryReturnsOneCard_deletesOneCardOnly() {
        List<MarketplaceCard> queryResult = new ArrayList<>(List.of(mockCard1));
        when(marketplaceCardRepository.getAllExpiredBefore(any())).thenReturn(queryResult);
        when(expiryEventRepository.getByExpiringCard(any())).thenReturn(Optional.empty());
        
        invokeInitiateCardCheckEvents();

        Mockito.verify(expiryEventRepository, times(0)).delete(expiryEventArgumentCaptor.capture());

        Mockito.verify(marketplaceCardRepository, times(1)).delete(marketplaceCardArgumentCaptor.capture());
    }

    @Test
    void multipleCardsExpiredWithMutlipleExpiryEvents_queryReturnsMutlipleCardsAndExpiryEvents_deletesAllExpiredCardsAndExpiryEvents() {
        List<MarketplaceCard> queryResult = new ArrayList<>(List.of(mockCard1, mockCard2, mockCard3));
        when(marketplaceCardRepository.getAllExpiredBefore(any())).thenReturn(queryResult);
        when(expiryEventRepository.getByExpiringCard(mockCard1)).thenReturn(Optional.of(expiryEvent1));
        when(expiryEventRepository.getByExpiringCard(mockCard2)).thenReturn(Optional.of(expiryEvent2));
        when(expiryEventRepository.getByExpiringCard(mockCard3)).thenReturn(Optional.of(expiryEvent3));

        invokeInitiateCardCheckEvents();

        Mockito.verify(expiryEventRepository, times(3)).delete(expiryEventArgumentCaptor.capture());

        Mockito.verify(marketplaceCardRepository, times(3)).delete(marketplaceCardArgumentCaptor.capture());
    }

    @Test
    void multipleCardsExpiredWithNoExpiryEvent_queryReturnsMultipleCards_deletesAllExpiredCardsOnly() {
        List<MarketplaceCard> queryResult = new ArrayList<>(List.of(mockCard1, mockCard2, mockCard3));
        when(marketplaceCardRepository.getAllExpiredBefore(any())).thenReturn(queryResult);
        when(expiryEventRepository.getByExpiringCard(any())).thenReturn(Optional.empty());
        
        invokeInitiateCardCheckEvents();

        Mockito.verify(expiryEventRepository, times(0)).delete(expiryEventArgumentCaptor.capture());

        Mockito.verify(marketplaceCardRepository, times(3)).delete(marketplaceCardArgumentCaptor.capture());
    }

    
    @Test
    void multipleCardsExpiredWithSomeExpiryEvents_queryReturnsMultipleCardsAndSomeExpiryEvents_deletesAllExpiredCardsOnly() {
        List<MarketplaceCard> queryResult = new ArrayList<>(List.of(mockCard1, mockCard2, mockCard3));
        when(marketplaceCardRepository.getAllExpiredBefore(any())).thenReturn(queryResult);
        when(expiryEventRepository.getByExpiringCard(mockCard1)).thenReturn(Optional.of(expiryEvent1));
        when(expiryEventRepository.getByExpiringCard(mockCard2)).thenReturn(Optional.empty());
        when(expiryEventRepository.getByExpiringCard(mockCard3)).thenReturn(Optional.of(expiryEvent3));
        
        invokeInitiateCardCheckEvents();

        Mockito.verify(expiryEventRepository, times(2)).delete(expiryEventArgumentCaptor.capture());

        Mockito.verify(marketplaceCardRepository, times(3)).delete(marketplaceCardArgumentCaptor.capture());
    }

    @Test
    void noCardsExpiredNoExpiryEvent_queryReturnsNoCardsOrExpiryEvents_nothingDeleted() {
        List<MarketplaceCard> queryResult = new ArrayList<>(List.of());
        when(marketplaceCardRepository.getAllExpiredBefore(any())).thenReturn(queryResult);
        when(expiryEventRepository.getByExpiringCard(any())).thenReturn(Optional.empty());
        
        invokeInitiateCardCheckEvents();

        Mockito.verify(expiryEventRepository, times(0)).delete(expiryEventArgumentCaptor.capture());

        Mockito.verify(marketplaceCardRepository, times(0)).delete(marketplaceCardArgumentCaptor.capture());
    }

    @AfterEach
    void tearDown() {
        mockedInstant.close();
    }
}