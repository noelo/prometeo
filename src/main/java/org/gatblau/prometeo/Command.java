package org.gatblau.prometeo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;

@Component
public class Command {

    @Value("${HTTP_PROXY:_}")
    private String _httpProxy;

    @Value("${HTTPS_PROXY:_}")
    private String _httpsProxy;

    private Runtime _runtime;

    public Command() {
        _runtime = Runtime.getRuntime();
    }

    public Result execute(String[] command, String workingDir){
        Result result= new Result();
        try {
            String[] envp = getEnvParams(_httpProxy, _httpsProxy);
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
     * @return array of String
     */
    private String[] getEnvParams(String httpProxy, String httpsProxy) {
        if (!httpProxy.equals("_") && !httpsProxy.equals("_")){
            // http and https proxies are set
            return new String[]{ String.format("http_proxy=%s", httpProxy), String.format("https_proxy=%s", httpsProxy) };
        } else if (!httpProxy.equals("_") && httpsProxy.equals("_")) {
            // only http proxy is set
            return new String[] { String.format("http_proxy=%s", httpProxy) };
        } else if (httpProxy.equals("_") && !httpsProxy.equals("_")) {
            // only https proxy is set
            return new String[]{ String.format("https_proxy=%s", httpsProxy) };
        }
        return null;
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
