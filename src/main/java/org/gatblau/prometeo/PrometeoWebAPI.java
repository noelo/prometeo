package org.gatblau.prometeo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
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

    @Value("${WORK_DIR:/prometeo}")
    private String _workDir;

    @Value("${RUN_AS:prometeo}")
    private String _runAs;

    @ApiOperation(value = "Returns OK if the service is up and running.", notes = "Use it as a readiness probe for the service.", response = String.class)
    @RequestMapping(value = "/", method = RequestMethod.GET, produces = "text/html")
    public String index() {
        return "OK";
    }

    @ApiOperation(value = "Request the execution of an Ansible playbook in an asynchronous way.", notes = "Pass a YAML payload with command information and playbook variables. The request returns immediately with a GUID for the process that has been launched.")
    @RequestMapping(path = "/run", method = RequestMethod.POST, consumes = "application/x-yaml")
    public ResponseEntity<String> run(@RequestBody List<Object> payload) throws InterruptedException {
        String processId = UUID.randomUUID().toString();
        _executor.execute(new Processor(processId, _runAs, payload, _cmd, _log, _workDir));
        return ResponseEntity.ok(String.format("ProcessId: %s", processId));
    }

    @ApiOperation(value = "Request the execution of an Ansible playbook in a synchronous way.", notes = "Pass a YAML payload with command information and playbook variables. The request returns a GUID for the process that has been launched after the whole process has completed.")
    @RequestMapping(path = "/run/sync", method = RequestMethod.POST, consumes = "application/x-yaml")
    public ResponseEntity<String> runSync(@RequestBody List<Object> payload) throws InterruptedException {
        String processId = UUID.randomUUID().toString();
        new Processor(processId, _runAs, payload, _cmd, _log, _workDir).run();
        return ResponseEntity.ok(String.format("ProcessId: %s", processId));
    }

    @ApiOperation(value = "Returns a list of events associated with the specified process.", notes = "provides information about the execution of a playbook based on the specified process.")
    @RequestMapping(path = "/log/{processId}", method = RequestMethod.GET, produces = {"application/json", "application/x-yaml" } )
    public ResponseEntity<List<Event>> process(@PathVariable("processId") String processId) {
        List<Event> logs = _log.getLogs(processId);
        return ResponseEntity.ok(logs);
    }

    @ApiOperation(value = "Returns the result of the execution of the specified process.", notes = "provides the result of the execution of a playbook based on the specified process.")
    @RequestMapping(path = "/result/{processId}", method = RequestMethod.GET, produces = {"application/json", "application/x-yaml" } )
    public ResponseEntity<Result> result(@PathVariable("processId") String processId) {
        Result result = _log.getResult(processId);
        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "Gets the command used by Prometeo to execute the playbook.", notes = "Pass a YAML payload with command information and playbook variables. The request returns the command used to run the playbook specified in the payload without actually running it. It is provided for testing purposes.")
    @RequestMapping(path = "/peek", method = RequestMethod.POST, consumes = "application/x-yaml")
    public ResponseEntity<String> peek(@RequestBody List<Object> payload) throws InterruptedException {
        Data data = new Data("", payload);
        return ResponseEntity.ok(String.format("ansible-playbook site.yml -i inventory -%s --extra-vars %s", data.getVerbosity(), data.getVars()));
    }

    @ApiOperation(value = "Gets the public key used by Prometeo to connect to a managed host.", notes = "Use to retrieve the public key required to configure managed hosts to accept incoming connections.")
    @RequestMapping(path = "/pubkey", method = RequestMethod.GET, produces = "text/html")
    public ResponseEntity<String> pubkey() throws InterruptedException {
        String path = "/prometeo/id_rsa.pub";
        File pubkey = new File(path);
        if (pubkey.exists()) {
            StringBuilder builder = new StringBuilder();
            try (BufferedReader r = Files.newBufferedReader(pubkey.toPath(), Charset.defaultCharset())){
                r.lines().forEach(builder::append);
            }
            catch (IOException ioex) {
                throw new RuntimeException(String.format("Failed to retrieve public key: %s", ioex.getMessage()));
            }
            return ResponseEntity.ok(builder.toString());
        }
        return ResponseEntity.notFound().build();
    }
}
