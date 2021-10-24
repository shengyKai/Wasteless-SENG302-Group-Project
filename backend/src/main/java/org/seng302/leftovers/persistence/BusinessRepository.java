package org.seng302.leftovers.persistence;

import org.seng302.leftovers.entities.Business;
import org.seng302.leftovers.exceptions.DoesNotExistResponseException;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BusinessRepository extends CrudRepository<Business, Long>, JpaSpecificationExecutor<Business> {
    Business findByName(@Param("name") String name);

    /**
     * Gets a business from the database matching a given Business Id
     * Performs sanity checks to ensure the business is not null
     * Throws ResponseStatusException if business does not exist
     * @param businessId The id of the business to retrieve
     * @return The business matching the given Id
     */
    default Business getBusinessById(Long businessId) {
        // check business exists
        Optional<Business> business = this.findById(businessId);
        if (business.isEmpty()) {
            throw new DoesNotExistResponseException(Business.class);
        }
        return business.get();
    }
}