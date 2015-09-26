package preprocessingmodule;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientException;
import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import twitter4j.Status;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2015.09.27_0202_wave1
 */
public class MongoHandler {
    
    public static MongoClient client;
    public static MongoDatabase db;
    
    public MongoHandler() {
        try {
        client = new MongoClient(Config.serverName, Config.serverPort);
        } catch (MongoClientException e) {
            System.out.println("Error connecting to client");
            client = null;
        }
    }
    
    /**
     * Create a connection to the MongoDB database
     */
    public final void getMongoConnection() {
        
        try {
            
            db = client.getDatabase(Config.dbName);
            System.out.println("Succesfully connected to '" + db.getName() + "'");
        } catch (Exception e) {
            System.out.println("There was a problem connecting to MongoDB client.");
        }
    }
    
    /**
     * Closes an open connection with the MongoDB client
     */
    public final void closeMongoConnection() {
        
        try {
            client.close();
            System.out.println("Database '" + Config.dbName + "' closed!");
        } catch (Exception e) {
            System.out.println("Problem closing the database");
        }
    }
    
    /**
     * Method to store a newly retrieved tweet into the MongoDB store
     * @param status The tweet along with its additional information (location, date etc)
     * @return True if the process succeeds, false otherwise
     */
    public final boolean insertTweetToMongoDB(Status status) {
        
        try {
//            System.out.println(status.getUser().getName());
//            System.out.println(status.getText());
//            System.out.println(status.getCreatedAt());
//            System.out.println(status.getPlace().toString());
//            System.out.println(status.isRetweet());
            db.getCollection(Config.rawTweetsCollectionName).insertOne(
                new Document("tweet",
                        new Document()
                                .append("user", status.getUser().getName())
                                .append("text", status.getText())
                                .append("date", status.getCreatedAt())
//                                .append("place", status.getPlace().toString())
                                .append("is_retweet", status.isRetweet())
                )
            );
        return true;
        } catch(MongoException e) {
            System.out.println("There was a problem inserting the tweet.");
            return false;
        }
    }
}
