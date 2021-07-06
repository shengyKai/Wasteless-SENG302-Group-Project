package org.seng302.leftovers.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.leftovers.entities.ExpiryEvent;
import org.seng302.leftovers.entities.MarketplaceCard;
import org.seng302.leftovers.persistence.MarketplaceCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/**
 * Class for performing scheduled checks on marketplace cards.
 */
@Service
public class CardService {

    private MarketplaceCardRepository marketplaceCardRepository;
    private EventService eventService;
    private Logger logger = LogManager.getLogger(CardService.class);


    @Autowired
    public CardService(MarketplaceCardRepository marketplaceCardRepository, EventService eventService) {
        this.marketplaceCardRepository = marketplaceCardRepository;
        this.eventService = eventService;
    }

    /**
     * Perform a scheduled check every 5 minutes to identify marketplace cards which are expiring within the next day.
     * Construct and dispatch an expiry event for these cards.
     */
    @Scheduled(fixedRate = 5 * 60 * 1000)
    private void sendCardExpiryEvents() {
        logger.info("Checking for cards which are expiring within the next 24 hours");
        Instant cutOff = Instant.now().plus(Duration.ofDays(1));
        Iterable<MarketplaceCard> allCards = marketplaceCardRepository.getAllExpiringBefore(cutOff);
        for (MarketplaceCard card : allCards) {
            logger.info("Card {} is expiring before date {}", card.getID(), cutOff);
            ExpiryEvent event = new ExpiryEvent(card);
            eventService.addUserToEvent(card.getCreator(), event);
            logger.info("Expiry notification event sent for card {}", card.getID());
        }
    }

    /**
     * Perform a scheduled check every 5 minutes to identify marketplace cards which are expired.
     * Expired cards will be deleted and removed from the repository.
     */
    @Scheduled(fixedRate = 5 * 60 * 1000)
    private void deleteExpiredCards() {
        logger.info("Checking for cards which are expired");
        Iterable<MarketplaceCard> allCards = marketplaceCardRepository.getAllExpiringAfterOrEqual(Instant.now());
        for (MarketplaceCard card : allCards) {
            logger.info("Card {} has just expired, deleting card from marketplace repository", card.getID());
            marketplaceCardRepository.delete(card);
            logger.info("Card {} deleted from marketplace repository", card.getID());
        }
    }
}
