package org.seng302.leftovers.controllers;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.leftovers.entities.ExpiryEvent;
import org.seng302.leftovers.entities.Keyword;
import org.seng302.leftovers.entities.MarketplaceCard;
import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.persistence.ExpiryEventRepository;
import org.seng302.leftovers.persistence.KeywordRepository;
import org.seng302.leftovers.persistence.MarketplaceCardRepository;
import org.seng302.leftovers.persistence.UserRepository;
import org.seng302.leftovers.tools.AuthenticationTokenManager;
import org.seng302.leftovers.tools.JsonTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.seng302.leftovers.tools.SearchHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * This controller handles requests involving marketplace cards
 */
@RestController
public class CardController {
    private static final Set<String> VALID_CARD_ORDERINGS = Set.of("created", "title", "closes", "creatorFirstName", "creatorLastName", "country");

    private final MarketplaceCardRepository marketplaceCardRepository;
    private final KeywordRepository keywordRepository;
    private final UserRepository userRepository;
    private final ExpiryEventRepository expiryEventRepository;
    private final Logger logger = LogManager.getLogger(CardController.class.getName());

    @Autowired
    public CardController(MarketplaceCardRepository marketplaceCardRepository, KeywordRepository keywordRepository, UserRepository userRepository, ExpiryEventRepository expiryEventRepository) {
        this.marketplaceCardRepository = marketplaceCardRepository;
        this.keywordRepository = keywordRepository;
        this.userRepository = userRepository;
        this.expiryEventRepository = expiryEventRepository;
    }

    /**
     * Endpoint to create a card with the properties given in the json body of the request.
     * Will return a 401 error if the request is unauthorized, a 403 error if the user does not have perimssion to create
     * a card with the given creatorId, or a 400 error if the json body is not formatted as expected.
     * Will return a 201 response and a json with the id of the created card if the request is successful.
     * @param request The HTTP request, used for checking permissions.
     * @param response The HTTP response, used for changing response status.
     * @param cardProperties The JSON body of the request with data for creating the cards.
     * @return A json with a cardId attribute if the request is successful.
     */
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

    /**
     * Endpoint to delete the marketplace card with the provided card id.
     * If the user is unauthorised then a 401 response is returned
     * If the user is not allowed to modify the card then a 403 response is returned
     * If the card does not exist then a 404 response is returned
     * Otherwise if the operation is successful then a 200 response is returned
     * @param request The HTTP request, used for checking permissions.
     * @param id Card ID to delete
     */
    @DeleteMapping("/cards/{id}")
    private void deleteCard(HttpServletRequest request, @PathVariable Long id) {
        logger.info("Request to delete marketplace card (id={})", id);
        try {
            // Check that authentication token is present and valid
            AuthenticationTokenManager.checkAuthenticationToken(request);

            MarketplaceCard card = marketplaceCardRepository.getCard(id);

            if (!AuthenticationTokenManager.sessionCanSeePrivate(request, card.getCreator().getUserID())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Current user does not have permission to delete this card");
            }

            Optional<ExpiryEvent> expiryEvent = expiryEventRepository.getByExpiringCard(card);
            if (expiryEvent.isPresent()) {
                expiryEventRepository.delete(expiryEvent.get());
            }
            marketplaceCardRepository.delete(card);

        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieve the User and Keywords associated with this card from the database and use them to construct the
     * marketplace card with the properties given by the json object. A response status exception with 400 status
     * will be thrown if any part of the given json has invalid format.
     * @param cardProperties A json with the properites to be used when constructing the card.
     * @return A marketplace card constructed with the given properties.
     */
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


    /**
     *
     * @param request The HTTP request, used for checking permissions.
     * @param id Card ID to delete
     */
    @PutMapping("/cards/{id}/extenddisplayperiod")
    private void extendCardDisplayPeriod(HttpServletRequest request, @PathVariable Long id) {
        logger.info("Request to extend card display period (id={})", id);
        try {
            // Check that authentication token is present and valid
            AuthenticationTokenManager.checkAuthenticationToken(request);

            MarketplaceCard card = marketplaceCardRepository.getCard(id);

            if (!AuthenticationTokenManager.sessionCanSeePrivate(request, card.getCreator().getUserID())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Current user does not have permission to delete this card");
            }

            Optional<ExpiryEvent> expiryEvent = expiryEventRepository.getByExpiringCard(card);
            if (expiryEvent.isPresent()) {
                expiryEventRepository.delete(expiryEvent.get());
            }
            card.delayCloses();
            marketplaceCardRepository.save(card);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieve all of the Marketplace Cards for a given section.
     * @param sectionName The name of the section to retrieve
     * @param page The page number of the current requested section
     * @param resultsPerPage Maximum number of results to retrieve
     * @return A JSON Array of Marketplace cards
     */
    @GetMapping("/cards")
    public JSONObject getCards(HttpServletRequest request,
                              @RequestParam(name = "section") String sectionName,
                              @RequestParam(required = false) String orderBy,
                              @RequestParam(required = false) Integer page,
                              @RequestParam(required = false) Integer resultsPerPage,
                              @RequestParam(required = false) Boolean reverse) {
        
        logger.info("Request to get marketplace cards for {}", sectionName);
        AuthenticationTokenManager.checkAuthenticationToken(request);

        // parse the section
        MarketplaceCard.Section section = MarketplaceCard.sectionFromString(sectionName);
        Sort.Direction direction = SearchHelper.getSortDirection(reverse);
        if (orderBy == null) {
            orderBy = "created";
        }

        if (!VALID_CARD_ORDERINGS.contains(orderBy)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid card ordering");
        }

        List<Sort.Order> sortOrder;
        //If the orderBy is by address, creates a Sort.Order list for Location, else it creates a List for a normal orderBy attribute
        //For location sort, the primary sort would be by country, followed by the city, since these both attributes are shown to the user in the marketplace card.
        if (orderBy.equals("country")) {
            sortOrder = List.of(new Sort.Order(direction, "creator.address.country").ignoreCase(), new Sort.Order(direction, "creator.address.city").ignoreCase());
        } else {
            sortOrder = List.of(new Sort.Order(direction, orderBy).ignoreCase());
        }

        PageRequest pageRequest = SearchHelper.getPageRequest(page, resultsPerPage, Sort.by(sortOrder));
        Page<MarketplaceCard> results = marketplaceCardRepository.getAllBySection(section, pageRequest);

        //return JSON Object
        JSONArray resultArray = new JSONArray();
        for (MarketplaceCard card : results) {
            resultArray.appendElement(card.constructJSONObject());
        }
        JSONObject json = new JSONObject();
        json.put("count", results.getTotalElements());
        json.put("results", resultArray);
        return json;
    }
}
