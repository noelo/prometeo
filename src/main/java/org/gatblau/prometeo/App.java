package org.gatblau.prometeo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.Executor;


@SpringBootApplication(exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
public class App {
    public static String ProcessId;

    @Value("${CORE_POOL_SIZE:2}")
    private int _corePoolSize;

    @Value("${MAX_POOL_SIZE:2}")
    private int _maxPoolSize;

    @Value("${QUEUE_CAPACITY:500}")
    private int _queueCapacity;

    @Autowired
    private LogManager log;

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @PostConstruct
    private void onStartUp(){
    }

    @PreDestroy
    private void onShutDown(){
    }

    @Bean
    public Executor _executor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(_corePoolSize);
        executor.setMaxPoolSize(_maxPoolSize);
        executor.setQueueCapacity(_queueCapacity);
        executor.setThreadNamePrefix("Ansible-Process-");
        executor.initialize();
        return executor;
    }
}
