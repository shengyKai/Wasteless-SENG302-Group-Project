`package org.seng302.leftovers.service;

import org.junit.jupiter.api.AfterEach;
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

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class KeywordServiceTest {

    @Mock
    User    userOne;
    @Mock
    User    userTwo;
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
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void sendNewKeywordsEvents_queriesUserRepositoryForAllAdmin() {
        Keyword keyword = new Keyword("Blah");
        keywordService.sendNewKeywordEvent(keyword);
        Mockito.verify(userRepository, Mockito.times(2)).findAllByRole(stringArgumentCaptor.capture());

        List<String> capturedRole = stringArgumentCaptor.getAllValues();
        assertEquals("defaultGlobalApplicationAdmin", capturedRole.get(0));
        assertEquals("globalApplicationAdmin", capturedRole.get(1));

    }

    @Test
    void sendNewKeywordsEvents_callEventServiceWithAdminReturnFromUserRepository() {
        List<User> adminList = new ArrayList<User>();
        adminList.add(userOne);
        adminList.add(userTwo);
        when(userRepository.findAllByRole(any())).thenReturn(adminList);

        Keyword keyword = new Keyword("Blah");
        keywordService.sendNewKeywordEvent(keyword);
        Mockito.verify(eventService, Mockito.times(1)).addUsersToEvent(adminArgumentCaptor.capture(), eventArgumentCaptor.capture());

        Set<User> adminSet= adminArgumentCaptor.getValue();
        assertTrue(adminSet.contains(userOne));
        assertTrue(adminSet.contains(userTwo));
    }

    @Test
    void sendNewKeywordsEvents_callEventServiceWithKeywordPassedIntoMethod() {
        List<User> adminList = new ArrayList<User>();
        adminList.add(userOne);
        adminList.add(userTwo);
        when(userRepository.findAllByRole(any())).thenReturn(adminList);

        Keyword keyword = new Keyword("Blah");
        keywordService.sendNewKeywordEvent(keyword);
        Mockito.verify(eventService, Mockito.times(1)).addUsersToEvent(adminArgumentCaptor.capture(), eventArgumentCaptor.capture());

        CreateKeywordEvent createKeywordEvent = eventArgumentCaptor.getValue();
        assertEquals(keyword,createKeywordEvent.getKeyword());

    }
}

