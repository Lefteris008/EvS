/*
 * Copyright (C) 2016 lefte
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
package com.left8.evs.utilities;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2016.11.26_1240
 */
public class PrintUtilities {

    private PrintUtilities() {
        ///
    }
    
    private static final String INFORMATION_LABEL   = "INFO";
    private static final String WARNING_LABEL       = "WARNING";
    private static final String ERROR_LEVEL         = "ERROR";
    
    public static final void printInfoMessage(String message) {
        printMessage(message, INFORMATION_LABEL);
    }
    
    public static final void printWarningMessage(String message) {
        printMessage(message, WARNING_LABEL);
    }
    
    public static final void printErrorMessage(String message) {
        printMessage(message, ERROR_LEVEL);
    }
    
    public static final void printInfoMessageln(String message) {
        printMessageln(message, INFORMATION_LABEL);
    }
    
    public static final void printWarningMessageln(String message) {
        printMessageln(message, WARNING_LABEL);
    }
    
    public static final void printErrorMessageln(String message) {
        printMessageln(message, ERROR_LEVEL);
    }
    
    /**
     * Supplies a message to the error stream, formatting it according to a
     * standard form, without creating a new line.
     * @param message The message to be printed.
     * @param levelLabel The level of the output
     * @see #printMessageln(java.lang.String) printMessageln() method.
     */
    private static void printMessage(String message, String levelLabel) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        
        System.err.print(levelLabel 
                + ": " + dateFormat.format(cal.getTime()) + " " + message);
    }
    
     /**
     * Supplies a message to the error stream, formatting it according to a
     * standard form, appending '\n' escape character at the end.
     * @param message The message to be printed.
     * @param levelLabel The level of the output
     * @see #printMessage(java.lang.String) printMessage() method.
     */
    private static void printMessageln(String message, String levelLabel) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        
        System.err.println(levelLabel 
                + ": " + dateFormat.format(cal.getTime()) + " " + message);
        
    }

    /**
     * Prints the execution time of a current running method in seconds.
     * @param startTime Long representing the current System time when the method started.
     * @param endTime Long representing the current System time when the method finished.
     * @param className The name of the class the method belongs to.
     * @param methodName A String containing the name of the current running method.
     */
    public static void printExecutionTime(long startTime, long endTime, String className, String methodName) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        long runningTime = (endTime - startTime) / 1000; //Convert to seconds
        System.err.println("INFO: " + dateFormat.format(cal.getTime()) + " " + className + " " + methodName + " run for " + (runningTime == 1 ? runningTime + " second." : runningTime + " seconds."));
    }    
}
