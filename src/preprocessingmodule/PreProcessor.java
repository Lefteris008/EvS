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
import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2015.11.11_1450_planet1
 */
public class PreProcessor {
    
    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public final static void main(String[] args) throws IOException {
        
        String[] keywords;
        int choice;
        Config config = new Config(); //Create the configuration object
        
        System.out.println("Select a method to create your dataset.");
        System.out.println("1. By Streaming API (Collect real-time data)");
        System.out.println("2. By ID (Collect historical data)");
        System.out.print("Your choice: ");
        
        Scanner keyboard = new Scanner(System.in);
        choice = keyboard.nextInt();
        
        MongoHandler mongoDB = new MongoHandler(config);
        
        try {
            mongoDB.getMongoConnection(config); //Get MongoDB connection

            switch(choice) {
                case 1: {
                    System.out.println("Streaming API");
                    System.out.println("Collect real-time data and store them in MongoDB.");
                    keywords = Utils.extractTermsFromFile(config); //Get the keywords
                    retrieveByStreamingAPI(config, mongoDB, keywords); //Retrieve tweets
                } case 2: {
                    System.out.println("Direct call by ID");
                    System.out.println("Collect historical data and store them in MongoDB.");
                    retrieveByID(config, mongoDB); //Retrieve tweets
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
     * 
     * @param config
     * @param mongoDB 
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
     * 
     * @param config
     * @param mongoDB
     * @param keywords 
     */
    public final static void retrieveByStreamingAPI(Config config, MongoHandler mongoDB, String[] keywords) {
        new TweetsRetriever().retrieveTweetsWithStreamingAPI(keywords, mongoDB, config); //Run the streamer
    }
}
