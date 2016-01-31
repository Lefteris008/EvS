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
package dsretriever;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import utilities.Config;
import preprocessingmodule.PreProcessor;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2016.01.31_1921
 */
public class Utils {
    
    /**
     * Method to extract search terms from the 'search_terms.txt' file.
     * More formally, it generates a String array containing the search terms
     * that will be used in Twitter Streaming API.
     * @param config The configuration object
     * @return A String array containing the search terms
     */
    public final static String[] extractTermsFromFile(Config config) {
        
        String line;
        ArrayList<String> terms = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(config.getSearchTermsFile()))) {
            while ((line = br.readLine()) != null) {
                terms.add(line); //Open the search terms file and read them
            }
            br.close();
        } catch (IOException e) {
            System.out.println("The file '" + config.getSearchTermsFile() + "' is missing.\nPlace a correct file in classpath and re-run the project");
            Logger.getLogger(PreProcessor.class.getName()).log(Level.SEVERE, null, e);
            System.exit(1);
        }
        
        return terms.toArray(new String[terms.size()]);
    }
    
    /**
     * Method to read the IDs of the tweets that are going to be retrieved from Twitter.
     * @param config The configuration object
     * @param filename The folder name in which the .txt files containing the IDs are placed
     * @return A list containing the tweet IDs
     */
    public final static List<String> extractTweetIDsFromFile(Config config, String filename) {
        
        List<String> list = new ArrayList<>();
        
        String path = config.getDatasetPath() + filename + "\\";
        
        try {
            Files.walk(Paths.get(path)).forEach(filePath -> { //For all files in the current folder
                if (Files.isRegularFile(filePath)) { //Open every single file
                    try (BufferedReader br = new BufferedReader(new FileReader(filePath.toString()))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                           list.add(line); //Store the id to the list
                        }
                    } catch(IOException e) {
                        System.out.println("No filed found in '" + config.getDatasetPath() + filename + "\\'Place the appropriate files in classpath and re-run the project");
                        Logger.getLogger(PreProcessor.class.getName()).log(Level.SEVERE, null, e);
                        System.exit(1);
                    }
                }
            });     
        } catch (IOException e) {
            System.out.println("Folder '" + config.getDatasetPath() + filename + "\\' is missing.\nPlace a correct folder in classpath and re-run the project");
            Logger.getLogger(PreProcessor.class.getName()).log(Level.SEVERE, null, e);
            System.exit(1);
        }
        return list;
    }
    
    /**
     * Method to parse a text file containing tweet information and return the extracted fields.
     * @param config A configuration object.
     * @return An ArrayList containing ArrayLists of tweets.
     */
    public final static ArrayList<ArrayList<String>> getTweetDataFromFile(Config config) {
        String path = config.getDatasetPath() + config.getTweetDataFile();
        ArrayList<ArrayList<String>> tweets = new ArrayList<>();
        String[] fields;
        ArrayList<String> tweet;
        String line;
        
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            while ((line = br.readLine()) != null) {
                fields = line.split("\t");
                if(!"404".equals(fields[0]) && !"true".equals(fields[1])) { //Tweet must contain info
                    tweet = new ArrayList<>(getTweetFieldsFromArray(fields));
                    tweets.add(tweet);
                }
            }
            br.close();
            return tweets;
        } catch (IOException e) {
            System.out.println("The file '" + config.getTweetDataFile() + "' is missing.\nPlace a correct file in '" + config.getDatasetPath() + "' and re-run the project");
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, e);
            return null;
        }
    }
    
    /**
     * Parses a String array and creates an ArrayList containing only the appropriate fields.
     * @param fields A String array containing fields of data of a tweet.
     * @return An ArrayList containing the appropriate fields of the original array.
     */
    public final static ArrayList<String> getTweetFieldsFromArray(String[] fields) {
        ArrayList<String> tweet = new ArrayList<>();
        
        tweet.add(fields[6]); //Get tweet ID
        tweet.add(fields[7]); //Get username
        if("R".equals(fields[5])) { //Retweet status
            tweet.add("1");
        } else {
            tweet.add("0");
        }
        tweet.add(fields[8]); //Text
        tweet.add(fields[9]); //Date
        tweet.add(fields[10]); //Number of retweets
        tweet.add(fields[11]); //Number of favorites
        tweet.add("-1"); //Latitude
        tweet.add("-1"); //Longitude
        
        return tweet;
    }
    
    /**
     * Combines the tweet's text and the retweet information to assemble the final text.
     * @param isRetweet 1 for retweet, 0 otherwise
     * @param text The original text of the tweet
     * @return The assembled text
     */
    public final static String assembleText(String isRetweet, String text) {
        if(Integer.parseInt(isRetweet) == 1) {
            return "RT " + text;
        } else {
            return text;
        }
    }
    
    /**
     * Returns a Date object for a given String.
     * @param date A String formed in 'hh:MM PM/AM - dd MMM YYYY'.
     * @return A Date object 
     */
    public final static Date stringToDate(String date, ArrayList<String> tweet) {
        try {
            DateFormat format = new SimpleDateFormat("h:m a - d MMM yyyy", Locale.ENGLISH);
            return format.parse(date);
        } catch(ParseException e) {
            System.err.println("Input String was malformed.");
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, e);
            return null;
        }
    }
}
