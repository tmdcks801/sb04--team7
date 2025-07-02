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

  private String host;
  private int port;
  private String dbName; // 기본값 설정
  private String collectionName;
  private String username;
  private String password;

  private MongoClient mongoClient;

   @Override
  public void start() {
    try {
      host = System.getenv("MONGO_HOST") != null ? System.getenv("MONGO_HOST") : host;
      port = System.getenv("MONGO_PORT") != null ? Integer.parseInt(System.getenv("MONGO_PORT"))
          : port;
      dbName = System.getenv("LOG_MONGO_DB") != null ? System.getenv("LOG_MONGO_DB") : dbName;

      mongoClient = MongoClients.create(buildConnectionUri());
      super.start();
    } catch (Exception e) {
      addError("MongoDBAppender 시작 실패", e);
    }
  }

  @Override
  protected void append(ILoggingEvent event) {
    if (mongoClient == null) {
      addError("MongoClient가 null이라 오류.");
      return;
    }

    try {
      MongoDatabase database = mongoClient.getDatabase(dbName);

      String dateSuffix = new SimpleDateFormat("yyyy_MM_dd").format(new Date());
      String dynamicCollectionName;

      if (event.getLoggerName().contains(".security") ||  //패이지 이름에 따라 분류되도록 함
          event.getLoggerName().contains(".user")) {
        dynamicCollectionName = collectionName + "_" + dateSuffix + "_security_test";
      } else {
        dynamicCollectionName = collectionName + "_" + dateSuffix + "_test";
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
      try {
        mongoClient.close();
      } catch (Exception e) {
        addError("종료 시 오류", e);
      }
    }
    super.stop();
  }

  private String buildConnectionUri() {
    if (username != null && password != null) {
      return String.format("mongodb://%s:%s@%s:%d", username, password, host, port);
    } else {
      return String.format("mongodb://%s:%d", host, port);
    }
  }
}