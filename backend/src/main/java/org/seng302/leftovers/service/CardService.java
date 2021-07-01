package org.seng302.leftovers.service;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.leftovers.entities.ExpiryEvent;
import org.seng302.leftovers.entities.Keyword;
import org.seng302.leftovers.entities.MarketplaceCard;
import org.seng302.leftovers.persistence.KeywordRepository;
import org.seng302.leftovers.persistence.MarketplaceCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class CardService {

    private MarketplaceCardRepository marketplaceCardRepository;
    private KeywordRepository keywordRepository;
    private EventService eventService;
    private Logger logger = LogManager.getLogger(CardService.class);


    @Autowired
    public CardService(MarketplaceCardRepository marketplaceCardRepository, EventService eventService) {
        this.marketplaceCardRepository = marketplaceCardRepository;
        this.keywordRepository = keywordRepository;
        this.eventService = eventService;
    }

    @Scheduled(fixedRate = 1000)
    private void checkCardExtension() {
        logger.info("Doing the check");
        Instant cutOff = Instant.now().minus(Duration.ofDays(1));
        Iterable<MarketplaceCard> allCards = marketplaceCardRepository.getAllExpiringAfter(cutOff);
        for (MarketplaceCard card : allCards) {
//            if (card.getExpiryEvent() == null) {
                logger.info("Card {} is expiring", card.getID());
                ExpiryEvent event = new ExpiryEvent(card);
//                logger.info(event.constructJSONObject());
                eventService.addUserToEvent(card.getCreator(), event);
        }
    }

    public JSONObject constructJSONObject(MarketplaceCard marketplaceCard) {
        JSONObject json = marketplaceCard.constructJSONObject();
        JSONArray keywordArray = new JSONArray();
        Iterable<Keyword> keywords = keywordRepository.getAllByCards(marketplaceCard);
        // jsonify the keywords
        for (Keyword keyword : keywords) {
            keywordArray.appendElement(keyword.constructJSONObject());
        }
        json.appendField("keywords", keywordArray);
        return json;
    }

}
