package org.seng302.controllers;

import net.minidev.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.entities.DemoEvent;
import org.seng302.entities.Event;
import org.seng302.entities.User;
import org.seng302.persistence.UserRepository;
import org.seng302.service.EventService;
import org.seng302.tools.AuthenticationTokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class EventController {
    private static final Logger LOGGER = LogManager.getLogger(EventController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventService eventService;

    @GetMapping("/emitter")
    public synchronized SseEmitter eventEmitter(@RequestParam long userId, HttpServletRequest request) {
        AuthenticationTokenManager.checkAuthenticationToken(request);

        AuthenticationTokenManager.sessionCanSeePrivate(request, userId);

        return eventService.createEmitterForUser(userId);
    }

    @PostMapping("/demoevent")
    public void postDemoEvent(@RequestBody JSONObject messageInfo) {
        Event event = new DemoEvent(messageInfo.getAsString("message"));

        Set<User> allUsers = new HashSet<>();
        userRepository.findAll().forEach(allUsers::add);

        eventService.addUsersToEvent(allUsers, event);
    }
}
