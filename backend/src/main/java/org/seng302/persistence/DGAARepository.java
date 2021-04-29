package org.seng302.persistence;

import org.seng302.entities.DefaultGlobalApplicationAdmin;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DGAARepository extends CrudRepository<DefaultGlobalApplicationAdmin, Long>{

        /**
         *
         * @param email the email to search for
         * @return a list of Account object with email matching the search parameter.
         * Should have length of one or zero as email is unique
         */
        DefaultGlobalApplicationAdmin findByEmail(@Param("email") String email);
}
