/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package preprocessingmodule;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2015.09.30_1438_wave2
 */
public class Main {
    
    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     * @throws java.lang.InterruptedException
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        
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
            System.exit(1);
        }
        
        keywords = terms.toArray(new String[terms.size()]);
        
        MongoHandler mongoDB = new MongoHandler(config);
        mongoDB.getMongoConnection(config); //Get MongoDB connection

        new TweetsRetriever().retrieveTweetsWithStreamingAPI(keywords, mongoDB, config); //Run the streamer
        
        mongoDB.closeMongoConnection(config); //Close DB
    }
}
