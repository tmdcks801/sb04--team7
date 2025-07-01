import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
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

  private String username;
  private String password;

  private MongoClient mongoClient;

  @Override
  public void start() {
    try {

      super.start();
    } catch (Exception e) {
    }
  }

  @Override
  protected void append(ILoggingEvent event) {
    try {
      MongoDatabase database = mongoClient.getDatabase(dbName);

      String dateSuffix = new SimpleDateFormat("yyyy_MM_dd").format(new Date());
      MongoCollection<Document> currentCollection = database.getCollection(dynamicCollectionName);


      currentCollection.insertOne(doc);
    } catch (Exception e) {
    }
  }

  @Override
  public void stop() {
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