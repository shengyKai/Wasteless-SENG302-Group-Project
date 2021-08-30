package org.seng302.leftovers.persistence;

import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.entities.event.Event;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Repository interfaces are used to declare accessors to JPA objects.
 *
 * Spring will scan the project files for its own annotations and perform some
 * startup operations (e.g., instantiating classes).
 *
 * By declaring a "repository rest resource", we can expose repository (JPA)
 * objects through REST API calls. However, This is discouraged in a
 * Model-View-Controller (or similar patterns).
 *
 * See https://docs.spring.io/spring-data/rest/docs/current/reference/html/
 */

public interface UserRepository extends CrudRepository<User, Long>, JpaSpecificationExecutor<User> {

    /**
     *
     * @param email the email to search for
     * @return a list of Account object with email matching the search parameter.
     * Should have length of one or zero as email is unique
     */
    User findByEmail(@Param("email") String email);


    /**
     * Searches for users where the query partially matches one of firstName, middleName, lastName, nickname or partially matches firstName lastName
     * @param query The search term
     * @return List of matching Users
     */
    @Query("SELECT u from User u WHERE lower(concat(u.firstName, u.lastName, u.middleName, u.nickname)) like concat('%',lower(:query), '%') or lower(concat(u.firstName, ' ', u.lastName)) like concat('%',lower(:query), '%') or lower(concat(u.firstName, ' ', u.middleName, ' ', u.lastName)) like concat('%',lower(:query), '%')")
    List<User> findAllByQuery(@Param("query") String query);

    /**
     * Finds all the users for a given event
     * @param event Event to filter users by
     * @return List of users that should be notified by the given event
     */
    List<User> getAllByEvents(Event event);
    
    List<User> findAllByRole(String role);

    /**
     * Gets a user with the given ID
     * If not user is found, a response status exception is thrown with 406: Not acceptable
     * @param id ID of the user to get
     * @return A found user
     */
    default User getUser(Long id) {
        return findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "The given user does not exist"));
    }
}







