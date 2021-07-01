package org.seng302.leftovers.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.leftovers.entities.Event;
import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.persistence.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EventServiceImpl implements EventService {
    private static final Logger LOGGER = LogManager.getLogger(EventServiceImpl.class);

    private final EventRepository eventRepository;

    /**
     * Mapping between user id and all their active SseEmitters
     */
    private final Map<Long, List<SseEmitter>> connections = new ConcurrentHashMap<>();

    @Autowired
    public EventServiceImpl(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    /**
     * Creates a SseEmitter for the provided user
     * This function will also immediately send all the user's events through the returned emitter
     * This is only expected to be called from EventController
     * @param user User to make emitter for
     * @return Newly created SseEmitter
     */
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

    /**
     * Adds a set of users to the provided event.
     * It will also immediately notify connected users of the event
     * This method should be used in place of eventRepository.save
     *
     * @param users Set of users to notify
     * @param event Event to notify users of
     * @return Updated event
     */
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

    /**
     * Emits an event through the provided emitter
     * @param emitter Emitter to send event to
     * @param event Event to send
     */
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
