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
import edmodule.utils.StopWordsHandlers;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import utilities.Config;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2016.01.07_1819_planet3  
 */
public class PeakFindingCorpus {
    
    private final StopWordsHandlers swH;
    private final List<Tweet> tweets;
    private final Config config;
    private final HashMap<String, Integer> messageDistribution = new HashMap<>();
    private final HashMap<String, ArrayList<String>> tweetsByWindow = new HashMap<>();
    private final HashMap<Integer, String> binsWithKeys = new HashMap<>();
    
    public PeakFindingCorpus(Config config, List<Tweet> tweets, StopWordsHandlers swH) {
        this.config = config;
        this.swH = swH;
        this.tweets = tweets;
    }
    
    /**
     * Method to create and return the bins needed for OfflinePeakFinding algorithm to operate.
     * More formally, it creates an ArrayList of integers, containing the count of tweets in a pre-specified
     * time interval (windows).
     * @param window An integer indicating the time interval in which the tweets should be counted.
     * All values in minutes. <br/>
     * E.g. For 1 minute interval --> 1. <br/>
     * For half an hour interval --> 30. <br/>
     * For 5 hours interval --> 300.
     * @return A HashMap containing the bins.
     */
    public final HashMap<String, Integer> createCorpus(int window) {
        int year;
        int month;
        int day;
        int hour;
        int minute;
        Calendar cal = Calendar.getInstance();
        for(Tweet tweet : tweets) {
            cal.setTime(tweet.getDate());
            year = cal.get(Calendar.YEAR); //Current year
            month = cal.get(Calendar.MONTH) + 1; //Zero-index based
            day = cal.get(Calendar.DAY_OF_MONTH); //Get the current day of month
            hour = cal.get(Calendar.HOUR_OF_DAY); //24h
            minute = cal.get(Calendar.MINUTE) / window; //Nearest window, starting from 0

            //Assemble the key in YYYYMMDD_HHMM form.
            String key = String.valueOf(year) 
                    + (month < 10 ? "0" + String.valueOf(month) : String.valueOf(month)) 
                    + (day < 10 ? "0" + String.valueOf(day) : String.valueOf(day))
                    + "_" //Separate actual date from hour information
                    + (hour < 10 ? "0" + String.valueOf(hour) : String.valueOf(hour))
                    + String.valueOf(minute) + "0";

            if(messageDistribution.containsKey(key)) {
                messageDistribution.put(key, messageDistribution.get(key) + 1);
                ArrayList<String> tweetsInWindow = new ArrayList<>(tweetsByWindow.get(key));
                tweetsInWindow.add(tweet.getText());
                tweetsByWindow.put(key, tweetsInWindow);
            } else {
                messageDistribution.put(key, 1);
                ArrayList<String> tweetsInWindow = new ArrayList<>();
                tweetsInWindow.add(tweet.getText());
                tweetsByWindow.put(key, tweetsInWindow);
            }
        }
        return messageDistribution;
    }
    
    /**
     * Returns the tweet counts in every refresh window.
     * @return A HashMap which key is the refresh window and its value is the tweet count in this window.
     */
    public final HashMap<String, ArrayList<String>> getTweetsByWindow() { return tweetsByWindow; }
    
    /**
     * Method to generate a reference HashMap for bins and windows.
     * More formally, it matches the index number of a certain bin with the key
     * of the refresh window that the bin is referring to.
     * @param binsHash A HashMap containing the keys of every window along with the tweet counts.
     * @param bins A List containing the same information with 'binsHash' but using an index-based style.
     */
    public final void generateBinsWithKeysReference(HashMap<String, Integer> binsHash, List<Integer> bins) {
        int i = 0;
        for(String key : binsHash.keySet()) {
            binsWithKeys.put(i, key);
            i++;
        }
    }
    
    /**
     * Returns the HashMap with the reference of the bins and windows.
     * @see generateBinsWithKeysReference()
     * @return A HashMap
     * 
     */
    public final HashMap<Integer, String> getBinsWithKeysReference() { return binsWithKeys; }
}
