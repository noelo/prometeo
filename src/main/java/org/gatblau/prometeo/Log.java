package org.gatblau.prometeo;

import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class Log {
    private MongoClient _mongo;
    private MongoDatabase _db;
    private boolean _connected = false;
    private MongoCollection<Event> _events;

    public Log(String dbName, String host, String port) {
        try {
            _db = getDb(dbName, host, Integer.parseInt(port));
            _events = _db.getCollection("events", Event.class);
            _connected = true;
        }
        catch (Exception ex) {
            System.out.println(String.format("WARNING: cannot connect to LOG database: %s. Will attempt to connect later.", ex.getMessage()));
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
}
