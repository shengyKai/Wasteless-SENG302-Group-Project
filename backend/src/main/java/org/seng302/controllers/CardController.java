package org.seng302.controllers;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.seng302.entities.User;
import org.seng302.persistence.KeywordRepository;
import org.seng302.persistence.MarketplaceCardRepository;
import org.seng302.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;

/**
 * This controller handles requests involving marketplace cards
 */
@RestController
public class CardController {

    private MarketplaceCardRepository marketplaceCardRepository;
    private KeywordRepository keywordRepository;
    private UserRepository userRepository;

    @Autowired
    public CardController(MarketplaceCardRepository marketplaceCardRepository, KeywordRepository keywordRepository, UserRepository userRepository) {
        this.marketplaceCardRepository = marketplaceCardRepository;
        this.keywordRepository = keywordRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/cards")
    public JSONObject createCard(HttpServletRequest request, @RequestBody JSONObject cardProperties) throws Exception {
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Not yet implemented");
    }
}
