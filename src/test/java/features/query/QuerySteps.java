package features.query;

import cucumber.api.PendingException;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import features.IntegrationTest;
import features.Vars;
import org.gatblau.prometeo.PrometeoWebAPI;
import org.gatblau.prometeo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

public class QuerySteps extends IntegrationTest {
    @Autowired
    private PrometeoWebAPI controller;

    @And("^the result of the execution is requested$")
    public void the_result_of_the_execution_is_requested() throws Throwable {
        String pid = UUID.randomUUID().toString();
        ResponseEntity<Result> response = client.getForEntity(
            "http://localhost:" + port + "/result/" + pid,
            Result.class,
            pid);

        assert(response.getStatusCodeValue() == 200);
    }

    @And("^the execution result is retrieved$")
    public void the_execution_result_is_retrieved() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @When("^the public key is requested$")
    public void thePublicKeyIsRequested() throws Throwable {
        ResponseEntity<String> response = client.getForEntity("http://localhost:" + port + "/pubkey", String.class);

        assert(response.getStatusCodeValue() == 200);

        util.put(Vars.KEY_PUBKEY, response.getBody().toString());
    }

    @Then("^the public key is retrieved$")
    public void thePublicKeyIsRetrieved() throws Throwable {
        assert(!util.get(Vars.KEY_PUBKEY).toString().isEmpty());
    }
}
