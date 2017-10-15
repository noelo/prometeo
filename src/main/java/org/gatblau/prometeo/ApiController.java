package org.gatblau.prometeo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;

@RestController
public class ApiController {

    @Autowired
    private Command _cmd;

    @Autowired
    private LogManager _log;

    @Autowired
    private Executor _executor;

    @RequestMapping("/")
    public String index() {
        return "OK";
    }

    @RequestMapping(path = "/run", method = RequestMethod.POST, consumes = "application/x-yaml")
    public ResponseEntity<String> run(@RequestBody List<Object> payload) throws InterruptedException {
        String processId = UUID.randomUUID().toString();
        _executor.execute(new Processor(processId, payload, _cmd, _log));
        return ResponseEntity.ok(String.format("ProcessId: %s", processId));
    }
}
