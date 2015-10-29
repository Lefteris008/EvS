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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2015.10.03_1844_planet1
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
    private static int maximumTweetsRetrieved;
    
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

            //Get the property values and store them
            consumerKey = prop.getProperty("ConsumerKey");
            consumerSecret = prop.getProperty("ConsumerSecret");
            accessToken = prop.getProperty("AccessToken");
            accessTokenSecret = prop.getProperty("AccessTokenSecret");
            searchTermsFile = prop.getProperty("SearchTermsFile");
            serverName = prop.getProperty("ServerName");
            serverPort = Integer.parseInt(prop.getProperty("ServerPort"));
            dbName = prop.getProperty("DBName");
            rawTweetsCollectionName = prop.getProperty("RawTweetsCollection");
            maximumTweetsRetrieved = Integer.parseInt(prop.getProperty("MaximumTweetsRetrieved"));
            
            inputStream.close();
            
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
    public String getConsumerKey() {
        return consumerKey;
    }
    
    /**
     * Returns the Consumer Secret Key to the application
     * @return A string consisting of 50 characters
     */
    public String getConsumerSecret() {
        return consumerSecret;
    }
    
    /**
     * Returns the Access Token to the application
     * @return A string consisting of 50 characters
     */
    public String getAccessToken() {
        return accessToken;
    }
    
    /**
     * Returns the Secret Access Token to the application
     * @return A string consisting of 45 characters
     */
    public String getAccessTokenSecret() {
        return accessTokenSecret;
    }
    
    /**
     * Returns a String with the search terms file name. You have to have a relevant txt file in the classpath.
     * @return The filename of the search terms file, usually in the form 'search_terms.txt'
     */
    public String getSearchTermsFile() {
        return searchTermsFile;
    }
    
    /**
     * Returns the name of the server the MongoDB instance is running.
     * @return A string containing the name of the server, usually 'localhost'
     */
    public String getServerName() {
        return serverName;
    }
    
    /**
     * Returns the port of the server the MongoDB instance is running into.
     * @return An integer containing the server port number, usually 27017
     */
    public int getServerPort() {
        return serverPort;
    }
    
    /**
     * Returns the name of the MongoDB database raw tweets are going to be stored to.
     * @return A string containing the name of the MongoDB database name
     */
    public String getDBName() {
        return dbName;
    }
    
    /**
     * Returns the name of the collection of the raw tweets.
     * @return A string containing the name of the collection
     */
    public String getRawTweetsCollectionName() {
        return rawTweetsCollectionName;
    }
    
    /**
     * Returns the number of the tweets that the streamer is going to retrieve before it will be shut down.
     * @return An integer containing the number of the tweets that are going to be retrieved
     */
    public int getMaximumTweetsNumber() {
        return maximumTweetsRetrieved;
    }
}
