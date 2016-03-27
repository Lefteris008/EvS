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
 * @version 2016.03.27_2328
 */
public class MongoHandler {
    
    private static MongoClient client;
    private static MongoDatabase db;
    private Config config;
    private String langFilter = "no_filter";
    
    /**
     * Constructor, creates a connection with the MongoDB instance.
     * @param config A configuration object
     */
    public MongoHandler(Config config) {
        try {
            this.config = config;
            client = new MongoClient(this.config.getServerName(), this.config.getServerPort());
            
            //********************
            langFilter = "en";
            //********************
            
        } catch (MongoClientException e) {
            Utilities.printMessageln("Error connecting to client");
            client = null;
            Logger.getLogger(MongoHandler.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
    /**
     * Creates a connection to the MongoDB database.
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
     * Creates an index on the 'id' field of the 'Tweet' collection.
     * @return True if the process succeeds, false otherwise.
     */
    public final boolean createIndexOnId() {
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
     * Closes an open connection with the MongoDB client.
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
     * Applies a filter on the language of the tweets that are going to be
     * retrieved from MongoDB.
     * @param filter A String representing the language for which the user
     * wishes to retrieve tweets.
     */
    public final void applyLangFilter(String filter) {
        this.langFilter = filter;
    }
    
    /**
     * Stores a tweet retrieved previously from a 3-party source (e.g. a text file).
     * @param tweet An ArrayList containing the appropriate information for the 
     * tweet. The list must be created according to the following scheme:<br/>
     * [0] -> Tweet ID<br/>
     * [1] -> User (For retweets this is the original user created the tweet)<br/>
     * [2] -> 1 if the tweet is a retweet, 0 otherwise<br/>
     * [3] -> Text of the tweet<br/>
     * [4] -> Date and time of the original tweet in YYYY-MM-DD HH:MM:SS.ZZZZ fashion<br/>
     * [5] -> Number of retweets<br/>
     * [6] -> Number of favorites<br/>
     * [7] -> Latitude (if available, -1 otherwise)<br/>
     * [8] -> Longitude (if available, -1 otherwise)<br/>
     * @param config A configuration object.
     * @return True if the process succeeds, false otherwise.
     */
    public final boolean insertTweetIntoMongo(ArrayList<String> tweet) {
        try {
            if("0".equals(tweet.get(2))) {
                db.getCollection(config.getRawTweetsCollectionName()).insertOne(new Document()
                        .append("id", Long.parseLong(tweet.get(0))) //Tweet ID
                        .append("user", new Document() //Embedded document
                            .append("name", tweet.get(1))
                        )
                        .append("text", Utilities.assembleText(tweet.get(2), tweet.get(3))) //Actual tweet
                        .append("created_at", Utilities.stringToDate(tweet.get(4), tweet)) //Date published/retrieved
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
                        .append("text", Utilities.assembleText(tweet.get(2), tweet.get(3))) //Actual tweet
                        .append("created_at", Utilities.stringToDate(tweet.get(4), tweet)) //Date published/retrieved
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
     * Store a single tweet retrieved previously by using the Twitter API.
     * @param status The tweet along with its additional information
     * (location, date etc).
     * @param event The ground truth event, for which the tweet is actually
     * referring to.
     * @return True if the process succeeds, false otherwise.
     */
    public final boolean insertSingleTweetIntoMongoDB(Status status, String event) {
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
    public final List<Tweet> retrieveAllTweetsFiltered() {
        List<Tweet> retrievedTweets = new ArrayList<>();
        if(langFilter.equals("no_filter")) {
            Utilities.printMessageln("No language filter was applied. Retrieving "
                    + "all tweets, unfiltered.");
        }
        
        
        MongoCollection<Document> collection = db.getCollection(
                config.getRawTweetsCollectionName());
        
        try {
            FindIterable<Document> iterable = collection.find(); //Load all documents
            iterable.forEach(new Block<Document>() {
                @Override
                public void apply(final Document tweetDoc) {
                    if(langFilter.equals("no_filter") || tweetDoc.getString("lang")
                            .equals(langFilter)) {
                        //Get tweet ID
                        long id = tweetDoc.getLong("id");
                        Document user = tweetDoc.get("user", Document.class); //Get the embedded document
                        
                        //User details
                        String username = user.getString("name"); //Name
                        long userId; //User ID
                        try {
                            userId = user.getLong("id");
                        } catch(ClassCastException e) {
                            userId = user.getInteger("id");
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
                        
                        int stanfordSentiment;
                        try {
                            stanfordSentiment = tweetDoc.getInteger(
                                    config.getStanfordSentimentName());
                        } catch(NullPointerException e) {
                            stanfordSentiment = -1;
                        }
                        
                        int naiveBayesSentiment;
                        try {
                            naiveBayesSentiment = tweetDoc.getInteger(
                                    config.getNaiveBayesSentimentName());
                        } catch(NullPointerException e) {
                            naiveBayesSentiment = -10;
                        }
                        
                        int bayesianNetSentiment;
                        try {
                            bayesianNetSentiment = tweetDoc.getInteger(
                                    config.getBayesianNetSentimentName());
                        } catch(NullPointerException e) {
                            bayesianNetSentiment = -10;
                        }
                        
                        int posEmot, negEmot;
                        try {
                            posEmot = tweetDoc.getInteger("posEmot");
                            negEmot = tweetDoc.getInteger("negEmot");
                        } catch(NullPointerException e) {
                            posEmot = 0;
                            negEmot = 0;
                        }
                        
                        Tweet tweet = new Tweet(id, username, userId, text, date, latitude, 
                                longitude, numberOfRetweets, numberOfFavorites,
                                isRetweet, isFavorited, isRetweeted, language,
                                retweetId, stanfordSentiment, posEmot, negEmot,
                                naiveBayesSentiment, bayesianNetSentiment);
                        
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
     * This method parses the MongoDB store and returns the tweet that matches
     * a given ID. <br/>
     * <i>Note: Consider creating an index on the field 'id' of the MongoDB Store.</i>
     * @param id The id of the tweet to be retrieved.
     * @return A Tweet object containing all the information found in the document
     * that matched the long 'id'.
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
            long userId;
            try {
                userId = user.getLong("id");
            } catch(ClassCastException e) {
                userId = user.getInteger("id");
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
            long retweetId;
            try {
                Document retweetStatus = tweetDoc.get("retweeted_status", Document.class);
                isRetweet = true;
                retweetId = retweetStatus.getLong("id");
            } catch(NullPointerException e) {
                isRetweet = false;
                retweetId = -1;
            }

            int stanfordSentiment;
            try {
                stanfordSentiment = tweetDoc.getInteger(
                        config.getStanfordSentimentName());
            } catch(NullPointerException e) {
                stanfordSentiment = -1;
            }

            int naiveBayesSentiment;
            try {
                naiveBayesSentiment = tweetDoc.getInteger(
                        config.getNaiveBayesSentimentName());
            } catch(NullPointerException e) {
                naiveBayesSentiment = -10;
            }

            int bayesianNetSentiment;
            try {
                bayesianNetSentiment = tweetDoc.getInteger(
                        config.getBayesianNetSentimentName());
            } catch(NullPointerException e) {
                bayesianNetSentiment = -10;
            }

            int posEmot, negEmot;
            try {
                posEmot = tweetDoc.getInteger("posEmot");
                negEmot = tweetDoc.getInteger("negEmot");
            } catch(NullPointerException e) {
                posEmot = 0;
                negEmot = 0;
            }
            Tweet tweet = new Tweet(id_, username, userId, text, date, latitude, 
                    longitude, numberOfRetweets, numberOfFavorites, isRetweet,
                    isFavorited, isRetweeted, language, retweetId, stanfordSentiment,
                    posEmot, negEmot, naiveBayesSentiment, bayesianNetSentiment);

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
     * Updates an existing tweet with its sentiment information. <br/>
     * @param id The id of the tweet to be updated
     * @param sentiment An integer representing the sentiment polarity of the tweet.
     * @param fieldName The name of the field where the sentiment will be stored.
     */
    public final void updateSentiment(long id, int sentiment, String fieldName) {
        MongoCollection<Document> collection = db.getCollection(config
                .getRawTweetsCollectionName());
        try {
            collection.updateOne(new Document("id", id),
                                 new Document("$set", 
                                         new Document(fieldName, sentiment)));
        } catch(NullPointerException e) {
            Utilities.printMessageln("There is no tweet with id '" + id + "'.");
            Logger.getLogger(MongoHandler.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
    /**
     * Updates an existing tweet with emoticon information. <br/>
     * More formally, the method adds two new attributes namely 'posEmot' and
     * 'negEmot' that indicate whether or not a given tweet has any positive or
     * negative emoticon, with zero indicating total absence and one indicating
     * at least one of each kind.
     * @param id The id of the tweet to be updated
     * @param positiveEmoticon An integer representing whether the tweet has any
     * positive emoticon in its text.
     * @param negativeEmoticon An integer representing whether the tweet has any
     * negative emoticon in its text.
     */
    public final void updateTweetWithEmoticonInfo(long id, int positiveEmoticon, int negativeEmoticon) {
        MongoCollection<Document> collection = db.getCollection(config
                .getRawTweetsCollectionName());
        try {
            collection.updateOne(new Document("id", id),
                                 new Document("$set", 
                                         new Document("posEmot", positiveEmoticon)));
            collection.updateOne(new Document("id", id),
                                 new Document("$set", 
                                         new Document("negEmot", negativeEmoticon)));
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
     * Checks whether a tweet is already annotated with its stanfordSentiment.
     * @param id The ID of the tweet to be checked for.
     * @return True if the tweet is stanfordSentiment annotated, false otherwise.
     */
    public final boolean tweetHasSentiment(long id, String fieldName) {
        MongoCollection<Document> collection = db.getCollection(config.getRawTweetsCollectionName());
        int sentiment;
        FindIterable<Document> iterable = collection.find(new Document("id", id));
        Document tweetDoc = iterable.first();
        try {
            sentiment = tweetDoc.getInteger(fieldName);
            return true;
        } catch(NullPointerException e) { //Tweet does not exist
            return false;
        }
    }
    /**
     * Method to delete a collection.
     * @param collectionName The collection to be deleted from the DB.
     * @return True if the process succeeds, false otherwise.
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
    
    /**
     * Removes all retweets that are found in the MongoDB Store for which their
     * original tweet is also stored in the DB.<br/>
     * More formally, it parses all stored tweets and checks whether a specific
     * tweet is also a retweet. When this condition is true, the method tries to
     * find whether the tweet that this retweet is originated from, also exists
     * in the store and if so, the retweet is removed from the collection.
     * <br/>
     * *WARNING:* This process is "one-way", meaning that once initiated the 
     * retweets that are going to be removed cannot be restored back.
     */
    public void removeRetweets() {
        Utilities.printMessageln("Starting process of removing...");
        List<Tweet> tweets = retrieveAllTweetsFiltered();
        if(tweets.isEmpty()) {
            Utilities.printMessageln("There are no tweets stored in the database.");
            return;
        }
        MongoCollection<Document> collection = db.getCollection(config.getRawTweetsCollectionName());
        List<Tweet> tweetsToBeRemoved = new ArrayList<>();
        
        Utilities.printMessageln("Gathering retweets...");
        
        //Find the retweets
        for(int i = 0; i < tweets.size(); i++) {
            Tweet tweet = tweets.get(i);
            
            //If the tweet is a retweet and the source of it exists, remove it
            if(tweet.isRetweet() && tweetExists(tweet.getOriginalIDOfRetweet())) {
                tweetsToBeRemoved.add(tweet);
                tweets.remove(tweet);
            }
        }
        
        Utilities.printMessageln("Removing retweets from MongoDB Store...");
        for(Tweet tweet : tweetsToBeRemoved) {
            collection.deleteMany(new Document("id", tweet.getID()));
        }
        
        Utilities.printMessageln("Retweets were successfully removed.");
        Utilities.printMessageln("Total size of removed retweets: " 
                + tweetsToBeRemoved.size());
        Utilities.printMessageln("Size of stored tweets after deletion: " + tweets.size());
    }
}
