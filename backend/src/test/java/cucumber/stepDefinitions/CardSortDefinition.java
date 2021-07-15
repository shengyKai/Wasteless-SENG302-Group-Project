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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class CardSortDefinition {
    @Autowired
    private CardContext cardContext;

    @Autowired
    private UserContext userContext;

    @Autowired
    private RequestContext requestContext;

    private List<String> expectedOrder = new ArrayList<String>();

    private MvcResult cards;

    @Given("Multiple cards of different owners exists in the section {string}")
    public void multiple_cards_of_different_owners_exists_in_the_section(String string) throws NoSuchFieldException, IllegalAccessException {
        var user1 = new User.Builder()
                .withFirstName("Alpha")
                .withMiddleName("Hector")
                .withLastName("Delta")
                .withNickName("nick")
                .withEmail("here1@testing")
                .withPassword("12345678abc")
                .withBio("g")
                .withDob("2001-03-11")
                .withPhoneNumber("123-456-7890")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Auckland,New Zealand," +
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
                .withPhoneNumber("123-456-7890")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Christchurch,New Zealand," +
                        "Canterbury,8041"))
                .build();
        var user3 = new User.Builder()
                .withFirstName("Charlie")
                .withMiddleName("Hector")
                .withLastName("Foxtrot")
                .withNickName("nick")
                .withEmail("here3@testing")
                .withPassword("12345678abc")
                .withBio("g")
                .withDob("2001-03-11")
                .withPhoneNumber("123-456-7890")
                .withAddress(Location.covertAddressStringToLocation("4,Rountree Street,Ashburton,Singapore,Singapore," +
                        "Canterbury,8041"))
                .build();
        userContext.save(user1);
        userContext.save(user2);
        userContext.save(user3);
        //withCloses has to be added because we are overwriting the created field below, which means the closing date
        //would not align with the default 2 weeks with additional seconds
        var card1 = new MarketplaceCard.Builder()
                .withCreator(userContext.getByName("Alpha"))
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
        //overwrite the created time field here so that we can produce expected result orderings
        Field createdField = MarketplaceCard.class.getDeclaredField("created");
        createdField.setAccessible(true);
        createdField.set(card1, Instant.now());
        createdField.set(card2, Instant.now().plus(Duration.ofSeconds(1)));
        createdField.set(card3, Instant.now().plus(Duration.ofSeconds(2)));
        cardContext.save(card1);
        expectedOrder.add(card1.getCreator().getFirstName());
        cardContext.save(card2);
        expectedOrder.add(card2.getCreator().getFirstName());
        cardContext.save(card3);
        expectedOrder.add(card3.getCreator().getFirstName());
    }

    @Then("the cards should be ordered by {string} by default")
    public void the_cards_should_be_ordered_by_by_default(String string) throws UnsupportedEncodingException, ParseException {
        cards = requestContext.performRequest(get("/cards")
                .queryParam("section", "Wanted"));
        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject jsonObject = (JSONObject) parser.parse(cards.getResponse().getContentAsString());

        JSONObject creator;
        int iterator = 0;
        for (Object object: (JSONArray) jsonObject.get("results")) {
            creator = (JSONObject) ((JSONObject) object).get("creator");
            Assert.assertEquals(expectedOrder.get(iterator), creator.get("firstName"));
            iterator += 1;
        }
    }

    @When("have them ordered by {string}")
    public void have_them_ordered_by(String order) {
        cards = requestContext.performRequest(get("/cards")
                .queryParam("section", "Wanted")
                .queryParam("orderBy", order));
    }

    @When("have them ordered by {string} in reverse")
    public void have_them_ordered_by_in_reverse(String order) {
        cards = requestContext.performRequest(get("/cards")
                .queryParam("section", "Wanted")
                .queryParam("orderBy", order)
                .queryParam("reverse", "true"));
    }

    @Then("the cards should be ordered by their {string}")
    public void the_cards_should_be_ordered_by_their(String order) throws UnsupportedEncodingException, ParseException {
        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject jsonObject = (JSONObject) parser.parse(cards.getResponse().getContentAsString());

        JSONObject creator;
        int iterator = 0;
        for (Object object: (JSONArray) jsonObject.get("results")) {
            creator = (JSONObject) ((JSONObject) object).get("creator");
            Assert.assertEquals(expectedOrder.get(iterator), creator.get("firstName"));
            iterator += 1;
        }
    }

    @Then("the cards should be ordered by their {string} in reverse")
    public void the_cards_should_be_ordered_by_their_in_reverse(String string) throws UnsupportedEncodingException, ParseException {
        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject jsonObject = (JSONObject) parser.parse(cards.getResponse().getContentAsString());

        Collections.reverse(expectedOrder);

        JSONObject creator;
        int iterator = 0;
        for (Object object: (JSONArray) jsonObject.get("results")) {
            creator = (JSONObject) ((JSONObject) object).get("creator");
            Assert.assertEquals(expectedOrder.get(iterator), creator.get("firstName"));
            iterator += 1;
        }
    }
}
