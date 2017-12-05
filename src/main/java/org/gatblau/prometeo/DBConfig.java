package org.gatblau.prometeo;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;

import java.util.Arrays;

@Configuration
@ConfigurationProperties
public class DBConfig extends AbstractMongoConfiguration {

    @Value("${database-user}")
    private String username;

    @Value("${database-password}")
    private String password;

    @Value("${database-name}")
    private String dbname;

    @Value("${database-admin-password}")
    private String adminpwd;

    @Value("${dbhostname:mongodb}")
    private String dbhost;

    @Value("${dbhostport:27017}")
    private int dbport;

    @Override
    protected String getDatabaseName() {
        return this.dbname;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    @Bean
    public MongoCredential mongoCredential() {
        return MongoCredential.createCredential(
                this.username,
                this.dbname,
                this.password.toCharArray());
    }

    @Override
    @Bean
    public Mongo mongo() throws Exception {
        ServerAddress myAddress = new ServerAddress(dbhost,dbport);
        return new MongoClient(myAddress, Arrays.asList(mongoCredential()));
    }

}