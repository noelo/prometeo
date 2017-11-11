package org.gatblau.prometeo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class Event {
    private String _eventId;
    private String _processId;
    private String _projectCode;
    private String _date;
    private String _eventType;
    private boolean _success;
    private String _info;
    private String _command;
    private String _error;

    public Event() {
    }

    public Event(String processId, String projectCode, EventType eventType, String info){
        this(processId, projectCode, eventType, info, null, true);
    }

    public Event(String processId, String projectCode, EventType eventType, String info, boolean success, String error){
        this(processId, projectCode, eventType, info, null, success, error);
    }

    public Event(String processId, String projectCode, EventType eventType, String info, String command, boolean success) {
        this(processId, projectCode, eventType, info, null, success, null);
    }

    public Event(String processId, String projectCode, EventType eventType, String info, String command, boolean success, String error){
        _eventId = UUID.randomUUID().toString();
        _projectCode = projectCode;
        _processId = processId;
        _date = getCurrentTime();
        _eventType = eventType.name();
        _info = info;
        _command = command;
        _success = success;
        _error = error;
    }

    public String getEventId() {
        return _eventId;
    }

    public void setEventId(String eventId) {
        _eventId = eventId;
    }

    public String getProjectCode() {
        return _projectCode;
    }

    public void setProjectCode(String value) {
        _projectCode = value;
    }

    public String getProcessId() {
        return _processId;
    }

    public void setProcessId(String processId) {
        _processId = processId;
    }

    public String getDate() {
        return _date;
    }

    public void setDate(String date) {
        _date = date;
    }

    public String getEventType() {
        return _eventType;
    }

    public void setEventType(String eventType) {
        _eventType = eventType;
    }

    public String getInfo() {
        return _info;
    }

    public void setInfo(String info) {
        _info = info;
    }

    public String getCommand(){
        return _command;
    }

    public void setCommand(String command){
        _command = command;
    }

    private static String getCurrentTime() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

    public boolean getSuccess() {
        return _success;
    }

    public void setSuccess(boolean success) {
        _success = success;
    }

    public String getError() {
        return _error;
    }

    public void setError(String error) {
        _error = error;
    }
}