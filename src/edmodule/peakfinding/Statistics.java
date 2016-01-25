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

import edmodule.utils.BinPair;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import utilities.Config;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2016.01.12_1436_gargantua
 */
public class Statistics {
    
    /**
     * Calculates the summary of all elements in an integer list.
     * @param list The list with the elements to be summed
     * @return The summary of the elements
     */
    public static int sum(List<BinPair<String, Integer>> list) {
        int sum = 0;
        sum = list.stream().map((list1) -> list1.getValue()).reduce(sum, Integer::sum);
        return sum;
    }

    /**
     * Calculates the average of the elements of an integer list.
     * @param list The list for which the average value will be computed
     * @return The average value of the elements of the list
     */
    public static double average(List<BinPair<String, Integer>> list) {
        return sum(list) / list.size();
    }

    /**
     * Calculates the variance of the elements of an integer list.
     * @param list The list for which the variance of its elements will be computed
     * @return The variance of the elements of the list
     */
    public static double variance(List<BinPair<String, Integer>> list) {
        
        double sumMinusAverage = sum(list) - average(list);
        double variance;
        try {
            variance = sumMinusAverage * sumMinusAverage / (list.size()-1);
        } catch(ArithmeticException e) {
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, e);
            return -1.0;
        }
        return variance;
    }

}
