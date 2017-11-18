package features.deploy;

import cucumber.api.PendingException;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import features.IntegrationTest;
import features.Vars;
import org.gatblau.prometeo.PrometeoWebAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.assertThatThrownBy;

public class DeploySteps extends IntegrationTest {

    @Autowired
    private PrometeoWebAPI controller;

    @And("^the payload is well defined$")
    public void the_deployment_configuration_configuration_variables_are_known() throws Throwable {
        String payload = util.getFile("payload.yml");
        util.put(Vars.KEY_CONFIG_PAYLOAY, payload);
    }

    @And("^the execution of the deployment configuration is requested$")
    public void the_execution_of_the_deployment_configuration_is_requested() throws Throwable {
        executePayload("/run/cfg/sync");
    }

    @And("^the deployment process has been launched$")
    public void the_deployment_process_has_been_launched() throws Throwable {
        ResponseEntity<Object[]> response = client.getForEntity(
            "http://localhost:" + port + "/log/" + util.get(Vars.KEY_PROCESS_ID).toString(),
                Object[].class,
                util.get(Vars.KEY_PROCESS_ID).toString());

        assert(response.getStatusCodeValue() == 200);
    }

    @Given("^the working directory environment variable is set$")
    public void theWorkingDirectoryEnvironmentVariableIsSet() throws Throwable {
        if(System.getenv("WORK_DIR").isEmpty()) {
            throw new Exception("WORK_DIR environment variable needs to be set up for this use case.");
        };
    }

    @Given("^the payload for dev mode is well defined$")
    public void thePayloadForDevModeIsWellDefined() throws Throwable {
        String payload = util.getFile("payload_dev.yml");
        util.put(Vars.KEY_CONFIG_PAYLOAY, payload);
    }

    @Given("^the payload for single role execution is defined$")
    public void thePayloadForSingleRoleExecutionIsDefined() throws Throwable {
        String payload = util.getFile("payload_role.yml");
        util.put(Vars.KEY_CONFIG_PAYLOAY, payload);
    }

    @When("^the execution of the role is requested$")
    public void theExecutionOfTheRoleIsRequested() throws Throwable {
        executePayload("/run/role/sync");
    }

    private void executePayload(String resoursePath) {
        assertThat(controller).isNotNull();

        String payload = util.get(Vars.KEY_CONFIG_PAYLOAY);

        ResponseEntity<String> response = client.postForEntity(
                "http://localhost:" + port + resoursePath,
                util.getEntity(payload),
                String.class);

        assertThat(response.getStatusCodeValue() == 200);

        util.put(Vars.KEY_PROCESS_ID, response.getBody().replace("ProcessId: ", ""));
    }
}