package cucumber.context;

import io.cucumber.java.Before;
import org.seng302.leftovers.entities.MarketplaceCard;
import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.persistence.MarketplaceCardRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

public class CardContext {
    private MarketplaceCard lastCard = null;
    private final Map<String, MarketplaceCard> cardMap = new HashMap<>();

    @Autowired
    private MarketplaceCardRepository marketplaceCardRepository;

    @Before
    public void setup() {
        lastCard = null;
        cardMap.clear();
    }

    /**
     * Returns the last modified Card
     * @return last modified Card
     */
    public MarketplaceCard getLast() {return lastCard;}

    /**
     * Saves a card using the marketplaceCard repository
     * Also sets the last card
     * @param card Card to save
     * @return Saved card
     */
    public MarketplaceCard save(MarketplaceCard card) {
        lastCard = marketplaceCardRepository.save(card);
        cardMap.put(card.getTitle(), card);
        return lastCard;
    }

    /**
     * Finds a marketplace card that has been saved to the card context by its title.
     * @param title The title of the marketplace card.
     * @return The card with the matching title, if there is one.
     */
    public MarketplaceCard getByTitle(String title) {
        return cardMap.get(title);
    }
}
