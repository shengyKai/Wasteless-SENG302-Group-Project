package org.seng302.leftovers.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.seng302.leftovers.entities.Keyword;
import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.persistence.UserRepository;
import org.seng302.leftovers.entities.CreateKeywordEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
class KeywordServiceTest {

    @Mock
    User mockDgaa;
    @Mock
    User mockGaa;
    @MockBean
    EventService eventService;
    @MockBean
    UserRepository userRepository;
    @Autowired
    KeywordService keywordService;

    @Captor
    ArgumentCaptor<String> stringArgumentCaptor;
    @Captor
    ArgumentCaptor<Set<User>> adminArgumentCaptor;
    @Captor
    ArgumentCaptor<CreateKeywordEvent> eventArgumentCaptor;

    Keyword keyword;

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
        keywordService.sendNewKeywordEvent(keyword);
        Mockito.verify(userRepository, Mockito.times(2))
                .findAllByRole(stringArgumentCaptor.capture());

        List<String> capturedRole = stringArgumentCaptor.getAllValues();
        assertEquals("defaultGlobalApplicationAdmin", capturedRole.get(0));
        assertEquals("globalApplicationAdmin", capturedRole.get(1));
    }

    @Test
    void sendNewKeywordsEvents_callEventServiceWithAdminReturnFromUserRepository() {
        keywordService.sendNewKeywordEvent(keyword);
        Mockito.verify(eventService, Mockito.times(1))
                .addUsersToEvent(adminArgumentCaptor.capture(), eventArgumentCaptor.capture());

        Set<User> adminSet= adminArgumentCaptor.getValue();
        assertTrue(adminSet.contains(mockDgaa));
        assertTrue(adminSet.contains(mockGaa));
    }

    @Test
    void sendNewKeywordsEvents_callEventServiceWithKeywordPassedIntoMethod() {
        keywordService.sendNewKeywordEvent(keyword);
        Mockito.verify(eventService, Mockito.times(1))
                .addUsersToEvent(adminArgumentCaptor.capture(), eventArgumentCaptor.capture());

        CreateKeywordEvent createKeywordEvent = eventArgumentCaptor.getValue();
        assertEquals(keyword,createKeywordEvent.getNewKeyword());
    }
}

