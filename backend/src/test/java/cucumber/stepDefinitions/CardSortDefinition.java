package cucumber.stepDefinitions;

import cucumber.context.CardContext;
import cucumber.context.RequestContext;
import cucumber.context.UserContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.junit.Assert;
import org.seng302.leftovers.entities.Location;
import org.seng302.leftovers.entities.MarketplaceCard;
import org.seng302.leftovers.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class CardSortDefinition {
    @Autowired
    private CardContext cardContext;

    @Autowired
    private UserContext userContext;

    @Autowired
    private RequestContext requestContext;

    private List<String> expectedOrder;

    private MvcResult cards;
    private String cardCreatorName1;
    private String cardCreatorName2;
    private String cardCreatorName3;

    @Given("Multiple cards of different owners exists in the section {string}")
    public void multiple_cards_of_different_owners_exists_in_the_section(String string) throws NoSuchFieldException, IllegalAccessException {
        var user1 = new User.Builder()
                .withFirstName("Carl")
                .withMiddleName("Hector")
                .withLastName("Foxtrot")
                .withNickName("nick")
                .withEmail("here1@testing")
                .withPassword("12345678abc")
                .withBio("g")
                .withDob("2001-03-11")
                .withPhoneNumber("12 34567890")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        var user2 = new User.Builder()
                .withFirstName("Beta")
                .withMiddleName("Hector")
                .withLastName("Echo")
                .withNickName("nick")
                .withEmail("here2@testing")
                .withPassword("12345678abc")
                .withBio("g")
                .withDob("2001-03-11")
                .withPhoneNumber("12 34567890")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Singapore,Singapore," +
                        "Canterbury,8041"))
                .build();
        var user3 = new User.Builder()
                .withFirstName("Charlie")
                .withMiddleName("Hector")
                .withLastName("Delta")
                .withNickName("nick")
                .withEmail("here3@testing")
                .withPassword("12345678abc")
                .withBio("g")
                .withDob("2001-03-11")
                .withPhoneNumber("12 34567890")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Auckland,New Zealand," +
                        "Canterbury,8041"))
                .build();
        userContext.save(user1);
        userContext.save(user2);
        userContext.save(user3);
        //withCloses has to be added because we are overwriting the created field below, which means the closing date
        //would not align with the default 2 weeks with additional seconds
        var card1 = new MarketplaceCard.Builder()
                .withCreator(userContext.getByName("Carl"))
                .withSection("Wanted")
                .withTitle("Awesome")
                .withDescription("A cool vintage car")
                .withCloses(Instant.now().plus(Duration.ofDays(14)))
                .build();
        var card2 = new MarketplaceCard.Builder()
                .withCreator(userContext.getByName("Beta"))
                .withSection("Wanted")
                .withTitle("Boring")
                .withDescription("A cool vintage car")
                .withCloses(Instant.now().plus(Duration.ofDays(14).plus(Duration.ofSeconds(1))))
                .build();
        var card3 = new MarketplaceCard.Builder()
                .withCreator(userContext.getByName("Charlie"))
                .withSection("Wanted")
                .withTitle("Colourful")
                .withDescription("A cool vintage car")
                .withCloses(Instant.now().plus(Duration.ofDays(14).plus(Duration.ofSeconds(2))))
                .build();
        //overwrite the last renewed time field here so that we can produce expected result orderings
        Field lastRenewedField = MarketplaceCard.class.getDeclaredField("lastRenewed");
        lastRenewedField.setAccessible(true);
        lastRenewedField.set(card1, Instant.now());
        lastRenewedField.set(card2, Instant.now().plus(Duration.ofSeconds(1)));
        lastRenewedField.set(card3, Instant.now().plus(Duration.ofSeconds(2)));
        cardContext.save(card1);
        cardCreatorName1 = card1.getCreator().getFirstName();
        cardContext.save(card2);
        cardCreatorName2 = card2.getCreator().getFirstName();
        cardContext.save(card3);
        cardCreatorName3 = card3.getCreator().getFirstName();
    }

    @When("the cards are ordered by {string}")
    public void the_cards_are_ordered_by(String order) {
        cards = requestContext.performRequest(get("/cards")
                .queryParam("section", "Wanted")
                .queryParam("orderBy", order));
    }

    @When("the cards are ordered by {string} in reverse")
    public void the_cards_are_ordered_by_in_reverse(String order) {
        cards = requestContext.performRequest(get("/cards")
                .queryParam("section", "Wanted")
                .queryParam("orderBy", order)
                .queryParam("reverse", "true"));
    }

    @Then("the cards in the response should be ordered by last renewed date by default")
    public void the_cards_in_the_response_should_be_ordered_by_last_renewed_date_by_default() throws UnsupportedEncodingException, ParseException {
        //"lastRenewed" for these cards are as such: 0second, +1second, +2 second
        expectedOrder = List.of(cardCreatorName1, cardCreatorName2, cardCreatorName3);

        cards = requestContext.performRequest(get("/cards")
                .queryParam("section", "Wanted"));
        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject jsonObject = (JSONObject) parser.parse(cards.getResponse().getContentAsString());

        int iterator = 0;
        try {
            for (JSONObject object : (List<JSONObject>) jsonObject.get("results")) {
                JSONObject creator = (JSONObject) object.get("creator");
                Assert.assertEquals(expectedOrder.get(iterator), creator.get("firstName"));
                iterator += 1;
            }
        } catch (ClassCastException e) {
            fail("Invalid error type for response");
        }
    }

    @Then("the cards in the response should be ordered by their title")
    public void the_cards_in_the_response_should_be_ordered_by_their_title() throws UnsupportedEncodingException, ParseException {
        //"title" for these cards are as such: Awesome, Boring, Colourful
        expectedOrder = List.of(cardCreatorName1, cardCreatorName2, cardCreatorName3);

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject jsonObject = (JSONObject) parser.parse(cards.getResponse().getContentAsString());

        int iterator = 0;
        try {
            for (JSONObject object : (List<JSONObject>) jsonObject.get("results")) {
                JSONObject creator = (JSONObject) object.get("creator");
                Assert.assertEquals(expectedOrder.get(iterator), creator.get("firstName"));
                iterator += 1;
            }
        } catch (ClassCastException e) {
            fail("Invalid error type for response");
        }
    }

    @Then("the cards in the response should be ordered by their title in reverse")
    public void the_cards_in_the_response_should_be_ordered_by_their_title_in_reverse() throws UnsupportedEncodingException, ParseException {
        //"title" for these cards in reverse are as such: Colourful, Boring, Awesome
        expectedOrder = List.of(cardCreatorName3, cardCreatorName2, cardCreatorName1);

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject jsonObject = (JSONObject) parser.parse(cards.getResponse().getContentAsString());

        int iterator = 0;
        try {
            for (JSONObject object : (List<JSONObject>) jsonObject.get("results")) {
                JSONObject creator = (JSONObject) object.get("creator");
                Assert.assertEquals(expectedOrder.get(iterator), creator.get("firstName"));
                iterator += 1;
            }
        } catch (ClassCastException e) {
            fail("Invalid error type for response");
        }
    }

    @Then("the cards in the response should be ordered by their location")
    public void the_cards_in_the_response_should_be_ordered_by_their_location() throws UnsupportedEncodingException, ParseException {
        //"location" for these cards are as such: Auckland-NewZealand, Christchurch-NewZealand, Singapore-Singapore
        expectedOrder = List.of(cardCreatorName3, cardCreatorName1, cardCreatorName2);

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject jsonObject = (JSONObject) parser.parse(cards.getResponse().getContentAsString());

        int iterator = 0;
        try {
            for (JSONObject object : (List<JSONObject>) jsonObject.get("results")) {
                JSONObject creator = (JSONObject) object.get("creator");
                Assert.assertEquals(expectedOrder.get(iterator), creator.get("firstName"));
                iterator += 1;
            }
        } catch (ClassCastException e) {
            fail("Invalid error type for response");
        }
    }

    @Then("the cards in the response should be ordered by their location in reverse")
    public void the_cards_in_the_response_should_be_ordered_by_their_location_in_reverse() throws UnsupportedEncodingException, ParseException {
        //"location" for these cards are as such: Singapore-Singapore, Christchurch-NewZealand, Auckland-NewZealand
        expectedOrder = List.of(cardCreatorName2, cardCreatorName1, cardCreatorName3);

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject jsonObject = (JSONObject) parser.parse(cards.getResponse().getContentAsString());

        int iterator = 0;
        try {
            for (JSONObject object : (List<JSONObject>) jsonObject.get("results")) {
                JSONObject creator = (JSONObject) object.get("creator");
                Assert.assertEquals(expectedOrder.get(iterator), creator.get("firstName"));
                iterator += 1;
            }
        } catch (ClassCastException e) {
            fail("Invalid error type for response");
        }
    }

    @Then("the cards in the response should be ordered by their closes")
    public void the_cards_in_the_response_should_be_ordered_by_their_closes() throws UnsupportedEncodingException, ParseException {
        //"closes" for these cards are similar to the "created" which is as such:
        // 0second, +1second, +2 second
        expectedOrder = List.of(cardCreatorName1, cardCreatorName2, cardCreatorName3);

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject jsonObject = (JSONObject) parser.parse(cards.getResponse().getContentAsString());

        int iterator = 0;
        try {
            for (JSONObject object : (List<JSONObject>) jsonObject.get("results")) {
                JSONObject creator = (JSONObject) object.get("creator");
                Assert.assertEquals(expectedOrder.get(iterator), creator.get("firstName"));
                iterator += 1;
            }
        } catch (ClassCastException e) {
            fail("Invalid error type for response");
        }
    }

    @Then("the cards in the response should be ordered by their closes in reverse")
    public void the_cards_in_the_response_should_be_ordered_by_their_closes_in_reverse() throws UnsupportedEncodingException, ParseException {
        //"closes" for these cards in reverse are similar to the "created" which is as such:
        //+2second, +1second, 0second
        expectedOrder = List.of(cardCreatorName3, cardCreatorName2, cardCreatorName1);

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject jsonObject = (JSONObject) parser.parse(cards.getResponse().getContentAsString());

        int iterator = 0;
        try {
            for (JSONObject object : (List<JSONObject>) jsonObject.get("results")) {
                JSONObject creator = (JSONObject) object.get("creator");
                Assert.assertEquals(expectedOrder.get(iterator), creator.get("firstName"));
                iterator += 1;
            }
        } catch (ClassCastException e) {
            fail("Invalid error type for response");
        }
    }

    @Then("the cards in the response should be ordered by their creatorFirstName")
    public void the_cards_in_the_response_should_be_ordered_by_their_creator_first_name() throws UnsupportedEncodingException, ParseException {
        //"creatorFirstName" for these cards are as such: Beta, Carl, Charlie
        expectedOrder = List.of(cardCreatorName2, cardCreatorName1, cardCreatorName3);

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject jsonObject = (JSONObject) parser.parse(cards.getResponse().getContentAsString());

        int iterator = 0;
        try {
            for (JSONObject object : (List<JSONObject>) jsonObject.get("results")) {
                JSONObject creator = (JSONObject) object.get("creator");
                Assert.assertEquals(expectedOrder.get(iterator), creator.get("firstName"));
                iterator += 1;
            }
        } catch (ClassCastException e) {
            fail("Invalid error type for response");
        }
    }

    @Then("the cards in the response should be ordered by their creatorFirstName in reverse")
    public void the_cards_in_the_response_should_be_ordered_by_their_creator_first_name_in_reverse() throws UnsupportedEncodingException, ParseException {
        //"creatorFirstName" for these cards in reverse are as such: Charlie, Carl, Beta
        expectedOrder = List.of(cardCreatorName3, cardCreatorName1, cardCreatorName2);

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject jsonObject = (JSONObject) parser.parse(cards.getResponse().getContentAsString());

        int iterator = 0;
        try {
            for (JSONObject object : (List<JSONObject>) jsonObject.get("results")) {
                JSONObject creator = (JSONObject) object.get("creator");
                Assert.assertEquals(expectedOrder.get(iterator), creator.get("firstName"));
                iterator += 1;
            }
        } catch (ClassCastException e) {
            fail("Invalid error type for response");
        }
    }

    @Then("the cards in the response should be ordered by their creatorLastName")
    public void the_cards_in_the_response_should_be_ordered_by_their_creator_last_name() throws UnsupportedEncodingException, ParseException {
        //"creatorLastName" for these cards are as such: Delta, Echo, Foxtrot
        expectedOrder = List.of(cardCreatorName3, cardCreatorName2, cardCreatorName1);

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject jsonObject = (JSONObject) parser.parse(cards.getResponse().getContentAsString());

        int iterator = 0;
        try {
            for (JSONObject object : (List<JSONObject>) jsonObject.get("results")) {
                JSONObject creator = (JSONObject) object.get("creator");
                Assert.assertEquals(expectedOrder.get(iterator), creator.get("firstName"));
                iterator += 1;
            }
        } catch (ClassCastException e) {
            fail("Invalid error type for response");
        }
    }

    @Then("the cards in the response should be ordered by their creatorLastName in reverse")
    public void the_cards_in_the_response_should_be_ordered_by_their_creator_last_name_in_reverse() throws UnsupportedEncodingException, ParseException {
        //"creatorLastName" for these cards in reverse are as such: Foxtrot, Echo, Delta
        expectedOrder = List.of(cardCreatorName1, cardCreatorName2, cardCreatorName3);

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject jsonObject = (JSONObject) parser.parse(cards.getResponse().getContentAsString());

        int iterator = 0;
        try {
            for (JSONObject object : (List<JSONObject>) jsonObject.get("results")) {
                JSONObject creator = (JSONObject) object.get("creator");
                Assert.assertEquals(expectedOrder.get(iterator), creator.get("firstName"));
                iterator += 1;
            }
        } catch (ClassCastException e) {
            fail("Invalid error type for response");
        }
    }

    @Then("the cards in the response should be ordered by their last renewed date in reverse")
    public void the_cards_in_the_response_should_be_ordered_by_their_last_renewed_date_in_reverse() throws UnsupportedEncodingException, ParseException {
        //"lastRenewed" for these cards are as such: +2second, +1second, 0second
        expectedOrder = List.of(cardCreatorName3, cardCreatorName2, cardCreatorName1);

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject jsonObject = (JSONObject) parser.parse(cards.getResponse().getContentAsString());

        int iterator = 0;
        try {
            for (JSONObject object : (List<JSONObject>) jsonObject.get("results")) {
                JSONObject creator = (JSONObject) object.get("creator");
                Assert.assertEquals(expectedOrder.get(iterator), creator.get("firstName"));
                iterator += 1;
            }
        } catch (ClassCastException e) {
            fail("Invalid error type for response");
        }
    }
}
