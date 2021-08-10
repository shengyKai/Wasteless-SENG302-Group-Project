package org.seng302.leftovers.controllers;

import net.minidev.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.leftovers.entities.Conversation;
import org.seng302.leftovers.entities.Message;
import org.seng302.leftovers.persistence.*;
import org.seng302.leftovers.tools.AuthenticationTokenManager;
import org.seng302.leftovers.tools.JsonTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Set;


@RestController
public class ConversationController {
    private final MarketplaceCardRepository marketplaceCardRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final Logger logger = LogManager.getLogger(CardController.class.getName());


    @Autowired
    public ConversationController(MarketplaceCardRepository marketplaceCardRepository, ConversationRepository conversationRepository, UserRepository userRepository, MessageRepository messageRepository) {
        this.marketplaceCardRepository = marketplaceCardRepository;
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
    }

    /**
     * POST endpoint for posting a new message to a conversation regarding a marketplace card.
     * A conversation consists of a marketplace card and a potential buyer
     * The conversation may not exist, if so a new conversation is created
     * @param cardId The ID of the card
     * @param buyerId The ID of the potential buyer
     * @param body Consists of senderId and message content
     */
    @PostMapping("/cards/{cardId}/conversations/{buyerId}")
    public ResponseEntity postMarketplaceCardMessage(HttpServletRequest request, @PathVariable Long cardId, @PathVariable Long buyerId, @RequestBody JSONObject body) {
        AuthenticationTokenManager.checkAuthenticationToken(request);

        var card = marketplaceCardRepository.getCard(cardId, HttpStatus.NOT_ACCEPTABLE);
        var buyer = userRepository.getUser(buyerId);
        var senderId = JsonTools.parseLongFromJsonField(body, "senderId");
        var content = JsonTools.parseStringFromJsonField(body, "message");
        var sender = userRepository.getUser(senderId);

        // Is the currently logged in user == senderId or an admin
        if (!AuthenticationTokenManager.sessionCanSeePrivate(request, senderId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to post to this conversation");
        }
        // Is the sender card creator or potential buyer or admin?
        if (!Set.of(buyer, card.getCreator()).contains(sender) && !AuthenticationTokenManager.sessionIsAdmin(request)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to post to this conversation");
        }

        var conversation = conversationRepository.findByCardAndBuyer(card, buyer).orElse(new Conversation(card, buyer));

        // The first message must be from the buyer
        if (conversation.getMessages().isEmpty() && sender == card.getCreator()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot start a conversation with this person");
        }

        logger.info("Posting a message to conversation about card {}, with sender {}", card.getID(), sender.getUserID());
        conversationRepository.save(conversation);
        messageRepository.save(new Message(conversation, sender, content));
        return new ResponseEntity(HttpStatus.CREATED);
    }
}
