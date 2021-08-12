package org.seng302.leftovers.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.leftovers.entities.Conversation;
import org.seng302.leftovers.entities.Message;
import org.seng302.leftovers.entities.MessageEvent;
import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.persistence.MessageEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MessageService {

    private final EventService eventService;
    private final MessageEventRepository messageEventRepository;
    private final Logger logger = LogManager.getLogger(MessageService.class.getName());

    @Autowired
    public MessageService(EventService eventService, MessageEventRepository messageEventRepository) {
        this.eventService = eventService;
        this.messageEventRepository = messageEventRepository;
    }

    public void notifyConversationParticipants(Message message, User buyer, User seller) {
        for (User user : List.of(buyer, seller)) {
            logger.info("Notifying user {} of new message in conversation {}", user.getUserID(), message.getConversation().getId());

            MessageEvent messageEvent;
            Optional<MessageEvent> optional = messageEventRepository.findByNotifiedUserAndConversation(user, message.getConversation());
            if (optional.isPresent()) {
                messageEvent = optional.get();
                messageEvent.setMessage(message);
            } else {
                messageEvent = new MessageEvent(user, message);
            }

            eventService.saveEvent(messageEvent);
        }
    }

}
