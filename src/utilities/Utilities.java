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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2016.04.30_1831
 */
public class Utilities {
    
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
        System.err.println("INFO: " + 
                dateFormat.format(cal.getTime()) + " " + 
                className + " " + methodName + " run for " + 
                (runningTime == 1 ? runningTime + " second." : runningTime + " seconds."));
    }
    
    /**
     * Supplies a message to the error stream, formatting it according to a 
     * standard form, appending '\n' escape character at the end.
     * @param message The message to be printed.
     * @see #printMessage(java.lang.String) printMessage() method.
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
     * @see #printMessageln(java.lang.String) printMessageln() method.
     */
    public final static void printMessage(String message) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        System.err.print("INFO: " + 
                dateFormat.format(cal.getTime()) + " " + 
                message);
    }
    
    /**
     * Exports a List of Strings to a standard UTF-8 file. The file is created
     * if it does not exist.
     * @param filePath The directory along with the name of the file.
     * @param lines A List containing the lines of the file as Strings.
     * @param appendToFile If true the contents of 'lines' are going to be appended
     * to the end of the file. False always replaces file's contents.
     * @return True if the process succeeds, false otherwise.
     */
    public final static boolean exportToFileUTF_8(String filePath, List<String> lines, 
            boolean appendToFile) {
        
        File f = new File(filePath);
        if(!f.exists()) { //Create if not exists
            try {
                f.createNewFile();
            } catch (IOException ex) {
                printMessageln("There was a problem creating the file. Consider"
                        + "manually creating it using you OS's file manager.");
                Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }
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

    /**
     * Combines the tweet's text and the retweet information to assemble the final text.
     * @param isRetweet 1 for retweet, 0 otherwise
     * @param text The original text of the tweet
     * @return The assembled text
     */
    public static final String assembleText(String isRetweet, String text) {
        if (Integer.parseInt(isRetweet) == 1) {
            return "RT " + text;
        } else {
            return text;
        }
    }

    /**
     * Method to extract search terms from the 'search_terms.txt' file.
     * More formally, it generates a String array containing the search terms
     * that will be used in Twitter Streaming API.
     * @param config The configuration object
     * @return A String array containing the search terms
     */
    public static final String[] extractTermsFromFile(Config config) {
        String line;
        ArrayList<String> terms = new ArrayList<>();
        try (final BufferedReader br = new BufferedReader(new FileReader(config.getSearchTermsFile()))) {
            while ((line = br.readLine()) != null) {
                terms.add(line);
            }
            br.close();
        } catch (IOException e) {
            printMessageln("The file '" + config.getSearchTermsFile() + "' is missing.\nPlace a correct file in classpath and re-run the project");
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, e);
            System.exit(1);
        }
        return terms.toArray(new String[terms.size()]);
    }

    /**
     * Returns a Date object for a given String.
     * @param date A String formed in 'hh:MM PM/AM - dd MMM YYYY'.
     * @return A Date object
     */
    public static final Date stringToDate(String date) {
        try {
            DateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
            return format.parse(date);
        } catch (ParseException e) {
            printMessageln("Input String was malformed.");
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, e);
            return null;
        }
    }

    /**
     * Method to read the IDs of the tweets that are going to be retrieved from Twitter.
     * @param config The configuration object
     * @param filename The folder name in which the .txt files containing the IDs are placed
     * @return A list containing the tweet IDs
     */
    public static final List<String> extractTweetIDsFromFile(Config config, String filename) {
        List<String> list = new ArrayList<>();
        String path = config.getDatasetPath() + filename + "\\";
        try {
            Files.walk(Paths.get(path)).forEach((Path filePath) -> {
                if (Files.isRegularFile(filePath)) {
                    try (final BufferedReader br = new BufferedReader(new FileReader(filePath.toString()))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            list.add(line);
                        }
                    } catch (IOException e) {
                        printMessageln("No filed found in '" + config.getDatasetPath() 
                                + filename + "\\'Place the appropriate files in "
                                + "classpath and re-run the project");
                        Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, e);
                        System.exit(1);
                    }
                }
            });
        } catch (IOException e) {
            printMessageln("Folder '" + config.getDatasetPath() + filename + "\\' is missing.\nPlace a correct folder in classpath and re-run the project");
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, e);
            System.exit(1);
        }
        return list;
    }

    /**
     * Parses a String array and creates an ArrayList containing only the appropriate fields.
     * @param fields A String array containing fields of data of a tweet.
     * @return An ArrayList containing the appropriate fields of the original array.
     */
    public static final ArrayList<String> getTweetFieldsFromArray(String[] fields) {
        ArrayList<String> tweet = new ArrayList<>();
        tweet.add(fields[6]);
        tweet.add(fields[7]);
        if ("R".equals(fields[5])) {
            tweet.add("1");
        } else {
            tweet.add("0");
        }
        tweet.add(fields[8]);
        tweet.add(fields[9]);
        tweet.add(fields[10]);
        tweet.add(fields[11]);
        tweet.add("-1");
        tweet.add("-1");
        return tweet;
    }
    
    /**
     * Method to parse a text file containing tweet information and return the extracted fields.
     * @param config A configuration object.
     * @return An ArrayList containing ArrayLists of tweets.
     */
    public static final ArrayList<ArrayList<String>> getTweetDataFromFile(Config config) {
        String path = config.getDatasetPath() + config.getTweetDataFile();
        ArrayList<ArrayList<String>> tweets = new ArrayList<>();
        String[] fields;
        ArrayList<String> tweet;
        String line;
        try (final BufferedReader br = new BufferedReader(new FileReader(path))) {
            while ((line = br.readLine()) != null) {
                fields = line.split("\t");
                if (!"404".equals(fields[0]) && !"true".equals(fields[1])) {
                    tweet = new ArrayList<>(getTweetFieldsFromArray(fields));
                    tweets.add(tweet);
                }
            }
            br.close();
            return tweets;
        } catch (IOException e) {
            printMessageln("The file '" + config.getTweetDataFile() + "' is missing."
                    + "\nPlace a correct file in '" + config.getDatasetPath() + 
                    "' and re-run the project");
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, e);
            return null;
        }
    }
}
