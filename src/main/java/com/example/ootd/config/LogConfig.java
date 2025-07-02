package com.example.ootd.config;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.text.SimpleDateFormat;
import java.util.Date;
import lombok.Setter;
import org.bson.Document;
import org.springframework.context.annotation.Configuration;

@Configuration
@Setter
public class LogConfig extends AppenderBase<ILoggingEvent> {

  private String uri;
  private String collectionName = "logs";
  private String dbName;
  private MongoClient mongoClient;
  private MongoDatabase database;

   @Override
  public void start() {
    if (uri == null || uri.isBlank()) {
      addError("MongoDB uri 빠짐");
      return;
    }
    try {
      mongoClient = MongoClients.create(uri);

      String databaseName = (dbName != null && !dbName.isBlank())
          ? dbName
          : extractDbName(uri);

      database = mongoClient.getDatabase(databaseName);
      super.start();
    } catch (Exception e) {
      addError("MongoDB 연결 실패", e);
    }
  }

  @Override
  protected void append(ILoggingEvent event) {
    if (database == null) {
      addError("MongoDB 연결 실패");
      return;
    }
    try {
      String dateSuffix = new SimpleDateFormat("yyyy_MM_dd").format(new Date(event.getTimeStamp()));
      String dynamicCollectionName;
      if (event.getLoggerName() != null &&
          (event.getLoggerName().contains(".security") || event.getLoggerName()
              .contains(".user"))) {
        dynamicCollectionName = collectionName + "_" + dateSuffix + "_security";
      } else {
        dynamicCollectionName = collectionName + "_" + dateSuffix;
      }

      MongoCollection<Document> currentCollection = database.getCollection(dynamicCollectionName);

      Document doc = new Document()
          .append("timestamp", new Date(event.getTimeStamp()))
          .append("level", event.getLevel().toString())
          .append("thread", event.getThreadName())
          .append("logger", event.getLoggerName())
          .append("message", event.getFormattedMessage());

      currentCollection.insertOne(doc);
    } catch (Exception e) {
      addError("MongoDB 로그 저장 실패", e);
    }
  }

 @Override
  public void stop() {
    if (mongoClient != null) {
      mongoClient.close();
    }
    super.stop();
  }

  private String extractDbName(String connectionUri) {
    int slash = connectionUri.lastIndexOf('/');
    if (slash == -1 || slash == connectionUri.length() - 1) {
      throw new IllegalArgumentException("uri 빠짐");
    }
    int question = connectionUri.indexOf('?', slash);
    return (question == -1)
        ? connectionUri.substring(slash + 1)
        : connectionUri.substring(slash + 1, question);
  }
}