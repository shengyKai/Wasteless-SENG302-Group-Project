package cucumber.stepDefinitions;

import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.context.EventContext;
import cucumber.context.RequestContext;
import cucumber.context.UserContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.seng302.leftovers.dto.event.EventStatus;
import org.seng302.leftovers.dto.event.EventTag;
import org.seng302.leftovers.entities.event.Event;
import org.seng302.leftovers.entities.event.GlobalMessageEvent;
import org.seng302.leftovers.persistence.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

public class EventStepDefinition {

    @Autowired
    private EventContext eventContext;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserContext userContext;

    @Autowired
    private RequestContext requestContext;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    public void cleanUp() {
        eventRepository.deleteAll();
    }

    @Given("An event is sent to the user")
    public void an_event_is_sent_to_the_user() {
        Event event = new GlobalMessageEvent(userContext.getLast(), "Test message");
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
                .param("userId", requestContext.getLoggedInId().toString()));
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

        var tag = objectMapper.convertValue(tagName, EventTag.class);
        assertEquals(tag, event.getTag());
    }

    @When("I try to delete an event from my feed")
    public void i_try_to_delete_an_event_from_my_feed() {
        requestContext.performRequest(delete("/feed/" + eventContext.getLast().getId()));
    }

    @Then("The event is deleted")
    public void the_event_is_deleted() {
        Long eventId = eventContext.getLast().getId();
        Assertions.assertFalse(eventRepository.existsById(eventId));
    }

    @Then("The event is not deleted from my feed")
    public void the_event_is_not_deleted_from_my_feed() {
        Long eventId = eventContext.getLast().getId();
        Assertions.assertTrue(eventRepository.existsById(eventId));
    }

    @Given("The default read status is false")
    public void the_default_read_status_is_false() {
        Event event = eventContext.getLast();
        assertFalse(event.isRead());
    }

    @When("I try to mark an event from my feed as read")
    public void i_try_to_mark_an_event_from_my_feed_as_read() {
        requestContext.performRequest(put("/feed/" + eventContext.getLast().getId() + "/read"));
    }

    @When("I try to mark an event that does not exist from my feed as read")
    public void i_try_to_mark_an_event_that_does_not_exist_from_my_feed_as_read() {
        requestContext.performRequest(put("/feed/" + 999 + "/read"));
    }

    @Then("The event will be updated as read")
    public void the_event_will_be_updated_as_read() {
        Long eventId = eventContext.getLast().getId();
        Optional<Event> event = eventRepository.findById(eventId);
        assertTrue(event.isPresent());
        assertTrue(event.get().isRead());
    }

    @Then("The event is not marked as read")
    public void the_event_is_not_marked_as_read() {
        Long eventId = eventContext.getLast().getId();
        Optional<Event> event = eventRepository.findById(eventId);
        assertTrue(event.isPresent());
        assertFalse(event.get().isRead());
    }

    @Given("the event status has been set to {string}")
    public void the_event_status_has_been_set_to(String status) {
        Long eventId = eventContext.getLast().getId();
        Optional<Event> event = eventRepository.findById(eventId);
        assertTrue(event.isPresent());
        event.get().updateEventStatus(EventStatus.valueOf(status.toUpperCase()));
        assertEquals(EventStatus.valueOf(status.toUpperCase()), event.get().getStatus());
    }

    @Then("the event has status {string}")
    public void the_event_has_status(String status) {
        Long eventId = eventContext.getLast().getId();
        Optional<Event> event = eventRepository.findById(eventId);
        assertTrue(event.isPresent());
        assertEquals(EventStatus.valueOf(status.toUpperCase()), event.get().getStatus());
    }

    @When("I try to change the status of the event to {string}")
    public void i_try_to_change_the_status_of_the_event_to(String newStatus) {
        JSONObject json = new JSONObject();
        json.put("value", newStatus);
        requestContext.performRequest(put("/feed/" + eventContext.getLast().getId() + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toJSONString()));
    }

}
