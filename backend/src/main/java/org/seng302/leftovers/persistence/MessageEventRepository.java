package org.seng302.leftovers.persistence;

import org.seng302.leftovers.entities.Conversation;
import org.seng302.leftovers.entities.Message;
import org.seng302.leftovers.entities.MessageEvent;
import org.seng302.leftovers.entities.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository class for persisting and accessing MessageEvent data from the database.
 */
@Repository
public interface MessageEventRepository extends CrudRepository<MessageEvent, Long> {

    @Query("SELECT m FROM MessageEvent m WHERE m.message.conversation = :conversation AND m.notifiedUser = :user")
    Optional<MessageEvent> findByNotifiedUserAndConversation(User user, Conversation conversation);

}
