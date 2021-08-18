package org.seng302.leftovers.persistence;

import org.seng302.leftovers.entities.Conversation;
import org.seng302.leftovers.entities.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends CrudRepository<Message, Long> {

    Page<Message> findAllByConversation(Conversation conversation, Pageable pageable);

    List<Message> findAllByConversationOrderByCreatedDesc(Conversation conversation);
}
