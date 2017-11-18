package org.gatblau.prometeo;

import net.minidev.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Processor implements Runnable {
    private LogManager _log;
    private Command _cmd;
    private String _processId;
    private String _runAs;
    private List<Object> _payload;
    private String _workDir;
    private boolean _runSingleRoleOnly;

    public Processor(String processId, String runAs, List<Object> payload, Command cmd, LogManager log, String workDir, boolean runSingleRoleOnly) {
        _cmd = cmd;
        _log = log;
        _payload = payload;
        _processId = processId;
        _runAs = runAs;
        _workDir = workDir;
        _runSingleRoleOnly = runSingleRoleOnly;
    }

    @Override
    public void run() {
        if (devMode()) {
            runInDevMode(_runSingleRoleOnly);
        }
        else {
            runInProdMode(_runSingleRoleOnly);
        }
    }

    private void runInProdMode(boolean singleRoleOnly) {
        try {
            Data data = getData();
            if (data == null) return;
            _log.start(data);
            _log.payload(data);

            RunResult result = run(data, getGitCloneCmd(data), EventType.DOWNLOAD_SCRIPTS);
            if (shouldHaltProcess(data, result)) return;

            if (data.hasTag()){
                if (singleRoleOnly) {
                    result = run(data, getGitCheckoutTagCmd(data, "/roles"), EventType.CHECKOUT_TAG);
                }
                else {
                    result = run(data, getGitCheckoutTagCmd(data, ""), EventType.CHECKOUT_TAG);
                }
                if (shouldHaltProcess(data, result)) return;
            }

            if (singleRoleOnly) {
                // no requirements.yml and site.yml programmatically created
                createSiteFile(data);
            }
            else {
                // process requirements.yml
                result = run(data, getAnsibleSetupCmd(data), EventType.SETUP_ANSIBLE);
            }

            result = run(data, getAnsibleRunCmd(data), EventType.RUN_ANSIBLE);
            if (shouldHaltProcess(data, result)) return;

            doCallback(data, null);

            result = run(data, getCleanupCmd(data), EventType.REMOVE_WORKDIR);
            if (shouldHaltProcess(data, result)) return;

            _log.shutdown(data);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private Data getData() {
        Data data;
        try {
            data = new Data(_processId, _payload);
        }
        catch (Exception e) {
            _log.invalidPayload(_processId, arrayToString(_payload), e.getMessage());
            return null;
        }
        return data;
    }

    private String arrayToString(List<Object> payload) {
        StringBuilder b = new StringBuilder();
        for(Object item: payload){
            b.append(item);
        }
        return b.toString();
    }

    private void createSiteFile(Data data) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/role_playbook.yml")));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            String site = String.format(sb.toString(), data.getHostPattern(), data.getRoleRepoName());
            FileOutputStream out = new FileOutputStream(String.format(_workDir + "/%2$s_%1$s/site.yml", data.getProcessId(), data.getRoleRepoName()));
            out.write(site.getBytes());
            out.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            br.close();
        }
    }

    private void runInDevMode(boolean singleRoleOnly){
        try {
            Data data = getData();
            if (data == null) return;
            _log.start(data);
            _log.payload(data);

            RunResult result = run(data, getAnsibleDevModeRunCmd(data), EventType.RUN_ANSIBLE);
            if (shouldHaltProcess(data, result)) return;

            result = run(data, getCleanupCmd(data), EventType.REMOVE_WORKDIR);
            if (shouldHaltProcess(data, result)) return;

            _log.shutdown(data);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private RunResult run(Data data, String[] cmd, EventType eventType) {
        Command.Result r = _cmd.execute(cmd, _workDir);
        if (r.exitVal == 0) {
            _log.process(data, r.output, ArrayToString(cmd), eventType, true, null);
            return new RunResult(true, null);
        }
        else {
            _log.process(data, null, ArrayToString(cmd), eventType, false, r.output);
            _log.shutdown(data);
            return new RunResult(false, r.output);
        }
    }

    private String ArrayToString(String[] values) {
        StringBuilder sb = new StringBuilder();
        for (String value : values) {
            sb.append(value);
            sb.append(" ");
        }
        String value = sb.toString().trim();
        return (value.isEmpty() ?  null : value);
    }

    private String[] getAnsibleRunCmd(Data data) {
        String inventory = data.getInventory();
        if (inventory.equals("hosts")) {
            inventory = data.getHostPattern();
        }
        else {
            inventory = "inventory";
        }
        ArrayList<String> list = new ArrayList<>();
        list.add("ansible-playbook");
        String repoName = (data.getCfgRepoUri() != null) ? data.getCfgRepoName() : data.getRoleRepoName();
        list.add(String.format("./%2$s_%1$s/site.yml", data.getProcessId(), repoName));
        if (!data.getInventory().trim().toLowerCase().equals("none")) {
            list.add("-i");
            if (data.getInventory().trim().toLowerCase().equals("hosts")) {
                list.add(String.format("%s", data.getHostPattern()));
            }
            else {
                list.add(String.format("./%2$s_%1$s/%3$s", data.getProcessId(), data.getCfgRepoName(), inventory));
            }
        }
        list.add(String.format("-%s", data.getVerbosity()));
        if  (data.checkMode()) {
            list.add("--check");
        }
        list.add("-u");
        list.add((data.getRunAs() != null) ? data.getRunAs() : _runAs);
        list.add("--extra-vars");
        list.add(data.getVars());

        return list.toArray(new String[list.size()-1]);
    }

    private String[] getAnsibleDevModeRunCmd(Data data) {
        ArrayList<String> list = new ArrayList<>();
        list.add("ansible-playbook");
        list.add(String.format("./%1$s/site.yml", data.getProjectFolder()));
        list.add("-i");
        list.add(String.format("./%1$s/inventory", data.getProjectFolder()));
        list.add(String.format("-%s", data.getVerbosity()));
        if  (data.checkMode()) {
            list.add("--check");
        }
        list.add("--extra-vars");
        list.add(data.getVars());

        return list.toArray(new String[list.size()-1]);
    }

    private String[] getAnsibleSetupCmd(Data data) {
        return new String[]{
            "ansible-galaxy",
            "install",
            "-r",
            String.format("./%2$s_%1$s/requirements.yml", data.getProcessId(), data.getCfgRepoName()),
            String.format("--roles-path=./%2$s_%1$s/roles", data.getProcessId(), data.getCfgRepoName())
        };
    }

    private String[] getGitCloneCmd(Data data) {
        ArrayList<String> list = new ArrayList<>();
        list.add("git");
        list.add("clone");
        if (data.getCfgRepoUri() != null) {
            list.add(data.getCfgRepoUri());
            list.add(String.format("./%2$s_%1$s", data.getProcessId(), data.getCfgRepoName()));
        }
        else {
            list.add(data.getRoleRepoUri());
            list.add(String.format("./%2$s_%1$s/roles/%2$s", data.getProcessId(), data.getRoleRepoName()));
        }
        return list.toArray(new String[list.size()-1]);
    }

    private String[] getGitCheckoutTagCmd(Data data, String rolesPath) {
        return new String[] {
            "cd",
            String.format("./%2$s_%1$s%3$s", data.getProcessId(), data.getCfgRepoName(), rolesPath),
            "&&",
            "git",
            "checkout",
            data.getTag()
        };
    }

    private String[] getCleanupCmd(Data data) {
         return new String[]{
             "rm",
             "-rf",
             String.format("./%2$s_%1$s", data.getProcessId(), data.getCfgRepoName())
         };
    }

    private HttpEntity<String> getEntity(Map<String, Object> map) {
        JSONObject jsonObject = new JSONObject(map);
        String payload = jsonObject.toString().replace("\\", "");
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("Content-Type", "application/x-yaml");
        return new HttpEntity<String>(payload, requestHeaders);
    }

    private boolean devMode() {
        return !_workDir.equals("/prometeo");
    }

    private class RunResult {
        RunResult(boolean success, String error){
            this.success = success;
            this.error = error;
        }
        public boolean success;
        public String error;
    }

    private void doCallback(Data data, String error) {
        if (data.getCallbackUri() != null && data.getCallbackUri().trim().length() > 0) {
            try {
                RestTemplate client = new RestTemplate();
                Map<String, Object> payload = new HashMap<>();
                if (error == null) {
                    payload.put("result", "OK");
                }
                else {
                    payload.put("result", error);
                }
                payload.put("processId", data.getProcessId());
                payload.put("repoUri", data.getCfgRepoUri());
                payload.put("tag", data.getTag());
                ResponseEntity<String> response = client.postForEntity(data.getCallbackUri(), getEntity(payload), String.class);
                if (response.getStatusCodeValue() == 200) {
                    if (error == null){
                        _log.callback(data, "Called back reported successful execution.");
                    } else {
                        _log.callback(data, false, "Callback failed.", response.getStatusCode().getReasonPhrase());
                    }
                }
                else {
                    _log.callback(data, false, "Called back reporting execution with errors.", response.getBody());
                }
            }
            catch (Exception ex) {
                _log.callback(data, false, "Callback failed.", ex.getMessage());
            }
        }
    }

    private boolean shouldHaltProcess(Data data, RunResult result) {
        if (!result.success) {
            doCallback(data, result.error);
            return true;
        }
        return false;
    }
}