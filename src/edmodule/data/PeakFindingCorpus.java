/*
 * Copyright (C) 2016 Lefteris Paraskevas
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
package edmodule.data;

import dsretriever.Tweet;
import edmodule.peakfinding.StringDateUtils;
import edmodule.utils.StopWordsHandlers;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import utilities.Config;
import utilities.Utilities;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2016.01.25_1656_gargantua  
 */
public class PeakFindingCorpus {
    
    private final StopWordsHandlers swH;
    private List<Tweet> tweets;
    private final Config config;
    private final HashMap<String, Integer> messageDistribution = new HashMap<>();
    private final HashMap<String, ArrayList<Tweet>> tweetsByWindow = new HashMap<>();
    private Date earliestDate;
    private Date latestDate;
    
    /**
     * Public constructor.
     * @param config A Config object.
     * @param tweets A List containing all relevant tweets for the analysis.
     * @param swH A StopWordsHandlers object.
     */
    public PeakFindingCorpus(Config config, List<Tweet> tweets, StopWordsHandlers swH) {
        this.config = config;
        this.swH = swH;
        this.tweets = tweets;
        removeDublicateTweets();
    }
    
    /**
     * Method to quickly remove duplicate tweets.
     * More formally, this method hashes the tweets by its text and stores
     * the first one to show up. Tweets that have the exact same text, were
     * ignored. Finally, the new list replaces the 'tweets' collection.
     */
    private void removeDublicateTweets() {
        Utilities.printMessageln("Removing dublicates...");
        long startTime = System.currentTimeMillis();
        
        HashMap<Integer, Tweet> uniqueTweetsMap = new HashMap<>();
        tweets.stream().filter((tweet) -> (!uniqueTweetsMap.containsKey(
                tweet.getText().hashCode()))).forEach((tweet) -> {
            uniqueTweetsMap.put(tweet.getText().hashCode(), tweet);
        });
        List<Tweet> cleanedTweets = new ArrayList<>(uniqueTweetsMap.values());
        
        long endTime = System.currentTimeMillis();
        Utilities.printExecutionTime(startTime, endTime, PeakFindingCorpus.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName());
        Utilities.printMessageln("Original size of tweets: " + tweets.size());
        Utilities.printMessageln("New size of tweets: " + cleanedTweets.size());
        Utilities.printMessageln("Removed " + (tweets.size() - cleanedTweets.size()) + " duplicates.");
        tweets = new ArrayList<>(cleanedTweets);
    }
    
    /**
     * Method to create and return the windows needed for OfflinePeakFinding 
     * algorithm to operate.
     * More formally, it creates HashMap of Strings and Integers. Every key is
     * a specific time window and its value is the corresponding summary of tweets
     * in this time interval (window). Note that only non-zero windows are created.
     * Due to the fact that a HashMap does not store its values ordered, additional
     * configuration is required to generate all the time intervals between the
     * earliest and the latest date of corpus if needed (assuming that the corpus
     * has some extend of sparseness).
     * @param window An integer indicating the time interval in which the tweets
     * should be counted. All values in minutes. <br/>
     * E.g. For 1 minute interval --> 1. <br/>
     * For half an hour interval --> 30. <br/>
     * For 5 hours interval --> 300.
     * @return A HashMap containing the bins.
     * @see BinsCreator BinsCreator class.
     * @see OfflinePeakFinding OfflinePeakFinding class.
     */
    public final HashMap<String, Integer> createCorpus(int window) {   
        //Initialize variables
        earliestDate = tweets.get(0).getDate();
        latestDate = tweets.get(0).getDate();
        Calendar cal = Calendar.getInstance();
        
        tweets.stream().forEach((tweet) -> {
            Date tweetDate = tweet.getDate();
            if(tweetDate.before(earliestDate)) {
                earliestDate = tweetDate;
            }
            if(tweetDate.after(latestDate)) {
                latestDate = tweetDate;
            }

            //Assemble the date key
            String key = StringDateUtils.getDateKey(cal, tweetDate, window);

            if(messageDistribution.containsKey(key)) {
                messageDistribution.put(key, messageDistribution.get(key) + 1);
                ArrayList<Tweet> tweetsInWindow = new ArrayList<>(tweetsByWindow.get(key));
                tweetsInWindow.add(tweet);
                tweetsByWindow.put(key, tweetsInWindow);
            } else {
                messageDistribution.put(key, 1);
                ArrayList<Tweet> tweetsInWindow = new ArrayList<>();
                tweetsInWindow.add(tweet);
                tweetsByWindow.put(key, tweetsInWindow);
            }
        });
        return messageDistribution;    
    }
    
    /**
     * Returns the earliest date a tweet was published in the corpus.
     * @return A Date object.
     */
    public final Date getEarliestDateOfCorpus() { return earliestDate; }
    
    /**
     * Returns the latest date a tweet was published in the corpus.
     * @return A Date object.
     */
    public final Date getLatestDateOfCorpus() { return latestDate; }
    
    /**
     * Returns the tweet counts in every refresh window.
     * Only non-zero windows are returned.
     * @return A HashMap which key is the refresh window and its value is the 
     * tweet count in this window.
     */
    public final HashMap<String, ArrayList<Tweet>> getTweetsByWindow() { return tweetsByWindow; }
    
    /**
     * Return the configuration object, already stored in the Constructor.
     * @return A Configuration object.
     */
    public final Config getConfigHandler() { return config; }
    
    /**
     * Return the StopWordsHandlers object, already stored in the Constructor.
     * @return A StopWordsHandlers object.
     */
    public final StopWordsHandlers getStopWordsHandlers() { return swH; }
}
