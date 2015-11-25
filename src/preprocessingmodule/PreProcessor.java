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

import com.mongodb.MongoException;
import edmodule.EDMethodPicker;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import preprocessingmodule.nlp.Tokenizer;
import preprocessingmodule.language.LangUtils;
import preprocessingmodule.nlp.stopwords.StopWords;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2015.11.25_2355_planet2
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
        
        String[] keywords;
        int choice;
        Config config = new Config(); //Create the configuration object
        
        System.out.println("Pick one");
        System.out.println("1. Collect dataset by Streaming API (Collect real-time data)");
        System.out.println("2. Collect dataset by ID (Collect historical data)");
        System.out.println("3. Get a specific tweet from MongoDB store");
        System.out.println("4. Retrieve all tweets from MongoDB Store");
        System.out.println("5. Apply Event Detection");
        System.out.print("Your choice: ");
        
        Scanner keyboard = new Scanner(System.in);
        choice = keyboard.nextInt();
        
        MongoHandler mongoDB = new MongoHandler(config);
        
        try {
            mongoDB.connectToMongoDB(config); //Get MongoDB connection

            switch(choice) {
                case 1: {
                    System.out.println("Streaming API");
                    System.out.println("Collect real-time data and store them in MongoDB.");
                    keywords = Utils.extractTermsFromFile(config); //Get the keywords
                    retrieveByStreamingAPI(config, mongoDB, keywords); //Retrieve tweets
                    break;
                } case 2: {
                    System.out.println("Direct call by ID");
                    System.out.println("Collect historical data and store them in MongoDB.");
                    retrieveByID(config, mongoDB); //Retrieve tweets
                    break;
                } case 3: {
                    System.out.print("Type in the ID you want to search for: ");
                    long id = keyboard.nextLong();
                    System.out.println("Test search for tweet with ID: '"+ id + "'");
                    Tweet tweet = mongoDB.getATweetByIdFromMongoDBStore(config, id);
                    tweet.printTweetData();
                    
                    StopWords sw = new StopWords(config);
                    sw.loadStopWords(LangUtils.getLanguageISOCodeFromString(tweet.getLanguage())); //Load stopwords
                    Tokenizer tk = new Tokenizer(tweet.getText(), sw);
                    tk.textTokenizingTester();
                    break;
                } case 4: {
                    System.out.println("Retrieve all tweets from MongoDB Store");
                    List<Tweet> tweet = mongoDB.retrieveAllTweetsFromMongoDBStore(config);
                    System.out.println("Database '" + config.getDBName() + "' contains " + tweet.size() + " tweets.");
                } case 5: {
                    EDMethodPicker picker = new EDMethodPicker(config);
                } default : {
                    System.out.println("Wrong choice. Exiting now...");
                }
            }
        } catch(MongoException e) {
            System.out.println("Error: Can't establish a connection with MongoDB");
            Logger.getLogger(PreProcessor.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            mongoDB.closeMongoConnection(config); //Close DB
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
