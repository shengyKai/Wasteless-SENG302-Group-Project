package org.seng302.controllers;

import net.minidev.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.entities.Keyword;
import org.seng302.entities.MarketplaceCard;
import org.seng302.entities.User;
import org.seng302.persistence.KeywordRepository;
import org.seng302.persistence.MarketplaceCardRepository;
import org.seng302.persistence.UserRepository;
import org.seng302.tools.AuthenticationTokenManager;
import org.seng302.tools.JsonTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * This controller handles requests involving marketplace cards
 */
@RestController
public class CardController {

    private final MarketplaceCardRepository marketplaceCardRepository;
    private final KeywordRepository keywordRepository;
    private final UserRepository userRepository;
    private final Logger logger = LogManager.getLogger(CardController.class.getName());

    @Autowired
    public CardController(MarketplaceCardRepository marketplaceCardRepository, KeywordRepository keywordRepository, UserRepository userRepository) {
        this.marketplaceCardRepository = marketplaceCardRepository;
        this.keywordRepository = keywordRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/cards")
    public JSONObject createCard(HttpServletRequest request, HttpServletResponse response, @RequestBody JSONObject cardProperties) {
        logger.info("Request to create marketplace card received");
        try {
            // Check that authentication token is present and valid
            AuthenticationTokenManager.checkAuthenticationToken(request);

            // Check that request comes from a user whose account matches the card's creator id or who is an admin
            long creatorId = JsonTools.parseLongFromJsonField(cardProperties, "creatorId");
            if (!AuthenticationTokenManager.sessionCanSeePrivate(request, creatorId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User cannot create card for another user");
            }

            // Save the card to the database
            MarketplaceCard card = constructCardFromJson(cardProperties);
            card = marketplaceCardRepository.save(card);

            // Construct and return a json with the card id
            JSONObject json = new JSONObject();
            json.appendField("cardId", card.getID());
            response.setStatus(201);
            return json;
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    private MarketplaceCard constructCardFromJson(JSONObject cardProperties) {
        // Retrieve the user from their id
        long creatorId = JsonTools.parseLongFromJsonField(cardProperties, "creatorId");
        Optional<User> optionalUser = userRepository.findById(creatorId);
        if (optionalUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("User with ID %d does not exist", creatorId));
        }
        User creator = optionalUser.get();

        // Create the card
        MarketplaceCard card = new MarketplaceCard.Builder()
                .withTitle(cardProperties.getAsString("title"))
                .withDescription(cardProperties.getAsString("description"))
                .withSection(cardProperties.getAsString("section"))
                .withCreator(creator)
                .build();

        // Retrieve all the keywords and add them to the card
        long[] keywordIds = JsonTools.parseLongArrayFromJsonField(cardProperties, "keywordIds");
        for (long keywordId : keywordIds) {
            Optional<Keyword> optional = keywordRepository.findById(keywordId);
            if (optional.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Keyword with ID %d does not exist", keywordId));
            }
            card.addKeyword(optional.get());
        }

        return card;
    }
}
