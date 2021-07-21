package cucumber.stepDefinitions;

import cucumber.context.RequestContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.minidev.json.JSONObject;
import org.seng302.leftovers.entities.Keyword;
import org.seng302.leftovers.persistence.KeywordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class KeywordStepDefinition {
    @Autowired
    private KeywordRepository keywordRepository;

    @Autowired
    private RequestContext requestContext;

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
}
