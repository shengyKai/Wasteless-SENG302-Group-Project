package org.seng302.leftovers.persistence;

import org.seng302.leftovers.entities.CreateKeywordEvent;
import org.seng302.leftovers.entities.Keyword;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository class for persisting and accessing CreateKeywordEvent data from the database.
 */
@Repository
public interface CreateKeywordEventRepository extends CrudRepository<CreateKeywordEvent, Long> {

    Optional<CreateKeywordEvent> getByNewKeyword(Keyword keyword);

}
