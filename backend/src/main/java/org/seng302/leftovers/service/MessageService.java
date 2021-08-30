package org.seng302.leftovers.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.leftovers.entities.Message;
import org.seng302.leftovers.entities.event.MessageEvent;
import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.persistence.MessageEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

/**
 * Class responsible for sending notifications relating to messages in a conversation.
 */
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

    /**
     * Send a notification containing the new message to both participants in the conversation.
     * If the notification cannot be sent an error will be added to the log but no exception will be thrown.
     * @param message The message which has been added to the conversation.
     * @param buyer The participant in the conversation who is not the owner of the marketplace card.
     * @param seller The participant in the conversation who is the owner of the marketplace card.
     */
    public void notifyConversationParticipants(Message message, User buyer, User seller) {
        for (User user : List.of(buyer, seller)) {
            logger.info("Notifying user (id={}) of new message in conversation (id={})", user.getUserID(), message.getConversation().getId());
            try {
                MessageEvent messageEvent;
                Optional<MessageEvent> optional = messageEventRepository.findByNotifiedUserAndConversation(user, message.getConversation());
                if (optional.isPresent()) {
                    messageEvent = optional.get();
                    messageEvent.setMessage(message);
                } else {
                    messageEvent = new MessageEvent(user, message);
                }
                eventService.saveEvent(messageEvent);
            } catch (ResponseStatusException e) {
                logger.error(e.getMessage());
            }
        }
    }

}
