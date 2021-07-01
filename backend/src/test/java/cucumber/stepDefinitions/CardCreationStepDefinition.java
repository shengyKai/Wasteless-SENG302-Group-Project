package cucumber.stepDefinitions;

import cucumber.context.RequestContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.seng302.controllers.CardController;
import org.seng302.entities.Keyword;
import org.seng302.entities.MarketplaceCard;
import org.seng302.entities.User;
import org.seng302.persistence.KeywordRepository;
import org.seng302.persistence.MarketplaceCardRepository;
import org.seng302.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class CardCreationStepDefinition {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private RequestContext requestContext;
    @Autowired
    private MarketplaceCardRepository marketplaceCardRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private KeywordRepository keywordRepository;
    private MarketplaceCard createdCard;

    /**
     * Take a string of keyword names and return an array with the id number of those keywords
     * @param keywordString A string of comma seperated names
     * @return An array of long keyword ids
     */
    private long[] convertKeywordStringToIdArray(String keywordString) {
        List<Keyword> allKeywords = (List<Keyword>) keywordRepository.findAll();
        String[] keywordNames = keywordString.split(",");
        long[] keywordIds = new long[keywordNames.length];
        for (int i = 0; i < keywordNames.length; i++) {
            for (Keyword keyword : allKeywords) {
                if (keyword.getName().equals(keywordNames[i].strip())) {
                    keywordIds[i] = keyword.getID();
                }
            }
        }
        return keywordIds;
    }

    /**
     * Create a marketplace card from a map object where the key is the name of the attribute and the value is the
     * value of the attribute
     * @param cardProperties A map of string representing the properties of the card
     * @return A card constructed from the given properties
     * @throws Exception
     */
    private MarketplaceCard createCardFromMap(Map<String, String> cardProperties) throws Exception {
        Optional<User> optionalUser = userRepository.findById(requestContext.getLoggedInId());
        if (optionalUser.isEmpty()) {
            throw new Exception("User should be saved to repository");
        }
        MarketplaceCard marketplaceCard = new MarketplaceCard.Builder()
                .withTitle(cardProperties.get("title"))
                .withSection(cardProperties.get("section"))
                .withCreator(optionalUser.get())
                .build();
        if (cardProperties.containsKey("description")) {
            marketplaceCard.setDescription(cardProperties.get("description"));
        }
        if (cardProperties.containsKey("keywords")) {
            List<Keyword> allKeywords = (List<Keyword>) keywordRepository.findAll();
            for (String name : cardProperties.get("keywords").split(",")) {
                for (Keyword keyword : allKeywords) {
                    if (keyword.getName().equals(name)) {
                        marketplaceCard.addKeyword(keyword);
                    }
                }
            }
        }
        return marketplaceCard;
    }

    @Given("Keywords with the following names exist:")
    public void keywords_with_the_following_names_exist(List<String> keywordNames) {
        for (String keywordName : keywordNames) {
            Keyword keyword = new Keyword(keywordName);
            keywordRepository.save(keyword);

        }
        CardController controller = new CardController(marketplaceCardRepository, keywordRepository, userRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @When("I try to create a card with the following properties:")
    public void i_try_to_create_a_card_with_the_following_properties(Map<String, String> cardProperties) throws Exception {
         JSONObject createCardJson = new JSONObject();
         for (Map.Entry<String, String> property : cardProperties.entrySet()) {
             if (property.getKey().equals("keywords")) {
                createCardJson.appendField("keywordIds", convertKeywordStringToIdArray(property.getValue()));
             } else {
                 createCardJson.appendField(property.getKey(), property.getValue());
             }
         }
         createCardJson.appendField("creatorId", requestContext.getLoggedInId());
         if (!createCardJson.containsKey("keywordIds")) {createCardJson.appendField("keywordIds", new int[0]);}

         requestContext.performRequest(post("/cards")
                 .content(createCardJson.toString())
                 .contentType(MediaType.APPLICATION_JSON));

         try {
             createdCard = createCardFromMap(cardProperties);
         } catch (Exception e) {
             System.out.println(e);
             // The card will not always be created as some scenarios use invalid data
             createdCard = null;
         }
    }

    @Then("I expect the card to be saved to the application")
    public void i_expect_the_card_to_be_saved_to_the_application() throws Exception {
        JSONParser jsonParser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject responseBody = (JSONObject) jsonParser.parse(requestContext.getLastResult().getResponse().getContentAsString());
        String cardId = responseBody.getAsString("cardId");
        Optional<MarketplaceCard> optional = marketplaceCardRepository.findById(Long.parseLong(cardId));
        if (optional.isEmpty()) {
            throw new Exception("Card was not saved to repository");
        }
        MarketplaceCard savedCard = optional.get();
        assertEquals(createdCard.getTitle(), savedCard.getTitle());
        assertEquals(createdCard.getDescription(), savedCard.getDescription());
        assertEquals(createdCard.getCreator().getUserID(), savedCard.getCreator().getUserID());
        assertEquals(0, ChronoUnit.SECONDS.between(createdCard.getCreated(), savedCard.getCreated()));
        assertEquals(0, ChronoUnit.SECONDS.between(createdCard.getCloses(), savedCard.getCloses()));
        assertEquals(createdCard.getSection(), savedCard.getSection());
    }

    @Then("I expect the card to not be created")
    public void i_expect_the_card_to_not_be_created() {
        assertEquals(0, marketplaceCardRepository.count());
    }
}
