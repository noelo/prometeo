package org.gatblau.prometeo;

import java.util.List;

public class Processor implements Runnable {
    private static String WORK_DIR = "/prometeo";
    private LogManager _log;
    private Command _cmd;
    private String _processId;
    private List<Object> _payload;

    public Processor(String processId, List<Object> payload, Command cmd, LogManager log) {
        _cmd = cmd;
        _log = log;
        _payload = payload;
        _processId= processId;
    }

    @Override
    public void run() {
        try {
            Data data = new Data(_processId, _payload);
            _log.logStart(data);
            _log.payload(data);
            if (!run(data, getGitCloneCmd(data), EventType.DOWNLOAD_SCRIPTS)) return;
            if (data.hasTag()){
                if (!run(data, getGitCheckoutTagCmd(data), EventType.CHECKOUT_TAG)) return;
            }
            if (!run(data, getAnsibleSetupCmd(data), EventType.SETUP_ANSIBLE)) return;
            if (!run(data, getAnsibleRunCmd(data), EventType.RUN_ANSIBLE)) return;
            if (!run(data, getCleanupCmd(data), EventType.REMOVE_WORKDIR)) return;
            callBack(data);
            _log.logShutdown(data);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void callBack(Data data) {
        // TODO: create implementation to callback after process has finished
    }

    private boolean run(Data data, String[] cmd, EventType eventType) {
        Command.Result r = _cmd.execute(cmd, WORK_DIR);
        if (r.exitVal == 0) {
            _log.logProcess(data, r.output, ArrayToString(cmd), eventType);
            return true;
        } else {
            _log.logError(data, r.output, ArrayToString(cmd));
            _log.logShutdown(data);
            return false;
        }
    }

    private String ArrayToString(String[] values) {
        StringBuilder sb = new StringBuilder();
        for (String value : values) {
            sb.append(value);
            sb.append(" ");
        }
        return sb.toString().trim();
    }

    private String[] getAnsibleRunCmd(Data data) {
        return new String[]{
            "ansible-playbook",
            String.format("./%2$s_%1$s/site.yml", data.getProcessId(), data.getRepoName()),
            "-i",
            String.format("./%2$s_%1$s/inventory", data.getProcessId(), data.getRepoName()),
            String.format("-%s", data.getVerbosity()),
            "--extra-vars",
            data.getVars()
        };
    }

    private String[] getAnsibleSetupCmd(Data data) {
        return new String[]{
            "ansible-galaxy",
            "install",
            "-r",
            String.format("./%2$s_%1$s/requirements.yml", data.getProcessId(), data.getRepoName()),
            String.format("--roles-path=./%2$s_%1$s/roles", data.getProcessId(), data.getRepoName())
        };
    }

    private String[] getGitCloneCmd(Data data) {
        return new String[]{
            "git",
            "clone",
            data.getRepoUri(),
            String.format("./%2$s_%1$s", data.getProcessId(), data.getRepoName())
        };
    }

    private String[] getGitCheckoutTagCmd(Data data) {
        return new String[] {
            "cd",
            String.format("./%2$s_%1$s", data.getProcessId(), data.getRepoName()),
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
             String.format("./%2$s_%1$s", data.getProcessId(), data.getRepoName())
         };
    }
}