package cucumber.stepDefinitions;

import cucumber.context.RequestContext;
import io.cucumber.java.en.Then;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

public class RequestStepDefinition {
    @Autowired
    private RequestContext requestContext;

    @Then("The request fails due to bad request")
    public void the_request_fails_due_to_bad_request() {
        Assertions.assertEquals(400, requestContext.getLastResult().getResponse().getStatus());
    }

    @Then("The request fails due to not authorised")
    public void the_request_fails_due_to_not_authorised() {
        Assertions.assertEquals(401, requestContext.getLastResult().getResponse().getStatus());
    }

    @Then("The request fails due to forbidden")
    public void the_request_fails_due_to_forbidden() {
        Assertions.assertEquals(403, requestContext.getLastResult().getResponse().getStatus());
    }
}
