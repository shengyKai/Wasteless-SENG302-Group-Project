package org.seng302.leftovers.controllers;

import net.minidev.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.leftovers.entities.MessageEvent;
import org.seng302.leftovers.entities.Event;
import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.persistence.UserRepository;
import org.seng302.leftovers.service.EventService;
import org.seng302.leftovers.tools.AuthenticationTokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Controller for the /events/* endpoints.
 * Provides access point for listening to events (GET /events/emitter) and posting new events
 */
@RestController
public class EventController {
    private static final Logger LOGGER = LogManager.getLogger(EventController.class);

    private final UserRepository userRepository;

    private final EventService eventService;

    @Autowired
    public EventController(UserRepository userRepository, EventService eventService) {
        this.userRepository = userRepository;
        this.eventService = eventService;
    }

    /**
     * Gets the event stream for the given user id.
     * For a successful response the client must first be authenticated as the user or as an admin.
     * @param userId User to get event stream of
     */
    @GetMapping("/events/emitter")
    public synchronized SseEmitter eventEmitter(@RequestParam long userId, HttpServletRequest request, HttpServletResponse response) {
        try {
            AuthenticationTokenManager.checkAuthenticationToken(request);
            if (!AuthenticationTokenManager.sessionCanSeePrivate(request, userId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot subscribe to other user's event stream");
            }

            User user = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "User not found"));
            response.setHeader("X-Accel-Buffering", "no"); // Fix for Nginx sse issues
            return eventService.createEmitterForUser(user);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw e;
        }
    }

    /**
     * Posts a message to all users of the application.
     * This endpoint is only available to admin accounts
     * @param messageInfo Object containing message to send
     */
    @PostMapping("/events/globalmessage")
    public void postDemoEvent(@RequestBody JSONObject messageInfo, HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("Posting a message to all users");

        try {
            AuthenticationTokenManager.checkAuthenticationToken(request);
            if (!AuthenticationTokenManager.sessionIsAdmin(request)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient permissions to send global message");
            }

            Event event = new MessageEvent(messageInfo.getAsString("message"));

            Set<User> allUsers = new HashSet<>();
            userRepository.findAll().forEach(allUsers::add);

            eventService.addUsersToEvent(allUsers, event);
            response.setStatus(201);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw e;
        }
    }

    @DeleteMapping("/event/globalmessage")
    public void deleteEvent(HttpServletRequest request, @PathVariable Long id) {
        LOGGER.info("Request to delete event (id={})", id);
        try {
            // Check that authentication token is present and valid
            AuthenticationTokenManager.checkAuthenticationToken(request);


        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw e;
        }
    }
}
