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

import utilities.Config;
import dsretriever.TweetsRetriever;
import dsretriever.Tweet;
import dsretriever.MongoHandler;
import dsretriever.Utils;
import com.mongodb.MongoException;
import edmodule.EDMethodPicker;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import preprocessingmodule.nlp.Tokenizer;
import preprocessingmodule.language.LangUtils;
import preprocessingmodule.nlp.stopwords.StopWords;
import samodule.SentimentAnalyzer;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2015.12.16_2101_planet3
 */
public class PreProcessor {
    
    public final static boolean showMongoLogging = false;
    
    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public final static void main(String[] args) throws IOException {
        
        if(!showMongoLogging) {
            
            //Stop reporting logging information
            Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
            mongoLogger.setLevel(Level.SEVERE); 
        }

        int choice;
        Config config = new Config(); //Create the configuration object
        
        System.out.println("Select one of the following options");
        System.out.println("1.\tTest a tweet");
        System.out.println("\tGet a specific tweet from MongoDB Store and test the preprocessing procedure.\n");
        System.out.println("2.\tApply Event Detection");
        System.out.println("\tApply the EDCoW algorithm.\n");
        System.out.println("3.\tApply Event Detection combining Sentiment Analysis");
        System.out.println("\tMain option of project.\n");
        System.out.print("Your choice: ");
        
        Scanner keyboard = new Scanner(System.in);
        choice = keyboard.nextInt();
        
        switch(choice) {
            case 1: {
                MongoHandler mongoDB = new MongoHandler(config);
                try {
                    //Establish the connection
                    mongoDB.connectToMongoDB(config);

                    System.out.print("Type in the ID you want to search for: ");
                    long id = keyboard.nextLong();

                    //Get tweet and print its data
                    System.out.println("Test search for tweet with ID: '"+ id + "'");
                    Tweet tweet = mongoDB.getATweetByIdFromMongoDBStore(config, id);
                    tweet.printTweetData();

                    //Sentiment part
                    SentimentAnalyzer.initAnalyzer(true);
                    System.out.println("Sentiment: " + SentimentAnalyzer.getSentiment(SentimentAnalyzer.getSentimentOfSentence(tweet.getText())));
                    SentimentAnalyzer.postActions(true);

                    //Preprocess part
                    StopWords sw = new StopWords(config);
                    sw.loadStopWords(LangUtils.getLanguageISOCodeFromString(tweet.getLanguage())); //Load stopwords
                    Tokenizer tk = new Tokenizer(config, tweet.getText(), sw);
                    tk.textTokenizingTester();
                    break;
                } catch(MongoException e) {
                    System.out.println("Error: Can't establish a connection with MongoDB");
                    Logger.getLogger(PreProcessor.class.getName()).log(Level.SEVERE, null, e);
                    mongoDB.closeMongoConnection(config); //Close DB
                } 
            } case 2: {
                EDMethodPicker.selectEDMethod(config);
                break;
            } case 3: {
                System.out.println("Not supported yet");
                break;
            } case 4: {
                System.out.println("Test case\n");
                MongoHandler mongoDB = new MongoHandler(config);
                mongoDB.connectToMongoDB(config);
                ArrayList<ArrayList<String>> tweets = new ArrayList<>(Utils.getTweetDataFromFile(config));
                tweets.stream().forEach((tweet) -> {
                    mongoDB.insertTweetIntoMongo(tweet, config);
                });
                mongoDB.closeMongoConnection(config);
                break;
            } default : {
                System.out.println("Wrong choice. Exiting now...");
            }
        }
    }
    
    /**
     * Method to extract the ground truth events and then retrieve historical tweets by their IDs.
     * @param config A configuration object
     * @param mongoDB A MongoDB object
     */
    public final static void retrieveByID(Config config, MongoHandler mongoDB) {
        List<String> event1List = Utils.extractTweetIDsFromFile(config, "fa_cup");
        List<String> event2List = Utils.extractTweetIDsFromFile(config, "super_tuesday");
        List<String> event3List = Utils.extractTweetIDsFromFile(config, "us_elections");

        new TweetsRetriever().retrieveTweetsById(event1List, mongoDB, config, "FA Cup");
        new TweetsRetriever().retrieveTweetsById(event2List, mongoDB, config, "Super Tuesday");
        new TweetsRetriever().retrieveTweetsById(event3List, mongoDB, config, "US Elections");
    }
    
    /**
     * Method that retrieves real-time tweets by querying the API with specific search terms.
     * @param config A configuration object
     * @param mongoDB A mongoDB object
     * @param keywords A String array with the search terms
     */
    public final static void retrieveByStreamingAPI(Config config, MongoHandler mongoDB, String[] keywords) {
        new TweetsRetriever().retrieveTweetsWithStreamingAPI(keywords, mongoDB, config); //Run the streamer
    }
}
