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
package utilities;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2015.12.08_1715_planet3
 */
public class Utils {
    
    /**
     * Prints the execution time of a current running method in seconds.
     * @param startTime Long representing the current System time when the method started.
     * @param endTime Long representing the current System time when the method finished.
     * @param methodName A String containing the name of the current running method.
     */
    public static void printExecutionTime(long startTime, long endTime, String className, String methodName) {
        long runningTime = (endTime - startTime) / 1000; //Convert to seconds
        System.out.println(className + "->" + methodName + "() run for " + (runningTime == 1 ? runningTime + " second." : runningTime + " seconds."));
    }
    
}
