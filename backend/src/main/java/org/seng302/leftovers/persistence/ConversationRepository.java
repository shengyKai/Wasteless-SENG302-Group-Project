package org.seng302.leftovers.persistence;

import org.seng302.leftovers.entities.*;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ConversationRepository extends CrudRepository<Conversation, Long> {

    /**
     * Finds a conversation from a card and prospective buyer
     * @param card Card to filter conversations
     * @param buyer Buyer to filter conversations
     * @return An optional containing the result, if the conversation exists else an empty optional
     */
    Optional<Conversation> findByCardAndBuyer(@Param("card") MarketplaceCard card, @Param("buyer") User buyer);
}
