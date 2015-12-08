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
import java.util.HashSet;
import java.util.Set;
import preprocessingmodule.language.LangUtils;
import preprocessingmodule.nlp.Tokenizer;
import preprocessingmodule.nlp.stemming.Stemmer;
import utilities.Utils;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2015.12.07_1939_planet3
 */
public final class Dataset {
    
    private int numberOfTweets;
    private final StopWordsHandlers swH;
    private Integer[] numberOfDocuments;
    private List<String> terms = new ArrayList<>(); //List containing all occuring termsprivate final HashMap<String, Integer> termsMap = new HashMap<>(); //A map containing terms along with their frequency of occurance
    private final List<DocumentTermFrequencyItem> termDocFreqId = new ArrayList<>(); //A list containg triplets of tweetIDs, termIDs and their frequenciesprivate final HashMap<Integer, List<String>> docTerms = new HashMap<>(); //A map containing the tweetIDs and a list of the terms that each one of them includes
    private final List<Tweet> tweets;
    private final HashMap<Integer, HashMap<String, Integer>> termsDocsWithOccurencies = new HashMap<>();
    private final HashMap<String, Integer> termIds = new HashMap<>(); //A map containing the ids of the terms (namely, their index as they are being read)
    private final HashMap<Integer, Integer> messageDistribution = new HashMap<>();
    private final HashMap<Integer, Integer> documentIndices = new HashMap<>();

    /**
     * It retrieves a dataset from the already stored MongoDB collection.
     * @param config A Configuration object.
     * @param sw A StopWords handler
     */
    public Dataset(Config config) {
        long startTime = System.currentTimeMillis(); //Start time
        
        MongoHandler mongo = new MongoHandler(config);
        mongo.connectToMongoDB(config);
 
        //Initialize stopwords and stemmers
        swH = new StopWordsHandlers(config);
        Stemmers.initStemmers();
        
        //Load all tweets from MongoDB Store
        tweets = mongo.retrieveAllTweetsFromMongoDBStore(config);
        mongo.closeMongoConnection(config);
 
        long endTime = System.currentTimeMillis();
        Utils.printExecutionTime(startTime, endTime, Dataset.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName());
    }
    
    /**
     * Creates a working corpus from a loaded dataset.
     * More formally, it iterates through the tweets retrieved, tokenizes their text,
     * gets the stems of every single token and updates various HashMaps and fields for
     * future use.
     */
    public final void createCorpus() {
        long startTime = System.currentTimeMillis();
        
        Calendar cal;
        int docKey;
        int termCount = 0;
        int documentCount = 0;        
        
        //Set the calendar instance
        cal = Calendar.getInstance();

        for(Tweet tweet : tweets) {            
            
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
            Tokenizer tokens = new Tokenizer(text, 
                    swH.getSWHandlerAccordingToLanguage(LangUtils.getLanguageISOCodeFromString(tweet.getLanguage())));

            //Iterate through the stemmed clean tokens/hashtags
            for(String token : getStemsAsList(tokens.getCleanTokensAndHashtags(), 
                    Stemmers.getStemmerAccordingToLanguage(LangUtils.getLanguageISOCodeFromString(tweet.getLanguage())))) {

                //Update the HashMap with the triplet Document, Token, Frequency
                if(termsDocsWithOccurencies.containsKey(docKey)) { //Document already exists, update it
                    HashMap<String, Integer> termsWithOccurencies = termsDocsWithOccurencies.get(docKey);
                    if(termsWithOccurencies.containsKey(token)) { //Token already exists, update it
                        termsWithOccurencies.put(token, termsWithOccurencies.get(token) + 1);
                    } else { //Token does not exist, put it
                        termsWithOccurencies.put(token, 1);
                    }
                    termsDocsWithOccurencies.put(docKey, termsWithOccurencies); //Store inner HashMap
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
        setNumberOfDocuments(messageDistribution);
        
        long endTime = System.currentTimeMillis();
        Utils.printExecutionTime(startTime, endTime, Dataset.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName());
    }
    
    /**
     * Updates the distribution of incoming messages (tweets).
     * More formally, it calculates the tweets belonging to a certain document. In this iteration, a document
     * is set in a 24-h window, so the method calculates and stores the tweets of a certain day. It then stores
     * this information into a HashMap which its docKey is the corresponding date and its value is the summary of
     * the messages that have been generated into that day.
     * @param cal A Calendar instance, already set.
     * @param date The date to be checked.
     * @return The key of the document, for HashMap storage. The key is in the form of 'mmDD', e.g.
     * for December 26th it will be '1226' but for January 1st it will be '101'.
     */
    public final int updateMessageDistribution(Calendar cal, Date date) {
        int day;
        int month;
        cal.setTime(date);
        day = cal.get(Calendar.DAY_OF_MONTH); //Get the current day of month
        month = cal.get(Calendar.MONTH) + 1; //Zero-index based
        int key = (month * 100) + day; //E.g. for December 6th, the docKey would be 1206 (12 * 100 + 6)
        if(messageDistribution.containsKey(key)) {
            messageDistribution.put(key, messageDistribution.get(key) + 1);
        } else {
            messageDistribution.put(key, 1);
        }
        return key;
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
     * @return A String list containing the unique terms of the dataset.
     */
    public List<String> getTerms() { 
        Set<String> termsSet = new HashSet<>();
        for(int docKey : termsDocsWithOccurencies.keySet()) {
            termsSet = termsDocsWithOccurencies.get(docKey).keySet();
        }
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
        short frequency;
        for(int docKey : termsDocsWithOccurencies.keySet()) {
            documentID = documentIndices.get(docKey); //Get the actual ID
            for(String token : termsDocsWithOccurencies.get(docKey).keySet()) {
                termID = termIds.get(token);
                frequency = termsDocsWithOccurencies.get(docKey).get(token).shortValue();
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
     * @return A Short array containing the frequencies of the term in all documents.
     */
    public Short[] getDocumentsTermFrequency(int term) {
        
        if(!frequencyListContainsValues()) {
            return null;
        }
        
        //Creates a short array which equals the number of documents
        Short[] frqs = new Short[numberOfDocuments.length];
        
        //Initializes the 'frqs' array with zeros
        IntStream.range(0, numberOfDocuments.length).forEach(i -> frqs[i] = 0);
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
        for(int key : distribution.keySet()) {
            numberOfDocuments[i] = distribution.get(key);
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
}
