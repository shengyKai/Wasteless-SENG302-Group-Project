package org.seng302.persistence;

import org.seng302.entities.MarketplaceCard;
import org.seng302.entities.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarketplaceCardRepository extends CrudRepository<MarketplaceCard, Long> {
    List<MarketplaceCard> getAllByCreator(@Param("Creator") User user);
}
