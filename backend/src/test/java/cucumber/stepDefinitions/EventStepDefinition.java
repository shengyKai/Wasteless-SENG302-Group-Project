package cucumber.stepDefinitions;

import cucumber.context.EventContext;
import cucumber.context.RequestContext;
import cucumber.context.UserContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.SneakyThrows;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.seng302.leftovers.entities.Event;
import org.seng302.leftovers.entities.MessageEvent;
import org.seng302.leftovers.persistence.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

public class EventStepDefinition {

    @Autowired
    private EventContext eventContext;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserContext userContext;

    @Autowired
    private RequestContext requestContext;

    @Given("An event is sent to the user")
    public void an_event_is_sent_to_the_user() {
        Event event = new MessageEvent(userContext.getLast(), "Test message");
        eventContext.save(event);
    }

    @Then("I have not received a notification")
    public void i_have_not_received_a_notification() {
        List<JSONObject> events = eventContext.lastReceivedEvents("newsfeed");

        assertEquals(0, events.size());
    }

    @Then("I receive a notification")
    public void i_receive_a_notification() {
        List<JSONObject> events = eventContext.lastReceivedEvents("newsfeed");

        assertEquals(1, events.size());
    }

    @When("I check my notification feed")
    public void i_check_my_notification_feed() {
        requestContext.performRequest(get("/events/emitter")
                .param("userId", userContext.getLast().getUserID().toString()));
    }

    @When("I try to change the event tag to {string}")
    public void i_try_to_change_the_event_tag_to(String tagName) {
        var json = new JSONObject();
        json.put("value", tagName);
        requestContext.performRequest(put("/feed/" + eventContext.getLast().getId() + "/tag")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()));
    }

    @Then("The event has the tag {string}")
    public void the_event_has_the_tag(String tagName) {
        Event event = eventRepository.findById(eventContext.getLast().getId()).orElseThrow();
        assertEquals(tagName, event.constructJSONObject().get("tag"));
    }
}
