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
package com.left8.evs.utilities;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2016.04.29_1430
 */
public class Config {
    private static String consumerKey;
    private static String consumerSecret;
    private static String accessToken;
    private static String accessTokenSecret;
    private static String searchTermsFile;
    private static String serverName;
    private static int serverPort;
    private static String dbName;
    private static String rawTweetsCollectionName;
    private static String secondaryDBName;
    private static String edcowEventsFileName;
    private static String peakFindingEventsFileName;
    private static int maximumTweetsRetrieved;
    private static String datasetLocation;
    private static String stopwordsLocation;
    private static String tweetDataFile;
    private static String resourcesPath;
    private static String emoticonsPath;
    private static String outputPath;
    private static String edcowOutputPath;
    private static String peakFindingOutputPath;
    private static String sentimentFilesPath;
    private static String punctuationFile;
    private static String specialCharFile;
    private static String groundTruthDataFile;
    private static Pattern punctuationPattern;
    private static Pattern urlPattern;
    
    private static String positiveEmoticonsFile;
    private static String negativeEmoticonsFile;
    
    //Output file name
    private static String insideSentiment;
    private static String outsideSentiment1;
    private static String outsideSentiment2;

    //MongoDB Fields
    private static String iD;
    private static String user;
    private static String language;
    private static String username;
    private static String userId;
    private static String text;
    private static String date;
    private static String coordinates;
    private static String retweetsCount;
    private static String favoritesCount;
    private static String retweeted;
    private static String favorited;
    private static String retweetedStatus;
    private static String retweetId;
    private static String positiveEmoticon;
    private static String negativeEmoticon;
    
    //Console arguments
    private static String showMongoLogging;
    private static String showInlineInfo;
    private static String edcow;
    private static String opf;
    private static String noSentiment;

    public Config() throws IOException {
        
        InputStream inputStream = null;
        try {
            Properties prop = new Properties();
            String propFileName = "config.properties";

            inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("Property file '" + propFileName + "' not found in the classpath");
            }
            inputStream.close();
            
            //Get the property values and store them
            consumerKey = prop.getProperty("ConsumerKey");
            consumerSecret = prop.getProperty("ConsumerSecret");
            accessToken = prop.getProperty("AccessToken");
            accessTokenSecret = prop.getProperty("AccessTokenSecret");
            searchTermsFile = prop.getProperty("SearchTermsFile");
            serverName = prop.getProperty("ServerName");
            serverPort = Integer.parseInt(prop.getProperty("ServerPort"));
            dbName = prop.getProperty("DBName");
            secondaryDBName = prop.getProperty("SecondaryDBName");
            rawTweetsCollectionName = prop.getProperty("RawTweetsCollection");
            edcowEventsFileName = prop.getProperty("EDCoWEventsFile");
            peakFindingEventsFileName = prop.getProperty("PeakFindingEventsFile");
            maximumTweetsRetrieved = Integer.parseInt(prop.getProperty("MaximumTweetsRetrieved"));
            datasetLocation = prop.getProperty("DatasetLocation");
            stopwordsLocation = prop.getProperty("StopwordsLocation");
            tweetDataFile = prop.getProperty("TweetDataFile");
            resourcesPath = prop.getProperty("ResourcesPath");
            emoticonsPath = prop.getProperty("EmoticonsPath");
            outputPath = prop.getProperty("OutputPath");
            edcowOutputPath = prop.getProperty("EdcowOutputPath");
            peakFindingOutputPath = prop.getProperty("PeakFindingOutputPath");
            sentimentFilesPath = prop.getProperty("SentimentPath");
            groundTruthDataFile = prop.getProperty("GroundTruthDataFile");
            punctuationFile = prop.getProperty("PunctuationFile");
            specialCharFile = prop.getProperty("SpecialCharFile");
            punctuationPattern = Pattern.compile("[`~!@#$%^&*()_+-=:\"<>?;\',./{}|]");
            urlPattern = Pattern.compile("[hH][tT]{2}[Pp][sS]?://(\\w+(\\.\\w+?)?)+");
            
            positiveEmoticonsFile = prop.getProperty("PositiveEmoticons");
            negativeEmoticonsFile = prop.getProperty("NegativeEmoticons");
            
            insideSentiment = prop.getProperty("InsideSentiment");
            outsideSentiment1 = prop.getProperty("OutsideSentiment1");
            outsideSentiment2 = prop.getProperty("OutsideSentiment2");
            
            iD = prop.getProperty("id");
            user = prop.getProperty("user");
            language = prop.getProperty("language");
            username = prop.getProperty("username");
            userId = prop.getProperty("userId");
            text = prop.getProperty("text");
            date = prop.getProperty("date");
            coordinates = prop.getProperty("coordinates");
            retweetsCount = prop.getProperty("retweetsCount");
            favoritesCount = prop.getProperty("favoritesCount");
            favorited = prop.getProperty("favorited");
            retweeted = prop.getProperty("retweeted");
            retweetedStatus = prop.getProperty("retweetedStatus");
            retweetId = prop.getProperty("retweetId");
            positiveEmoticon = prop.getProperty("positiveEmoticon");
            negativeEmoticon = prop.getProperty("negativeEmoticon");
            
            showMongoLogging = prop.getProperty("showMongoLogging");
            showInlineInfo = prop.getProperty("showInlineInfo");
            edcow = prop.getProperty("edcow");
            opf = prop.getProperty("opf");
            noSentiment = prop.getProperty("noSentiment");
            
        } catch (IOException | NumberFormatException e) {
            if(inputStream != null) {
                inputStream.close();
            }
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    /**
     * Returns the Consumer Key to the application
     * @return A string consisting of 25 characters 
     */
    public String getConsumerKey() { return consumerKey; }
    
    /**
     * Returns the Consumer Secret Key to the application
     * @return A string consisting of 50 characters
     */
    public String getConsumerSecret() { return consumerSecret; }
    
    /**
     * Returns the Access Token to the application
     * @return A string consisting of 50 characters
     */
    public String getAccessToken() { return accessToken; }
    
    /**
     * Returns the Secret Access Token to the application
     * @return A string consisting of 45 characters
     */
    public String getAccessTokenSecret() { return accessTokenSecret; }
    
    /**
     * Returns a String with the search terms file name. You have to have a relevant txt file in the classpath.
     * @return The filename of the search terms file, usually in the form 'search_terms.txt'
     */
    public String getSearchTermsFile() { return searchTermsFile; }
    
    /**
     * Returns the filename of the positive emoticons file.
     * @return A String containing the positive emoticons filename.
     */
    public static String getPositiveEmoticonsFile() { return positiveEmoticonsFile; }

    /**
     * Returns the filename of the negative emoticons file.
     * @return A String containing the negative emoticons filename.
     */
    public static String getNegativeEmoticonsFile() { return negativeEmoticonsFile; }
    
    /**
     * Returns the name of the server the MongoDB instance is running.
     * @return A string containing the name of the server, usually 'localhost'
     */
    public String getServerName() { return serverName; }

    public static String getOutputPath() {
        return outputPath;
    }

    public static String getEdcowOutputPath() {
        return edcowOutputPath;
    }

    public static String getPeakFindingOutputPath() {
        return peakFindingOutputPath;
    }
    
    /**
     * Returns the port of the server the MongoDB instance is running into.
     * @return An integer containing the server port number, usually 27017
     */
    public int getServerPort() { return serverPort; }
    
    /**
     * Returns the name of the MongoDB database raw tweets are going to be stored to.
     * @return A string containing the name of the MongoDB database name
     */
    public String getDBName() { return dbName; }
    
    /**
     * Returns the name of the secondary DB of raw tweets.
     * @return A String containing the name of the collection.
     */
    public String getSecondaryDBName() { return secondaryDBName; }
    
    /**
     * Returns the name of the collection of the raw tweets.
     * @return A string containing the name of the collection
     */
    public String getRawTweetsCollectionName() { return rawTweetsCollectionName; }
    
    /**
     * Returns the name of the EDCoW Events file.
     * @return A String containing the name of the file.
     */
    public String getEDCoWEventFileName() { return edcowEventsFileName; }
    
    /**
     * Returns the name of the Peak Finding Events file.
     * @return A String containing the name of the file.
     */
    public String getPeakFindingEventsFileName() { return peakFindingEventsFileName; }
    
    /**
     * Returns the number of the tweets that the streamer is going to retrieve before it will be shut down.
     * @return An integer containing the number of the tweets that are going to be retrieved
     */
    public int getMaximumTweetsNumber() { return maximumTweetsRetrieved; }
    
    /**
     * Returns the directory in which the datasets are stored
     * @return A String containing the file path of the datasets' location
     */
    public String getDatasetPath() { return datasetLocation; }
    
    /**
     * Returns the directory in which the stopwords files are stored.
     * @return A String containing the path of the stopwords files location.
     */
    public String getStopwordsPath() { return stopwordsLocation; }
    
    /**
     * Returns the directory in which the emoticons file are stored.
     * @return A String containing the path of the emoticons files location.
     */
    public static String getEmoticonsPath() { return emoticonsPath; }
    
    /**
     * Returns the directory in which the sentiment files are stored.
     * @return A String containing the path of the sentiment files location.
     */
    public static String getSentimentFilesPath() { return sentimentFilesPath; }
    
    /**
     * Returns the name of file that contains tweet information.
     * @return A String containing the name of the tweet's file.
     */
    public String getTweetDataFile() { return tweetDataFile; }
    
    /**
     * Returns the path of the resources folder.
     * @return A String containing the resources path.
     */
    public String getResourcesPath() { return resourcesPath; }
    
    /**
     * Returns the punctuation text file name.
     * @return A String containing the punctuation file name.
     */
    public String getPunctuationFile() { return punctuationFile; }
    
    /**
     * Returns the special characters file name.
     * @return A String containing the special characters file name.
     */
    public String getSpecialCharFile() { return specialCharFile; }
    
    /**
     * Returns the ground data file location.
     * @return A String containing the ground truth data file name.
     */
    public String getGroundTruthDataFile() { return groundTruthDataFile; }
    
    /**
     * Returns a pre-compiled pattern for punctuation removal.
     * @return A pre-compiled pattern.
     */
    public Pattern getPunctuationPattern() { return punctuationPattern; }
    
    /**
     * Returns a pre-compiled pattern for URL matching.
     * @return A pre-compiled pattern.
     */
    public Pattern getURLPattern() { return urlPattern; }
    
    /**
     * Returns the field name of the sentiment predicted using the SST.
     * @return A String containing the field name.
     */
    public String getStanfordSentimentName() { return insideSentiment; }
    
    /**
     * Returns the field name of the sentiment predicted using the Naive Bayes
     * classifier.
     * @return A String containing the field name.
     */
    public String getNaiveBayesSentimentName() { return outsideSentiment1; }
    
    /**
     * Returns the field name of the sentiment predicted using the Bayesian Net
     * classifier.
     * @return A String containing the field name.
     */
    public String getBayesianNetSentimentName() { return outsideSentiment2; }
    
    /**
     * Returns the ID field name of a retrieved tweet.
     * @return a String containing the ID field name of the tweet.
     */
    public static String getTweetIdFieldName() { return iD; }

    /**
     * Returns the user document field name.
     * @return a String containing the user document field name.
     */
    public static String getUserDocumentFieldName() { return user;}

    /**
     * Returns the language code field name of the tweet.
     * @return a String representing the language field name of the tweet.
     */
    public static String getLanguageFieldName() { return language;}

    /**
     * Returns the display name field name of the creator of the tweet.
     * @return a String containing the display name field name of the tweet's creator.
     */
    public static String getUsernameFieldName() { return username;}

    /**
     * Returns the user ID field name of the tweet's creator.
     * @return a String containing the user ID field name of the tweet's creator.
     */
    public static String getUserIdFieldName() { return userId;}

    /**
     * Returns the text field name of the tweet.
     * @return a String containing the text field name of the tweet.
     */
    public static String getTextFieldName() { return text; }

    /**
     * Returns the date field name of the tweet.
     * @return a String containing the date field name of the tweet.
     */
    public static String getDateFieldName() { return date; }

    /**
     * Returns the coordinates document field name.
     * @return a String containing the coordinates document field name.
     */
    public static String getCoordinatesDocumentFieldName() { return coordinates; }

    /**
     * Returns the retweet count field name.
     * @return a String containing the retweet field name.
     */
    public static String getRetweetsCountFieldName() { return retweetsCount; }

    /**
     * Returns the favorite count field name.
     * @return a String containing the favorite field name.
     */
    public static String getFavoritesCountFieldName() { return favoritesCount; }

    /**
     * Returns the retweeted flag field name.
     * @return a String containing the retweeted flag field name.
     */
    public static String getRetweetedFieldName() { return retweeted; }

    /**
     * Returns the favorited flag field name.
     * @return a String containing the favorited flag field name.
     */
    public static String getFavoritedFieldName() { return favorited; }

    /**
     * Returns the retweeted status document field name.
     * @return a String containing the retweeted status document field name.
     */
    public static String getRetweetedStatusDocumentFieldName() { return retweetedStatus; }

    /**
     * Returns the retweet ID field name (the id of the original tweet).
     * @return a String containing the retweet ID filed name.
     */
    public static String getRetweetIdFieldName() { return retweetId; }

    /**
     * Returns the positive emoticon field name.
     * @return a String containing the positive emoticon field name.
     */
    public static String getPositiveEmoticonFieldName() { return positiveEmoticon; }

    /**
     * Returns the negative emoticon field name.
     * @return a String containing the negative emoticon field name.
     */
    public static String getNegativeEmoticonFieldName() { return negativeEmoticon; }
    
    /**
     * Returns the argument name of MongoLogging flag.
     * @return the argument name of MongoLogging flag.
     */
    public static String getMongoLoggingArgName() { return showMongoLogging; }
    
    /**
     * Returns the argument name of InlineInfo flag.
     * @return the argument name of InlineInfo flag.
     */
    public static String getInlineInfoArgName() { return showInlineInfo; }
    
    /**
     * Returns the argument name of EDCoW flag.
     * @return the argument name of EDCoW flag.
     */
    public static String getEdcowArgName() { return edcow; }
    
    /**
     * Returns the argument name of Offline Peak Finding flag.
     * @return the argument name of Offline Peak Finding flag.
     */
    public static String getOpfArgName() { return opf; }
    
    /**
     * Returns the argument name of NoSentiment flag.
     * @return the argument name of NoSentiment flag.
     */
    public static String getNoSentimentArgName() { return noSentiment; }
}
