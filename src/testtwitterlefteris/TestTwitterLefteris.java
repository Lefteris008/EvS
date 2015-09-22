/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testtwitterlefteris;

import java.io.IOException;
import java.util.ArrayList;
import twitter4j.GeoLocation;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 *
 * @author Lefteris
 */
public class TestTwitterLefteris {

    void setup() throws IOException {

        ConfigurationBuilder cb = new ConfigurationBuilder();
        Config conf = new Config();
        cb.setOAuthConsumerKey(conf.getConsumerKey());
        cb.setOAuthConsumerSecret(conf.getConsumerSecret());
        cb.setOAuthAccessToken(conf.getAccessToken());
        cb.setOAuthAccessTokenSecret(conf.getAccessTokenSecret());

        Twitter twitter = new TwitterFactory(cb.build()).getInstance();
        Query query = new Query("#peace");
        int numberOfTweets = 512;
        long lastID = Long.MAX_VALUE;
        ArrayList<Status> tweets = new ArrayList<>();
        while (tweets.size () < numberOfTweets) {
            if (numberOfTweets - tweets.size() > 100) {
                query.setCount(100);
            } else { 
                query.setCount(numberOfTweets - tweets.size());
            }
            try {
                QueryResult result = twitter.search(query);
                tweets.addAll(result.getTweets());
                System.out.println("Gathered " + tweets.size() + " tweets");
                for (Status t: tweets) 
                    if(t.getId() < lastID) {
                        lastID = t.getId();
                    }
            } catch (TwitterException te) {
                System.out.println("Couldn't connect: " + te);
            } 
            query.setMaxId(lastID-1);
        }

        for (int i = 0; i < tweets.size(); i++) {
            Status t = (Status) tweets.get(i);

            GeoLocation loc = t.getGeoLocation();

            String user = t.getUser().getScreenName();
            String msg = t.getText();
            String time = "";
            if (loc!=null) {
                Double lat = t.getGeoLocation().getLatitude();
                Double lon = t.getGeoLocation().getLongitude();
                System.out.println(i + " USER: " + user + " wrote: " + msg + " located at " + lat + ", " + lon);
            } else 
                System.out.println(i + " USER: " + user + " wrote: " + msg);
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        new TestTwitterLefteris().setup();
    }
    
}
