package features.deploy;

import cucumber.api.java.en.And;
import features.IntegrationTest;
import features.Vars;
import org.gatblau.prometeo.PrometeoWebAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Java6Assertions.assertThat;

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
        assertThat(controller).isNotNull();

        String payload = util.get(Vars.KEY_CONFIG_PAYLOAY);

        ResponseEntity<String> response = client.postForEntity(
                "http://localhost:" + port + "/run",
                util.getEntity(payload),
                String.class);

        assertThat(response.getStatusCodeValue() == 200);

        util.put(Vars.KEY_PROCESS_ID, response.getBody().replace("ProcessId: ", ""));
    }

    @And("^the deployment process has been launched$")
    public void the_deployment_process_has_been_launched() throws Throwable {
        ResponseEntity<Object[]> response = client.getForEntity(
            "http://localhost:" + port + "/log/" + util.get(Vars.KEY_PROCESS_ID).toString(),
                Object[].class,
                util.get(Vars.KEY_PROCESS_ID).toString());

        assert(response.getStatusCodeValue() == 200);
    }
}