package cucumber.stepDefinitions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.context.CardContext;
import cucumber.context.EventContext;
import cucumber.context.RequestContext;
import cucumber.context.UserContext;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Assertions;
import org.seng302.leftovers.dto.card.MarketplaceCardResponseDTO;
import org.seng302.leftovers.entities.Keyword;
import org.seng302.leftovers.entities.MarketplaceCard;
import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.persistence.KeywordRepository;
import org.seng302.leftovers.persistence.MarketplaceCardRepository;
import org.seng302.leftovers.service.CardService;
import org.seng302.leftovers.tools.JsonTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;


public class CardStepDefinition {

    @Autowired
    private MarketplaceCardRepository marketplaceCardRepository;

    @Autowired
    private KeywordRepository keywordRepository;

    @Autowired
    private CardContext cardContext;

    @Autowired
    private UserContext userContext;

    @Autowired
    private RequestContext requestContext;

    @Autowired
    private EventContext eventContext;

    @Autowired
    private CardService cardService;

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private ObjectMapper mapper;

    private JSONObject modifyParameters;

    @Given("a card exists")
    public void a_card_exists() {
        var card = new MarketplaceCard.Builder()
                .withCreator(userContext.getLast())
                .withSection("Wanted")
                .withTitle("Vintage car")
                .withDescription("A cool vintage car")
                .build();
        cardContext.save(card);
    }

    @Given("a card exists with title {string} and creator {string}")
    public void a_card_exists_with_title_and_creator(String title, String creatorName) {
        var creator = userContext.getByName(creatorName);
        var card = new MarketplaceCard.Builder()
                .withCreator(creator)
                .withSection("Wanted")
                .withTitle(title)
                .withDescription("A cool vintage car")
                .build();
        cardContext.save(card);
    }

    @Given("The card expiry is changed to less than a day from now")
    public void the_card_expiry_is_changed_to_less_than_a_day_from_now() {
        var card = cardContext.getLast();
        card.setCloses(Instant.now().plus(23, ChronoUnit.HOURS));
        cardContext.save(card);
    }

    @Given("the card has section {string}")
    public void the_card_has_section(String sectionName) {
        var card = cardContext.getLast();
        MarketplaceCard.Section section;
        switch (sectionName) {
            case "ForSale":
                section = MarketplaceCard.Section.FOR_SALE;
                break;
            case "Exchange":
                section = MarketplaceCard.Section.EXCHANGE;
                break;
            default: section = MarketplaceCard.Section.WANTED;
        }
        card.setSection(section);
        cardContext.save(card);
    }

    @When("I request cards in the {string} section")
    public void when_i_request_cards_in_section(String sectionName) throws Exception {
        requestContext.performRequest(get("/cards")
                .param("section", sectionName));
    }

    @Then("I expect the card to be returned")
    public void i_expect_the_card_to_be_returned() throws UnsupportedEncodingException, ParseException {
        var expectedCard = cardContext.getLast();
        Assertions.assertEquals(200, requestContext.getLastResult().getResponse().getStatus());
        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject response = (JSONObject) parser.parse(requestContext.getLastResult().getResponse().getContentAsString());

        Assertions.assertEquals(1, response.get("count"));

        JSONArray resultArray = (JSONArray) response.get("results");
        var item = (JSONObject) resultArray.get(0);
        Assertions.assertEquals(((Number)item.get("id")).longValue(), expectedCard.getID());
        Assertions.assertEquals(item.getAsString("title"), expectedCard.getTitle());
        Assertions.assertEquals(item.getAsString("section"), expectedCard.getSection().getName());
        Assertions.assertEquals(item.getAsString("description"), expectedCard.getDescription());
        Assertions.assertEquals(item.getAsString("created").substring(0,19), expectedCard.getCreated().toString().substring(0,19));
    }

    @When("I try to delete the card")
    public void i_try_to_delete_the_card() {
        requestContext.performRequest(delete("/cards/" + cardContext.getLast().getID()));
    }

    @When("I try to extend the display period of my card")
    public void i_try_to_extend_the_display_period_of_my_card() {
        requestContext.performRequest(put("/cards/" + cardContext.getLast().getID() + "/extenddisplayperiod"));
    }

    @Then("I expect the card to be deleted")
    public void i_expect_the_card_to_be_deleted() {
        Long cardId = cardContext.getLast().getID();
        Assertions.assertFalse(marketplaceCardRepository.existsById(cardId));
    }

    @Then("I expect the card to still exist")
    public void i_expect_the_card_to_still_exist() {
        Long cardId = cardContext.getLast().getID();
        Assertions.assertTrue(marketplaceCardRepository.existsById(cardId));
    }

    @Then("I expect the display period of my card to be extended")
    public void i_expect_the_display_period_of_my_card_to_be_extended() {
        MarketplaceCard previouslyLoaded = cardContext.getLast();
        Instant expectedCloses = previouslyLoaded.getCloses().plus(14, ChronoUnit.DAYS);

        MarketplaceCard card = cardContext.save(marketplaceCardRepository.getCard(previouslyLoaded.getID()));

        Assertions.assertEquals(0, ChronoUnit.SECONDS.between(expectedCloses, card.getCloses()));
    }

    @Then("I expect the display period of my card to not be extended")
    public void i_expect_the_display_period_of_my_card_to_not_be_extended() {
        MarketplaceCard previouslyLoaded = cardContext.getLast();
        MarketplaceCard card = cardContext.save(marketplaceCardRepository.getCard(previouslyLoaded.getID()));

        Assertions.assertEquals(0, ChronoUnit.SECONDS.between(previouslyLoaded.getCloses(), card.getCloses()));
    }

    @Given("The system has performed its scheduled check for cards that are close to expiry")
    public void the_system_has_performed_its_scheduled_check_for_cards_that_are_close_to_expiry()
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Method initiateCardCheckEvents = CardService.class.getDeclaredMethod("initiateCardCheckEvents");
        initiateCardCheckEvents.setAccessible(true);
        initiateCardCheckEvents.invoke(cardService);
    }

    @Then("I have received a message telling me the card is about to expire")
    public void i_have_received_a_message_telling_me_the_card_is_about_to_expire()
            throws JsonProcessingException, UnsupportedEncodingException, ParseException {

        List<JSONObject> events = eventContext.mvcResultToJsonObjectList(requestContext.getLastResult());

        Assertions.assertEquals(1, events.size());
        JSONObject event = events.get(0);

        // Check that the received notification is for card expiry and relates to the expected card
        Assertions.assertEquals("ExpiryEvent", event.get("type"));

        JSONObject cardJson = (JSONObject) event.get("card");

        try (Session session = sessionFactory.openSession()) {
            MarketplaceCard card = session.find(MarketplaceCard.class, cardContext.getLast().getID());

            var expectedJson = mapper.convertValue(new MarketplaceCardResponseDTO(card), JSONObject.class);
            assertEquals(mapper.readTree(mapper.writeValueAsString(expectedJson)), mapper.readTree(cardJson.toJSONString()));
        }
    }

    @Then("I have received a message telling me the card has expired")
    public void i_have_received_a_message_telling_me_the_card_has_expired() throws UnsupportedEncodingException, ParseException {
        List<JSONObject> events = eventContext.mvcResultToJsonObjectList(requestContext.getLastResult());

        Assertions.assertEquals(1, events.size());
        JSONObject event = events.get(0);

        // Check that the received notification is for card deleting and relates to the expected card
        Assertions.assertEquals("DeleteEvent", event.get("type"));
        Assertions.assertEquals(cardContext.getLast().getTitle(), event.get("title"));
        Assertions.assertEquals(cardContext.getLast().getSection().getName(), event.get("section"));
    }

    @Given("The card has expired")
    public void the_card_has_expired()
            throws  IllegalAccessException, NoSuchFieldException {
        Instant pastInstant = Instant.now().minus(Duration.ofHours(1));
        var card = cardContext.getLast();
        Field closes = MarketplaceCard.class.getDeclaredField("closes");
        closes.setAccessible(true);
        closes.set(card, pastInstant);
        cardContext.save(card);
    }

    @Then("The card will be removed from the marketplace")
    public void the_card_will_be_removed_from_the_marketplace() {
        Long cardId = cardContext.getLast().getID();
        Assertions.assertFalse(marketplaceCardRepository.existsById(cardId));
    }

    @And("{string} has the cards")
    public void has_the_cards(String name, DataTable table) {
        User user = userContext.getByName(name);

        List<Map<String, String>> rows = table.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            MarketplaceCard card = new MarketplaceCard.Builder()
                    .withCreator(user)
                    .withTitle(row.get("title"))
                    .withDescription(row.get("description"))
                    .withSection(row.get("section"))
                    .build();
            cardContext.save(card);
        }
    }

    @Given("The user has the following cards:")
    public void the_user_has_the_following_cards(List<Map<String, String>> rows) {
        User user = userContext.getLast();
        for (Map<String, String> row : rows) {
            MarketplaceCard card = new MarketplaceCard.Builder()
                    .withCreator(user)
                    .withTitle(row.get("title"))
                    .withSection(row.get("section"))
                    .build();

            // Add the provided keywords
            Arrays.stream(row.get("keywords").split(","))
                    .map(String::trim)
                    .map(name -> keywordRepository.findByName(name).orElseThrow())
                    .forEach(card::addKeyword);

            cardContext.save(card);
        }
    }

    @When("I request cards by {string}")
    public void i_request_cards_by(String name) {
        User user = userContext.getByName(name);
        requestContext.performRequest(get("/users/" + user.getUserID() + "/cards"));
    }

    @And("I expect the cards for {string} to be returned")
    public void i_expect_the_cards_for_to_be_returned(String name) throws ParseException, UnsupportedEncodingException {
        User user = userContext.getByName(name);

        // Gets all the cards for the specified user by filter all cards in the database
        Map<Long, MarketplaceCard> expectedCards = StreamSupport.stream(marketplaceCardRepository.findAll().spliterator(), false)
                .filter(card -> card.getCreator().getUserID().equals(user.getUserID()))
                .collect(Collectors.toMap(MarketplaceCard::getID, Function.identity()));


        String response = requestContext.getLastResult().getResponse().getContentAsString();
        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject page = parser.parse(response, JSONObject.class);

        // Validates the results
        List<JSONObject> results = (List<JSONObject>) page.get("results");
        for (JSONObject object : results) {
            MarketplaceCard userCard = expectedCards.get(((Number)object.get("id")).longValue());
            assertNotNull(userCard);
            assertEquals(userCard.getTitle(), object.get("title"));
            assertEquals(userCard.getDescription(), object.get("description"));
            assertEquals(userCard.getSection().getName(), object.get("section"));
        }
        assertEquals(expectedCards.size(), results.size());
        assertEquals(expectedCards.size(), page.get("count"));
    }

    @Given("The keyword {string} is added to the card")
    public void the_keyword_is_added_to_the_card(String name) {
        Keyword keyword = keywordRepository.findByName(name).orElseThrow();
        var card = cardContext.getLast();
        card.addKeyword(keyword);
        cardContext.save(card);
    }

    @Then("The card does not have the keyword {string}")
    public void the_card_does_not_have_the_keyword(String name) {
        MarketplaceCard card = marketplaceCardRepository.getCard(cardContext.getLast().getID());
        assertFalse(card.getKeywords().stream().map(Keyword::getName).anyMatch(str -> str.equals(name)));
    }

    private void searchCardsByKeywords(String sectionName, List<String> keywords, boolean union) {
        MockHttpServletRequestBuilder requestBuilder = get("/cards/search")
                .param("section", sectionName)
                .param("union", String.valueOf(union));
        keywords.stream()
                .map(name -> keywordRepository.findByName(name).orElseThrow())
                .map(Keyword::getID)
                .map(String::valueOf)
                .forEach(id -> requestBuilder.param("keywordIds", id));

        requestContext.performRequest(requestBuilder);
    }

    @When("I try to search for cards in the section {string} with all of the keywords:")
    public void i_try_to_search_for_cards_in_the_section_with_all_of_the_keywords(String sectionName, List<String> keywords) {
        searchCardsByKeywords(sectionName, keywords, false);
    }

    @When("I try to search for cards in the section {string} with any of the keywords:")
    public void i_try_to_search_for_cards_in_the_section_with_any_of_the_keywords(String sectionName, List<String> keywords) {
        searchCardsByKeywords(sectionName, keywords, true);
    }

    @Then("I expect the cards to be returned:")
    public void i_expect_the_cards_to_be_returned(List<String> expectedTitles) throws UnsupportedEncodingException, ParseException {
        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject response = parser.parse(requestContext.getLastResult().getResponse().getContentAsString(), JSONObject.class);

        Set<String> actualTitles = new HashSet<>();
        @SuppressWarnings("unchecked")
        List<JSONObject> cards = (List<JSONObject>)response.get("results");
        for (JSONObject card : cards) {
            actualTitles.add(card.getAsString("title"));
        }

        assertEquals(expectedTitles.size(), response.get("count"));
        assertEquals(new HashSet<>(expectedTitles), actualTitles);
    }

    @When("I try to updated the fields of the card to:")
    public void i_try_to_updated_the_fields_of_the_card_to(Map<String, String> properties) {
        MarketplaceCard card = cardContext.getLast();

        modifyParameters = new JSONObject(properties);

        String keywords = modifyParameters.getAsString("keywords");
        modifyParameters.remove("keywords");

        List<Long> keywordIds = Arrays.stream(keywords.split(","))
                .map(keywordRepository::findByName)
                .map(keyword -> keyword.map(Keyword::getID).orElse(9999L))
                .collect(Collectors.toList());
        modifyParameters.put("keywordIds", keywordIds);

        requestContext.performRequest(put("/cards/" + card.getID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(modifyParameters.toJSONString()));
    }

    @Then("The card is not updated")
    public void the_card_is_not_updated() {
        MarketplaceCard card = cardContext.getLast();
        MarketplaceCard updatedCard = marketplaceCardRepository.getCard(card.getID());

        assertEquals(card.getSection(), updatedCard.getSection());
        assertEquals(card.getTitle(), updatedCard.getTitle());
        assertEquals(card.getDescription(), updatedCard.getDescription());

        Set<Long> expectedKeywordIds = card.getKeywords().stream()
                .map(Keyword::getID)
                .collect(Collectors.toSet());
        Set<Long> actualKeywordIds = updatedCard.getKeywords().stream()
                .map(Keyword::getID)
                .collect(Collectors.toSet());
        assertEquals(expectedKeywordIds, actualKeywordIds);
    }

    @Then("The card is updated")
    public void the_card_is_updated() {
        MarketplaceCard updatedCard = marketplaceCardRepository.getCard(cardContext.getLast().getID());

        assertEquals(modifyParameters.getAsString("section"), updatedCard.getSection().getName());
        assertEquals(modifyParameters.getAsString("title"), updatedCard.getTitle());
        assertEquals(modifyParameters.getAsString("description"), updatedCard.getDescription());

        Set<Long> expectedKeywordIds = Arrays.stream(JsonTools.parseLongArrayFromJsonField(modifyParameters, "keywordIds"))
                .boxed()
                .collect(Collectors.toSet());
        Set<Long> actualKeywordIds = updatedCard.getKeywords().stream()
                .map(Keyword::getID)
                .collect(Collectors.toSet());
        assertEquals(expectedKeywordIds, actualKeywordIds);
    }
}
