package com.example.ootd.config;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.bson.Document;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LogConfig extends AppenderBase<ILoggingEvent> {

  private String host = "localhost";
  private int port = 27017;
  private String dbName;
  private String collectionName = "log";
  private String username;
  private String password;

  private MongoClient mongoClient;

  @Override
  public void start() {
    try {
      if (System.getenv("MONGO_HOST") != null) {
        this.host = System.getenv("MONGO_HOST");
      }
      if (System.getenv("MONGO_PORT") != null) {
        this.port = Integer.parseInt(System.getenv("MONGO_PORT"));
      }
      if (System.getenv("LOG_MONGO_DB") != null) {
        this.dbName = System.getenv("LOG_MONGO_DB");
      }

      String uri = buildConnectionUri();
      mongoClient = MongoClients.create(uri);
      super.start();
    } catch (Exception e) {
      addError("Failed to start MongoDBAppender", e);
    }
  }

  @Override
  protected void append(ILoggingEvent event) {
    try {
      if (mongoClient == null) {
        mongoClient = MongoClients.create(buildConnectionUri());
      }

      MongoDatabase database = mongoClient.getDatabase(dbName);

      String dateSuffix = new SimpleDateFormat("yyyy_MM_dd").format(new Date());
      String dynamicCollectionName = collectionName + "_" + dateSuffix;
      MongoCollection<Document> currentCollection = database.getCollection(dynamicCollectionName);

      Document doc = new Document();
      doc.append("timestamp", new Date(event.getTimeStamp()));
      doc.append("level", event.getLevel().toString());
      doc.append("thread", event.getThreadName());
      doc.append("logger", event.getLoggerName());
      doc.append("message", event.getFormattedMessage());

      currentCollection.insertOne(doc);
    } catch (Exception e) {
      addError("Failed to write log to MongoDB", e);
    }
  }

  @Override
  public void stop() {
    super.stop();
    if (mongoClient != null) {
      mongoClient.close();
    }
  }

  private String buildConnectionUri() {
    if (username != null && password != null) {
      return String.format("mongodb://%s:%s@%s:%d", username, password, host, port);
    } else {
      return String.format("mongodb://%s:%d", host, port);
    }
  }

}