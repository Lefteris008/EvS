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
import preprocessingmodule.Utils;
import preprocessingmodule.nlp.Tokenizer;
import preprocessingmodule.nlp.stemming.EnglishStemming;
import preprocessingmodule.nlp.stopwords.StopWords;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2015.11.22_1829_planet2
 */
public class Dataset {
    
    private static List<String> terms = new ArrayList<>(); //List containing all occuring terms
    private HashMap<String, Integer> termsWithOccurencies; //A map containing terms along with their frequency of occurance
    private int numberOfTweets;
    private List<DocumentTermFrequencyItem> termDocFreqId; //A list containg triplets of tweetIDs, termIDs and their frequencies
    private HashMap<Integer, List<String>> docTerms; //A map containing the tweetIDs and a list of the terms that each one of them includes
    private HashMap<String, Integer> termIds; //A map containing the ids of the terms (namely, their index as they are being read)
    
    /**
     * It creates a working dataset. More formally, the constructor retrieves all tweets from MongoDB
     * store, iterates through them, tokenizes the text of every single one of them, generates the English
     * stem of every token of them and updates a hashmap that contains terms along with their occurencies.
     * @param config A Configuration object.
     * @param sw A StopWords handler
     */
    public Dataset(Config config, StopWords sw) {
        MongoHandler mongo = new MongoHandler(config);
        
        //Load the tweet IDs
        List<String> tweetIDs = new ArrayList<>();
        tweetIDs.addAll(Utils.extractTweetIDsFromFile(config, "fa_cup"));
        tweetIDs.addAll(Utils.extractTweetIDsFromFile(config, "super_tuesday"));
        tweetIDs.addAll(Utils.extractTweetIDsFromFile(config, "us_elections"));
        
        //Create an English Stemmer
        EnglishStemming engStem = new EnglishStemming();
        
        //Iterate through all tweets
        tweetIDs.stream().forEach((id) -> {
            
            //Retrieve the tweet from MongoDB Store
            Tweet tweet = mongo.retrieveTweetFromMongoDBStore(config, id);
            
            if(tweet != null) { //If the tweet exists
                
                numberOfTweets++; //Count it
                
                //Get the tweet's text and tokenize it
                String text = tweet.getText();
                Tokenizer tokens = new Tokenizer(text, sw);
                
                //Store tokens of tweet for future use
                docTerms.put(numberOfTweets-1, tokens.getCleanTokensAndHashtags());
                
                //Iterate through the clean tokens/hashtags
                tokens.getCleanTokensAndHashtags().stream().forEach((token) -> {
                    
                    //Get the token's english stem and update hashmap
                    if(termsWithOccurencies.containsKey(engStem.stem(token))) {
                        termsWithOccurencies.put(engStem.stem(token), termsWithOccurencies.get(engStem.stem(token)) + 1);
                    } else {
                        termsWithOccurencies.put(engStem.stem(token), 1);
                    }
                });
            }
        });
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
        
        docTerms.keySet().stream().forEach((tweetId) -> {
            docTerms.get(tweetId).stream().forEach((_item) -> {
                DocumentTermFrequencyItem item = new DocumentTermFrequencyItem(tweetId, termIds.get(_item), termsWithOccurencies.get(_item).shortValue());
                termDocFreqId.add(item);
            });
        });
        
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
