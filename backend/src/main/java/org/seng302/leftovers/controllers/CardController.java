package org.seng302.leftovers.controllers;

import net.minidev.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.leftovers.dto.CreateMarketplaceCardDTO;
import org.seng302.leftovers.dto.MarketplaceCardDTO;
import org.seng302.leftovers.dto.ModifyMarketplaceCardDTO;
import org.seng302.leftovers.dto.ResultPageDTO;
import org.seng302.leftovers.entities.Keyword;
import org.seng302.leftovers.entities.MarketplaceCard;
import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.entities.event.ExpiryEvent;
import org.seng302.leftovers.persistence.KeywordRepository;
import org.seng302.leftovers.persistence.MarketplaceCardRepository;
import org.seng302.leftovers.persistence.SearchMarketplaceCardHelper;
import org.seng302.leftovers.persistence.UserRepository;
import org.seng302.leftovers.persistence.event.ExpiryEventRepository;
import org.seng302.leftovers.tools.AuthenticationTokenManager;
import org.seng302.leftovers.tools.SearchHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.util.Streamable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * This controller handles requests involving marketplace cards
 */
@RestController
public class CardController {
    private static final Set<String> VALID_CARD_ORDERINGS = Set.of("lastRenewed", "created", "title", "closes", "creatorFirstName", "creatorLastName", "location");
    private static final String DEFAULT_ORDERING = "lastRenewed";

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
    public JSONObject createCard(HttpServletRequest request, HttpServletResponse response, @RequestBody @Valid CreateMarketplaceCardDTO cardProperties) {
        logger.info("Request to create marketplace card received");
        try {
            // Check that authentication token is present and valid
            AuthenticationTokenManager.checkAuthenticationToken(request);

            // Check that request comes from a user whose account matches the card's creator id or who is an admin
            long creatorId = cardProperties.getCreatorId();
            if (!AuthenticationTokenManager.sessionCanSeePrivate(request, creatorId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User cannot create card for another user");
            }

            // Save the card to the database
            MarketplaceCard card = constructCardFromProperties(cardProperties);
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
     * Endpoint to modify an already existing marketplace card.
     * Will return a 401 error if request is not logged in
     * Will return a 403 error if the user
     * @param request The HTTP request, used for checking permissions.
     * @param id The card ID to modify, provided in the request path
     * @param cardProperties Updated card properties
     */
    @PutMapping("/cards/{id}")
    public void modifyCard(HttpServletRequest request, @PathVariable Long id, @RequestBody @Valid ModifyMarketplaceCardDTO cardProperties) {
        logger.info("Request to modify existing card (id={})", id);
        AuthenticationTokenManager.checkAuthenticationToken(request);
        try {
            MarketplaceCard card = marketplaceCardRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Card not found"));

            if (!AuthenticationTokenManager.sessionCanSeePrivate(request, card.getCreator().getUserID())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Current user does not have permission to modify this card");
            }

            // Updates fields
            card.setSection(cardProperties.getSection());
            card.setTitle(cardProperties.getTitle());
            card.setDescription(cardProperties.getDescription());
            card.setKeywords(getCardKeywordsFromKeywordIds(cardProperties.getKeywordIds()));

            // Save result
            marketplaceCardRepository.save(card);
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
    public void deleteCard(HttpServletRequest request, @PathVariable Long id) {
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
     * Fetches the list of card keywords from the JSONObject attribute "keywordIds"
     * @param keywordIds list of keyword ids
     * @return List of keywords from "keywordIds"
     */
    private List<Keyword> getCardKeywordsFromKeywordIds(List<Long> keywordIds) {
        if (keywordIds.isEmpty()) {
            return List.of();
        }

        List<Keyword> keywords = Streamable.of(
                keywordRepository.findAllById(keywordIds)
        ).toList();

        if (keywords.isEmpty()) { // findAllById will return an empty iterable if any keyword id is invalid
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid keyword ID");
        }

        return keywords;
    }

    /**
     * Retrieve the User and Keywords associated with this card from the database and use them to construct the
     * marketplace card with the properties given by the request body. A response status exception with 400 status
     * will be thrown if any part of the given json has invalid format.
     * @param cardProperties A DTO with the properites to be used when constructing the card.
     * @return A marketplace card constructed with the given properties.
     */
    private MarketplaceCard constructCardFromProperties(CreateMarketplaceCardDTO cardProperties) {
        // Retrieve the user from their id
        long creatorId = cardProperties.getCreatorId();
        Optional<User> optionalUser = userRepository.findById(creatorId);
        if (optionalUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("User with ID %d does not exist", creatorId));
        }
        User creator = optionalUser.get();

        // Create the card
        MarketplaceCard card = new MarketplaceCard.Builder()
                .withTitle(cardProperties.getTitle())
                .withDescription(cardProperties.getDescription())
                .withSection(cardProperties.getSection())
                .withCreator(creator)
                .build();

        // Adds keywords
        card.setKeywords(getCardKeywordsFromKeywordIds(cardProperties.getKeywordIds()));

        return card;
    }


    /**
     *
     * @param request The HTTP request, used for checking permissions.
     * @param id Card ID to delete
     */
    @PutMapping("/cards/{id}/extenddisplayperiod")
    public void extendCardDisplayPeriod(HttpServletRequest request, @PathVariable Long id) {
        logger.info("Request to extend card display period (id={})", id);
        try {
            // Check that authentication token is present and valid
            AuthenticationTokenManager.checkAuthenticationToken(request);

            MarketplaceCard card = marketplaceCardRepository.getCard(id);

            if (!AuthenticationTokenManager.sessionCanSeePrivate(request, card.getCreator().getUserID())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Current user does not have permission to extend display period for this card");
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
     * Helper for generating a page request for marketplace cards from sort and pagination parameters
     *
     * @param orderBy Name of field to order cards by, if it is null a default ordering will be used
     * @param page Page index to start from (1 indexed)
     * @param resultsPerPage Maximum number of results per page
     * @param reverse Whether to reverse the search
     * @return Page request that contains ordering and pagination information
     */
    private PageRequest generatePageRequest(String orderBy, Integer page, Integer resultsPerPage, Boolean reverse) {
        Sort.Direction direction = SearchHelper.getSortDirection(reverse);
        if (orderBy == null) {
            orderBy = DEFAULT_ORDERING;
        }

        if (!VALID_CARD_ORDERINGS.contains(orderBy)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid card ordering");
        }

        List<Sort.Order> sortOrder;
        //If the orderBy is by address, creates a Sort.Order list for Location, else it creates a List for a normal orderBy attribute
        //For location sort, the primary sort would be by country, followed by the city, since these both attributes are shown to the user in the marketplace card.
        if (orderBy.equals("location")) {
            sortOrder = List.of(new Sort.Order(direction, "creator.address.country").ignoreCase(), new Sort.Order(direction, "creator.address.city").ignoreCase());
        } else {
            sortOrder = List.of(new Sort.Order(direction, orderBy).ignoreCase());
        }

        return SearchHelper.getPageRequest(page, resultsPerPage, Sort.by(sortOrder));
    }

    /**
     * Retrieve all of the Marketplace Cards for a given section.
     * @param sectionString The name of the section to retrieve
     * @param orderBy the field to use for sorting the results. Will default to last renewed if none is provided.
     * @param page The page number of the current requested section
     * @param resultsPerPage Maximum number of results to retrieve
     * @param reverse Indicates which way the results will be ordered. They will be in descending order if it is true,
     *                or ascending if it is false or null.
     * @return A JSON Array of Marketplace cards
     */
    @GetMapping("/cards")
    public ResultPageDTO<MarketplaceCardDTO> getCards(HttpServletRequest request,
                                  @RequestParam(name = "section") String sectionString,
                                  @RequestParam(required = false) String orderBy,
                                  @RequestParam(required = false) Integer page,
                                  @RequestParam(required = false) Integer resultsPerPage,
                                  @RequestParam(required = false) Boolean reverse) {
        
        logger.info("Request to get marketplace cards for {}", sectionString);
        try {
            AuthenticationTokenManager.checkAuthenticationToken(request);

            MarketplaceCard.Section section = MarketplaceCard.sectionFromString(sectionString);
            PageRequest pageRequest = generatePageRequest(orderBy, page, resultsPerPage, reverse);
            Page<MarketplaceCard> results = marketplaceCardRepository.getAllBySection(section, pageRequest);

            return new ResultPageDTO<>(results.map(MarketplaceCardDTO::new));
        } catch (Exception exception) {
            logger.error(exception.getMessage());
            throw exception;
        }
    }

    /**
     * Retrieve all of the Marketplace Cards for a given section and with the matching keywords.
     * @param sectionString The name of the section to retrieve
     * @param keywordIds The keywords to filter the results by
     * @param union Mode selection for keyword filtering (false = card has all, true = card has any)
     * @param orderBy The field to use for sorting the results. Will default to last renewed if none is provided.
     * @param page The page number of the current requested section
     * @param resultsPerPage Maximum number of results to retrieve
     * @param reverse Indicates which way the results will be ordered. They will be in descending order if it is true,
     *                or ascending if it is false or null.
     * @return A JSONObject page of Marketplace cards
     */
    @GetMapping("/cards/search")
    public ResultPageDTO<MarketplaceCardDTO> searchCards(HttpServletRequest request,
                                  @RequestParam(name = "section") String sectionString,
                                  @RequestParam(value="keywordIds") List<Long> keywordIds,
                                  @RequestParam Boolean union,
                                  @RequestParam(required = false) String orderBy,
                                  @RequestParam(required = false) Integer page,
                                  @RequestParam(required = false) Integer resultsPerPage,
                                  @RequestParam(required = false) Boolean reverse) {
        logger.info("Searching cards with section=\"{}\" and keywordsIds={}", sectionString, keywordIds);
        try {
            AuthenticationTokenManager.checkAuthenticationToken(request);

            MarketplaceCard.Section section = MarketplaceCard.sectionFromString(sectionString);
            // fetch the keywords
            List<Keyword> keywords = Streamable.of(keywordRepository.findAllById(keywordIds)).toList();
            if (keywords.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to find a provided keyword");
            }

            PageRequest pageRequest = generatePageRequest(orderBy, page, resultsPerPage, reverse);

            Specification<MarketplaceCard> spec =
                    SearchMarketplaceCardHelper.cardHasKeywords(keywords, union)
                    .and(SearchMarketplaceCardHelper.cardIsInSection(section));

            Page<MarketplaceCard> results = marketplaceCardRepository.findAll(spec, pageRequest);

            return new ResultPageDTO<>(results.map(MarketplaceCardDTO::new));
        } catch (Exception exception) {
            logger.error(exception.getMessage());
            throw exception;
        }
    }

    /**
     * Retrieves all of the marketplace cards for a given user
     * @param id ID of the user
     * @param page Page number to retrieve
     * @param resultsPerPage Number of results per page
     * @return A page of marketplace cards
     */
    @GetMapping("/users/{id}/cards")
    public ResultPageDTO<MarketplaceCardDTO> getCardsForUser(HttpServletRequest request,
                                      @PathVariable Long id,
                                      @RequestParam(required = false) Integer page,
                                      @RequestParam(required = false) Integer resultsPerPage) {
        logger.info("Request to get marketplace cards for user {}", id);
        AuthenticationTokenManager.checkAuthenticationToken(request);

        User user = userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "User not found"));

        PageRequest pageRequest = SearchHelper.getPageRequest(page, resultsPerPage, Sort.by(Sort.Direction.DESC, DEFAULT_ORDERING));
        var results = marketplaceCardRepository.getAllByCreator(user, pageRequest);

        return new ResultPageDTO<>(results.map(MarketplaceCardDTO::new));
    }
}
