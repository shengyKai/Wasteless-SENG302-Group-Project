package org.seng302.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.tools.AuthenticationTokenManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class EventController {
    private static final Logger LOGGER = LogManager.getLogger(EventController.class);

    // Since user could have multiple tabs open, then cover our bases.
    // More optimal solution may need to be looked into in the future
    private final Map<Long, List<SseEmitter>> connections = new ConcurrentHashMap<>();

    @GetMapping("/emitter")
    public synchronized SseEmitter eventEmitter(@RequestParam long userId, HttpServletRequest request) {
        AuthenticationTokenManager.checkAuthenticationToken(request);

        AuthenticationTokenManager.sessionCanSeePrivate(request, userId);


        SseEmitter emitter = new SseEmitter(30000L);

        List<SseEmitter> userConnections = connections.computeIfAbsent(userId, k -> new ArrayList<>());
        userConnections.add(emitter);
        emitter.onCompletion(() -> {
            LOGGER.info("Emitter done");
            userConnections.remove(emitter);
        });
        emitter.onTimeout(() -> {
            LOGGER.info("Emitter timed out");
            userConnections.remove(emitter);
        });

        return emitter;
    }

    @Scheduled(fixedRate = 2000)
    public void notifyUsers() {
        for (var entries : connections.entrySet()) {
            for (SseEmitter connection : entries.getValue()) {
                try {
                    connection.send(SseEmitter.event()
                            .name("newsfeed")
                            .id("seven")
                            .data("You are still " + entries.getKey()));
                } catch (Exception e) {
                    LOGGER.info("Send failed: {}", e.getMessage());
                    connection.completeWithError(e);
                }
            }
        }
    }
}
