package org.seng302.leftovers.persistence;

import org.seng302.leftovers.entities.Message;
import org.springframework.data.repository.CrudRepository;

public interface MessageRepository extends CrudRepository<Message, Long> {
}
