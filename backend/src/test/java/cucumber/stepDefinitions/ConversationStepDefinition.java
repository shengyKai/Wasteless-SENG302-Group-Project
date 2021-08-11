package cucumber.stepDefinitions;

import cucumber.context.CardContext;
import cucumber.context.EventContext;
import cucumber.context.RequestContext;
import cucumber.context.UserContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Assertions;
import org.seng302.leftovers.persistence.ConversationRepository;
import org.seng302.leftovers.persistence.KeywordRepository;
import org.seng302.leftovers.persistence.MarketplaceCardRepository;
import org.seng302.leftovers.persistence.MessageRepository;
import org.seng302.leftovers.service.CardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class ConversationStepDefinition {

    @Autowired
    private CardContext cardContext;

    @Autowired
    private RequestContext requestContext;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserContext userContext;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private SessionFactory sessionFactory;

    private String message;

    @Given("I create a message for a given marketplace card owner")
    public void i_create_a_message_for_a_conversation() {
        JSONObject json = new JSONObject();
        json.appendField("senderId", userContext.getLast().getUserID());
        json.appendField("message", "Hello, I have a couple of questions regarding the car");
        message = json.toJSONString();

        var card = cardContext.getLast();
        var user = userContext.getLast();
        var conversation = conversationRepository.findByCardAndBuyer(card, user);
        Assertions.assertTrue(conversation.isEmpty());
    }

    @When("I send the message")
    public void i_send_the_message() {
        requestContext.performRequest(
                post(String.format("/cards/%d/conversations/%d", cardContext.getLast().getID(), userContext.getLast().getUserID()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(message));
    }

    @Then("I expect the message to be sent")
    public void i_expect_message_to_be_sent() {
        MvcResult result = requestContext.getLastResult();
        var response = result.getResponse();
        Assertions.assertEquals(201, response.getStatus());

        var card = cardContext.getLast();
        var user = userContext.getLast();

        var conversation = conversationRepository.findByCardAndBuyer(card, user);
        Assertions.assertTrue(conversation.isPresent());
    }
}
