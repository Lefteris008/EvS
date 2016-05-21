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
package com.left8.evs.utilities.dsretriever;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientException;
import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import twitter4j.Status;

import com.left8.evs.utilities.Config;
import com.left8.evs.utilities.Utilities;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2016.04.30_1832
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
                    .createIndex(new Document(config.getTweetIdFieldName(), 1));
            return true;
        } catch(MongoException e) {
            Utilities.printMessageln("Cannot create index for collection '" 
                    + config.getRawTweetsCollectionName() + "'");
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
     * tweet. The list must be created according to the following scheme:<br>
     * [0] -&gt; Tweet ID<br>
     * [1] -&gt; User (For retweets this is the original user created the tweet)<br>
     * [2] -&gt; 1 if the tweet is a retweet, 0 otherwise<br>
     * [3] -&gt; Text of the tweet<br>
     * [4] -&gt; Date and time of the original tweet in YYYY-MM-DD HH:MM:SS.ZZZZ fashion<br>
     * [5] -&gt; Number of retweets<br>
     * [6] -&gt; Number of favorites<br>
     * [7] -&gt; Latitude (if available, -1 otherwise)<br>
     * [8] -&gt; Longitude (if available, -1 otherwise)<br>
     * @param tweet A List containing a tweet.
     * @return True if the process succeeds, false otherwise.
     */
    public final boolean insertTweetIntoMongo(ArrayList<String> tweet) {
        try {
            if("0".equals(tweet.get(2))) {
                db.getCollection(config.getRawTweetsCollectionName()).insertOne(new Document()
                        .append(config.getTweetIdFieldName(), Long.parseLong(tweet.get(0))) //Tweet ID
                        .append(config.getUserDocumentFieldName(), new Document() //Embedded document
                            .append(config.getUsernameFieldName(), tweet.get(1))
                        )
                        .append(config.getTextFieldName(), 
                                Utilities.assembleText(tweet.get(2), tweet.get(3))) //Actual tweet
                        .append(config.getDateFieldName(), 
                                Utilities.stringToDate(tweet.get(4))) //Date published/retrieved
                        .append(config.getRetweetedFieldName(), 
                                (Integer.parseInt(tweet.get(5)) > 0)) //Boolean
                        .append(config.getRetweetsCountFieldName(), 
                                Integer.parseInt(tweet.get(5)))
                        .append(config.getFavoritedFieldName(), 
                                (Integer.parseInt(tweet.get(6)) > 0)) //Boolean
                        .append(config.getFavoritesCountFieldName(), 
                                Integer.parseInt(tweet.get(6)))
                        .append(config.getLanguageFieldName(), "en") //Language of text
                        .append(config.getCoordinatesDocumentFieldName(), null)
//                        .append(config.getCoordinatesDocumentFieldName(), 
//                        new Document() //Embedded document
//                            .append(config.getCoordinatesDocumentFieldName(), 
//                        asList( //Embeded array
//                                tweet.get(7), //Latitude
//                                tweet.get(8)) //Longitude
//                            )   
//                        )
                );
            } else {
                db.getCollection(config.getRawTweetsCollectionName()).insertOne(new Document()
                        .append(config.getTweetIdFieldName(), 
                                Long.parseLong(tweet.get(0))) //Tweet ID
                        .append(config.getUserDocumentFieldName(), 
                                new Document() //Embedded document
                            .append(config.getUsernameFieldName(), tweet.get(1))
                        )
                        .append(config.getRetweetedStatusDocumentFieldName(), new Document())
                        .append(config.getTextFieldName(), 
                                Utilities.assembleText(tweet.get(2), tweet.get(3))) //Actual tweet
                        .append(config.getDateFieldName(), 
                                Utilities.stringToDate(tweet.get(4))) //Date published/retrieved
                        .append(config.getRetweetedFieldName(), 
                                (Integer.parseInt(tweet.get(5)) > 0)) //Boolean
                        .append(config.getRetweetsCountFieldName(), 
                                Integer.parseInt(tweet.get(5)))
                        .append(config.getFavoritedFieldName(), (
                                Integer.parseInt(tweet.get(6)) > 0)) //Boolean
                        .append(config.getFavoritesCountFieldName(), 
                                Integer.parseInt(tweet.get(6)))
                        .append(config.getLanguageFieldName(), "en") //Language of text
                        .append(config.getCoordinatesDocumentFieldName(), null)
//                        .append(config.getCoordinatesDocumentFieldName(), 
//                        new Document() //Embedded document
//                            .append(config.getCoordinatesDocumentFieldName(), 
//                        asList( //Embeded array
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
     * @deprecated 
     */
    public final boolean insertSingleTweetIntoMongoDB(Status status, String event) {
        try {
//            db.getCollection(config.getRawTweetsCollectionName()).insertOne(new Document()
//                    .append(config.getTweetIdFieldName(), status.getId()) //Tweet ID
//                    .append(config.getUsernameFieldName(), status.getUser().getName()) //Username
//                    .append(config.getTextFieldName(), status.getText()) //Actual tweet
//                    .append(config.getDateFieldName(), String.valueOf(status.getCreatedAt())) //Date published
//                    //.append("latitude", status.getGeoLocation() != null ? String.valueOf(status.getGeoLocation().getLatitude()) : "NULL") //Latitude
//                    //.append("longitude", status.getGeoLocation() != null ? String.valueOf(status.getGeoLocation().getLongitude()) : "NULL") //Longitude
//                    .append(config.getRetweetsCountFieldName(), String.valueOf(status.getRetweetCount())) //Retweet count
//                    .append(config.getFavoritesCountFieldName(), String.valueOf(status.getFavoriteCount())) //Favorite count
//                    .append(c, status.isRetweet() ? "true" : "false") //True if the tweet is a retweet, false otherwise
//                    .append("is_favorited", status.getFavoriteCount() > 0 ? "true" : "false") //True if the tweet is favorited, false otherwise
//                    .append("is_retweeted", status.getRetweetCount() > 0 ? "true" : "false") //True if the tweet is retweeted, false otherwise
//                    .append("language", status.getLang()) //Language of text
//            );
        return false;
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
                    if(langFilter.equals("no_filter") || tweetDoc.getString(config.getLanguageFieldName())
                            .equals(langFilter)) {
                        //Get tweet ID
                        long id = tweetDoc.getLong(config.getTweetIdFieldName());
                        Document user = tweetDoc.get(config.getUserDocumentFieldName(), Document.class); //Get the embedded document
                        
                        //User details
                        String username = user.getString(config.getUsernameFieldName()); //Name
                        long userId; //User ID
                        try {
                            userId = user.getLong(config.getUserIdFieldName());
                        } catch(ClassCastException e) {
                            userId = user.getInteger(config.getUserIdFieldName());
                        }

                        //Tweet text, date and language
                        String text = tweetDoc.getString(config.getTextFieldName());
                        Date date = tweetDoc.getDate(config.getDateFieldName());
                        String language = tweetDoc.getString(config.getLanguageFieldName());

                        //Coordinates
                        Document coordinates = tweetDoc.get(config.getCoordinatesDocumentFieldName(), Document.class);
                        double latitude = -1;
                        double longitude = -1;
                        if(coordinates != null) {
                            try {
                                List<Double> coords = coordinates.get(config.getCoordinatesDocumentFieldName(), ArrayList.class);
                                latitude = coords.get(0);
                                longitude = coords.get(1);
                            } catch(ClassCastException e) { //Case where data where incorrectly casted as integers
                                List<Integer> coords = coordinates.get(config.getCoordinatesDocumentFieldName(), ArrayList.class);
                                latitude = coords.get(0);
                                longitude = coords.get(1);
                            }
                        }

                        //Number of retweets and favorites
                        int numberOfRetweets = tweetDoc.getInteger(config.getRetweetsCountFieldName());
                        int numberOfFavorites = tweetDoc.getInteger(config.getFavoritesCountFieldName());
                        boolean isFavorited = tweetDoc.getBoolean(config.getFavoritedFieldName());
                        boolean isRetweeted = tweetDoc.getBoolean(config.getRetweetedFieldName());
                        long retweetId;

                        //Retweet status
                        boolean isRetweet;
                        try {
                            Document retweetStatus = tweetDoc.get(config.getRetweetedStatusDocumentFieldName(), 
                                    Document.class);
                            isRetweet = true;
                            try {
                                retweetId = retweetStatus.getLong(config.getRetweetIdFieldName());
                            } catch(ClassCastException e) {
                                retweetId = retweetStatus.getInteger(config.getRetweetIdFieldName());
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
                            posEmot = tweetDoc.getInteger(config.getPositiveEmoticonFieldName());
                            negEmot = tweetDoc.getInteger(config.getNegativeEmoticonFieldName());
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
     * a given ID. <br>
     * <i>Note: Consider creating an index on the field 'id' of the MongoDB Store.</i>
     * @param id The id of the tweet to be retrieved.
     * @return A Tweet object containing all the information found in the document
     * that matched the long 'id'.
     */
    public final Tweet getATweetByIdFromMongoDBStore(long id) {
        MongoCollection<Document> collection = db.getCollection(config.getRawTweetsCollectionName());
        try {
            FindIterable<Document> iterable = collection.find(new Document(
                    config.getTweetIdFieldName(), id));
            
            Document tweetDoc = iterable.first();
            
            //Get tweet ID
            long id_ = tweetDoc.getLong(config.getTweetIdFieldName());
            Document user = tweetDoc.get(config.getUserDocumentFieldName(), 
                    Document.class); //Get the embedded document
            
            //User details
            String username = user.getString(config.getUsernameFieldName()); //Name
            long userId;
            try {
                userId = user.getLong(config.getUserIdFieldName());
            } catch(ClassCastException e) {
                userId = user.getInteger(config.getUserIdFieldName());
            }
            
            //Tweet text, date and language
            String text = tweetDoc.getString(config.getTextFieldName());
            Date date = tweetDoc.getDate(config.getDateFieldName());
            String language = tweetDoc.getString(config.getLanguageFieldName());
            
            //Coordinates
            Document coordinates = tweetDoc.get(config.getCoordinatesDocumentFieldName(),
                    Document.class);
            double latitude = -1;
            double longitude = -1;
            if(coordinates != null) {
                try {
                    List<Double> coords = coordinates.get(
                            config.getCoordinatesDocumentFieldName(), ArrayList.class);
                    latitude = coords.get(0);
                    longitude = coords.get(1);
                } catch(ClassCastException e) { //Case where data where incorrectly casted as integers
                    List<Integer> coords = coordinates.get(
                            config.getCoordinatesDocumentFieldName(), ArrayList.class);
                    latitude = coords.get(0);
                    longitude = coords.get(1);
                }
            }
            
            //Number of retweets and favorites
            int numberOfRetweets = tweetDoc.getInteger(config.getRetweetsCountFieldName());
            int numberOfFavorites = tweetDoc.getInteger(config.getFavoritesCountFieldName());
            boolean isFavorited = tweetDoc.getBoolean(config.getFavoritedFieldName());
            boolean isRetweeted = tweetDoc.getBoolean(config.getRetweetedFieldName());
            
            //Retweet status
            boolean isRetweet;
            long retweetId;
            try {
                Document retweetStatus = tweetDoc.get(
                        config.getRetweetedStatusDocumentFieldName(), Document.class);
                isRetweet = true;
                retweetId = retweetStatus.getLong(config.getRetweetIdFieldName());
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
                posEmot = tweetDoc.getInteger(config.getPositiveEmoticonFieldName());
                negEmot = tweetDoc.getInteger(config.getNegativeEmoticonFieldName());
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
     * Updates an existing tweet with its sentiment information. <br>
     * @param id The id of the tweet to be updated
     * @param sentiment An integer representing the sentiment polarity of the tweet.
     * @param fieldName The name of the field where the sentiment will be stored.
     */
    public final void updateSentiment(long id, int sentiment, String fieldName) {
        MongoCollection<Document> collection = db.getCollection(config
                .getRawTweetsCollectionName());
        try {
            collection.updateOne(new Document(config.getTweetIdFieldName(), id),
                                 new Document("$set", 
                                         new Document(fieldName, sentiment)));
        } catch(NullPointerException e) {
            Utilities.printMessageln("There is no tweet with id '" + id + "'.");
            Logger.getLogger(MongoHandler.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
    /**
     * Updates an existing tweet with emoticon information. <br>
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
            collection.updateOne(new Document(config.getTweetIdFieldName(), id),
                                 new Document("$set", 
                                         new Document(config.getPositiveEmoticonFieldName(), 
                                                 positiveEmoticon)));
            collection.updateOne(new Document(config.getTweetIdFieldName(), id),
                                 new Document("$set", 
                                         new Document(config.getNegativeEmoticonFieldName(), 
                                                 negativeEmoticon)));
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
            collection.find(new Document(config.getTweetIdFieldName(), id));
            return true;
        } catch(NullPointerException e) { //Tweet does not exist
            return false;
        }
    }
    
    /**
     * Checks whether a tweet is already annotated with its stanfordSentiment.
     * @param id The ID of the tweet to be checked for.
     * @param fieldName The MongoDB field name of ID.
     * @return True if the tweet is stanfordSentiment annotated, false otherwise.
     */
    public final boolean tweetHasSentiment(long id, String fieldName) {
        MongoCollection<Document> collection = db.getCollection(config.getRawTweetsCollectionName());
        int sentiment;
        FindIterable<Document> iterable = collection.find(new Document(
                config.getTweetIdFieldName(), id));
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
     * original tweet is also stored in the DB.<br>
     * More formally, it parses all stored tweets and checks whether a specific
     * tweet is also a retweet. When this condition is true, the method tries to
     * find whether the tweet that this retweet is originated from, also exists
     * in the store and if so, the retweet is removed from the collection.
     * <br>
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
            collection.deleteMany(new Document(config.getTweetIdFieldName(), tweet.getID()));
        }
        
        Utilities.printMessageln("Retweets were successfully removed.");
        Utilities.printMessageln("Total size of removed retweets: " 
                + tweetsToBeRemoved.size());
        Utilities.printMessageln("Size of stored tweets after deletion: " + tweets.size());
    }
}
