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
package edmodule.dataset;

import edmodule.edcow.frequencies.DocumentTermFrequencyItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;
import preprocessingmodule.Config;
import dsretriever.MongoHandler;
import dsretriever.Tweet;
import edmodule.utils.Stemmers;
import edmodule.utils.StopWordsHandlers;
import java.util.Calendar;
import java.util.Date;
import preprocessingmodule.language.LangUtils;
import preprocessingmodule.nlp.Tokenizer;
import preprocessingmodule.nlp.stemming.Stemmer;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2015.12.06_1934_planet3
 */
public final class Dataset {
    
    private List<String> terms = new ArrayList<>(); //List containing all occuring terms
    private final HashMap<String, Integer> termsWithOccurencies = new HashMap<>(); //A map containing terms along with their frequency of occurance
    private int numberOfTweets;
    private final List<DocumentTermFrequencyItem> termDocFreqId = new ArrayList<>(); //A list containg triplets of tweetIDs, termIDs and their frequencies
    private final HashMap<Integer, List<String>> docTerms = new HashMap<>(); //A map containing the tweetIDs and a list of the terms that each one of them includes
    private final HashMap<String, Integer> termIds = new HashMap<>(); //A map containing the ids of the terms (namely, their index as they are being read)
    private Integer[] numberOfDocuments;
    private final HashMap<Integer, Integer> messageDistribution = new HashMap<>();
    
    /**
     * It creates a working dataset. More formally, the constructor retrieves all tweets from MongoDB
     * store, iterates through them, tokenizes the text of every single one of them, generates the English
     * stem of every token of them and updates a hashmap that contains terms along with their occurencies.
     * @param config A Configuration object.
     * @param sw A StopWords handler
     */
    public Dataset(Config config) {
        long startTime = System.currentTimeMillis(); //Start time
        
        MongoHandler mongo = new MongoHandler(config);
        mongo.connectToMongoDB(config);
        Calendar cal;
        
        //Initialize stopwords and stemmers
        StopWordsHandlers swH = new StopWordsHandlers(config);
        Stemmers.initStemmers();
        
        //Load all tweets from MongoDB Store
        List<Tweet> tweets = mongo.retrieveAllTweetsFromMongoDBStore(config);
        mongo.closeMongoConnection(config);
        
        //Get the date of the first tweet
        cal = Calendar.getInstance();
        
        //Iterate through all tweets
        for(Tweet tweet : tweets) {            
            numberOfTweets++; //Count it
            
            updateMessageDistribution(cal, tweet.getDate()); //Store the date
            
            //Get the tweet's text and tokenize it
            String text = tweet.getText();
            Tokenizer tokens = new Tokenizer(text, 
                    swH.getSWHandlerAccordingToLanguage(LangUtils.getLanguageISOCodeFromString(tweet.getLanguage())));

            //Store tokens of tweet for future use
            docTerms.put(numberOfTweets-1, getStemsAsList(tokens.getCleanTokensAndHashtags(), 
                    Stemmers.getStemmerAccordingToLanguage(LangUtils.getLanguageISOCodeFromString(tweet.getLanguage()))));

            //Iterate through the stemmed clean tokens/hashtags
            for(String token : getStemsAsList(tokens.getCleanTokensAndHashtags(), 
                    Stemmers.getStemmerAccordingToLanguage(LangUtils.getLanguageISOCodeFromString(tweet.getLanguage())))) {

                //Update hashmap
                if(termsWithOccurencies.containsKey(token)) {
                    termsWithOccurencies.put(token, termsWithOccurencies.get(token) + 1);
                } else {
                    termsWithOccurencies.put(token, 1);
                }
            }
            
        }
        this.setNumberOfDocuments(messageDistribution);
        terms = new ArrayList<>(termsWithOccurencies.keySet());
        
        //Generate the list of term IDs
        int termCount = 0;
        for(String term : terms) {
            termIds.put(term, termCount);
            termCount++;
        }
        
        long endTime = System.currentTimeMillis();
        System.out.println(Dataset.class.getName() + " run for " + ((endTime - startTime)/1000) + " seconds");
    }
    
    /**
     * Updates the distribution of incoming messages (tweets).
     * More formally, it calculates the tweets belonging to a certain document. In this iteration, a document
     * is set in a 24-h window, so the method calculates and stores the tweets of a certain day. It then stores
     * this information into a HashMap which its key is the corresponding date and its value is the summary of
     * the messages that have been generated into that day.
     * @param cal A Calendar instance, already set.
     * @param date The date to be checked.
     */
    public final void updateMessageDistribution(Calendar cal, Date date) {
        int day;
        int month;
        cal.setTime(date);
        day = cal.get(Calendar.DAY_OF_MONTH); //Get the current day of month
        month = cal.get(Calendar.MONTH);
        int key = (month * 100) + day; //E.g. for December 6th, the key would be 1106 (11 * 100 + 6) -0 index used
        if(messageDistribution.containsKey(key)) {
            messageDistribution.put(key, messageDistribution.get(key) + 1);
        } else {
            messageDistribution.put(key, 1);
        }
    }
    
    /**
     * Transforms a list of tokens into their stems.
     * @param tokens A String list with the tokens to be transformed.
     * @param stemmer An EnglishStemming handle.
     * @return A String list with the stems of the original terms.
     */
    public final static List<String> getStemsAsList(List<String> tokens, Stemmer stemmer) {
        List<String> stemmedTokens = new ArrayList<>();
        tokens.stream().forEach((token) -> {
            stemmedTokens.add(stemmer.stem(token));
        });
        return stemmedTokens;
    }
    
    /**
     * Returns the terms of the dataset.
     * @return A String list containing the terms of the dataset.
     */
    public List<String> getTerms() { return terms; }
    
    /**
     * Initializes and stores a list containing objects
     * of DocumentTermFrequencyItem class. More formally, each
     * listing in this list contains a triplet with the id of a tweet,
     * the id of the term that the tweet contains and the term's frequency.
     * @param config A configuration object
     */
    public final void setDocTermFreqIdList(Config config) {
        
        try {
            for(int tweetId : docTerms.keySet()) {
                for(String _item : docTerms.get(tweetId)) {
                    DocumentTermFrequencyItem item = new DocumentTermFrequencyItem(tweetId, termIds.get(_item), termsWithOccurencies.get(_item).shortValue());
                    termDocFreqId.add(item);
                }
            }
        } catch(NullPointerException e) {
            System.out.println("");
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
     * Returns the frequencies in all occurring documents of a term.
     * @param term The index of the term in the 'terms' list.
     * @return A Short array containing the frequencies of the term in all documents.
     */
    public Short[] getDocumentsTermFrequency(int term) {
        
        if(!frequencyListContainsValues()) {
            return null;
        }
        
        //Creates a short array which length equals to the number of retrieved tweets
        Short[] frqs = new Short[numberOfTweets];
        
        //Initializes the 'frqs' array with zeros
        IntStream.range(0, numberOfTweets).forEach(i -> frqs[i] = 0);
        if (term != -1)
            
            //For every single element in the list 'termDocFreqId'
            termDocFreqId.stream()
                .filter(dti -> dti.term_id == term) //Filter out irrelevant terms
                .forEach(dti -> frqs[dti.doc_id] = dti.frequency); //Go to the corresponding index in 'frqs' array
                //defined by the id of the document ('doc_id') and store the corresponding frequency that already
                //exists in 'termDocFreqId' array
        return frqs;
    }
    
    /**
     * Sets an integer array of numberOfDocuments size that contains
     * the tweets distribution of the 24-h window.
     * @param numberOfDocuments A HashMap containing the tweets distribution.
     */
    public void setNumberOfDocuments(HashMap<Integer, Integer> distribution) {
        numberOfDocuments = new Integer[distribution.size()];
        int i = 0;
        distribution.keySet().stream().forEach((key) -> {
            numberOfDocuments[i] = distribution.get(key);
        });
    }
    
    /**
     * Returns the number of documents.
     * @return An Integer array containing the number of tweets.
     */
    public Integer[] getNumberOfDocuments() {
        return numberOfDocuments;
    }
}
