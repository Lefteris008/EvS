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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2015.11.09_1858_planet1
 */
public class PreProcessor {
    
    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
        
        String line;
        String[] keywords;
        ArrayList<String> terms = new ArrayList<>();
        
        Config config = new Config(); //Create the configuration object
        
        try (BufferedReader br = new BufferedReader(new FileReader(config.getSearchTermsFile()))) {
            while ((line = br.readLine()) != null) {
                terms.add(line); //Open the search terms file and read them
            }
            br.close();
        } catch (IOException e) {
            System.out.println("The file '" + config.getSearchTermsFile() + "' is missing.\nPlace a correct file in classpath and re-run the project");
            Logger.getLogger(PreProcessor.class.getName()).log(Level.SEVERE, null, e);
            System.exit(1);
        }
        
        keywords = terms.toArray(new String[terms.size()]);
        
        MongoHandler mongoDB = new MongoHandler(config);
        mongoDB.getMongoConnection(config); //Get MongoDB connection

        //new TweetsRetriever().retrieveTweetsWithStreamingAPI(keywords, mongoDB, config); //Run the streamer
        
        List<String> event1List = extractTweetIDsFromFile(config, "fa_cup");
        List<String> event2List = extractTweetIDsFromFile(config, "super_tuesday");
        List<String> event3List = extractTweetIDsFromFile(config, "us_elections");

        new TweetsRetriever().retrieveTweetsById(event1List, mongoDB, config, "FA Cup");
        new TweetsRetriever().retrieveTweetsById(event2List, mongoDB, config, "Super Tuesday");
        new TweetsRetriever().retrieveTweetsById(event3List, mongoDB, config, "US Elections");
        
        mongoDB.closeMongoConnection(config); //Close DB
    }
    
    public static List<String> extractTweetIDsFromFile(Config config, String filename) {
        
        List<String> list = new ArrayList<>();
        
        String path = config.getDatasetLocation() + filename + "\\";
        
        try {
            Files.walk(Paths.get(path)).forEach(filePath -> { //For all files in the current folder
                if (Files.isRegularFile(filePath)) { //Open every single file
                    try (BufferedReader br = new BufferedReader(new FileReader(filePath.toString()))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                           list.add(line); //Store the id to the list
                        }
                    } catch(IOException e) {
                        Logger.getLogger(PreProcessor.class.getName()).log(Level.SEVERE, null, e);
                    }
                }
            });     
        } catch (IOException e) {
            Logger.getLogger(PreProcessor.class.getName()).log(Level.SEVERE, null, e);
        }
        return list;
    }
}
