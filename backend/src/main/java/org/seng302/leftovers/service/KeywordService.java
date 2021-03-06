package org.seng302.leftovers.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.leftovers.dto.user.UserRole;
import org.seng302.leftovers.entities.Keyword;
import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.entities.event.KeywordCreatedEvent;
import org.seng302.leftovers.persistence.UserRepository;
import org.seng302.leftovers.persistence.event.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class responsible for sending notifications for keyword related events.
 */
@Service
public class KeywordService {

    private static final Logger logger = LogManager.getLogger(KeywordService.class);
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Autowired
    public KeywordService(EventRepository eventRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    /**
     * Sends create keyword event to default global application admin and all other global application admins.
     * @param keyword The keyword that has been created.
     */
    public void sendNewKeywordEvent(Keyword keyword, User creator) {
        List<User> adminList = userRepository.findAllByRole(UserRole.DGAA);
        adminList.addAll(userRepository.findAllByRole(UserRole.GAA));
        Set<User> adminSet = new HashSet<>(adminList);

        logger.info("Sending keyword creation notification for keyword \"{}\" to system administrators",
                keyword.getName());
        for (User admin : adminSet) {
            KeywordCreatedEvent newKeywordEvent = new KeywordCreatedEvent(admin, creator, keyword);
            eventRepository.save(newKeywordEvent);
        }
    }
}

