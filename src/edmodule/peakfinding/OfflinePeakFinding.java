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

import edmodule.EDMethod;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2016.01.04_1908_planet3
 * 
 * Based on [1] Marcus A. et al., "TwitInfo: Aggregating and Visualizing Microblogs for Event Exploration", CHI 2011.
 */
public class OfflinePeakFinding implements EDMethod {
    
    private final double alpha;
    private final int taph;
    private final int pi;
    private final List<Integer> bins;
    
    /**
     * Public constructor.
     * @param bins Bins parameter, containing the count of tweets in pre-specified time intervals.
     * @param a Alpha parameter to capture historical information. Values lower than 1 are recommended.
     * @param t Threshold parameter. 
     * @param p Primary parameter indicates the first bins to be considered in calculating initial mean deviance.
     */
    public OfflinePeakFinding(List<Integer> bins, double a, int t, int p) {
        alpha = a;
        taph = t;
        pi = p;
        this.bins = bins;
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

    @Override
    public void apply() {
        findPeakWindow();
    }
    
    /**
     * Implements the main algorithm of paper [1].
     * @param bins The bins containing the tweet information
     * @param t Threshold 't'
     * @param r Threshold 'r'
     * @param a Threshold 'a'
     * @return A list containing the calculated windows
     */
    public List<Window<Integer, Integer>> findPeakWindow() {
        List<Window<Integer, Integer>> windows = new ArrayList<>();
        double mean = bins.get(0); //Set the first element as mean
        List<Integer> tempBins = new ArrayList<>();
        for(int i = 0; i < pi; i++) {
            tempBins.add(bins.get(i));
        }
        double meanDev = Statistics.variance(tempBins); //Initialize only with the first 'p' bins
        Window window;
        int start;
        int end = 0;
        
        for(int i=1; i < bins.size(); i++) {
            if(( (bins.get(i) - mean) / meanDev > taph ) && (bins.get(i) > bins.get(i-1))) {
                start = i - 1; //Update the starting point
                while( (i < bins.size()) && (bins.get(i) > bins.get(i-1)) ) {
                    mean = updateMean(meanDev, bins.get(i), alpha); //Update mean
                    meanDev = updateMeanDev(mean, meanDev, bins.get(i), alpha); //Update mean deviance
                    i++; //Move to next iteration
                }
                while( (i < bins.size()) && (bins.get(i) > bins.get(start)) ) {
                    if(( (bins.get(i) - mean) / meanDev > taph ) && (bins.get(i) > bins.get(i-1))) {
                        end = --i;
                        break;
                    } else {
                        mean = updateMean(meanDev, bins.get(i), alpha); //Update mean
                        meanDev = updateMeanDev(mean, meanDev, bins.get(i), alpha); //Update mean deviance
                        end = i++;
                    }
                }
                window = new Window(start, end); //Create a new window
                windows.add(window); //Append it to the windows array list
            } else {
                mean = updateMean(meanDev, bins.get(i), alpha); //Update mean
                meanDev = updateMeanDev(mean, meanDev, bins.get(i), alpha); //Update mean deviance
            }
        }
        return windows;
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
}
