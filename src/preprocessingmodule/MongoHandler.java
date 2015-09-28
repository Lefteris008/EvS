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
 * @version 2015.09.28_1646_wave1
 */
public class MongoHandler {
    
    public static MongoClient client;
    public static MongoDatabase db;
    
    /**
     * Constructor, creates a connection with the MongoDB instance
     * @param config A configuration object
     */
    public MongoHandler(Config config) {
        try {
        client = new MongoClient(config.getServerName(), config.getServerPort());
        } catch (MongoClientException e) {
            System.out.println("Error connecting to client");
            client = null;
        }
    }
    
    /**
     * Create a connection to the MongoDB database
     * @param config A configuration object
     */
    public final void getMongoConnection(Config config) {
        
        try {
            
            db = client.getDatabase(config.getDBName());
            System.out.println("Succesfully connected to '" + db.getName() + "'");
        } catch (Exception e) {
            System.out.println("There was a problem connecting to MongoDB client.");
        }
    }
    
    /**
     * Closes an open connection with the MongoDB client
     * @param config A configuration object
     */
    public final void closeMongoConnection(Config config) {
        
        try {
            client.close();
            System.out.println("Database '" + config.getDBName() + "' closed!");
        } catch (Exception e) {
            System.out.println("Problem closing the database");
        }
    }
    
    /**
     * Method to store a newly retrieved tweet into the MongoDB store
     * @param status The tweet along with its additional information (location, date etc)
     * @param config A configuration object
     * @return True if the process succeeds, false otherwise
     */
    public final boolean insertTweetToMongoDB(Status status, Config config) {
        
        try {
            db.getCollection(config.getRawTweetsCollectionName()).insertOne(
                new Document("tweet",
                        new Document()
                                .append("user", status.getUser().getName()) //Username
                                .append("text", status.getText()) //Actual tweet
                                .append("date", status.getCreatedAt()) //Date published
                                .append("is_retweet", status.isRetweet()) //True if the tweet is a retweet, false otherwise
                )
            );
        return true;
        } catch(MongoException e) {
            System.out.println("There was a problem inserting the tweet.");
            return false;
        }
    }
    
    /**
     * Method to delete a collection.
     * @param collectionName The collection to be deleted from the DB
     * @return True if the process succeeds, false otherwise
     */
    public final boolean dropCollection(String collectionName) {
        
        try {
            db.getCollection(collectionName).drop();
            return false;
        } catch (MongoException e) {
            System.out.println("There was a problem deleting collection '" + collectionName + "'");
            return false;
        }
    }
    
    /**
     * This method drops the MongoDB database that is opened.
     * @return True if the process succeeds, false otherwise
     */
    public final boolean dropDB() {
        try {
            db.drop();
            return true;
        } catch (MongoException e) {
            System.out.println("There was a problem deleting database '" + db.getName() + "'");
            return false;
        }
    }
}
