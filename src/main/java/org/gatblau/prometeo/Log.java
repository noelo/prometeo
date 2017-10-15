package org.gatblau.prometeo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class Log {
    private MongoClient _mongo;
    private MongoDatabase _db;
    private boolean _connected = false;

    public Log(String dbName, String host, String port) {
        try {
            _db = getDb(dbName, host, Integer.parseInt(port));
            _connected = true;
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean connected() {
        return _connected;
    }

    private MongoDatabase getDb(String dbName, String host, int port) {
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClient.getDefaultCodecRegistry(), fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        _mongo = new MongoClient(new ServerAddress(host, port), MongoClientOptions.builder().codecRegistry(pojoCodecRegistry).build());
        return _mongo.getDatabase(dbName);
    }

    public void insertEvent(Event event){
        String insertFailed = String.format("Connection to Log database failed: '%s'. Could not insert '%s' event.", "%s", event.getEventType().toString());
        try {
            MongoCollection<Event> collection = _db.getCollection("events", Event.class);
            collection.insertOne(event);
        }
        catch (Exception ex){
            System.out.println(String.format(insertFailed, ex.getMessage()));
        }
    }
}
