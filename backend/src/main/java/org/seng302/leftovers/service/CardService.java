package org.seng302.leftovers.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.seng302.leftovers.entities.MarketplaceCard;
import org.seng302.leftovers.entities.event.DeleteEvent;
import org.seng302.leftovers.entities.event.ExpiryEvent;
import org.seng302.leftovers.persistence.ConversationRepository;
import org.seng302.leftovers.persistence.MarketplaceCardRepository;
import org.seng302.leftovers.persistence.MessageRepository;
import org.seng302.leftovers.persistence.event.EventRepository;
import org.seng302.leftovers.persistence.event.ExpiryEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Class for performing scheduled checks on marketplace cards.
 */
@Service
public class CardService {

    private MarketplaceCardRepository marketplaceCardRepository;
    private ExpiryEventRepository expiryEventRepository;
    private Logger logger = LogManager.getLogger(CardService.class);
    private EventRepository eventRepository;
    private MessageRepository messageRepository;
    private ConversationRepository conversationRepository;
    private SessionFactory sessionFactory;

    @Autowired
    public CardService(MarketplaceCardRepository marketplaceCardRepository, EventRepository eventRepository,
                       ExpiryEventRepository expiryEventRepository, MessageRepository messageRepository,
                       ConversationRepository conversationRepository, SessionFactory sessionFactory) {
        this.marketplaceCardRepository = marketplaceCardRepository;
        this.eventRepository = eventRepository;
        this.expiryEventRepository = expiryEventRepository;
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.sessionFactory = sessionFactory;
    }

    /**
     * Perform a scheduled check every 5 minutes to identify marketplace cards which are expiring within the next day
     * or have expired from this instant.
     */
    @Scheduled(fixedRate = 5 * 60 * 1000)
    private void initiateCardCheckEvents() {
        deleteExpiredCards();
        sendCardExpiryEvents();
    }

    /**
     * Identifies marketplace cards which are expiring within the next day.
     * Construct and dispatch an expiry event for these cards.
     */
    private void sendCardExpiryEvents() {
        logger.info("Checking for cards which are expiring within the next 24 hours");
        Instant cutOff = Instant.now().plus(Duration.ofDays(1));
        Iterable<MarketplaceCard> allCards = marketplaceCardRepository.getAllExpiringBeforeWithoutEvent(cutOff);
        for (MarketplaceCard card : allCards) {
            logger.info("Card {} is expiring before date {}", card.getID(), cutOff);
            try (Session session = sessionFactory.openSession()) {
                card = session.find(MarketplaceCard.class, card.getID());
                ExpiryEvent event = new ExpiryEvent(card);
                eventRepository.save(event);
            }
            logger.info("Expiry notification event sent for card {}", card.getID());
        }
    }

    /**
     * Perform a check to identify marketplace cards which are expired comparing it to the current instant.
     * Deletes any associated expiry event to the expired marketplace card. Expired cards will be deleted 
     * and removed from the repository.
     */
    private void deleteExpiredCards() {
        logger.info("Checking for cards which are expired");
        Iterable<MarketplaceCard> allCards = marketplaceCardRepository.getAllExpiredBefore(Instant.now());
        for (MarketplaceCard card : allCards) {
            logger.info("Card {} has just expired, deleting card from marketplace repository", card.getID());

            logger.info("Deleting expiry event relating to card {}", card.getID());
            Optional<ExpiryEvent> expiredEvent = expiryEventRepository.getByExpiringCard(card);
            if (expiredEvent.isPresent()) {
                expiryEventRepository.delete(expiredEvent.get());
            } else {
                logger.info("No expiry event relating to card {}", card.getID());
            }

            DeleteEvent deleteEvent = new DeleteEvent(card);
            eventRepository.save(deleteEvent);

            deleteCardWithRelations(card);
            logger.info("Card {} deleted from marketplace repository", card.getID());
        }
    }

    /**
     * Delete the marketplace card by first deleting the conversations and messages asssociated with the card, and then
     * deleting the card itself.
     * @param card The marketplace card to be deleted.
     */
    public void deleteCardWithRelations(MarketplaceCard card) {
        var conversations = conversationRepository.findAllByCard(card);
        for (var conversation : conversations) {
            var messages = messageRepository.findAllByConversation(conversation);
            messageRepository.deleteAll(messages);
            conversationRepository.delete(conversation);
        }
        marketplaceCardRepository.delete(card);
    }
}
