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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import utilities.Config;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2016.01.05_1726_planet3  
 */
public class PeakFindingCorpus {
    
    private final StopWordsHandlers swH;
    private final List<Tweet> tweets;
    private final Config config;
    private final HashMap<String, Integer> messageDistribution = new HashMap<>();
    
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
    public final HashMap<String, Integer> createBins(int window) {
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
            } else {
                messageDistribution.put(key, 1);
            }
        }
        return messageDistribution;
    }
}
