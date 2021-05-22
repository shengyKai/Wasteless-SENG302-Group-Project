package cucumber.stepDefinitions;

import cucumber.RequestContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.mockito.MockitoAnnotations;
import org.seng302.controllers.CardController;
import org.seng302.entities.Keyword;
import org.seng302.entities.MarketplaceCard;
import org.seng302.persistence.KeywordRepository;
import org.seng302.persistence.MarketplaceCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.UnsupportedEncodingException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class CardCreationStepDefinition {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private RequestContext requestContext;
    @Autowired
    private MarketplaceCardRepository marketplaceCardRepository;
    private KeywordRepository keywordRepository;
    private MvcResult mvcResult;
    private MarketplaceCard createdCard;

    private static long[] convertKeywordIdStringToLongArray(String keywordIdString) {
        String[] keywordIdStrings = keywordIdString.split(",");
        long[] keywordIdNums = new long[keywordIdStrings.length];
        for (int i = 0; i < keywordIdStrings.length; i++) {
            keywordIdNums[i] = Long.parseLong(keywordIdStrings[i].strip());
        }
        return keywordIdNums;
    }

    private MarketplaceCard createCardFromMap(Map<String, String> cardProperties) {
        MarketplaceCard marketplaceCard = new MarketplaceCard.Builder()
                .withTitle(cardProperties.get("title"))
                .withSection(cardProperties.get("section"))
                .build();
        if (cardProperties.containsKey("description")) {
            marketplaceCard.setDescription(cardProperties.get("description"));
        }
        if (cardProperties.containsKey("keywordIds")) {
            for (long id : convertKeywordIdStringToLongArray(cardProperties.get("keywordIds"))) {
                Optional<Keyword> keyword = keywordRepository.findById(id);
                keyword.ifPresent(marketplaceCard::addKeyword);
            }
        }
        return marketplaceCard;
    }

    @Given("Keywords with the following properties exist:")
    public void keywords_with_the_following_properties_exist(io.cucumber.datatable.DataTable dataTable) {
        keywordRepository = mock(KeywordRepository.class);

        List<Map<String, String>>  keywordPropertyList = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> keywordProperties : keywordPropertyList) {
            Keyword keyword = new Keyword(keywordProperties.get("name"));
            when(keywordRepository.findById(Long.parseLong(keywordProperties.get("id")))).thenReturn(Optional.of(keyword));
        }
        CardController controller = new CardController(marketplaceCardRepository, keywordRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @When("I try to create a card with the following properties:")
    public void i_try_to_create_a_card_with_the_following_properties(Map<String, String> cardProperties) throws Exception {
         JSONObject createCardJson = new JSONObject();
         for (Map.Entry<String, String> property : cardProperties.entrySet()) {
             if (property.getKey().equals("keywordIds")) {
                createCardJson.appendField("keywordIds", convertKeywordIdStringToLongArray(property.getValue()));
             } else {
                 createCardJson.appendField(property.getKey(), property.getValue());
             }
         }
         mvcResult = mockMvc.perform(requestContext.addAuthorisationToken(post("/cards"))
                 .content(createCardJson.toString())
                 .contentType(MediaType.APPLICATION_JSON))
                 .andReturn();

         try {
             createdCard = createCardFromMap(cardProperties);
         } catch (Exception e) {
             // The card will not always be created as some scenarios use invalid data
             createdCard = null;
         }
    }

    @Then("I expect to receive a successful response")
    public void i_expect_to_receive_a_successful_response() {
        assertEquals(201, mvcResult.getResponse().getStatus());
    }

    @Then("I expect the card to be saved to the application")
    public void i_expect_the_card_to_be_saved_to_the_application() throws Exception {
        JSONParser jsonParser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject responseBody = (JSONObject) jsonParser.parse(mvcResult.getResponse().getContentAsString());
        String cardId = responseBody.getAsString("cardId");
        Optional<MarketplaceCard> optional = marketplaceCardRepository.findById(Long.parseLong(cardId));
        if (optional.isEmpty()) {
            throw new Exception("Card was not saved to repository");
        }
        MarketplaceCard savedCard = optional.get();
        assertEquals(createdCard.getTitle(), savedCard.getTitle());
        assertEquals(createdCard.getDescription(), savedCard.getDescription());
        assertEquals(createdCard.getCreator(), savedCard.getCreator());
        assertEquals(0, ChronoUnit.SECONDS.between(createdCard.getCreated(), savedCard.getCreated()));
        assertEquals(createdCard.getID(), savedCard.getID());
        assertEquals(0, ChronoUnit.SECONDS.between(createdCard.getCloses(), savedCard.getCloses()));
        assertEquals(createdCard.getSection(), savedCard.getSection());
    }

    @Then("I expect to receive a {string} error")
    public void i_expect_to_receive_a_error(String errorMessage) {
        int expectedStatus;
        switch (errorMessage) {
            case "Bad request":
                expectedStatus = 400;
            default:
                expectedStatus = 400;
        }
        assertEquals(expectedStatus, mvcResult.getResponse().getStatus());
    }

    @Then("I expect the card to not be created")
    public void i_expect_the_card_to_not_be_created() throws UnsupportedEncodingException, ParseException {
        JSONParser jsonParser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject responseBody = (JSONObject) jsonParser.parse(mvcResult.getResponse().getContentAsString());
        String cardId = responseBody.getAsString("cardId");
        Optional<MarketplaceCard> savedCard = marketplaceCardRepository.findById(Long.parseLong(cardId));
        assertTrue(savedCard.isEmpty());
    }
}
