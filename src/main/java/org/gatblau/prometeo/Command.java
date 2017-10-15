package org.gatblau.prometeo;

import org.springframework.stereotype.Component;

import java.io.*;

@Component
public class Command {
    private Runtime runtime;

    public Command() {
        runtime = Runtime.getRuntime();
    }

    public Result execute(String[] command, String workingDir){
        Result result= new Result();
        try {
            Process process = runtime.exec(command, null, new File(workingDir));
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
