package cucumber.stepDefinitions;

import cucumber.context.CardContext;
import cucumber.context.EventContext;
import cucumber.context.RequestContext;
import cucumber.context.UserContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.SneakyThrows;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Assertions;
import org.seng302.leftovers.persistence.ConversationRepository;
import org.seng302.leftovers.persistence.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.io.UnsupportedEncodingException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class ConversationStepDefinition {

    @Autowired
    private CardContext cardContext;

    @Autowired
    private RequestContext requestContext;

    @Autowired
    private EventContext eventContext;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserContext userContext;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private SessionFactory sessionFactory;

    private String message;

    private int getCardIdFromMessageEventJson(JSONObject messageEventJSON) {
        var conversationJSON = (JSONObject) messageEventJSON.get("conversation");
        var cardJSON = (JSONObject) conversationJSON.get("card");
        return (int) cardJSON.getAsNumber("id");
    }

    private int getBuyerIdFromMessageEventJson(JSONObject messageEventJSON) {
        var conversationJSON = (JSONObject) messageEventJSON.get("conversation");
        var buyerJSON = (JSONObject) conversationJSON.get("buyer");
        return (int) buyerJSON.getAsNumber("id");
    }

    private String getSenderNameFromMessageEventJson(JSONObject messageEventJSON) {
        var messageJSON = (JSONObject) messageEventJSON.get("message");
        var senderId = messageJSON.getAsNumber("senderId");
        var conversationJSON = (JSONObject) messageEventJSON.get("conversation");
        var buyerJSON = (JSONObject) conversationJSON.get("buyer");
        if (buyerJSON.getAsNumber("id").equals(senderId)) {
            return buyerJSON.getAsString("firstName");
        }
        var cardJSON = (JSONObject) conversationJSON.get("card");
        var ownerJSON = (JSONObject) cardJSON.get("creator");
        if (ownerJSON.getAsNumber("id").equals(senderId)) {
            return ownerJSON.getAsString("firstName");
        }
        Assertions.fail("Sender ID should match buyer or owner ID");
        return "";
    }

    private String getCardTitleFromMessageEventJson(JSONObject messageEventJSON) {
        var conversationJSON = (JSONObject) messageEventJSON.get("conversation");
        var cardJSON = (JSONObject) conversationJSON.get("card");
        return cardJSON.getAsString("title");
    }

    private String getOwnerNameFromMessageEventJson(JSONObject messageEventJSON) {
        var conversationJSON = (JSONObject) messageEventJSON.get("conversation");
        var cardJSON = (JSONObject) conversationJSON.get("card");
        var ownerJSON = (JSONObject) cardJSON.get("creator");
        return ownerJSON.getAsString("firstName");
    }

    private String getBuyerNameFromMessageEventJson(JSONObject messageEventJSON) {
        var conversationJSON = (JSONObject) messageEventJSON.get("conversation");
        var buyerJSON = (JSONObject) conversationJSON.get("buyer");
        return buyerJSON.getAsString("firstName");
    }

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

    @Given("user {string} has sent a message regarding card {string}")
    public void user_has_sent_a_message_regarding_card(String senderName, String cardTitle) {
        requestContext.setLoggedInAccount(userContext.getByName(senderName));

        JSONObject json = new JSONObject();
        json.appendField("senderId", userContext.getByName(senderName).getUserID());
        json.appendField("message", "Hello, I have a couple of questions regarding the car");
        message = json.toJSONString();

        var card = cardContext.getByTitle(cardTitle);
        var user = userContext.getLast();
        requestContext.performRequest(
                post(String.format("/cards/%d/conversations/%d", card.getID(), user.getUserID()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(message));

        var conversation = conversationRepository.findByCardAndBuyer(card, user);
        Assertions.assertTrue(conversation.isPresent());
    }

    @Then("I have received a notification from a conversation that I am involved in")
    public void i_have_received_a_notification_from_a_conversation_that_i_am_involved_in() throws UnsupportedEncodingException, ParseException {
        List<JSONObject> events = eventContext.mvcResultToJsonObjectList(requestContext.getLastResult());

        var notificationJSON = events.get(0);
        assertEquals("MessageEvent", notificationJSON.getAsString("type"));
    }

    @Then("the card title {string} is included in the notification")
    public void the_card_title_is_included_in_the_notification(String title) throws UnsupportedEncodingException, ParseException {
        List<JSONObject> events = eventContext.mvcResultToJsonObjectList(requestContext.getLastResult());

        var notificationJSON = events.get(0);

        assertEquals(title, getCardTitleFromMessageEventJson(notificationJSON));
    }

    @Then("the buyer name {string} is included in the message")
    public void the_buyer_name_is_included_in_the_message(String name) throws UnsupportedEncodingException, ParseException {
        List<JSONObject> events = eventContext.mvcResultToJsonObjectList(requestContext.getLastResult());

        var notificationJSON = events.get(0);
        assertEquals(name, getBuyerNameFromMessageEventJson(notificationJSON));
    }

    @Then("the card owner name {string} is included in the notification")
    public void the_card_owner_name_is_included_in_the_notification(String name) throws UnsupportedEncodingException, ParseException {
        List<JSONObject> events = eventContext.mvcResultToJsonObjectList(requestContext.getLastResult());

        var notificationJSON = events.get(0);
        assertEquals(name, getOwnerNameFromMessageEventJson(notificationJSON));
    }

    @Given("I have received a notification from user {string} regarding the card {string}")
    public void i_have_received_a_notification_from_user_regarding_the_card(String name, String title) throws UnsupportedEncodingException, ParseException {
        requestContext.performRequest(get(String.format("/users/%d/feed", requestContext.getLoggedInId())));
        List<JSONObject> events = eventContext.mvcResultToJsonObjectList(requestContext.getLastResult());


        var notificationJSON = events.get(0);
        assertEquals("MessageEvent", notificationJSON.getAsString("type"));
        assertEquals(name, getSenderNameFromMessageEventJson(notificationJSON));
        assertEquals(title, getCardTitleFromMessageEventJson(notificationJSON));
    }

    @When("I reply to the message with {string}")
    public void i_reply_to_the_message_with_string(String reply) throws UnsupportedEncodingException, ParseException {
        List<JSONObject> events = eventContext.mvcResultToJsonObjectList(requestContext.getLastResult());
        var notificationJSON = events.get(0);

        JSONObject json = new JSONObject();
        json.appendField("senderId", requestContext.getLoggedInId());
        json.appendField("message", reply);
        message = json.toJSONString();

        var cardId = getCardIdFromMessageEventJson(notificationJSON);
        var buyerId = getBuyerIdFromMessageEventJson(notificationJSON);

        requestContext.performRequest(
                post(String.format("/cards/%d/conversations/%d", cardId, buyerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(message));
    }

    @Then("the message {string} is added to the conversation with {string}")
    public void the_message_is_added_to_the_conversation(String content, String name) {
        var card = cardContext.getLast();
        var user = userContext.getByName(name);
        var conversation = conversationRepository.findByCardAndBuyer(card, user);
        Assertions.assertTrue(conversation.isPresent());

        var messages = messageRepository.findAllByConversationOrderByCreatedDesc(conversation.get());
        var latest = messages.get(0);
        assertEquals(content, latest.getContent());
    }

    @Given("user {string} has sent a reply to the conversation with user {string} regarding card {string}")
    public void user_has_sent_a_reply(String sender, String buyer, String cardTitle) {
        requestContext.setLoggedInAccount(userContext.getByName(sender));

        JSONObject json = new JSONObject();
        json.appendField("senderId", userContext.getByName(sender).getUserID());
        json.appendField("message", "Is this still available?");
        message = json.toJSONString();

        var cardId = cardContext.getByTitle(cardTitle).getID();
        var buyerId = userContext.getByName(buyer).getUserID();

        requestContext.performRequest(
                post(String.format("/cards/%d/conversations/%d", cardId, buyerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(message));
    }

    @When("I try to view all messages in the conversation with {string} regarding card {string}")
    public void i_try_to_view_all_messages_in_the_conversation(String buyerName, String cardTitle) {
        var buyer = userContext.getByName(buyerName);
        var card = cardContext.getByTitle(cardTitle);
        requestContext.performRequest(
                get(String.format("/cards/%d/conversations/%d", card.getID(), buyer.getUserID()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(message));
    }

    @SneakyThrows
    @Then("all messages in the conversation with {string} regarding card {string} will be available to me")
    public void all_messages_in_the_conversation_will_be_available_to_me(String buyerName, String cardTitle) {
        var buyer = userContext.getByName(buyerName);
        var card = cardContext.getByTitle(cardTitle);
        var conversation = conversationRepository.getConversation(card, buyer);
        var messages = messageRepository.findAllByConversationOrderByCreatedDesc(conversation);

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject response = parser.parse(requestContext.getLastResult().getResponse().getContentAsString(), JSONObject.class);

        assertEquals(messages.size(), response.getAsNumber("count"));

        var resultArray = (JSONArray) response.get("results");

        for (int i = 0; i < resultArray.size(); i++) {
            var messageJson = (JSONObject) resultArray.get(i);
            assertEquals(messages.get(i).getContent(), messageJson.getAsString("content"));
        }
    }



}
