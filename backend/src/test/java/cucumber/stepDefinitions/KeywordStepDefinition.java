package cucumber.stepDefinitions;

import cucumber.context.EventContext;
import cucumber.context.RequestContext;
import cucumber.context.UserContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.junit.jupiter.api.Assertions;
import org.seng302.leftovers.entities.Keyword;
import org.seng302.leftovers.persistence.KeywordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

public class KeywordStepDefinition {
    @Autowired
    private KeywordRepository keywordRepository;

    @Autowired
    private RequestContext requestContext;

    @Autowired
    private UserContext userContext;

    @Autowired
    private EventContext eventContext;

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

    @Then("The notification contains the keyword {string}")
    public void the_notification_contains_the_keyword(String keyword) throws UnsupportedEncodingException, ParseException {
        JSONObject notification = eventContext.mvcResultToJsonObjectList(requestContext.getLastResult()).get(0);

        Assertions.assertEquals("KeywordCreatedEvent", notification.get("type"));
        JSONObject keywordJson = new JSONObject((Map<String, ?>) notification.get("keyword"));
        Assertions.assertEquals(keyword, keywordJson.get("name"));
    }

    @When("I search for keywords with text {string}")
    public void i_search_for_keywords(String searchQuery) {
        requestContext.performRequest(get("/keywords/search").param("searchQuery", searchQuery));
    }

    @Then("The following keywords should be listed:")
    public void the_following_keywords_listed(List<String> keywordNames) throws Exception {
        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);

        JSONArray results = (JSONArray) parser.parse(requestContext.getLastResult().getResponse().getContentAsString());

        for (Object keyword : results) {
            String keywordName = ((JSONObject) keyword).getAsString("name");
            Assertions.assertTrue(keywordNames.contains(keywordName));
        }
    }

}
