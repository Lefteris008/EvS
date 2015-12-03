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
package preprocessingmodule.dataset;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientException;
import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bson.Document;
import preprocessingmodule.Config;
import twitter4j.Status;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2015.12.03_1723_planet2
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
     * @deprecated To be removed in future iteration -dataset already present and stored
     * @since Wave2
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
     * Retrieves all stored retrievedTweets in MongoDB Store.
     * @param config A configuration object
     * @return A List containing all retrieved retrievedTweets
     */
    public final List<Tweet> retrieveAllTweetsFromMongoDBStore(Config config) {
        
        List<Tweet> retrievedTweets = new ArrayList<>();
        MongoCollection<Document> collection = db.getCollection(config.getRawTweetsCollectionName());
        
        try {
            FindIterable<Document> iterable = collection.find(); //Load all documents
            iterable.forEach(new Block<Document>() {
                @Override
                public void apply(final Document tweetDoc) {
                    //Get tweet ID
                    long id = tweetDoc.getLong("id");
                    Document user = tweetDoc.get("user", Document.class); //Get the embedded document

                    //User details
                    String username = user.getString("name"); //Name
                    String screenName = user.getString("screen_name"); //Screen name
                    long userId; //User ID
                    try {
                        userId = user.getInteger("id");
                    } catch(ClassCastException e) { //Catch case where id was incorrectly stored as long
                        userId = user.getLong("id");
                    }
                    
                    //Tweet text, date and language
                    String text = tweetDoc.getString("text");
                    Date date = tweetDoc.getDate("created_at");
                    String language = tweetDoc.getString("lang");

                    //Coordinates
                    Document coordinates = tweetDoc.get("coordinates", Document.class);
                    double latitude = -1;
                    double longitude = -1;
                    if(coordinates != null) {
                        try {
                            List<Double> coords = coordinates.get("coordinates", ArrayList.class);
                            latitude = coords.get(0);
                            longitude = coords.get(1);
                        } catch(ClassCastException e) { //Case where data where incorrectly casted as integers
                            List<Integer> coords = coordinates.get("coordinates", ArrayList.class);
                            latitude = coords.get(0);
                            longitude = coords.get(1);
                        }
                    }

                    //Number of retweets and favorites
                    int numberOfRetweets = tweetDoc.getInteger("retweet_count");
                    int numberOfFavorites = tweetDoc.getInteger("favorite_count");
                    boolean isFavorited = tweetDoc.getBoolean("favorited");
                    boolean isRetweeted = tweetDoc.getBoolean("retweeted");

                    //Retweet status
                    boolean isRetweet;
                    try {
                        tweetDoc.get("retweeted_status", Document.class);
                        isRetweet = true;
                    } catch(NullPointerException e) {
                        isRetweet = false;
                    }

                    Tweet tweet = new Tweet(id, username, screenName, userId, text, date, latitude, 
                            longitude, numberOfRetweets, numberOfFavorites, isRetweet, isFavorited, 
                            isRetweeted, language);

                    retrievedTweets.add(tweet);
                }
            });
            return retrievedTweets;
        } catch(MongoException e) {
            System.out.println("Cannot find documents in MongoDB Store.");
            Logger.getLogger(MongoHandler.class.getName()).log(Level.SEVERE, null, e);
            return null;
        } 
    }
    
    /**
     * This method parses the MongoDB store and returns the tweet that matches the String 'id'.
     * @param config A configuration object
     * @param id The id of the document to be retrieved
     * @return A Tweet object containing all the information found in the document that matched the String 'id'
     * @deprecated Subsumed by retrieveAllTweetsFromMongoDBStore() method.
     */
    public final Tweet getATweetByIdFromMongoDBStore(Config config, long id) {
        MongoCollection<Document> collection = db.getCollection(config.getRawTweetsCollectionName());
        try {
            FindIterable<Document> iterable = collection.find(new Document("id", id));
            
            Document tweetDoc = iterable.first();
            
            //Get tweet ID
            long id_ = tweetDoc.getLong("id");
            Document user = tweetDoc.get("user", Document.class); //Get the embedded document
            
            //User details
            String username = user.getString("name"); //Name
            String screenName = user.getString("screen_name"); //Screen name
            long userId; //User ID
            try {
                userId = user.getInteger("id");
            } catch(ClassCastException e) { //Catch case where id was incorrectly stored as long
                userId = user.getLong("id");
            }
            
            //Tweet text, date and language
            String text = tweetDoc.getString("text");
            Date date = tweetDoc.getDate("created_at");
            String language = tweetDoc.getString("lang");
            
            //Coordinates
            Document coordinates = tweetDoc.get("coordinates", Document.class);
            double latitude = -1;
            double longitude = -1;
            if(coordinates != null) {
                try {
                    List<Double> coords = coordinates.get("coordinates", ArrayList.class);
                    latitude = coords.get(0);
                    longitude = coords.get(1);
                } catch(ClassCastException e) { //Case where data where incorrectly casted as integers
                    List<Integer> coords = coordinates.get("coordinates", ArrayList.class);
                    latitude = coords.get(0);
                    longitude = coords.get(1);
                }
            }
            
            //Number of retweets and favorites
            int numberOfRetweets = tweetDoc.getInteger("retweet_count");
            int numberOfFavorites = tweetDoc.getInteger("favorite_count");
            boolean isFavorited = tweetDoc.getBoolean("favorited");
            boolean isRetweeted = tweetDoc.getBoolean("retweeted");
            
            //Retweet status
            boolean isRetweet;
            try {
                tweetDoc.get("retweeted_status", Document.class);
                isRetweet = true;
            } catch(NullPointerException e) {
                isRetweet = false;
            }

            Tweet tweet = new Tweet(id_, username, screenName, userId, text, date, latitude, 
                    longitude, numberOfRetweets, numberOfFavorites, isRetweet, isFavorited, 
                    isRetweeted, language);

            return tweet;
        } catch(MongoException e) {
            System.out.println("Unknown Mongo problem with tweet '" + id + "'.");
            Logger.getLogger(MongoHandler.class.getName()).log(Level.SEVERE, null, e);
            return null;
        } catch(NullPointerException e) { //Tweet does not exist
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
