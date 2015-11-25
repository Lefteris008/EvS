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
package edmodule.utils;

import edmodule.edcow.frequencies.DocumentTermFrequencyItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;
import preprocessingmodule.Config;
import preprocessingmodule.MongoHandler;
import preprocessingmodule.Tweet;
import preprocessingmodule.language.LangUtils;
import preprocessingmodule.nlp.Tokenizer;
import preprocessingmodule.nlp.stemming.Stemmer;
import preprocessingmodule.nlp.stopwords.StopWords;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2015.11.25_2348_planet2
 */
public class Dataset {
    
    private static List<String> terms = new ArrayList<>(); //List containing all occuring terms
    private final HashMap<String, Integer> termsWithOccurencies = new HashMap<>(); //A map containing terms along with their frequency of occurance
    private int numberOfTweets;
    private final List<DocumentTermFrequencyItem> termDocFreqId = new ArrayList<>(); //A list containg triplets of tweetIDs, termIDs and their frequencies
    private final HashMap<Integer, List<String>> docTerms = new HashMap<>(); //A map containing the tweetIDs and a list of the terms that each one of them includes
    private final HashMap<String, Integer> termIds = new HashMap<>(); //A map containing the ids of the terms (namely, their index as they are being read)
    
    
    /**
     * It creates a working dataset. More formally, the constructor retrieves all tweets from MongoDB
     * store, iterates through them, tokenizes the text of every single one of them, generates the English
     * stem of every token of them and updates a hashmap that contains terms along with their occurencies.
     * @param config A Configuration object.
     * @param sw A StopWords handler
     */
    public Dataset(Config config, StopWords sw) {
        
        MongoHandler mongo = new MongoHandler(config);
        
        //Initialize stopwords and stemmers
        StopWordsHandlers.initStopWordsHandlers();
        Stemmers.initStemmers();
        
        //Load all tweets from MongoDB Store
        List<Tweet> tweets = mongo.retrieveAllTweetsFromMongoDBStore(config);
        
        //Iterate through all tweets
        for(Tweet tweet : tweets) {            
            numberOfTweets++; //Count it

            //Get the tweet's text and tokenize it
            String text = tweet.getText();
            Tokenizer tokens = new Tokenizer(text, 
                    StopWordsHandlers.getSWHandlerAccordingToLanguage(LangUtils.getLanguageISOCodeFromString(tweet.getLanguage())));

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
        terms = new ArrayList<>(termsWithOccurencies.keySet());
        
        //Generate the list of term IDs
        int termCount = 0;
        for(String term : terms) {
            termIds.put(term, termCount);
            termCount++;
        }
        
        mongo.closeMongoConnection(config);
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
     * Returns the number of documents.
     * @return An Integer array containing the number of tweets.
     */
    public Integer[] getNumberOfDocuments() {
        Integer[] numOfDocuments = new Integer[1];
        numOfDocuments[0] = numberOfTweets;
        return numOfDocuments;
    }
}
