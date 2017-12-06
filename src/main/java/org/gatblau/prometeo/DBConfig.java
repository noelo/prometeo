package org.gatblau.prometeo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties
public class DBConfig {

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

}