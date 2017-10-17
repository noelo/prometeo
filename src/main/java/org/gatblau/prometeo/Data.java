package org.gatblau.prometeo;

import net.minidev.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Data {
    private String _processId;
    private String _repoUri;
    private String _tag;
    private String _callbackUri;
    private String _project;
    private String _repoName;
    private String _vars;
    private String _verbosity;
    private String _check;

    public Data(String processId, List<Object> payload){
        _processId = processId;
        _repoUri = getCommandValue(payload, "repoUri", true);
        _tag = getCommandValue(payload, "tag");
        _callbackUri = getCommandValue(payload, "callbackUri");
        _project = getCommandValue(payload, "project", true);
        _repoName = getRepoName(_repoUri);
        _vars = toJSON(getVars(payload));
        _verbosity= getCommandValue(payload, "verbosity");
        _check = getCommandValue(payload, "checkMode");
    }

    private String getRepoName(String repoUri) {
        String[] values = repoUri.split("/");
        String value = values[values.length - 1];
        values = value.split("\\.");
        return values[0];
    }

    public String getProcessId() {
        return _processId;
    }

    public String getRepoUri() {
        return _repoUri;
    }

    public String getTag() {
        return _tag;
    }

    public String getCallbackUri() {
        return _callbackUri;
    }

    public String getProject() {
        return _project;
    }

    public String getRepoName() {
        return _repoName;
    }

    public String getVars() {
        return _vars;
    }

    public String getVerbosity() {
        if (_verbosity == null || _verbosity.length() == 0 || !_verbosity.matches("[v|V]{1,4}")) {
            return "v";
        }
        return _verbosity.toLowerCase();
    }

    private <T> T getCommandValue(List<Object> payload, String key, boolean required){
        T value = null;
        try {
           value = (T) ((Map<String, Object>)((LinkedHashMap)payload.get(0)).get("command")).get(key);
        }
        catch(Exception ex){
            throw new RuntimeException(String.format("Invalid Payload"));
        }
        if (required && value == null){
            throw new RuntimeException(String.format("Invalid Payload: could not find variable %s", key));
        }
        return value;
    }

    private <T> T getCommandValue(List<Object> payload, String key){
        return getCommandValue(payload, key,false);
    }

    private Map<String, Object> getVars(List<Object> payload){
        return (Map<String, Object>) ((LinkedHashMap)payload.get(1)).get("vars");
    }

    private String toJSON(Map<String, Object> map){
        JSONObject jsonObject = new JSONObject(map);
        String value = jsonObject.toString();
        return value.replace("\\", "");
    }

    public boolean hasTag() {
        return _tag != null && _tag.trim().length() > 0;
    }

    public String getCheck() {
        return _check;
    }

    public void setCheck(String check) {
        _check = check;
    }

    public boolean checkMode() {
        return (_check != null && _check.toLowerCase().trim().equals("yes"));
    }
}

