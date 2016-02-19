/*
 * Copyright (C) 2016 Lefteris Paraskevas
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
package dsretriever;

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
import utilities.Config;
import utilities.Utilities;
import twitter4j.Status;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2016.02.19_1657
 */
public class MongoHandler {
    
    private static MongoClient client;
    private static MongoDatabase db;
    private Config config;
    
    /**
     * Constructor, creates a connection with the MongoDB instance
     * @param config A configuration object
     */
    public MongoHandler(Config config) {
        try {
            this.config = config;
            client = new MongoClient(this.config.getServerName(), this.config.getServerPort());
        } catch (MongoClientException e) {
            Utilities.printMessageln("Error connecting to client");
            client = null;
            Logger.getLogger(MongoHandler.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
    /**
     * Create a connection to the MongoDB database
     * @return True if the process succeeds, false otherwise.
     */
    public final boolean connectToMongoDB() {
        
        try {
            db = client.getDatabase(config.getDBName());
            Utilities.printMessageln("Successfully connected to '" + db.getName() + "' database.");
            return true;
        } catch (Exception e) {
            Utilities.printMessageln("There was a problem connecting to MongoDB client.");
            Logger.getLogger(MongoHandler.class.getName()).log(Level.SEVERE, null, e);
            return false;
        }
    }
    
    /**
     * Create a connection to the secondary MongoDB.
     * @return True if the process succeeds, false otherwise.
     */
    public final boolean connectToSecondaryDB() {
         try {
            db = client.getDatabase(config.getSecondaryDBName());
            Utilities.printMessageln("Successfully connected to '" + db.getName() + "' database.");
            return true;
        } catch (Exception e) {
            Utilities.printMessageln("There was a problem connecting to MongoDB client.");
            Logger.getLogger(MongoHandler.class.getName()).log(Level.SEVERE, null, e);
            return false;
        }
    }
    
    /**
     * Creates an index on the 'id' field of the tweet collection
     * @return True if the process succeeds, false otherwise.
     */
    public final boolean createIndex() {
        try {
            db.getCollection(config.getRawTweetsCollectionName())
                    .createIndex(new Document("id", 1));
            return true;
        } catch(MongoException e) {
            Utilities.printMessageln("Cannot create index for collection '" + config.getRawTweetsCollectionName() + "'");
            Logger.getLogger(MongoHandler.class.getName()).log(Level.SEVERE, null, e);
            return false;
        }
    }
    
    /**
     * Closes an open connection with the MongoDB client
     * @return True if the process succeeds, false otherwise.
     */
    public final boolean closeMongoConnection() {
        
        try {
            client.close();
            Utilities.printMessageln("Database '" + db.getName() + "' closed.");
            return true;
        } catch (Exception e) {
            Utilities.printMessageln("There was a problem while closing the database.");
            Logger.getLogger(MongoHandler.class.getName()).log(Level.SEVERE, null, e);
            return false;
        }
    }
    
    /**
     * Store a tweet retrieved previously from a 3-party source (e.g. a text file).
     * @param tweet An ArrayList containing the appropriate information for the tweet. The list must
     * be created according to the following scheme:<br/>
     * [0] -> Tweet ID<br/>
     * [1] -> User (For retweets this is the original user created the tweet)<br/>
     * [2] -> 1 if the tweet is a retweet, 0 otherwise<br/>
     * [3] -> Text of the tweet<br/>
     * [4] -> Date and time of the original tweet<br/>
     * [5] -> Number of retweets<br/>
     * [6] -> Number of favorites<br/>
     * [7] -> Latitude (if available, -1 otherwise)<br/>
     * [8] -> Longitude (if available, -1 otherwise)<br/>
     * @param config A configuration object
     * @return True if the process succeeds, false otherwise
     */
    public final boolean insertTweetIntoMongo(ArrayList<String> tweet) {
        try {
            if("0".equals(tweet.get(2))) {
                db.getCollection(config.getRawTweetsCollectionName()).insertOne(new Document()
                        .append("id", Long.parseLong(tweet.get(0))) //Tweet ID
                        .append("user", new Document() //Embedded document
                            .append("name", tweet.get(1))
                        )
                        .append("text", Utils.assembleText(tweet.get(2), tweet.get(3))) //Actual tweet
                        .append("created_at", Utils.stringToDate(tweet.get(4), tweet)) //Date published/retrieved
                        .append("retweeted", (Integer.parseInt(tweet.get(5)) > 0)) //Boolean
                        .append("retweet_count", Integer.parseInt(tweet.get(5)))
                        .append("favorited", (Integer.parseInt(tweet.get(6)) > 0)) //Boolean
                        .append("favorite_count", Integer.parseInt(tweet.get(6)))
                        .append("lang", "en") //Language of text
                        .append("coordinates", null)
//                        .append("coordinates", new Document() //Embedded document
//                            .append("coordinates", asList( //Embeded array
//                                tweet.get(7), //Latitude
//                                tweet.get(8)) //Longitude
//                            )   
//                        )
                );
            } else {
                db.getCollection(config.getRawTweetsCollectionName()).insertOne(new Document()
                        .append("id", Long.parseLong(tweet.get(0))) //Tweet ID
                        .append("user", new Document() //Embedded document
                            .append("name", tweet.get(1))
                        )
                        .append("retweeted_status", new Document())
                        .append("text", Utils.assembleText(tweet.get(2), tweet.get(3))) //Actual tweet
                        .append("created_at", Utils.stringToDate(tweet.get(4), tweet)) //Date published/retrieved
                        .append("retweeted", (Integer.parseInt(tweet.get(5)) > 0)) //Boolean
                        .append("retweet_count", Integer.parseInt(tweet.get(5)))
                        .append("favorited", (Integer.parseInt(tweet.get(6)) > 0)) //Boolean
                        .append("favorite_count", Integer.parseInt(tweet.get(6)))
                        .append("lang", "en") //Language of text
                        .append("coordinates", null)
//                        .append("coordinates", new Document() //Embedded document
//                            .append("coordinates", asList( //Embeded array
//                                Long.parseLong(tweet.get(7)), //Latitude
//                                Long.parseLong(tweet.get(8))) //Longitude
//                            )   
//                        )
                );
            }
            return true;
        } catch(MongoException e) {
            Utilities.printMessageln("There was a problem inserting the tweet.");
            Logger.getLogger(MongoHandler.class.getName()).log(Level.SEVERE, null, e);
            return false;
        }
    }
    
    /**
     * Store a tweet retrieved previously by using the Twitter API.
     * @param status The tweet along with its additional information (location, date etc)
     * @param event The ground truth event, for which the tweet is actually referring to
     * @return True if the process succeeds, false otherwise
     * @deprecated Since Wave3
     * @see insertTweetToMongo()
     */
    public final boolean insertTweetIntoMongoDB(Status status, String event) {
        try {
            db.getCollection(config.getRawTweetsCollectionName()).insertOne(new Document()
                    .append("id", status.getId()) //Tweet ID
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
            );
        return true;
        } catch(MongoException e) {
            Utilities.printMessageln("There was a problem inserting the tweet.");
            Logger.getLogger(MongoHandler.class.getName()).log(Level.SEVERE, null, e);
            return false;
        }
    }
    
    /**
     * Retrieves all stored retrievedTweets in MongoDB Store.
     * @return A List containing all retrieved retrievedTweets
     */
    public final List<Tweet> retrieveAllTweetsFromMongoDBStore() {
        
        List<Tweet> retrievedTweets = new ArrayList<>();
        MongoCollection<Document> collection = db.getCollection(config.getRawTweetsCollectionName());
        
        try {
            FindIterable<Document> iterable = collection.find(); //Load all documents
            iterable.forEach(new Block<Document>() {
                @Override
                public void apply(final Document tweetDoc) {
                    if(tweetDoc.getString("lang").equals("en")) {
                        //Get tweet ID
                        long id = tweetDoc.getLong("id");
                        Document user = tweetDoc.get("user", Document.class); //Get the embedded document
                        
                        //User details
                        String username = user.getString("name"); //Name

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
                        long retweetId;

                        //Retweet status
                        boolean isRetweet;
                        try {
                            Document retweetStatus = tweetDoc.get("retweeted_status", 
                                    Document.class);
                            isRetweet = true;
                            try {
                                retweetId = retweetStatus.getLong("id");
                            } catch(ClassCastException e) {
                                retweetId = retweetStatus.getInteger("id");
                            }
                        } catch(NullPointerException e) {
                            isRetweet = false;
                            retweetId = -1;
                        }

                        Tweet tweet = new Tweet(id, username, text, date, latitude, 
                                longitude, numberOfRetweets, numberOfFavorites, isRetweet, isFavorited, 
                                isRetweeted, language, retweetId, 0);

                        retrievedTweets.add(tweet);
                    }
                }
            });
            return retrievedTweets;
        } catch(MongoException e) {
            Utilities.printMessageln("Cannot find documents in MongoDB Store.");
            Logger.getLogger(MongoHandler.class.getName()).log(Level.SEVERE, null, e);
            return null;
        } 
    }
    
    /**
     * This method parses the MongoDB store and returns the tweet that matches the String 'id'.
     * @param id The id of the document to be retrieved
     * @return A Tweet object containing all the information found in the document that matched the String 'id'
     * @deprecated Subsumed by retrieveAllTweetsFromMongoDBStore() method.
     */
    public final Tweet getATweetByIdFromMongoDBStore(long id) {
        MongoCollection<Document> collection = db.getCollection(config.getRawTweetsCollectionName());
        try {
            FindIterable<Document> iterable = collection.find(new Document("id", id));
            
            Document tweetDoc = iterable.first();
            
            //Get tweet ID
            long id_ = tweetDoc.getLong("id");
            Document user = tweetDoc.get("user", Document.class); //Get the embedded document
            
            //User details
            String username = user.getString("name"); //Name
            
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
            long retweetId;
            try {
                Document retweetStatus = tweetDoc.get("retweeted_status", Document.class);
                isRetweet = true;
                retweetId = retweetStatus.getLong("id");
            } catch(NullPointerException e) {
                isRetweet = false;
                retweetId = -1;
            }

            Tweet tweet = new Tweet(id_, username, text, date, latitude, 
                    longitude, numberOfRetweets, numberOfFavorites, isRetweet, isFavorited, 
                    isRetweeted, language, retweetId, 0);

            return tweet;
        } catch(MongoException e) {
            Utilities.printMessageln("Unknown Mongo problem with tweet '" + id + "'.");
            Logger.getLogger(MongoHandler.class.getName()).log(Level.SEVERE, null, e);
            return null;
        } catch(NullPointerException e) { //Tweet does not exist
            return null;
        }
    }
    
    /**
     * Updates an existing tweet with the sentiment information.
     * @param id The id of the tweet to be updated
     * @param sentiment An integer representing the sentiment polarity of the tweet.
     */
    public final void updateTweetWithSentiment(long id, int sentiment) {
        MongoCollection<Document> collection = db.getCollection(config
                .getRawTweetsCollectionName());
        try {
            collection.updateOne(new Document("id", id),
                                 new Document("$set", 
                                         new Document("sentiment", sentiment)));
        } catch(NullPointerException e) {
            Utilities.printMessageln("There is no tweet with id '" + id + "'.");
            Logger.getLogger(MongoHandler.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
    /**
     * Determines whether a tweet exists in the MongoDB Store.
     * @param id The id of the tweet to be searched.
     * @return True if the tweet exists, false otherwise.
     */
    public final boolean tweetExists(long id) {
        MongoCollection<Document> collection = db.getCollection(config.getRawTweetsCollectionName());
        try {
            collection.find(new Document("id", id));
            return true;
        } catch(NullPointerException e) { //Tweet does not exist
            return false;
        }
    }
    
    /**
     * Checks whether a tweet is already annotated with its sentiment.
     * @param id The ID of the tweet to be checked for.
     * @return True if the tweet is sentiment annotated, false otherwise.
     */
    public final boolean tweetHasSentiment(long id) {
        MongoCollection<Document> collection = db.getCollection(config.getRawTweetsCollectionName());
        int sentiment;
        FindIterable<Document> iterable = collection.find(new Document("id", id));
        Document tweetDoc = iterable.first();
        try {
            sentiment = tweetDoc.getInteger("sentiment");
            return true;
        } catch(NullPointerException e) { //Tweet does not exist
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
            return true;
        } catch (MongoException e) {
            Utilities.printMessageln("There was a problem deleting collection '" + collectionName + "'");
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
            Utilities.printMessageln("There was a problem deleting database '" + db.getName() + "'");
            Logger.getLogger(MongoHandler.class.getName()).log(Level.SEVERE, null, e);
            return false;
        }
    }
}
