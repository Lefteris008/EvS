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
package edmodule.data;

import dsretriever.Tweet;
import edmodule.edcow.frequencies.DocumentTermFrequencyItem;
import edmodule.utils.BinPair;
import edmodule.peakfinding.StringDateUtils;
import edmodule.utils.Stemmers;
import edmodule.utils.StopWordsHandlers;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import preprocessingmodule.language.LangUtils;
import preprocessingmodule.nlp.Tokenizer;
import preprocessingmodule.nlp.stemming.StemUtils;
import utilities.Config;
import utilities.Utilities;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2016.01.25_1934_gargantua
 */
public class EDCoWCorpus {
    
    private final Config config;
    private List<Tweet> tweets;
    
    private final StopWordsHandlers swH;
    private Integer[] numberOfDocuments;
    private final List<BinPair<String, Integer>> bins = new ArrayList<>();
    private List<String> terms = new ArrayList<>(); //List containing all occuring termsprivate final HashMap<String, Integer> termsMap = new HashMap<>(); //A map containing terms along with their frequency of occurance
    private final List<DocumentTermFrequencyItem> termDocFreqId = new ArrayList<>(); //A list containg triplets of tweetIDs, termIDs and their frequenciesprivate final HashMap<Integer, List<String>> docTerms = new HashMap<>(); //A map containing the tweetIDs and a list of the terms that each one of them includes
    private int numberOfTweets = 0;
    private final HashMap<String, HashMap<String, Integer>> termsDocsWithOccurencies = new HashMap<>();
    private final HashMap<String, Integer> termIds = new HashMap<>(); //A map containing the ids of the terms (namely, their index as they are being read)
    private final HashMap<String, Integer> messageDistribution = new HashMap<>();
    private final HashMap<String, Integer> documentIndices = new HashMap<>();
    
    private Date earliestDate;
    private Date latestDate;
    
    /**
     * Public constructor.
     * @param config A configuration object
     * @param tweets A ArrayList containing all retrieved tweets
     * @param swH A StopWordsHandlers object
     */
    public EDCoWCorpus(Config config, List<Tweet> tweets, StopWordsHandlers swH) {
        this.config = config;
        this.tweets = tweets;
        this.swH = swH;
        removeDublicateTweets();
    }
    
    /**
     * Method to quickly remove duplicate tweets.
     * More formally, this method hashes the tweets by its text and stores
     * the first one to show up. Tweets that have the exact same text, were
     * ignored. Finally, the new list replaces the 'tweets' collection.
     */
    private void removeDublicateTweets() {
        Utilities.printMessageln("Removing dublicates...");
        long startTime = System.currentTimeMillis();
        
        HashMap<Integer, Tweet> uniqueTweetsMap = new HashMap<>();
        tweets.stream().filter((tweet) -> (!uniqueTweetsMap.containsKey(
                tweet.getText().hashCode()))).forEach((tweet) -> {
            uniqueTweetsMap.put(tweet.getText().hashCode(), tweet);
        });
        List<Tweet> cleanedTweets = new ArrayList<>(uniqueTweetsMap.values());
        
        long endTime = System.currentTimeMillis();
        Utilities.printExecutionTime(startTime, endTime, PeakFindingCorpus.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName());
        Utilities.printMessageln("Original size of tweets: " + tweets.size());
        Utilities.printMessageln("New size of tweets: " + cleanedTweets.size());
        Utilities.printMessageln("Removed " + (tweets.size() - cleanedTweets.size()) + " duplicates.");
        tweets = new ArrayList<>(cleanedTweets);
    }
    
    /**
     * Main method that creates a working corpus for EDCoW algorithm.
     */
    public final void createCorpus() {
        long startTime = System.currentTimeMillis();
        
        //Initialize variables
        earliestDate = tweets.get(0).getDate();
        latestDate = tweets.get(0).getDate();
        
        Calendar cal;
        String docKey;
        int termCount = 0;
        int documentCount = 0;        
        
        //Set the calendar instance
        cal = Calendar.getInstance();

        for(Tweet tweet : tweets) {            
            Date tweetDate = tweet.getDate();
            if(tweetDate.before(earliestDate)) {
                earliestDate = tweetDate;
            }
            if(tweetDate.after(latestDate)) {
                latestDate = tweetDate;
            }
            
            //Count the tweet, update the distribution, get the docKey
            //and update the corresponding HashMap
            numberOfTweets++;
            docKey = updateMessageDistribution(cal, tweet.getDate(), 10);
            if(!documentIndices.containsKey(docKey)) {
                documentIndices.put(docKey, documentCount);
                documentCount++;
            }
            
            //Get the tweet's text and tokenize it
            String text = tweet.getText();
            Tokenizer tokens = new Tokenizer(config, text, 
                    swH.getSWHandlerAccordingToLanguage(LangUtils.getLanguageISOCodeFromString(tweet.getLanguage())));

            //Iterate through the stemmed clean tokens/hashtags
            StemUtils stemHandler = new StemUtils();
            for(String token : stemHandler.getStemsAsList(tokens.getCleanTokensAndHashtags(),
                    Stemmers.getStemmerAccordingToLanguage(LangUtils.getLanguageISOCodeFromString(tweet.getLanguage())))) {
                
                //Update the HashMap with the triplet Document, Token, Frequency
                if(termsDocsWithOccurencies.containsKey(docKey)) { //Document already exists, update it
                    if(termsDocsWithOccurencies.get(docKey).containsKey(token)) { //Token already exists, update it
                        termsDocsWithOccurencies.get(docKey).put(token, 
                                (termsDocsWithOccurencies.get(docKey).get(token) + 1));
                    } else { //Token does not exist, put it
                        termsDocsWithOccurencies.get(docKey).put(token, 1);
                    }
                } else { //Document does not exist, so as the token -create it and put it
                    HashMap<String, Integer> termsWithOccurencies = new HashMap<>();
                    termsWithOccurencies.put(token, 1);
                    termsDocsWithOccurencies.put(docKey, termsWithOccurencies);
                }
                
                //Generate a HashMap containig the index IDs of the terms
                if(!termIds.containsKey(token)) {
                    termIds.put(token, termCount);
                    termCount++;
                }
            }
        }
        setNumberOfDocuments(10, messageDistribution);
        
        long endTime = System.currentTimeMillis();
        Utilities.printExecutionTime(startTime, endTime, EDCoWCorpus.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName());
    }
    
    /**
     * Updates the distribution of incoming messages (tweets).
     * More formally, it calculates the tweets belonging to a certain document. In this iteration, a document
     * is set in a user-defined refresh window, so the method calculates and stores the tweets of this period. 
     * It then stores this information into a HashMap which its docKey is the corresponding date and its value
     * is the summary of the messages that have been generated into that time period.
     * @param cal A Calendar instance, already set.
     * @param date The date to be checked.
     * @param refreshWindow An integer representing the precision window (1-60) (in minutes) -E.g. 10 for 10-minute
     * window, 30 for 30 minute window etc.
     * @return The key of the document for HashMap storage. The key is assembled in YYYYMMDD_HHMM fashion
     * using a user-defined precision window (for values of 10, HH00-HH09 belong to the 0-th 
     * 10-minute window etc.)<br/> 
     * For December 6th, 2014 10:07 PM, the generated key would be 20141206_2200
     * For January 21st, 2015 10:27 AM, the generated key would be 20150121_1020
     */
    public final String updateMessageDistribution(Calendar cal, Date date, int refreshWindow) {
        int year;
        int month;
        int day;
        int hour;
        int minute;
        cal.setTime(date);
        year = cal.get(Calendar.YEAR); //Current year
        month = cal.get(Calendar.MONTH) + 1; //Zero-index based
        day = cal.get(Calendar.DAY_OF_MONTH); //Get the current day of month
        hour = cal.get(Calendar.HOUR_OF_DAY); //24h
        minute = cal.get(Calendar.MINUTE) / refreshWindow; //Nearest 10-minute window, starting from 0
        
        //Assemble the key in YYYYMMDD_HHMM form.
        String key = String.valueOf(year) 
                + (month < 10 ? "0" + String.valueOf(month) : String.valueOf(month)) 
                + (day < 10 ? "0" + String.valueOf(day) : String.valueOf(day))
                + "_" //Separate actual date from hour information
                + (hour < 10 ? "0" + String.valueOf(hour) : String.valueOf(hour))
                + String.valueOf(minute) + "0";
        
        if(messageDistribution.containsKey(key)) {
            messageDistribution.put(key, messageDistribution.get(key) + 1);
        } else {
            messageDistribution.put(key, 1);
        }
        return key;
    }
    
    /**
     * Returns the terms of the dataset.
     * @return A String list containing the unique terms of the dataset.
     */
    public List<String> getTerms() { 
        Set<String> termsSet = new HashSet<>();
        termsDocsWithOccurencies.keySet().stream().forEach((docKey) -> {
            termsSet.addAll(termsDocsWithOccurencies.get(docKey).keySet());
        });
        terms = new ArrayList<>(termsSet); //Store them
        return terms; 
    }
    
    /**
     * Initializes and stores a list containing objects of DocumentTermFrequencyItem class.
     * More formally, each listing in this list contains a triplet with the ID of a document,
     * the ID of a term that the document contains and the term's frequency.
     */
    public final void setDocTermFreqIdList() {
        int documentID, termID;
        int frequency;
        for(String docKey : termsDocsWithOccurencies.keySet()) {
            documentID = documentIndices.get(docKey); //Get the actual ID
            for(String token : termsDocsWithOccurencies.get(docKey).keySet()) {
                termID = termIds.get(token);
                frequency = termsDocsWithOccurencies.get(docKey).get(token);
                DocumentTermFrequencyItem item = new DocumentTermFrequencyItem(documentID, termID, frequency);
                termDocFreqId.add(item);
            }
        }
    }
    
    /**
     * Returns true if the termDocFreqId list contains values.
     * @return True if the list is not empty, false otherwise.
     */
    public final boolean frequencyListContainsValues() {
        return !termDocFreqId.isEmpty();
    }
    
    /**
     * Returns the frequencies in all occurring documents/tweets of a term.
     * @param term The index of the term in the 'terms' list.
     * @return An Integer array containing the frequencies of the term in all documents.
     */
    public Integer[] getDocumentsTermFrequency(int term) {
        
        if(!frequencyListContainsValues()) {
            return null;
        }
        
        //Creates a short array which equals the number of documents
        Integer[] frqs = new Integer[numberOfDocuments.length];
        
        //Initializes the 'frqs' array with zeros
        IntStream.range(0, numberOfDocuments.length).forEach(i -> frqs[i] = 0);
        if (term != -1) {   
            //For every single element in the list 'termDocFreqId'
            termDocFreqId.stream()
                .filter(dti -> dti.term_id == term) //Filter out irrelevant terms
                .forEach(dti -> frqs[dti.doc_id] = dti.frequency); //Go to the corresponding index in 'frqs' array
                //defined by the id of the document ('doc_id') and store the corresponding frequency that already
                //exists in 'termDocFreqId' array
        }
        return frqs;
    }
    
//    /**
//     * Sets an integer array of numberOfDocuments size that contains
//     * the tweets distribution of the user-defined window.
//     * @param numberOfDocuments A HashMap containing the tweets distribution.
//     */
//    public void setNumberOfDocuments(HashMap<String, Integer> distribution) {
//        numberOfDocuments = new Integer[distribution.size()];
//        int i = 0;
//        for(String key : distribution.keySet()) {
//            numberOfDocuments[i] = distribution.get(key);
//            i++;
//        }
//    }
    
    /**
     * Sets an integer array of numberOfDocuments size that contains
     * the tweets distribution of the user-defined window.
     * @param window An integer representing the refresh window.
     * @param numberOfDocuments A HashMap containing the tweets distribution.
     */
    private void setNumberOfDocuments(int window, HashMap<String, Integer> distribution) {
        
        Calendar cal = Calendar.getInstance();
        
        //Get earliest and latest dates of corpus
        String earliestKey = StringDateUtils.getDateKey(cal, getEarliestDateOfCorpus(), window);
        String latestKey = StringDateUtils.getDateKey(cal, getLatestDateOfCorpus(), window);
        int value;
        
        StringDateUtils.clearAndSetYearToMinute(cal, latestKey);
        long endMillis = cal.getTimeInMillis();
        StringDateUtils.clearAndSetYearToMinute(cal, earliestKey);
        
        //Iterate between the two dates and store all the corresponding 10-minute windows
        for (; cal.getTimeInMillis() <= endMillis; cal.add(Calendar.MINUTE, window)) {
            
            String currentKey = StringDateUtils.getDateKey(cal, cal.getTime(), window);
            if(distribution.containsKey(currentKey)) {
                value = distribution.get(currentKey);
            } else {
                value = 0;
            }
            BinPair pair = new BinPair(currentKey, value);
            bins.add(pair);
        }
        
        numberOfDocuments = new Integer[bins.size()];
        int i = 0;
        for(BinPair<String, Integer> bin : bins) {
            numberOfDocuments[i] = bin.getValue();
            i++;
        }
    }
    
    /**
     * Returns the number of documents.
     * @return An Integer array containing the number of tweets.
     */
    public Integer[] getNumberOfDocuments() {
        return numberOfDocuments;
    }
    
    /**
     * Returns the total number of tweets of the given corpus.
     * @return An integer containing the number of tweets.
     */
    public int getNumberOfTweets() {
        return numberOfTweets;
    }
    
    /**
     * Returns the earliest date a tweet was published in the corpus.
     * @return A Date object.
     */
    public final Date getEarliestDateOfCorpus() { return earliestDate; }
    
    /**
     * Returns the latest date a tweet was published in the corpus.
     * @return A Date object.
     */
    public final Date getLatestDateOfCorpus() { return latestDate; }
}
