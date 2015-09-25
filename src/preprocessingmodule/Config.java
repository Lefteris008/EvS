package preprocessingmodule;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2015.09.24_1751_wave1
 */
public class Config {
    private static String consumerKey;
    private static String consumerSecret;
    private static String accessToken;
    private static String accessTokenSecret;
    public final static String searchTermsFile = "search_terms.txt";
    
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
            inputStream.close();
            
        } catch (Exception e) {
            if(inputStream != null) {
                inputStream.close();
            }
            System.out.println("Exception: " + e);
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
}
