package cucumber.stepDefinitions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.context.CardContext;
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
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Assertions;
import org.mockito.MockedStatic;
import org.mockito.invocation.InvocationOnMock;
import org.seng302.leftovers.entities.Keyword;
import org.seng302.leftovers.entities.MarketplaceCard;
import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.persistence.KeywordRepository;
import org.seng302.leftovers.persistence.MarketplaceCardRepository;
import org.seng302.leftovers.service.CardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import org.yaml.snakeyaml.error.Mark;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.http.HttpResponse;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.time.Instant.ofEpochMilli;
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
    private CardService cardService;

    @Autowired
    private SessionFactory sessionFactory;

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

    @When("I check my notification feed")
    public void i_check_my_notification_feed() {
        requestContext.performRequest(get("/events/emitter")
                .param("userId", userContext.getLast().getUserID().toString()));
    }

    private List<JSONObject> parseEvents(MockHttpServletResponse response, String channel) throws UnsupportedEncodingException, ParseException {
        String content = response.getContentAsString();
        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);

        List<JSONObject> events = new ArrayList<>();

        // Iterable of lines that are not comments
        Iterator<String> lineIterator = content.lines().filter(line -> !line.startsWith(":")).iterator();

        while (lineIterator.hasNext()) {
            // Every iteration parses a single event
            // Expects the format:
            //  field:$(channel_name)
            //  data:$(data)
            //  --empty-line--

            String fieldLine = lineIterator.next();
            Assertions.assertTrue(fieldLine.startsWith("event:"));
            String foundChannel = fieldLine.substring("event:".length());

            Assertions.assertTrue(lineIterator.hasNext());
            String dataLine = lineIterator.next();
            Assertions.assertTrue(dataLine.startsWith("data:"));
            String data = dataLine.substring("data:".length());

            Assertions.assertTrue(lineIterator.hasNext());
            Assertions.assertEquals("", lineIterator.next());

            if (foundChannel.equals(channel)) {
                events.add(parser.parse(data, JSONObject.class));
            }
        }

        return events;
    }

    @Then("I have received a message telling me the card is about to expire")
    public void i_have_received_a_message_telling_me_the_card_is_about_to_expire()
            throws JsonProcessingException, UnsupportedEncodingException, ParseException {

        List<JSONObject> events = parseEvents(requestContext.getLastResult().getResponse(), "newsfeed");

        Assertions.assertEquals(1, events.size());
        JSONObject event = events.get(0);

        // Check that the received notification is for card expiry and relates to the expected card
        Assertions.assertEquals("ExpiryEvent", event.get("type"));

        JSONObject cardJson = (JSONObject) event.get("card");

        try (Session session = sessionFactory.openSession()) {
            MarketplaceCard card = session.find(MarketplaceCard.class, cardContext.getLast().getID());
            ObjectMapper mapper = new ObjectMapper();
            assertEquals(mapper.readTree(card.constructJSONObject().toJSONString()), mapper.readTree(cardJson.toJSONString()));
        }
    }

    @Then("I have received a message telling me the card has expired")
    public void i_have_received_a_message_telling_me_the_card_has_expired() throws UnsupportedEncodingException, ParseException {
        List<JSONObject> events = parseEvents(requestContext.getLastResult().getResponse(), "newsfeed");

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

    @Given("A card exists with the keyword {string}")
    public void a_card_exists_with_the_keyword(String name) {
        Keyword keyword = keywordRepository.findByName(name).orElseThrow();
        var card = new MarketplaceCard.Builder()
                .withCreator(userContext.getLast())
                .withSection("Wanted")
                .withTitle("Vintage car")
                .withDescription("A cool vintage car")
                .build();
        card.addKeyword(keyword);
        cardContext.save(card);
    }

    @Then("The card does not have the keyword {string}")
    public void the_card_does_not_have_the_keyword(String name) {
        MarketplaceCard card = marketplaceCardRepository.getCard(cardContext.getLast().getID());
        assertFalse(card.getKeywords().stream().map(Keyword::getName).anyMatch(str -> str.equals(name)));
    }
}
