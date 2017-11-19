package org.gatblau.prometeo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class Command {

    @Value("${HTTP_PROXY:_}")
    private String _httpProxy;

    @Value("${HTTPS_PROXY:_}")
    private String _httpsProxy;

    @Value("${NO_PROXY:_}")
    private String _noProxy;

    private Runtime _runtime;

    public Command() {
        _runtime = Runtime.getRuntime();
    }

    public Result execute(String[] command, String workingDir){
        Result result= new Result();
        try {
            String[] envp = getEnvParams(_httpProxy, _httpsProxy, _noProxy);
            Process process = _runtime.exec(command, envp, new File(workingDir));
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while((line = input.readLine()) != null) {
                output.append(line);
            }
            while((line = error.readLine()) != null) {
                output.append(line);
            }
            result.output = output.toString();
            result.exitVal = process.waitFor();
        }
        catch(Exception e) {
            result.output = getStackTrace(e);
            result.exitVal = 1;
        }
        return result;
    }

    /**
     * Returns a formatted array with http and https proxy information ready to pass to the runtime exec method.
     * Returns null if the proxies have not been set.
     * @param httpProxy
     * @param httpsProxy
     * @param noProxy
     * @return array of String
     */
    private String[] getEnvParams(String httpProxy, String httpsProxy, String noProxy) {
        List<String> list = new ArrayList<String>();
        if (!httpProxy.equals("_")) {
            list.add(String.format("http_proxy=%s", httpProxy));
        }
        if (!httpsProxy.equals("_")) {
            list.add(String.format("https_proxy=%s", httpsProxy));
        }
        if (!noProxy.equals("_")) {
            list.add(String.format("no_proxy=%s", noProxy));
        }
        return (list.size() > 0) ? list.toArray(new String[list.size()-1]) : null;
    }

    public class Result {
        public int exitVal;
        public String output;
    }

    private String getStackTrace(Exception ex){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        return sw.toString();
    }
}
