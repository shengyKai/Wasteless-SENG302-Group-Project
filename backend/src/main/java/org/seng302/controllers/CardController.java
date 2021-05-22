package org.seng302.controllers;

import org.seng302.persistence.KeywordRepository;
import org.seng302.persistence.MarketplaceCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

/**
 * This controller handles requests involving marketplace cards
 */
@RestController
public class CardController {

    private MarketplaceCardRepository marketplaceCardRepository;
    private KeywordRepository keywordRepository;

    @Autowired
    public CardController(MarketplaceCardRepository marketplaceCardRepository, KeywordRepository keywordRepository) {
        this.marketplaceCardRepository = marketplaceCardRepository;
        this.keywordRepository = keywordRepository;
    }
}
