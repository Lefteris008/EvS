/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testtwitterlefteris;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

/**
 *
 * @author Lefteris
 */
public class Config {
    private static String consumerKey;
    private static String consumerSecret;
    private static String accessToken;
    private static String accessTokenSecret;
    
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
    
    public String getConsumerKey() {
        return consumerKey;
    }
    
    public String getConsumerSecret() {
        return consumerSecret;
    }
    
    public String getAccessToken() {
        return accessToken;
    }
    
    public String getAccessTokenSecret() {
        return accessTokenSecret;
    }
}
