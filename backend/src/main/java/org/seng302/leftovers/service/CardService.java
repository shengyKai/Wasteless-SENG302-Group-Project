package org.seng302.leftovers.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seng302.leftovers.entities.ExpiryEvent;
import org.seng302.leftovers.entities.MarketplaceCard;
import org.seng302.leftovers.persistence.MarketplaceCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

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
    private SessionFactory sessionFactory;

    @Autowired
    public CardService(MarketplaceCardRepository marketplaceCardRepository, EventService eventService, SessionFactory sessionFactory) {
        this.marketplaceCardRepository = marketplaceCardRepository;
        this.eventService = eventService;
        this.sessionFactory = sessionFactory;
    }

    /**
     * Perform a scheduled check every hour to identify marketplace cards which are expiring within the next day.
     * Construct and dispatch an expiry event for these cards.
     */
    @Scheduled(fixedRate = 5 * 1000)
    private void sendCardExpiryEvents() {
        logger.info("Checking for cards which are expiring within the next 24 hours");
        Instant cutOff = Instant.now().plus(Duration.ofDays(1));
        Iterable<MarketplaceCard> allCards = marketplaceCardRepository.getAllExpiringBefore(cutOff);
        for (MarketplaceCard card : allCards) {
            logger.info("Card {} is expiring before date {}", card.getID(), cutOff);
            try (Session session = sessionFactory.openSession()) {
                card = session.find(MarketplaceCard.class, card.getID());
                ExpiryEvent event = new ExpiryEvent(card);
                eventService.addUserToEvent(card.getCreator(), event);
            }
            logger.info("Expiry notification event sent for card {}", card.getID());
        }
    }

}
