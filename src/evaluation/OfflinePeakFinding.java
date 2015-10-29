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
package evaluation;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2015.10.12_2017_planet1
 * 
 * Based on [1] Marcus A. et al., "TwitInfo: Aggregating and Visualizing Microblogs for Event Exploration", CHI 2011.
 */
public class OfflinePeakFinding {
    
    /**
     * Implements the main algorithm of paper [1].
     * @param bins The bins containing the tweet information
     * @param t Threshold 't'
     * @param r Threshold 'r'
     * @param a Threshold 'a'
     * @return A list containing the calculated windows
     */
    public static List<Window<Integer, Integer>> findPeakWindow(List<Integer> bins, int t, int r, int a) {
        List<Window<Integer, Integer>> windows = new ArrayList<>();
        double mean = bins.get(0); //Set the first element as mean
        double meanDev = Statistics.variance(bins);
        Window window;
        int start;
        int end = 0;
        
        for(int i=1; i < bins.size(); i++) {
            if(( (bins.get(i) - mean) / meanDev > t ) && (bins.get(i) > bins.get(i-1))) {
                start = i - 1; //Update the starting point
                while( (i < bins.size()) && (bins.get(i) > bins.get(i-1)) ) {
                    mean = updateMean(meanDev, bins.get(i), a); //Update mean
                    meanDev = updateMeanDev(mean, meanDev, bins.get(i), a); //Update mean deviance
                    i++; //Move to next iteration
                }
                while( (i < bins.size()) && (bins.get(i) > bins.get(start)) ) {
                    if(( (bins.get(i) - mean) / meanDev > r ) && (bins.get(i) > bins.get(i-1))) {
                        end = --i;
                        break;
                    } else {
                        mean = updateMean(meanDev, bins.get(i), a); //Update mean
                        meanDev = updateMeanDev(mean, meanDev, bins.get(i), a); //Update mean deviance
                        end = i++;
                    }
                }
                window = new Window(start, end); //Create a new window
                windows.add(window); //Append it to the windows array list
            } else {
                mean = updateMean(meanDev, bins.get(i), a); //Update mean
                meanDev = updateMeanDev(mean, meanDev, bins.get(i), a); //Update mean deviance
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
    public static double updateMean(double oldMean, int bin, int a) {
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
    public static double updateMeanDev(double oldMean, double oldMeanDev, int bin, int a) {
        double diff = Math.abs((oldMean - bin));
        return a * diff + (1-a) * oldMeanDev;
    }
    
    /**
     * Secondary method to create the bins
     * @return The created bins
     */
    public static List<Integer> createBins() {
        throw(new UnsupportedOperationException());
    }
    
}
