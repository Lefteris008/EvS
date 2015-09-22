/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testtwitterlefteris;

/**
 *
 * @author Lefteris
 */
public class Config {
    private static String consumerKey;
    private static String consumerSecret;
    private static String accessToken;
    private static String accessTokenSecret;
    
    public Config() {
        ///TODO
    }
    
    public static String getConsumerKey() {
        return consumerKey;
    }
    
    public static String getConsumerSecret() {
        return consumerSecret;
    }
    
    public static String getAccessToken() {
        return accessToken;
    }
    
    public static String getAccessTokenSecret() {
        return accessTokenSecret;
    }
}
