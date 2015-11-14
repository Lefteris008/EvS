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
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bson.Document;
import twitter4j.Status;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2015.11.14_1705_planet1
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
    public final void connectToMongoDB(Config config) {
        
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
     * Method to store a newly retrieved tweet and its metadata into the MongoDB store
     * @param status The tweet along with its additional information (location, date etc)
     * @param config A configuration object
     * @param event The ground truth event, for which the tweet is actually referring to
     * @return True if the process succeeds, false otherwise
     */
    public final boolean insertTweetIntoMongoDB(Status status, Config config, String event) {
        
        try {
            db.getCollection(config.getRawTweetsCollectionName()).insertOne(
                new Document("tweet",
                        new Document()
                                .append("id", String.valueOf(status.getId())) //Tweet ID
                                .append("user", status.getUser().getName()) //Username
                                .append("text", status.getText()) //Actual tweet
                                .append("date", String.valueOf(status.getCreatedAt())) //Date published
                                .append("latitude", status.getGeoLocation() != null ? String.valueOf(status.getGeoLocation().getLatitude()) : "NULL") //Latitude
                                .append("longitude", status.getGeoLocation() != null ? String.valueOf(status.getGeoLocation().getLongitude()) : "NULL") //Longitude
                                .append("number_of_retweets", String.valueOf(status.getRetweetCount())) //Retweet count
                                .append("number_of_favorites", String.valueOf(status.getFavoriteCount())) //Favorite count
                                .append("is_retweet", status.isRetweet() ? "true" : "false") //True if the tweet is a retweet, false otherwise
                                .append("is_favorited", status.getFavoriteCount() > 0 ? "true" : "false") //True if the tweet is favorited, false otherwise
                                .append("is_retweeted", status.getRetweetCount() > 0 ? "true" : "false") //True if the tweet is retweeted, false otherwise
                                .append("language", status.getLang()) //Language of text
                                .append("groundTruthEvent", event) //Ground truth event
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
     * This method parses the MongoDB store and returns the tweet that matches the String 'id'.
     * @param config A configuration object
     * @param id The id of the document to be retrieved
     * @return A Tweet object containing all the information found in the document that matched the String 'id'
     */
    public final Tweet retrieveTweetFromMongoDBStore(Config config, String id) {
        
        MongoCollection<Document> collection = db.getCollection(config.getRawTweetsCollectionName());
        
        try {
            FindIterable<Document> iterable = collection.find(new Document("tweet.id", id));
            
            Document document = iterable.first();
            Document doc = document.get("tweet", Document.class); //Get the embedded document
            
            //Get the tweet properties
            String user = doc.get("user", String.class);
            String text = doc.get("text", String.class);
            String date = doc.get("date", String.class);
            String latitude = doc.get("latitude", String.class);
            if(latitude.equals("NULL")) {
                latitude = "-1";
            }
            String longitude = doc.get("longitude", String.class);
            if(longitude.equals("NULL")) {
                longitude = "-1";
            }
            String numberOfRetweets = doc.get("number_of_retweets", String.class);
            String numberOfFavorites = doc.get("number_of_favorites", String.class);
            String isRetweet = doc.get("is_retweet", String.class);
            String isFavorited = doc.get("is_favorited", String.class);
            String isRetweeted = doc.get("is_retweeted", String.class);
            String language = doc.get("language", String.class);
            String groundTruthEvent = doc.get("groundTruthEvent", String.class);

            Tweet tweet = new Tweet(id, user, text, date, latitude, 
                    longitude, numberOfRetweets, numberOfFavorites, isRetweet, isFavorited, 
                    isRetweeted, language, groundTruthEvent);

            return tweet;
        } catch(MongoException e) {
            System.out.println("No document with id '" + id + "' was found in the collection.");
            Logger.getLogger(MongoHandler.class.getName()).log(Level.SEVERE, null, e);
            return null;
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
            return true;
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
