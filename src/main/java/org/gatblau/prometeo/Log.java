package org.gatblau.prometeo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;


//@Configuration
//@ConfigurationProperties
//@Component

@Component
public class Log {

    @Autowired
    private DBConfig dbConfig;

    private MongoClient _mongo;
    private MongoDatabase _db;
    private boolean _connected = false;
    private MongoCollection<Event> _events;

    @Value("${database-user:missing}")
    private String _username;

    @Value("${database-password:missing}")
    private String _password;

    @Value("${database-name:prometeo}")
    private String _dbName;

    @Value("${database-admin-password:missing}")
    private String _adminpwd;

    @Value("${dbhostname:mongodb}")
    private String _dbHost;

    @Value("${dbhostport:27017}")
    private String _dbPort;


    public boolean checkDB(){
        try {
            if (_db == null){
                _db = getDb(_dbName, _dbHost, Integer.parseInt(_dbPort));
            }
            _events = _db.getCollection("events", Event.class);
            _connected = true;
        }
        catch (Exception ex) {
            System.out.println("Unable to access database "+ex.getMessage());
            ex.printStackTrace();
            _db = null;
            _connected = false;
            return false;
        }
        return true;
    }

    public boolean connected() {
        return _connected;
    }

    public MongoCredential mongoCredential() {
        return MongoCredential.createCredential(
                _username,
                _dbName,
                _password.toCharArray());
    }


    private MongoDatabase getDb(String dbName, String host, int port) {
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClient.getDefaultCodecRegistry(), fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        _mongo = new MongoClient(new ServerAddress(host, port), Arrays.asList(mongoCredential()),MongoClientOptions.builder().codecRegistry(pojoCodecRegistry).build());
        return _mongo.getDatabase(dbName);
    }



    public void insertEvent(Event event){
        String insertFailed = String.format("Connection to Log database failed: '%s'. Could not insert '%s' event.", "%s", event.getEventType().toString());
        try {
            _events.insertOne(event);
        }
        catch (Exception ex){
            System.out.println(String.format(insertFailed, ex.getMessage()));
        }
    }

    public List<Event> get(String processId) {
        List<Event> events = new ArrayList<>();
        try (MongoCursor<Event> cursor = _events.find(eq("processId", processId)).iterator()) {
            while (cursor.hasNext()) {
                events.add(cursor.next());
            }
        }
        return events;
    }

    public Event getError(String processId) {
        return _events.find(and(eq("processId", processId), eq("success", false))).first();
    }
}
