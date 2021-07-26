package cucumber.stepDefinitions;

import cucumber.context.EventContext;
import cucumber.context.RequestContext;
import cucumber.context.UserContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.SneakyThrows;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.junit.jupiter.api.Assertions;
import org.seng302.leftovers.entities.Keyword;
import org.seng302.leftovers.entities.Location;
import org.seng302.leftovers.entities.User;
import org.seng302.leftovers.persistence.KeywordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class KeywordStepDefinition {
    @Autowired
    private KeywordRepository keywordRepository;

    @Autowired
    private RequestContext requestContext;

    @Autowired
    private UserContext userContext;

    private JSONObject notification;

    @When("I add a new keyword {string}")
    public void i_add_a_new_keyword(String keyword) {
        JSONObject body = new JSONObject();
        body.put("name", keyword);

        requestContext.performRequest(post("/keywords")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body.toJSONString()));
    }

    @Given("The keyword {string} exists")
    public void the_keyword_exists(String name) {
        keywordRepository.save(new Keyword(name));
    }

    @Then("The keyword {string} is present")
    public void the_keyword_is_present(String name) {
        assertTrue(keywordRepository.findByName(name).isPresent());
    }

    @Then("The keyword {string} is not present")
    public void the_keyword_is_not_present(String name) {
        assertTrue(keywordRepository.findByName(name).isEmpty());
    }

    @When("I try to delete the keyword {string}")
    public void i_try_to_delete_the_keyword(String name) {
        Keyword keyword = keywordRepository.findByName(name).orElseThrow();
        requestContext.performRequest(delete("/keywords/" + keyword.getID()));
    }

    @Given("A keyword {string} has been created by user {string}")
    public void a_keyword_has_been_created_by_a_user(String keyword, String name) {
        requestContext.setLoggedInAccount(userContext.getByName(name));
        JSONObject body = new JSONObject();
        body.put("name", keyword);

        requestContext.performRequest(post("/keywords")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body.toJSONString()));
    }

    @SneakyThrows
    @Then("I receive a notification")
    public void i_receive_a_notification() {
        List<JSONObject> events = EventContext.parseEvents(requestContext.getLastResult().getResponse(), "newsfeed");

        Assertions.assertEquals(1, events.size());
        notification = events.get(0);
    }

    @Then("The notification contains the keyword {string}")
    public void the_notification_contains_the_keyword(String keyword) {
        Assertions.assertEquals("CreateKeywordEvent", notification.get("type"));
        System.out.println(notification);
        JSONObject keywordJson = new JSONObject((Map<String, ?>) notification.get("keyword"));
        Assertions.assertEquals(keyword, keywordJson.get("name"));
    }

}
