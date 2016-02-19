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
package utilities;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2016.02.19_1657
 */
public class Utilities {
    
    /**
     * Prints the execution time of a current running method in seconds.
     * @param startTime Long representing the current System time when the method started.
     * @param endTime Long representing the current System time when the method finished.
     * @param methodName A String containing the name of the current running method.
     */
    public static void printExecutionTime(long startTime, long endTime, String className, String methodName) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        long runningTime = (endTime - startTime) / 1000; //Convert to seconds
        System.err.println("INFO: " + 
                dateFormat.format(cal.getTime()) + " " + 
                className + " " + methodName + " run for " + 
                (runningTime == 1 ? runningTime + " second." : runningTime + " seconds."));
    }
    
    /**
     * Supplies a message to the error stream, formatting it according to a 
     * standard form, appending '\n' escape character at the end.
     * @param message The message to be printed.
     * @see printMessage() printMessage() method.
     */
    public final static void printMessageln(String message) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        System.err.println("INFO: " + 
                dateFormat.format(cal.getTime()) + " " + 
                message);
    }
    
    /**
     * Supplies a message to the error stream, formatting it according to a 
     * standard form, without creating a new line.
     * @param message The message to be printed.
     * @see printMessageln() printMessageln() method.
     */
    public final static void printMessage(String message) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        System.err.print("INFO: " + 
                dateFormat.format(cal.getTime()) + " " + 
                message);
    }
    
    /**
     * Exports a List of Strings to a standard UTF-8 file.
     * @param filePath The directory along with the name of the file.
     * @param lines A List containing the lines of the file as Strings.
     * @param appendToFile If true the contents of 'lines' are going to be appended
     * to the end of the file. False always replaces file's contents.
     * @return True if the process succeeds, false otherwise.
     */
    public final static boolean exportToFileUTF_8(String filePath, List<String> lines, boolean appendToFile) {
        Path file = Paths.get(filePath);
        try {
            if(appendToFile) {
                Files.write(file, lines, Charset.forName("UTF-8"), StandardOpenOption.APPEND);
            } else {
                Files.write(file, lines, Charset.forName("UTF-8"));
            }
            printMessageln("Successfully exported to file.");
            return true;
        } catch (IOException e) {
            printMessageln("There was a problem exporting to file.");
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, e);
            return false;
        }
    }
}
