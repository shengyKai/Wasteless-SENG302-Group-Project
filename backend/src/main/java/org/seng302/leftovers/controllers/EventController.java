package org.seng302.leftovers.controllers;

import net.minidev.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.leftovers.entities.GlobalMessageEvent;
import org.seng302.leftovers.dto.WrappedValueDTO;
import org.seng302.leftovers.entities.Event;
import org.seng302.leftovers.entities.Tag;
import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.persistence.EventRepository;
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
import javax.validation.Valid;

/**
 * Controller for the /events/* endpoints.
 * Provides access point for listening to events (GET /events/emitter) and posting new events
 */
@RestController
public class EventController {
    private static final Logger LOGGER = LogManager.getLogger(EventController.class);

    private final UserRepository userRepository;

    private final EventService eventService;

    private final EventRepository eventRepository;

    @Autowired
    public EventController(UserRepository userRepository, EventService eventService, EventRepository eventRepository) {
        this.userRepository = userRepository;
        this.eventService = eventService;
        this.eventRepository = eventRepository;
    }

    /**
     * PUT endpoint for setting the tag of a event.
     * @param eventId The event to be modified
     * @param body The request body
     */
    @PutMapping("/feed/{eventId}/tag")
    public void setEventTag(@PathVariable long eventId, @Valid @RequestBody WrappedValueDTO<Tag> body, HttpServletRequest request) {
        LOGGER.info("Requested update of event tag (eventId={}, tag={})", eventId, body.getValue());

        AuthenticationTokenManager.checkAuthenticationToken(request);
        try {
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Event not found"));

            if (!AuthenticationTokenManager.sessionCanSeePrivate(request, event.getNotifiedUser().getUserID())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Current user does not have permission to modify this event");
            }

            event.setTag(body.getValue());
            eventService.saveEvent(event);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw e;
        }
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

            String message = messageInfo.getAsString("message");
            userRepository.findAll().forEach(user -> eventService.saveEvent(new GlobalMessageEvent(user, message)));

            response.setStatus(201);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw e;
        }
    }

    /**
     * Deletes a event from the home feed of the user
     * @param id ID of the event to be deleted
     */
    @DeleteMapping("/feed/{id}")
    public void deleteEvent(HttpServletRequest request, @PathVariable Long id) {
        LOGGER.info("Request to delete event (id={}) from feed", id);
        try {
            // Check that authentication token is present and valid
            AuthenticationTokenManager.checkAuthenticationToken(request);
            Event event = eventRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Event not found, unable to delete"));

            if (!AuthenticationTokenManager.sessionCanSeePrivate(request, event.getNotifiedUser().getUserID())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Current user does not have permission to delete this event");
            }

            eventRepository.delete(event);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw e;
        }
    }
}
