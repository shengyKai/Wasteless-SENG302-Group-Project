package org.seng302.leftovers.persistence;

import org.seng302.leftovers.entities.Location;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationRepository extends CrudRepository<Location, Long> {
  
}
