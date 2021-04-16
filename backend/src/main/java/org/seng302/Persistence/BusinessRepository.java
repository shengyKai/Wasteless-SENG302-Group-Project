package org.seng302.Persistence;

import org.seng302.Entities.Business;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessRepository extends CrudRepository<Business, Long> {
    Business findByName(@Param("name") String name);
}