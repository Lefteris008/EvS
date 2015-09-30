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

import java.io.IOException;
import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * 
 * @author  Lefteris Paraskevas
 * @version 2015.09.30_1450_wave2
 */
public class TweetsRetriever {

    /**
     * Method to get authorization from Twitter API
     * @return A ConfigurationBuilder object
     */
    public ConfigurationBuilder getAuthorization() {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        
        Config conf;
        try {
            conf = new Config();
        } catch(IOException e) {
            return null;
        }
        
        cb.setOAuthConsumerKey(conf.getConsumerKey());
        cb.setOAuthConsumerSecret(conf.getConsumerSecret());
        cb.setOAuthAccessToken(conf.getAccessToken());
        cb.setOAuthAccessTokenSecret(conf.getAccessTokenSecret());
        
        return cb;
    }
    
    /**
     * Method that handles the Twitter streaming API. WARNING: Method does not terminate by itself, due to
     * the fact that the streamer runs in a different thread.
     * @param keywords The keywords for which the streamer searches for tweets
     * @param mongoDB A handler for the MongoDB database
     * @param config A configuration object
     */
    public final void retrieveTweetsWithStreamingAPI(String[] keywords, MongoHandler mongoDB, Config config) {
        
        ConfigurationBuilder cb = getAuthorization();  
        
        TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
        
        final StatusListener listener;
        listener = new StatusListener() {
            
            @Override
            public final void onStatus(Status status) {
                mongoDB.insertTweetToMongoDB(status, config); //Insert tweet to MongoDB
            }

            @Override
            public final void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
                System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
            }

            @Override
            public final void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
            }

            @Override
            public final void onScrubGeo(long userId, long upToStatusId) {
                System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
            }

            @Override
            public final void onStallWarning(StallWarning warning) {
                System.out.println("Got stall warning:" + warning);
            }
            
            @Override
            public final void onException(Exception ex) {
                ex.printStackTrace(System.out);
            }
        };
        
        FilterQuery fq = new FilterQuery();
        fq.language("en"); //Set language of tweets to "English"
        fq.track(keywords); //Load the search terms
                
        twitterStream.addListener(listener); //Start listening to the stream
        twitterStream.filter(fq); //Apply the search filters
    }
}
