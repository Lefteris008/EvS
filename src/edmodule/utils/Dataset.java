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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
 * @version 2015.10.29_2049_planet1
 */
public class Dataset {
    
    private static List<String> terms = new ArrayList<>();
    private HashMap<String, Integer> termsWithOccurencies;
    
    /**
     * It generates a working dataset. More formally, the constructor retrieves all tweets from MongoDB
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
                
                //Get the tweet's text and tokenize it
                String text = tweet.getText();
                Tokenizer tokens = new Tokenizer(text, sw);
                
                //Iterate through the clean tokens/hashtags
                tokens.getCleanTokensAndHashtags().stream().forEach((token) -> {
                    
                    //Get its english stem and update hashmap
                    if(termsWithOccurencies.containsKey(engStem.stem(token))) {
                        termsWithOccurencies.put(engStem.stem(token), 1);
                    } else {
                        termsWithOccurencies.put(engStem.stem(token), termsWithOccurencies.get(engStem.stem(token)) + 1);
                    }
                });
            }
        });
        
        terms = new ArrayList<>(termsWithOccurencies.keySet());
    }
    
    /**
     * Returns the terms of the dataset.
     * @return A String list containing the terms of the dataset.
     */
    public List<String> getTerms() { return terms; }
    
    public Short[] getDocumentsTermFrequency(int i) {
        return new Short[0];
    }
    
    public Integer[] getNumberOfDocuments() {
        return new Integer[0];
    }
}
