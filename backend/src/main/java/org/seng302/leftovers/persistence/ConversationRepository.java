package org.seng302.leftovers.persistence;

import org.seng302.leftovers.entities.Conversation;
import org.seng302.leftovers.entities.MarketplaceCard;
import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.exceptions.DoesNotExistResponseException;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends CrudRepository<Conversation, Long> {

    /**
     * Finds a conversation from a card and prospective buyer
     * @param card Card to filter conversations
     * @param buyer Buyer to filter conversations
     * @return An optional containing the result, if the conversation exists else an empty optional
     */
    Optional<Conversation> findByCardAndBuyer(@Param("card") MarketplaceCard card, @Param("buyer") User buyer);

    List<Conversation> findAllByCard(@Param("card") MarketplaceCard card);

    /**
     * Will retrieve the conversation regarding the given card and expected buyer, or throw a 406 response status
     * exception if no conversation exists for the card and buyer.
     * @param card Card to filter conversations.
     * @param buyer Buyer to filter conversations.
     * @return The conversation with the matching buyer and card, if it exists.
     */
    default Conversation getConversation(MarketplaceCard card, User buyer) {
        return findByCardAndBuyer(card, buyer).orElseThrow(() -> new DoesNotExistResponseException(Conversation.class));
    }
}
