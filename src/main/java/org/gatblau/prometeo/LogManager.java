package org.gatblau.prometeo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class LogManager {
    private Log thisLog;
    @Value("${LOG_DB_NAME:prometeo}")
    private String _dbName;

    @Value("${LOG_DB_HOST:localhost}")
    private String _dbHost;

    @Value("${LOG_DB_PORT:27017}")
    private String _dbPort;

    private ObjectMapper _mapper = new ObjectMapper(new YAMLFactory());

    private void insert(Event event) {
        String insertFailed = String.format("Connection to Log database failed: '%s'. Could not insert '%s' event.", "%s", event.getEventType().toString());
        if (thisLog != null) {
            thisLog.insertEvent(event);
        }
        else {
            Log newLog = new Log(_dbName, _dbHost, _dbPort);
            if (newLog.connected()){
                newLog.insertEvent(event);
                thisLog = newLog;
            }
            else {
                System.out.println(String.format(insertFailed, "Connection timeout."));
            }
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

    public void process(Data data, String output, String command, EventType eventType) {
        insert(new Event(data.getProcessId(), data.getProject(), eventType, output, command));
    }

    public void error(Data data, String output, String command) {
        insert(new Event(data.getProcessId(), data.getProject(), EventType.ERROR, output, command));
    }

    public void payload(Data data) {
        try {
            String payload = _mapper.writeValueAsString(data);
            insert(new Event(data.getProcessId(), data.getProject(), EventType.CONFIG, payload));
        }
        catch (Exception ex) {
            insert(new Event(data.getProcessId(), data.getProject(), EventType.ERROR, ex.getMessage()));
        }
    }

    public void callback(Data data) {
        insert(new Event(data.getProcessId(), data.getProject(), EventType.CALLBACK, "Called back successfully."));
    }

    public List<Event> getLogs(String processId) {
        String queryFailed = String.format("Connection to Log database failed: '%s'. Could not query log for process Id ='%s'.", "%s", processId);
        if (thisLog != null) {
            return thisLog.get(processId);
        }
        else {
            Log newLog = new Log(_dbName, _dbHost, _dbPort);
            if (newLog.connected()){
                thisLog = newLog;
                return thisLog.get(processId);
            }
            else {
                System.out.println(String.format(queryFailed, "Connection timeout."));
            }
        }
        return new ArrayList<>();
    }
}
