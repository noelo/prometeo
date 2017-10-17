package org.gatblau.prometeo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;

@Api("Prometeo Web API")
@RestController
public class PrometeoWebAPI {

    @Autowired
    private Command _cmd;

    @Autowired
    private LogManager _log;

    @Autowired
    private Executor _executor;

    @ApiOperation(value = "Returns OK if the service is up and running.", notes = "Use it as a readiness probe for the service.", response = String.class)
    @RequestMapping(value = "/", method = RequestMethod.GET, produces = "text/html")
    public String index() {
        return "OK";
    }

    @ApiOperation(value = "Request the execution of an Ansible playbook.", notes = "Pass a YAML payload with command information and playbook variables. The request returns immediately with a GUID for the process that has been launched. Use it to query status of the execution using the /process operation.")
    @RequestMapping(path = "/run", method = RequestMethod.POST, consumes = "application/x-yaml")
    public ResponseEntity<String> run(@RequestBody List<Object> payload) throws InterruptedException {
        String processId = UUID.randomUUID().toString();
        _executor.execute(new Processor(processId, payload, _cmd, _log));
        return ResponseEntity.ok(String.format("ProcessId: %s", processId));
    }

    @ApiOperation(value = "Returns a list of events associated with the specified process.", notes = "provides information about the execution of a playbook based on the specified process.")
    @RequestMapping(path = "/log/{processId}", method = RequestMethod.GET, produces = {"application/json", "application/x-yaml" } )
    public ResponseEntity<List<Event>> process(@PathVariable("processId") String processId) {
        List<Event> logs = _log.getLogs(processId);
        return ResponseEntity.ok(logs);
    }

    @ApiOperation(value = "Gets the command used by Prometeo to execute the playbook.", notes = "Pass a YAML payload with command information and playbook variables. The request returns the command used to run the playbook specified in the payload without actually running it. It is provided for testing purposes.")
    @RequestMapping(path = "/peek", method = RequestMethod.POST, consumes = "application/x-yaml")
    public ResponseEntity<String> peek(@RequestBody List<Object> payload) throws InterruptedException {
        Data data = new Data("", payload);
        return ResponseEntity.ok(String.format("ansible-playbook site.yml -i inventory -%s --extra-vars %s", data.getVerbosity(), data.getVars()));
    }
}
