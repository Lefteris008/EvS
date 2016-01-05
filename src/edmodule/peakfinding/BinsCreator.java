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
package edmodule.peakfinding;

import edmodule.data.Dataset;
import edmodule.data.PeakFindingCorpus;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import utilities.Config;
import utilities.Utilities;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2016.01.05_2037_planet3
 */
public class BinsCreator {
    
    /**
     * Method to create and return the bins needed for OfflinePeakFinding algorithm to operate.
     * More formally, it creates an ArrayList of integers, containing the count of tweets in a pre-specified
     * time interval (windows).
     * @param window An integer indicating the time interval in which the tweets should be counted.
     * All values in minutes. <br/>
     * E.g. For 1 minute interval --> 1. <br/>
     * For half an hour interval --> 30. <br/>
     * For 5 hours interval --> 300.
     * @return An ArrayList containing the bins.
     */
    public static List<Integer> createBins(Config config, int window) {
        long startTime = System.currentTimeMillis();
        
        Dataset ds = new Dataset(config);
        PeakFindingCorpus corpus = new PeakFindingCorpus(config, ds.getTweetList(), ds.getSWH());
        HashMap<String, Integer> binsHash = corpus.createBins(window);
        List<Integer> bins = new ArrayList<>();
        binsHash.keySet().stream().forEach((bin) -> {
            bins.add(binsHash.get(bin));
        });
        
        long endTime = System.currentTimeMillis();
        Utilities.printExecutionTime(startTime, endTime, BinsCreator.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName());
        return bins;
    }
}
