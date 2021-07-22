package org.seng302.leftovers.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.leftovers.entities.CreateKeywordEvent;
import org.seng302.leftovers.entities.Keyword;
import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class KeywordService {

    private static final Logger logger = LogManager.getLogger(KeywordService.class);
    private EventService eventService;
    private UserRepository userRepository;

    @Autowired
    public KeywordService(EventService eventService, UserRepository userRepository) {
        this.eventService = eventService;
        this.userRepository = userRepository;
    }

    public void sendNewKeywordEvent(Keyword keyword) {
        
        List<User> adminList = userRepository.findAllByRole("defaultGlobalApplicationAdmin");
        adminList.addAll(userRepository.findAllByRole("globalApplicationAdmin"));
        Set<User> adminSet = new HashSet<User>(adminList);

        logger.info("Adding new keyword and send notification to  \"{}\"", Arrays.toString(adminSet.toArray()));
        CreateKeywordEvent newKeywordEvent = new CreateKeywordEvent(keyword);
        eventService.addUsersToEvent(adminSet, newKeywordEvent);
    }
}

