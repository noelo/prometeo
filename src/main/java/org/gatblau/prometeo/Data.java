package org.gatblau.prometeo;

import net.minidev.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Data {
    private String _processId;
    private String _cfgRepoUri;
    private String _roleRepoUri;
    private String _tag;
    private String _callbackUri;
    private String _project;
    private String _cfgRepoName;
    private String _roleRepoName;
    private String _vars;
    private String _verbosity;
    private String _check;
    private String _folder;
    private String _runAs;
    private String _hostPattern;
    private String _inventory;

    public Data(String processId, List<Object> payload) {
        _processId = processId;
        _cfgRepoUri = getCommandValue(payload, "cfgRepoUri");
        _roleRepoUri = getCommandValue(payload, "roleRepoUri");
        _tag = getCommandValue(payload, "tag");
        _callbackUri = getCommandValue(payload, "callbackUri");
        _project = getCommandValue(payload, "project");
        _cfgRepoName = getCfgRepoName(_cfgRepoUri);
        _roleRepoName = getCfgRepoName(_roleRepoUri);
        _vars = toJSON(getVars(payload));
        _verbosity= getCommandValue(payload, "verbosity");
        _check = getCommandValue(payload, "checkMode");
        _folder = getCommandValue(payload, "folder",System.getenv("WORK_DIR") != null);
        _runAs = getCommandValue(payload, "runAs");
        _hostPattern = getCommandValue(payload, "hostPattern", _roleRepoUri != null && !_roleRepoUri.isEmpty());
        _inventory = getCommandValue(payload, "inventory");  // none, local-file, hosts, remote-file (not implemented yet)

        // if not in dev mode
        if (_folder == null || _folder.isEmpty()) {
            // requires repo URI to be defined
            if ((_cfgRepoUri == null || _cfgRepoUri.isEmpty()) && (_roleRepoUri == null || _roleRepoUri.isEmpty())) {
                throw new RuntimeException(String.format("Invalid Payload: at least one repository URI (of the form cfgRepoUri or roleRepoUri) must be defined in the payload."));
            }
            if ((_cfgRepoUri != null) && (_roleRepoUri != null)) {
                throw new RuntimeException(String.format("Invalid Payload: conflicting cfgRepoUri and roleRepoUri defined. Only one value must be defined in the payload."));
            }
            if (_roleRepoUri != null && (!getInventory().equals("hosts") || _hostPattern == null)) {
                throw new RuntimeException(String.format("Invalid Payload: 'hostPattern' and 'inventory' option set to 'hosts' are required when specifying 'roleRepoUri'."));
            }
        }

        if (getInventory().equals("hosts") && _hostPattern == null) {
            throw new RuntimeException(String.format("Invalid Payload: 'hostPattern' is required when 'inventory' option is set to 'hosts'."));
        }

        if (!(getInventory().equals("none") || getInventory().equals("local-file") || getInventory().equals("hosts"))) {
            throw new RuntimeException(String.format("Invalid Payload: 'inventory' option is invalid - valid values are: none, local-file, hosts."));
        }
    }

    private String getCfgRepoName(String repoUri) {
        if (repoUri != null) {
            String[] values = repoUri.split("/");
            String value = values[values.length - 1];
            values = value.split("\\.");
            return values[0];
        }
        return null;
    }

    public String getProcessId() {
        return _processId;
    }

    public String getCfgRepoUri() {
        return _cfgRepoUri;
    }

    public String getRoleRepoUri() {
        return _roleRepoUri;
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

    public String getCfgRepoName() {
        return _cfgRepoName;
    }

    public String getRoleRepoName() {
        return _roleRepoName;
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

    public String getProjectFolder() {
        return _folder;
    }

    public void setProjectFolder(String folder) {
        _folder = folder;
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

    public String getRunAs() {
        return _runAs;
    }

    public void setRunAs(String runAs) {
        _runAs = runAs;
    }

    public String getHostPattern() {
        return _hostPattern;
    }

    public String getInventory() {
        return (_inventory == null || _inventory.isEmpty()) ? "local-file" : _inventory;
    }
}

