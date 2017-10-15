package features.deploy;

import cucumber.api.java.en.And;
import features.IntegrationTest;
import features.Vars;
import org.gatblau.prometeo.ApiController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class DeploySteps extends IntegrationTest {

    @Autowired
    private ApiController controller;

    @And("^the payload is well defined$")
    public void the_deployment_configuration_configuration_variables_are_known() throws Throwable {
        String payload = util.getFile("payload.yml");
        util.put(Vars.KEY_CONFIG_PAYLOAY, payload);
    }

    @And("^the execution of the deployment configuration is requested$")
    public void the_execution_of_the_deployment_configuration_is_requested() throws Throwable {
        assertThat(controller).isNotNull();
        String payload = util.get(Vars.KEY_CONFIG_PAYLOAY);
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("Content-Type", "application/x-yaml");
        requestHeaders.add("Accept", "application/x-yaml");
        HttpEntity<String> entity = new HttpEntity<String>(payload, requestHeaders);

        ResponseEntity<String> response = client.postForEntity("http://localhost:" + port + "/run", entity, String.class);
    }

    @And("^the deployment process has been launched$")
    public void the_deployment_process_has_been_launched() throws Throwable {
        // check for a logStart event in the log
    }
}