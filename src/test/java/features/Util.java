package features;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

@Component
public class Util {
    private Map<Vars, Object> cache = new HashMap<>();

    public Util(){
    }

    public void check(boolean condition, int attempts, int interval) {
        for (int i = 0; i < attempts; i++) {
            if (condition) {
                return;
            }
            else {
                wait(interval);
            }
        }

    }

    private void wait(int interval) {
        try {
            Thread.sleep(interval);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void put(Vars key, Object value) {
        cache.put(key, value);
    }

    public <T> T get(Vars key) {
        T value = (T) cache.get(key);
        if (value == null) {
            throw new RuntimeException(
                    String.format("Value with key %s not found in cache.", key));
        }
        return value;
    }

    public void remove(String key) {
        cache.remove(key);
    }

    public boolean exists(String key) {
        return cache.containsKey(key);
    }

    public String getFile(String fileName) {

        StringBuilder result = new StringBuilder("");

        //Get file from resources folder
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());

        try (Scanner scanner = new Scanner(file)) {

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                result.append(line).append("\n");
            }

            scanner.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result.toString();

    }

    public static void setEnv(Map<String, String> newenv) throws Exception {
        try {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            env.putAll(newenv);
            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map<String, String> cienv = (Map<String, String>)     theCaseInsensitiveEnvironmentField.get(null);
            cienv.putAll(newenv);
        } catch (NoSuchFieldException e) {
            Class[] classes = Collections.class.getDeclaredClasses();
            Map<String, String> env = System.getenv();
            for(Class cl : classes) {
                if("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                    Field field = cl.getDeclaredField("m");
                    field.setAccessible(true);
                    Object obj = field.get(env);
                    Map<String, String> map = (Map<String, String>) obj;
                    map.clear();
                    map.putAll(newenv);
                }
            }
        }
    }

    public HttpEntity<String> getEntity(String payload) {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("Content-Type", "application/x-yaml");
        requestHeaders.add("Accept", "application/x-yaml");
        if (payload != null) {
            return new HttpEntity<String>(payload, requestHeaders);
        }
        return new HttpEntity<String>(requestHeaders);
    }

    public HttpEntity<String> getEntity() {
        return getEntity(null);
    }
}
