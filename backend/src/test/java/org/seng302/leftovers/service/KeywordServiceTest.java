package org.seng302.leftovers.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.seng302.leftovers.entities.Keyword;
import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.entities.event.Event;
import org.seng302.leftovers.entities.event.KeywordCreatedEvent;
import org.seng302.leftovers.persistence.event.EventRepository;
import org.seng302.leftovers.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class KeywordServiceTest {

    @Mock
    User mockDgaa;
    @Mock
    User mockGaa;
    @MockBean
    EventRepository eventRepository;
    @MockBean
    UserRepository userRepository;
    @Autowired
    KeywordService keywordService;

    @Captor
    ArgumentCaptor<String> stringArgumentCaptor;
    @Captor
    ArgumentCaptor<KeywordCreatedEvent> eventArgumentCaptor;

    Keyword keyword;
    @Mock
    User mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        List<User> dgaaList = new ArrayList<>();
        dgaaList.add(mockDgaa);
        when(userRepository.findAllByRole("defaultGlobalApplicationAdmin")).thenReturn(dgaaList);

        List<User> gaaList = new ArrayList<>();
        gaaList.add(mockGaa);
        when(userRepository.findAllByRole("globalApplicationAdmin")).thenReturn(gaaList);

        keyword = new Keyword("Blah");
    }

    @Test
    void sendNewKeywordsEvents_queriesUserRepositoryForAllAdmin() {
        keywordService.sendNewKeywordEvent(keyword, mockUser);
        Mockito.verify(userRepository, Mockito.times(2))
                .findAllByRole(stringArgumentCaptor.capture());

        List<String> capturedRole = stringArgumentCaptor.getAllValues();
        assertEquals("defaultGlobalApplicationAdmin", capturedRole.get(0));
        assertEquals("globalApplicationAdmin", capturedRole.get(1));
    }

    @Test
    void sendNewKeywordsEvents_callEventServiceWithAdminReturnFromUserRepository() {
        keywordService.sendNewKeywordEvent(keyword, mockUser);
        Mockito.verify(eventRepository, Mockito.times(2)).save(eventArgumentCaptor.capture());

        Set<User> notifiedUsers = eventArgumentCaptor.getAllValues().stream().map(Event::getNotifiedUser).collect(Collectors.toSet());
        assertEquals(Set.of(mockDgaa, mockGaa), notifiedUsers);
    }

    @Test
    void sendNewKeywordsEvents_callEventServiceWithKeywordAndUserPassedIntoMethod() {
        keywordService.sendNewKeywordEvent(keyword, mockUser);
        verify(eventRepository, Mockito.times(2)).save(eventArgumentCaptor.capture());

        for (KeywordCreatedEvent keywordCreatedEvent : eventArgumentCaptor.getAllValues()) {
            assertEquals(keyword, keywordCreatedEvent.getNewKeyword());
            assertEquals(mockUser, keywordCreatedEvent.getCreator());
        }
    }
}

