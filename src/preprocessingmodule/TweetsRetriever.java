package preprocessingmodule;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import twitter4j.FilterQuery;
import twitter4j.GeoLocation;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * 
 * @author  Lefteris Paraskevas
 * @version 2015.09.27_0203_wave1
 */
public class TweetsRetriever {

    private final static int MAXIMUM_TWEETS = 100; //Maximum tweets retrieved until the thread will be terminated
    
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
     * @deprecated
     * Simple method to retrieve tweets by querying the API
     * @throws IOException 
     */
    void retrieveTweetsWithQuery() throws IOException {

        ConfigurationBuilder cb = getAuthorization();
        
        if(cb != null) {
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
                if (loc!=null) {
                    Double lat = t.getGeoLocation().getLatitude();
                    Double lon = t.getGeoLocation().getLongitude();
                    System.out.println(i + " USER: " + user + " wrote: " + msg + " located at " + lat + ", " + lon);
                } else 
                    System.out.println(i + " USER: " + user + " wrote: " + msg);
            }
        }
    }
    
    /**
     * Method that handles the Twitter streaming API
     * @param keywords The keywords for which the streamer searches for tweets
     * @param mongoDB
     * @return A list containing the retrieved tweets, along with their other data (user, location etc)
     * @throws InterruptedException 
     */
    public final List<Status> retrieveTweetsWithStreamingAPI(String[] keywords, MongoHandler mongoDB) throws InterruptedException {
        
        List<Status> statuses = new ArrayList<>();
        
        final Object lock = new Object();
        
        ConfigurationBuilder cb = getAuthorization();  
        
        TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
        
        final StatusListener listener;
        listener = new StatusListener() {
            
            @Override
            public final void onStatus(Status status) {
                statuses.add(status);
                
                mongoDB.insertTweetToMongoDB(status);
                
                //If the stream execution exceeds the tweets boundary
                //it is shut down and the retrieved tweets are returned
                if(statuses.size() >= MAXIMUM_TWEETS) {
                    synchronized (lock) {
                        lock.notify();
                    }
                    System.out.println("\nStreaming exceeded tweets boundary, terminating thread...\n");
                }
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
        
        try {
            synchronized (lock) {
                lock.wait(); //Wait until the limit of tweets is reached
            }
        } catch (InterruptedException e) {
            System.out.println("Thread broke synchronization, re-run project");
        } finally {
            twitterStream.cleanUp(); //Stop the stream
        }
        return statuses;
    }
    
    /**
     * Method to print tweets and statistics
     * @param statuses The retrieved tweets
     * @param startTime Time the project started (in milliseconds)
     * @param stopTime  Time the project finished (in milliseconds)
     */
    public static final void printStatistics(List<Status> statuses, long startTime, long stopTime) {
        System.out.println("Streamer run for " + (stopTime - startTime)/1000 + " seconds");
        System.out.println("Retrieved " + MAXIMUM_TWEETS + " tweets\n");
        System.out.println("Would you like to print them?");
        Scanner keyboard = new Scanner(System.in);
        if(keyboard.nextInt() == 1) {
            statuses.stream().forEach((status) -> {
                System.out.println( "@" + status.getUser().getName() + " : " + status.getText() +
                        " Date : " + status.getCreatedAt() + " Location : " + status.getGeoLocation());
            });
        }
    }
    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     * @throws java.lang.InterruptedException
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        
        String line;
        String[] keywords;
        ArrayList<String> terms = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(Config.searchTermsFile))) {
            while ((line = br.readLine()) != null) {
                terms.add(line); //Open the search terms file and read them
            }
            br.close();
        } catch (IOException e) {
            System.out.println("The file '" + Config.searchTermsFile + "' is missing.\nPlace a correct file in classpath and re-run the project");
            System.exit(1);
        }
        
        keywords = terms.toArray(new String[terms.size()]);
        
        MongoHandler mongoDB = new MongoHandler();
        mongoDB.getMongoConnection(); //Get MongoDB connection
        
        long startTime = System.currentTimeMillis();
        List<Status> statuses = new TweetsRetriever().retrieveTweetsWithStreamingAPI(keywords, mongoDB); //Run the streamer
        long stopTime = System.currentTimeMillis();
        
        mongoDB.closeMongoConnection();
        printStatistics(statuses, startTime, stopTime); //Print tweets
        
        
    }
    
}
