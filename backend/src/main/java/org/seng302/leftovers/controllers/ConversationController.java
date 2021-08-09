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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;


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

    @PostMapping("/cards/{cardId}/conversations/{buyerId}")
    public void postMarketplaceCardMessage(HttpServletRequest request, @PathVariable Long cardId, @PathVariable Long buyerId, @RequestBody JSONObject body) {
        AuthenticationTokenManager.checkAuthenticationToken(request);

        var card = marketplaceCardRepository.getCard(cardId, HttpStatus.NOT_ACCEPTABLE);
        var buyer = userRepository.getUser(buyerId);
        var senderId = JsonTools.parseLongFromJsonField(body, "senderId");
        var content = JsonTools.parseStringFromJsonField(body, "message");
        var sender = userRepository.getUser(senderId);

        var conversation = conversationRepository.findByCardAndBuyer(card, buyer).orElse(new Conversation(card, buyer));
        var message = new Message(conversation, sender, content);

        logger.info("Posting a message to conversation about card {}, with sender {}", card.getID(), sender.getUserID());
        conversationRepository.save(conversation);
        messageRepository.save(message);
    }
}
