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
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;

@Configuration
@ConfigurationProperties
//public class DBConfig extends AbstractMongoConfiguration {
public class DBConfig  {

    @Value("${database-user:missing}")
    private String username;

    @Value("${database-password:missing}")
    private String password;

    @Value("${database-name:missing}")
    private String dbname;

    @Value("${database-admin-password:missing}")
    private String adminpwd;

    @Value("${dbhostname:mongodb}")
    private String dbhost;

    @Value("${dbhostport:27017}")
    private int dbport;

//    @Override
    protected String getDatabaseName() {
        return this.dbname;
    }

    public String getPassword() {
        return this.password;
    }

    public String getUsername() {
        return this.username;
    }

    public String getHostname() {
        return this.dbhost;
    }

    public int getPort() {
        return this.dbport;
    }

    public String getAdminPassword() {
        return this.adminpwd;
    }

//    @Bean
//    public MongoCredential mongoCredential() {
//        return MongoCredential.createCredential(
//                this.username,
//                this.dbname,
//                this.password.toCharArray());
//    }
//
//    @Override
//    @Bean
//    public Mongo mongo() throws Exception {
//        ServerAddress myAddress = new ServerAddress(dbhost,dbport);
//        return new MongoClient(myAddress, Arrays.asList(mongoCredential()));
//    }

//    @PostConstruct
    public void init() {
        System.out.println(">>>>>>>Database config -->>>>" + dbhost + " port " + dbport +" with user "+username + " @DB "+dbname);
    }


}