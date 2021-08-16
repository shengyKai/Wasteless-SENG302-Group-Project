package org.seng302.leftovers.controllers;

import net.minidev.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.leftovers.dto.ResultPageDTO;
import org.seng302.leftovers.dto.SendMessageDTO;
import org.seng302.leftovers.entities.Conversation;
import org.seng302.leftovers.entities.Message;
import org.seng302.leftovers.persistence.ConversationRepository;
import org.seng302.leftovers.persistence.MarketplaceCardRepository;
import org.seng302.leftovers.persistence.MessageRepository;
import org.seng302.leftovers.persistence.UserRepository;
import org.seng302.leftovers.service.MessageService;
import org.seng302.leftovers.tools.AuthenticationTokenManager;
import org.seng302.leftovers.tools.SearchHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

/**
 * This controller handles requests involving Conversations between Users
 */
@RestController
public class ConversationController {
    private final MarketplaceCardRepository marketplaceCardRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final MessageService messageService;
    private final Logger logger = LogManager.getLogger(ConversationController.class.getName());


    @Autowired
    public ConversationController(MarketplaceCardRepository marketplaceCardRepository, ConversationRepository conversationRepository,
                                  UserRepository userRepository, MessageRepository messageRepository, MessageService messageService) {
        this.marketplaceCardRepository = marketplaceCardRepository;
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.messageService = messageService;
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
    public ResponseEntity<Void> postMarketplaceCardMessage(HttpServletRequest request, @PathVariable Long cardId, @PathVariable Long buyerId, @Valid @RequestBody SendMessageDTO body) {
        AuthenticationTokenManager.checkAuthenticationToken(request);

        var card = marketplaceCardRepository.getCard(cardId, HttpStatus.NOT_ACCEPTABLE);
        var buyer = userRepository.getUser(buyerId);
        var senderId = body.getSenderId();
        var content = body.getMessage();
        var sender = userRepository.getUser(senderId);

        // Is the currently logged in user == senderId or an admin
        if (!AuthenticationTokenManager.sessionCanSeePrivate(request, senderId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to post to this conversation");
        }
        // Is the sender card creator or potential buyer or admin?
        if (!List.of(buyer, card.getCreator()).contains(sender) && !AuthenticationTokenManager.sessionIsAdmin(request)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to post to this conversation");
        }

        var conversation = conversationRepository.findByCardAndBuyer(card, buyer).orElse(new Conversation(card, buyer));

        // The first message must be from the buyer
        if (conversation.getMessages().isEmpty() && sender.equals(card.getCreator())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot start a conversation with this person");
        }

        logger.info("Posting a message to conversation about card {}, with sender {}", card.getID(), sender.getUserID());
        conversationRepository.save(conversation);

        var message = new Message(conversation, sender, content);
        message = messageRepository.save(message);
        messageService.notifyConversationParticipants(message, buyer, card.getCreator());
        return new ResponseEntity(HttpStatus.CREATED);
    }

    /**
     * API endpoint for getting a page of messages from a conversation between a marketplace card owner and another
     * user who has responded to the card.
     * @param request The HTTP request, used for checking that the session is authenticated.
     * @param cardId The ID number of the card which the conversation involves.
     * @param buyerId The ID number of the user who has responded to the marketplace card.
     * @param page The number of the page in the results.
     * @param resultsPerPage The number of results per page to display.
     * @return A page in the results displaying the requested number of messges.
     */
    @GetMapping("/cards/{cardId}/conversations/{buyerId}")
    public ResultPageDTO<JSONObject> fetchMessagesInConversation(HttpServletRequest request,
                                                            @PathVariable Long cardId,
                                                            @PathVariable Long buyerId,
                                                            @RequestParam(required = false) Integer page,
                                                            @RequestParam(required = false) Integer resultsPerPage) {
        AuthenticationTokenManager.checkAuthenticationToken(request);

        var cardOptional = marketplaceCardRepository.findById(cardId);
        if (cardOptional.isEmpty()) {
            var exception = new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, String.format("No marketplace card with id %d", cardId));
            logger.error(exception.getMessage());
            throw exception;
        }

        var buyerOptional = userRepository.findById(buyerId);
        if (buyerOptional.isEmpty()) {
            var exception = new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, String.format("No user with id %d", buyerId));
            logger.error(exception.getMessage());
            throw exception;
        }

        if (!AuthenticationTokenManager.sessionCanSeePrivate(request, buyerId)
                && !AuthenticationTokenManager.sessionCanSeePrivate(request, cardOptional.get().getCreator().getUserID())) {
            var exception = new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to view this conversation");
            logger.error(exception.getMessage());
            throw exception;
        }

        var conversationOptional = conversationRepository.findByCardAndBuyer(cardOptional.get(), buyerOptional.get());
        if (conversationOptional.isEmpty()) {
            var exception = new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, String.format("No messages regarding marketplace card (id=%d) from user (id=%d)", cardId, buyerId));
            logger.error(exception.getMessage());
            throw exception;
        }

        var pageRequest = SearchHelper.getPageRequest(page, resultsPerPage, Sort.by("created").descending());
        var messages = messageRepository.findAllByConversation(conversationOptional.get(), pageRequest);

        return new ResultPageDTO<>(messages.map(Message::constructJSONObject));
    }
}
