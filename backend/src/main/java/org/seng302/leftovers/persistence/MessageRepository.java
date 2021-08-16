package org.seng302.leftovers.persistence;

import org.seng302.leftovers.entities.Conversation;
import org.seng302.leftovers.entities.Message;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MessageRepository extends CrudRepository<Message, Long> {

    List<Message> findAllByConversationOrderByCreatedDesc(Conversation conversation);
}
