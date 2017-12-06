package org.gatblau.prometeo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class LogManager {

    @Autowired
    private Log thisLog;

    private ObjectMapper _mapper = new ObjectMapper(new YAMLFactory());

    private void insert(Event event) {
        if (canAccessLog(event.getProcessId())) {
            thisLog.insertEvent(event);
        }
    }

    public void start(Data data) {
        String info = String.format("A new Prometeo process '%s' is starting up.", data.getProcessId());
        insert(new Event(data.getProcessId(), data.getProject(), EventType.START_PROCESS, info));
    }

    public void startDevMode(Data data) {
        String info = String.format("A new Prometeo process '%s' is starting up in DEVELOPER MODE..", data.getProcessId());
        insert(new Event(data.getProcessId(), data.getProject(), EventType.START_DEV_PROCESS, info));
    }

    public void shutdown(Data data) {
        String info = String.format("The Prometeo process '%s' is shutting down.", data.getProcessId());
        insert(new Event(data.getProcessId(), data.getProject(), EventType.END_PROCESS, info));
    }

    public void process(Data data, String output, String command, EventType eventType, boolean success, String error) {
        insert(new Event(data.getProcessId(), data.getProject(), eventType, output, command, success, error));
    }

    public void payload(Data data) {
        try {
            String payload = _mapper.writeValueAsString(data);
            insert(new Event(data.getProcessId(), data.getProject(), EventType.CONFIG, payload));
        } catch (Exception ex) {
            insert(new Event(data.getProcessId(), data.getProject(), EventType.ERROR, ex.getMessage()));
        }
    }

    public void callback(Data data, String info) {
        insert(new Event(data.getProcessId(), data.getProject(), EventType.CALLBACK, info));
    }

    public void callback(Data data, boolean success, String info, String error) {
        insert(new Event(data.getProcessId(), data.getProject(), EventType.CALLBACK, info, success, error));
    }

    public List<Event> getLogs(String processId) {
        if (canAccessLog(processId)) {
            return thisLog.get(processId);
        }
        return new ArrayList<>();
    }

    public Result getResult(String processId) {
        if (canAccessLog(processId)) {
            Event error = thisLog.getError(processId);
            if (error == null) {
                return new Result();
            }
            return new Result(error);
        }
        return new Result("Log database is not accessible.");
    }

    private boolean canAccessLog(String processId) {
        return thisLog.checkDB();
    }

    public void invalidPayload(String processId, String payload, String message) {
        insert(new Event(processId, "", EventType.INVALID_PAYLOAD, payload, false, message));
    }
}
