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
package evs.peakfinding;

import edmodule.utils.BinPair;
import edmodule.EDMethod;
import edmodule.peakfinding.Statistics;
import edmodule.peakfinding.Window;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import preprocessingmodule.nlp.stemming.StemUtils;
import evs.data.PeakFindingSentimentCorpus;
import evs.peakfinding.event.SentimentPeakFindingEvent;
import evs.peakfinding.event.SentimentPeakFindingEvents;
import utilities.Utilities;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2016.03.28_0007
 * 
 * Based on [1] Marcus A. et al., "TwitInfo: Aggregating and Visualizing 
 * Microblogs for Event Exploration", CHI 2011.
 */
public class SentimentPeakFinding implements EDMethod {
    
    private final double alpha;
    private final int taph;
    private final int pi;
    private final List<BinPair<String, Integer>> bins;
    private final int refreshWindow;
    private final List<Window<Integer, Integer>> windows = new ArrayList<>();
    private final List<Window<Integer, Integer>> actualEventWindows = new ArrayList<>();
    private final PeakFindingSentimentCorpus corpus;
    private int totalEvents;
    private final List<Integer> tweetCountsInWindows = new ArrayList<>();
    private List<SentimentPeakFindingEvent> sEventList;
    private final StemUtils stemsHandler;
    private final int sentimentSouce;
    private double executionTime;
    
    /**
     * Public constructor.
     * @param bins Bins parameter, containing the count of tweets in pre-specified time intervals.
     * @param a Alpha parameter to capture historical information. Values lower than 1 are recommended.
     * @param t Threshold parameter. 
     * @param p Primary parameter indicates the first bins to be considered in calculating initial mean deviance.
     * @param refreshWindow An integer representing the refresh window of every bin.
     * @param corpus A PeakFindingCorpus object.
     */
    public SentimentPeakFinding(List<BinPair<String, Integer>> bins, double a, 
            int t, int p, int refreshWindow, PeakFindingSentimentCorpus corpus,
            int sentimentSouce) {
        alpha = a;
        taph = t;
        pi = p;
        this.bins = bins;
        this.refreshWindow = refreshWindow;
        this.corpus = corpus;
        this.stemsHandler = new StemUtils();
        this.sentimentSouce = sentimentSouce;
    }
    
    @Override
    public String getName() {
        return "TwitInfo";
    }

    @Override
    public String getCitation() {
        return "Marcus A. et al., \"TwitInfo: Aggregating and Visualizing Microblogs for Event Exploration\", CHI 2011.";
    }

    @Override
    public String getAuthors() {
        return "Marcus A., Bernstein M., Badar O., Karger D., Madden S., Miller R.";
    }

    @Override
    public String getDescription() {
        return "Aggregating and Visualizing Microblogs for Event Exploration";
    }

    /**
     * Implements the main algorithm of paper [1].
     */
    @Override
    public void apply() throws FileNotFoundException {
        long startTime = System.currentTimeMillis();
        
        double mean = bins.get(0).getValue(); //Set the first element as mean
        List<BinPair<String, Integer>> tempBins = new ArrayList<>();
        for(int i = 0; i < pi; i++) {
            tempBins.add(bins.get(i));
        }
        double meanDev = Statistics.variance(tempBins); //Initialize only with the first 'p' bins
        Window window;
        
        int start;
        int end = 0;
        
        for(int i = 1; i < bins.size(); i++) {
            if(( (bins.get(i).getValue() - mean) / meanDev > taph ) && 
                    (bins.get(i).getValue() > bins.get(i-1).getValue())) {
                start = i - 1; //Update the starting point
                while( (i < bins.size()) && (bins.get(i).getValue() > bins.get(i-1).getValue()) ) {
                    mean = updateMean(meanDev, bins.get(i).getValue(), alpha); //Update mean
                    meanDev = updateMeanDev(mean, meanDev, bins.get(i).getValue(), alpha); //Update mean deviance
                    i++; //Move to the next iteration
                }
                while( (i < bins.size()) && (bins.get(i).getValue() > bins.get(start).getValue()) ) {
                    if(( (bins.get(i).getValue() - mean) / meanDev > taph ) && (bins.get(i).getValue() > bins.get(i-1).getValue())) {
                        end = --i;
                        break;
                    } else {
                        mean = updateMean(meanDev, bins.get(i).getValue(), alpha); //Update mean
                        meanDev = updateMeanDev(mean, meanDev, bins.get(i).getValue(), alpha); //Update mean deviance
                        end = i++;
                    }
                }
                window = new Window(start, end); //Create a new window
                windows.add(window); //Append it to the windows array list
            } else {
                mean = updateMean(meanDev, bins.get(i).getValue(), alpha); //Update mean
                meanDev = updateMeanDev(mean, meanDev, bins.get(i).getValue(), alpha); //Update mean deviance
            }
        }
        generateActualWindows(); //Generate non-zero windows
        SentimentPeakFindingEvents pfe = new SentimentPeakFindingEvents(corpus.getPeakFindingCorpus()
                .getTweetsByWindow(), bins, actualEventWindows, corpus, stemsHandler, sentimentSouce);
        sEventList = new ArrayList<>(pfe.getEvents());
        long endTime = System.currentTimeMillis();
        executionTime = (endTime - startTime) / 1000;
        Utilities.printExecutionTime(startTime, endTime, SentimentPeakFinding.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName());
    }
    
    /**
     * Method to update the mean value.
     * @param oldMean The old mean value
     * @param bin The current bin
     * @param a Threshold 'a'
     * @return The updated mean value
     */
    public static double updateMean(double oldMean, int bin, double a) {
        return a * bin + (1-a) * oldMean;
    }
    
    /**
     * Method to update the mean deviance.
     * @param oldMean The old mean value
     * @param oldMeanDev The old mean deviance value
     * @param bin The current bin
     * @param a Threshold 'a'
     * @return The updated mean deviance value
     */
    public static double updateMeanDev(double oldMean, double oldMeanDev, int bin, double a) {
        double diff = Math.abs((oldMean - bin));
        return a * diff + (1-a) * oldMeanDev;
    }
    
    /**
     * Method to generate a list with the windows that contains tweets and thus
     * can be considered as 'actual' events.
     */
    public final void generateActualWindows() {
        totalEvents = windows.size();
        int tweetCount;
        for(Window<Integer, Integer> window : windows) {
            tweetCount = 0;
            for(int i = window.getStart(); i < window.getEnd(); i++) {
                tweetCount += bins.get(i).getValue(); //Count the tweets in window
            }
            if(tweetCount != 0) {
                tweetCountsInWindows.add(tweetCount);
                actualEventWindows.add(window);
            } else {
                totalEvents--; //Reduce the number of events, if it contains 0 tweets
            }
        }
    }
    
    /**
     * Method to return windows that have calculated by main algorithm.
     * @return A List containing the windows.
     */
    public final List<Window<Integer, Integer>> getWindows() { return windows; }
    
    /**
     * Method to return bins that have been previously calculated in BinsCreator class.
     * @return A List containing BinPair objects.
     */
    public final List<BinPair<String, Integer>> getBins() { return bins; }
    
    /**
     * Returns a list of the extracted events, along with their sentiment info.
     * @return A List comprised by the events.
     */
    public final List<SentimentPeakFindingEvent> getEventList() { return sEventList; }
    
    /**
     * Returns an already initiated StemsHandler object.
     * @return A StemsHandler object.
     */
    public final StemUtils getStemsHandler() { return stemsHandler; }
    
    /**
     * Returns the total time of execution.
     * @return A double representing the time of execution.
     */
    public final double getExecutionTime() { return executionTime; }
}
