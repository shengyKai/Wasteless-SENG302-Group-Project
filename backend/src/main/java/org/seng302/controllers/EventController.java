package org.seng302.controllers;

import net.minidev.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.entities.MessageEvent;
import org.seng302.entities.Event;
import org.seng302.entities.User;
import org.seng302.persistence.UserRepository;
import org.seng302.service.EventService;
import org.seng302.tools.AuthenticationTokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
public class EventController {
    private static final Logger LOGGER = LogManager.getLogger(EventController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventService eventService;

    @GetMapping("/events/emitter")
    public synchronized SseEmitter eventEmitter(@RequestParam long userId, HttpServletRequest request) {
        try {
            AuthenticationTokenManager.checkAuthenticationToken(request);
            AuthenticationTokenManager.sessionCanSeePrivate(request, userId);

            User user = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "User not found"));
            return eventService.createEmitterForUser(user);
        } catch (Exception e) {
            LOGGER.error(e);
            throw e;
        }
    }

    @PostMapping("/events/globalmessage")
    public void postDemoEvent(@RequestBody JSONObject messageInfo, HttpServletRequest request) {
        LOGGER.info("Posting a message to all users");

        try {
            AuthenticationTokenManager.checkAuthenticationToken(request);
            AuthenticationTokenManager.sessionCanSeePrivate(request, null);

            Event event = new MessageEvent(messageInfo.getAsString("message"));

            Set<User> allUsers = new HashSet<>();
            userRepository.findAll().forEach(allUsers::add);

            eventService.addUsersToEvent(allUsers, event);
        } catch (Exception e) {
            LOGGER.error(e);
            throw e;
        }
    }
}
