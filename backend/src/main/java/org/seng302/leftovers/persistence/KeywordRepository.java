package org.seng302.leftovers.persistence;

import org.seng302.leftovers.entities.Keyword;
import org.seng302.leftovers.entities.MarketplaceCard;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

@Repository
public interface KeywordRepository extends CrudRepository<Keyword, Long> {

    List<Keyword> findByOrderByNameAsc();


    /**
     * Finds the keyword for the given name
     * @param name Name to select keyword by
     * @return Keyword if present otherwise empty
     */
    Optional<Keyword> findByName(@Param("name") String name);

    /**
     * Finds all the keywords for the given card
     * @param card Cards to get keywords for
     * @return List of keywords for the card
     */
    List<Keyword> getAllByCards(MarketplaceCard card);
}