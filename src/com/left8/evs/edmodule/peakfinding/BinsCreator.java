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
package com.left8.evs.edmodule.peakfinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import com.left8.evs.edmodule.utils.StringDateUtils;
import com.left8.evs.edmodule.utils.BinPair;
import com.left8.evs.edmodule.data.PeakFindingCorpus;
import com.left8.evs.utilities.Config;
import com.left8.evs.utilities.Utilities;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2016.04.30_1826
 */
public class BinsCreator {
    
    /**
     * Method to create and return the bins needed for OfflinePeakFinding algorithm to operate.
     * More formally, it creates an List of BinPair objects, containing the count
     * of tweets in pre-specified time intervals (windows).
     * @param corpus A PeakFindingCorpus object.
     * @param config A Configuration object.
     * @param refreshWindow An integer indicating the time interval in which the tweets
     * should be counted.All values in minutes. <br>
     * E.g. For 1 minute interval --&gt; 1. <br>
     * For half an hour interval --&gt; 30. <br>
     * For 5 hours interval --&gt; 300.
     * @return An List of BinPair objects containing the bins.
     * @see StringDateUtils StringDateUtils class.
     * @see BinPair BinPair class.
     */
    public final static List<BinPair<String, Integer>> createBins(PeakFindingCorpus corpus, Config config, int refreshWindow) {
        long startTime = System.currentTimeMillis();
        
        HashMap<String, Integer> binsHash = corpus.createCorpus(refreshWindow);
        
        List<BinPair<String, Integer>> bins = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        
        //Get earliest and latest dates of corpus
        String earliestKey = StringDateUtils.getDateKey(cal, corpus.getEarliestDateOfCorpus(), refreshWindow);
        String latestKey = StringDateUtils.getDateKey(cal, corpus.getLatestDateOfCorpus(), refreshWindow);
        int value;
        
        StringDateUtils.clearAndSetYearToMinute(cal, latestKey);
        long endMillis = cal.getTimeInMillis();
        StringDateUtils.clearAndSetYearToMinute(cal, earliestKey);
        
        //Iterate between the two dates and store all the corresponding 10-minute windows
        for (; cal.getTimeInMillis() <= endMillis; cal.add(Calendar.MINUTE, 10)) {
            
            String currentKey = StringDateUtils.getDateKey(cal, cal.getTime(), refreshWindow);
            if(binsHash.containsKey(currentKey)) {
                value = binsHash.get(currentKey);
            } else {
                value = 0;
            }
            BinPair pair = new BinPair(currentKey, value);
            bins.add(pair);
        }
        
        long endTime = System.currentTimeMillis();
        Utilities.printExecutionTime(startTime, endTime, BinsCreator.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName());
        return bins;
    }
}
