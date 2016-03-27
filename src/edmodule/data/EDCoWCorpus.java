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
import edmodule.utils.StringDateUtils;
import edmodule.utils.Stemmers;
import edmodule.utils.StopWordsHandlers;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
 * @version 2016.03.27_2374
 */
public class EDCoWCorpus {
    
    private final Config config;
    private List<Tweet> tweets;
    private final int refreshWindow;
    private final StopWordsHandlers swH;
    private Integer[] numberOfDocuments;
    private final List<BinPair<String, Integer>> bins = new ArrayList<>();
    private List<String> terms = new ArrayList<>(); //List containing all occuring termsprivate final HashMap<String, Integer> termsMap = new HashMap<>(); //A map containing terms along with their frequency of occurance
    private final List<DocumentTermFrequencyItem> termDocFreqId = new ArrayList<>(); //A list containg triplets of tweetIDs, termIDs and their frequenciesprivate final HashMap<Integer, List<String>> docTerms = new HashMap<>(); //A map containing the tweetIDs and a list of the terms that each one of them includes
    private int numberOfTweets = 0;
    private final Map<String, HashMap<String, Integer>> termsDocsWithOccurencies = new HashMap<>();
    private final Map<String, ArrayList<String>> idsDocs = new HashMap<>();
    private final Map<String, Integer> termIds = new HashMap<>(); //A map containing the ids of the terms (namely, their index as they are being read)
    private final Map<String, Integer> messageDistribution = new HashMap<>();
    private final Map<String, Integer> documentIndices = new HashMap<>();
    private final Map<String, Tweet> tweetMap = new HashMap<>();
    private final StemUtils stemHandler = new StemUtils();
    private Date earliestDate;
    private Date latestDate;
    
    /**
     * Public constructor.
     * @param config A configuration object
     * @param tweets A ArrayList containing all retrieved tweets
     * @param swH A StopWordsHandlers object
     */
    public EDCoWCorpus(Config config, List<Tweet> tweets, StopWordsHandlers swH, int refreshWindow) {
        this.config = config;
        this.tweets = tweets;
        this.swH = swH;
        this.refreshWindow = refreshWindow;
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
            tweetMap.put(String.valueOf(tweet.getID()), tweet);
            
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
            docKey = updateMessageDistribution(cal, tweet.getDate());
            if(!documentIndices.containsKey(docKey)) {
                documentIndices.put(docKey, documentCount);
                documentCount++;
            }
            
            //Get the tweet's text and tokenize it
            String text = tweet.getText();
            String id = String.valueOf(tweet.getID());
            Tokenizer tokens = new Tokenizer(config, text, 
                    swH.getSWHandlerAccordingToLanguage(LangUtils.getLangISOFromString(tweet.getLanguage())));

            //Iterate through the stemmed clean tokens/hashtags
            
            for(String token : stemHandler.getStemsAsList(tokens.getCleanTokensAndHashtags(),
                    Stemmers.getStemmer(LangUtils.getLangISOFromString(tweet.getLanguage())))) {
                
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
            //Store the tweet ID too
            if(idsDocs.containsKey(docKey)) {
                idsDocs.get(docKey).add(id);
            } else {
                ArrayList<String> temp = new ArrayList<>();
                temp.add(id);
                idsDocs.put(docKey, temp);
            }
        }
        setNumberOfDocuments(messageDistribution);
        
        long endTime = System.currentTimeMillis();
        Utilities.printExecutionTime(startTime, endTime, EDCoWCorpus.class.getName(), 
                Thread.currentThread().getStackTrace()[1].getMethodName());
    }
    
    /**
     * Updates the distribution of incoming messages (tweets).
     * More formally, it calculates the tweets belonging to a certain document. 
     * In this iteration, a document is set in a user-defined refresh window, 
     * so the method calculates and stores the tweets of this period. It then 
     * stores this information into a HashMap which its docKey is the corresponding
     * date and its value is the summary of the messages that have been generated
     * into that time period.
     * @param cal A Calendar instance, already set.
     * @param date The date to be checked.
     */
    public final String updateMessageDistribution(Calendar cal, Date date) {
        String key = StringDateUtils.getDateKey(cal, date, refreshWindow);
        
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
     * Initializes and stores a list containing objects of DocumentTermFrequencyItem
     * class. <br/>
     * More formally, each listing in this list contains a triplet with the ID of
     * a document, the ID of a term that the document contains and the term's
     * frequency.
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
    
    /**
     * Sets an integer array of numberOfDocuments size that contains.
     * the tweets distribution of the user-defined window.
     * @param numberOfDocuments A HashMap containing the tweets distribution.
     */
    private void setNumberOfDocuments(Map<String, Integer> distribution) {
        
        Calendar cal = Calendar.getInstance();
        
        //Get earliest and latest dates of corpus
        String earliestKey = StringDateUtils.getDateKey(cal, getEarliestDateOfCorpus(), refreshWindow);
        String latestKey = StringDateUtils.getDateKey(cal, getLatestDateOfCorpus(), refreshWindow);
        int value;
        
        StringDateUtils.clearAndSetYearToMinute(cal, latestKey);
        long endMillis = cal.getTimeInMillis();
        StringDateUtils.clearAndSetYearToMinute(cal, earliestKey);
        
        //Iterate between the two dates and store all the corresponding 10-minute windows
        for (; cal.getTimeInMillis() <= endMillis; cal.add(Calendar.MINUTE, refreshWindow)) {
            
            String currentKey = StringDateUtils.getDateKey(cal, cal.getTime(), refreshWindow);
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
    
    /**
     * Returns a pre-configured stems handler object.
     * @return A stemUtils object.
     */
    public final StemUtils getStemsHandler() { return stemHandler; }
    
    /**
     * Method to convert a timeslice used in EDCoW analysis into its date.
     * @param timeSlice An integer representing the timeslice of the analysis.
     * @return A String representing the date of the timeslice.
     */
    public final String getDateFromTimeSlice(int timeSlice) {
        return bins.get(timeSlice).getBin();
    }
    
    /**
     * Method to get all tweet IDs in a specific window [start, end).
     * @param start A String with starting point of the window, assembled in YYYYMMDD_HHMM fashion.
     * @param end A String with ending point of the window, assembled in YYYYMMDD_HHMM fashion.
     * @return The tweet IDs separated by white spaces.
     */
    public final String getIDsOfWindowAsString(String start, String end) {
        Calendar cal = Calendar.getInstance();
        StringBuilder ids = new StringBuilder();
        
        StringDateUtils.clearAndSetYearToMinute(cal, end);
        long endMillis = cal.getTimeInMillis();
        StringDateUtils.clearAndSetYearToMinute(cal, start);
        
        //Iterate between the two dates and store all the corresponding 10-minute windows
        for (; cal.getTimeInMillis() <= endMillis; cal.add(Calendar.MINUTE, refreshWindow)) {
            
            String currentKey = StringDateUtils.getDateKey(cal, cal.getTime(), refreshWindow);
            idsDocs.get(currentKey).stream().forEach((id) -> {
                ids.append(id).append(" ");
            }); //ids.append(idsDocs.get(currentKey));
        }
        return ids.toString();
    }
    
    /**
     * Returns the tweets that belong to the window [start, end) as a list.
     * @param start The start point of the window.
     * @param end The end point of the window.
     * @return A list comprised by all tweets in the specified window.
     */
    public final List<Tweet> getTweetsOfWindowAsList(String start, String end) {
        List<Tweet> tweetsInWindow = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        
        StringDateUtils.clearAndSetYearToMinute(cal, end);
        long endMillis = cal.getTimeInMillis();
        StringDateUtils.clearAndSetYearToMinute(cal, start);
        
        //Iterate between the two dates and store all the corresponding 10-minute windows
        for (; cal.getTimeInMillis() <= endMillis; cal.add(Calendar.MINUTE, refreshWindow)) {
            
            String currentKey = StringDateUtils.getDateKey(cal, cal.getTime(), refreshWindow);
            for(String id : idsDocs.get(currentKey)) {
                tweetsInWindow.add(tweetMap.get(id));
            }
        }
        return tweetsInWindow;
    }
}
