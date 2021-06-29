package org.seng302.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.controllers.EventController;
import org.seng302.entities.Event;
import org.seng302.entities.User;
import org.seng302.persistence.EventRepository;
import org.seng302.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EventServiceImpl implements EventService {
    private static final Logger LOGGER = LogManager.getLogger(EventServiceImpl.class);

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Mapping between user id and all their active SseEmitters
     */
    private final Map<Long, List<SseEmitter>> connections = new ConcurrentHashMap<>();

    @Override
    public SseEmitter createEmitterForUser(User user) {
        SseEmitter emitter = new SseEmitter(60000L);

        List<SseEmitter> userConnections = connections.computeIfAbsent(user.getUserID(), k -> new ArrayList<>());
        userConnections.add(emitter);
        emitter.onCompletion(() -> {
            LOGGER.info("Emitter done");
            userConnections.remove(emitter);
        });
        emitter.onTimeout(() -> {
            LOGGER.info("Emitter timed out");
            userConnections.remove(emitter);
        });

        LOGGER.info("Adding event emitter for user (id={}) total for user {}", user.getUserID(), userConnections.size());

        for (Event event : eventRepository.getAllByNotifiedUsersOrderByCreated(user)) {
            emitEvent(emitter, event);
        }

        return emitter;
    }

    @Override
    public Event addUsersToEvent(Set<User> users, Event event) {
        event.addUsers(users);
        event = eventRepository.save(event);

        LOGGER.info("Adding {} users to event (id={})", users.size(), event.getId());

        for (User user : users) {
            for (SseEmitter emitter : connections.getOrDefault(user.getUserID(), List.of())) {
                emitEvent(emitter, event);
            }
        }

        return event;
    }

    private void emitEvent(SseEmitter emitter, Event event) {
        try {
            emitter.send(SseEmitter.event()
                    .name("newsfeed")
                    .data(event.constructJSONObject(), MediaType.APPLICATION_JSON));
        } catch (Exception e) {
            LOGGER.warn("Send failed: {}", e.getMessage());
            emitter.completeWithError(e);
        }

    }
}
