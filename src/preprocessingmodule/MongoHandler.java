/*
 * Copyright (C) 2015 Lefteris Paraskevas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package preprocessingmodule;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientException;
import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bson.Document;
import twitter4j.Status;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2015.10.03_1843_planet1
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
            Logger.getLogger(MongoHandler.class.getName()).log(Level.SEVERE, null, e);
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
            Logger.getLogger(MongoHandler.class.getName()).log(Level.SEVERE, null, e);
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
            Logger.getLogger(MongoHandler.class.getName()).log(Level.SEVERE, null, e);
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
            Logger.getLogger(MongoHandler.class.getName()).log(Level.SEVERE, null, e);
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
            Logger.getLogger(MongoHandler.class.getName()).log(Level.SEVERE, null, e);
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
            Logger.getLogger(MongoHandler.class.getName()).log(Level.SEVERE, null, e);
            return false;
        }
    }
}
