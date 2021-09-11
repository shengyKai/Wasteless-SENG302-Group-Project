package org.seng302.leftovers.controllers;

import net.minidev.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.leftovers.dto.WrappedValueDTO;
import org.seng302.leftovers.dto.event.EventDTO;
import org.seng302.leftovers.dto.event.EventStatus;
import org.seng302.leftovers.dto.event.EventTag;
import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.entities.event.Event;
import org.seng302.leftovers.entities.event.GlobalMessageEvent;
import org.seng302.leftovers.persistence.EventRepository;
import org.seng302.leftovers.persistence.UserRepository;
import org.seng302.leftovers.tools.AuthenticationTokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for the /events/* endpoints.
 * Provides access point for listening to events (GET /events/emitter) and posting new events
 */
@RestController
public class EventController {
    private static final Logger LOGGER = LogManager.getLogger(EventController.class);

    private final UserRepository userRepository;

    private final EventRepository eventRepository;

    @Autowired
    public EventController(UserRepository userRepository, EventRepository eventRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    /**
     * PUT endpoint for setting the tag of a event.
     * @param eventId The event to be modified
     * @param body The request body
     */
    @PutMapping("/feed/{eventId}/tag")
    public void setEventTag(@PathVariable long eventId, @Valid @RequestBody WrappedValueDTO<EventTag> body, HttpServletRequest request) {
        LOGGER.info("Requested update of event tag (eventId={}, tag={})", eventId, body.getValue());

        AuthenticationTokenManager.checkAuthenticationToken(request);
        try {
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Event not found"));

            if (!AuthenticationTokenManager.sessionCanSeePrivate(request, event.getNotifiedUser().getUserID())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Current user does not have permission to modify this event");
            }

            event.setTag(body.getValue());
            eventRepository.save(event);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw e;
        }
    }

    /**
     * Gets the events associated with a user. If a modifiedSince parameter is provided, will return only the events
     * which have been modified after that datetime. Otherwise, returns all the events associated with the given user.
     * For a successful response the client must first be authenticated as the user or as an admin.
     * @param userId User to get newsfeed events for.
     */
    @GetMapping("/users/{userId}/feed")
    public List<EventDTO> getEvents(@PathVariable long userId, @RequestParam(required = false) String modifiedSince, HttpServletRequest request, HttpServletResponse response) {
        try {
            LOGGER.info("Retrieving newsfeed events for user (id={})", userId);
            AuthenticationTokenManager.checkAuthenticationToken(request);
            if (!AuthenticationTokenManager.sessionCanSeePrivate(request, userId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot retrieve events associated with another user");
            }

            User user = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "User not found"));

            List<Event> events;
            if (modifiedSince != null) {
                Instant filterDate = convertModifiedSinceStringToInstant(modifiedSince);
                events = eventRepository.findEventsForUser(user, filterDate);
            } else {
                events = eventRepository.findEventsForUser(user);
            }

            return events.stream().map(Event::asDTO).collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw e;
        }
    }

    /**
     * Convert the string representation of the modifiedSince date to filter events by to an instant if it is in a valid
     * format. Throw a response status exception if it is not in a valid format.
     * @param modifiedSince A string to be converted to an instant.
     * @return An instant derived from the modifiedSince string.
     */
    private Instant convertModifiedSinceStringToInstant(String modifiedSince) {
        try {
            return Instant.parse(modifiedSince);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The modified since parameter must be in a valid " +
                    "datetime format which includes the date, time and timezone");
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
            userRepository.findAll().forEach(user -> eventRepository.save(new GlobalMessageEvent(user, message)));

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

    /**
     * PUT endpoint for marking the event as read
     * @param eventId The event to be marked as read
     */
    @PutMapping("/feed/{eventId}/read")
    public void updateEventAsRead(@PathVariable long eventId, HttpServletRequest request) {
        LOGGER.info("Requested update of event to be marked as read (eventId={})", eventId);

        AuthenticationTokenManager.checkAuthenticationToken(request);
        try {
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Event not found"));

            if (!AuthenticationTokenManager.sessionCanSeePrivate(request, event.getNotifiedUser().getUserID())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Current user does not have permission to mark this event as read");
            }

            event.markAsRead();
            eventRepository.save(event);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw e;
        }
    }

    /**
     * PUT endpoint for changing the status of an event to starred, archived or normal.
     * @param eventId The ID number of the event to change the status of.
     */
    @PutMapping("/feed/{eventId}/status")
    public void updateEventStatus(@PathVariable long eventId, @Valid @RequestBody WrappedValueDTO<EventStatus> body, HttpServletRequest request) {
        LOGGER.info("Requested update of event status (eventId={}, status={})", eventId, body.getValue());

        AuthenticationTokenManager.checkAuthenticationToken(request);
        try {
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Event not found"));

            if (!AuthenticationTokenManager.sessionCanSeePrivate(request, event.getNotifiedUser().getUserID())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Current user does not have permission to modify this event");
            }

            if (event.getStatus().equals(EventStatus.ARCHIVED)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The status of an archived event cannot be changed");
            }

            event.updateEventStatus(body.getValue());
            eventRepository.save(event);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw e;
        }
    }
}
