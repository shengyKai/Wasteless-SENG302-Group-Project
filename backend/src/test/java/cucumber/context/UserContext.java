package cucumber.context;

import io.cucumber.java.Before;
import org.seng302.entities.Business;
import org.seng302.entities.User;
import org.seng302.persistence.BusinessRepository;
import org.seng302.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

public class UserContext {
    private User lastUser = null;
    private final Map<String, User> userMap = new HashMap<>();

    @Autowired
    private UserRepository userRepository;

    @Before
    public void setup() {
        lastUser = null;
        userMap.clear();
    }

    /**
     * Returns the last modified user
     * @return Last modified user
     */
    public User getLast() {
        return lastUser;
    }

    /**
     * Finds a user by their first name
     * @param firstName First name to select
     * @return User with matching first name, if none exist then null
     */
    public User getByName(String firstName) {
        return userMap.get(firstName);
    }

    /**
     * Saves a user using the user repository
     * Also sets the last user
     * @param user User to save
     * @return Saved user
     */
    public User save(User user) {
        lastUser = userRepository.save(user);
        userMap.put(user.getFirstName(), user);
        return lastUser;
    }

}
