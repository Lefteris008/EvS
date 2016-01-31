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
package edmodule.peakfinding;

import dsretriever.Tweet;
import edmodule.data.PeakFindingCorpus;
import edmodule.utils.Stemmers;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import preprocessingmodule.language.LangUtils;
import preprocessingmodule.nlp.Tokenizer;
import preprocessingmodule.nlp.stemming.StemUtils;
import utilities.Utilities;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2016.01.31_1921
 */
public class PeakFindingEvent {
    
    private final int id;
    private final Window<Integer, Integer> window;
    private final List<Tweet> tweets;
    private final List<String> commonTerms = new ArrayList<>();
    private final PeakFindingCorpus corpus;
    
    /**
     * Public constructor. It creates an event.
     * @param id A String with the id of the event.
     * @param window A Window object with the window of the event.
     * @param tweets A List of String containing the corresponding tweets of the
     * event.
     * @param corpus A PeakFindingCorpus object.
     */
    public PeakFindingEvent(int id, Window<Integer, Integer> window, List<Tweet> tweets, PeakFindingCorpus corpus) {
        this.id = id;
        this.window = window;
        this.tweets = new ArrayList<>(tweets);
        this.corpus = corpus;
        generateCommonTerms();
    }
    
    /**
     * Get the name of the event.
     * @return A String with event's name.
     */
    public final int getID() { return id; }
    
    /**
     * Get the window of the event.
     * @return Returns a Window object with the event's window.
     */
    public final Window<Integer, Integer> getWindow() { return window; }
    
    /**
     * Get the tweets of the event.
     * @return A List of String with the event's tweets.
     */
    public final List<Tweet> getTweets() { return tweets; }
    
    /**
     * Generates a List with the most common terms of the tweets that belong
     * to the specific event. <br/>
     * More formally, it parses every single tweet of the event, tokenizes it
     * and stores the terms in a HashMap with their respective occurencies as
     * values.
     */
    private void generateCommonTerms() {
        HashMap<String, Integer> unsortedTokens = new HashMap<>();
        StemUtils stemHandler = new StemUtils();
        tweets.stream().forEach((tweet) -> {
            String text = tweet.getText();
            Tokenizer tokens = new Tokenizer(corpus.getConfigHandler(), text, 
                    corpus.getStopWordsHandlers().getSWHandlerAccordingToLanguage
                            (LangUtils.getLanguageISOCodeFromString(tweet.getLanguage())));
            stemHandler.getStemsAsList(tokens.getCleanTokensAndHashtags(),
                    Stemmers.getStemmerAccordingToLanguage(
                            LangUtils.getLanguageISOCodeFromString(tweet.getLanguage()))).stream().forEach((token) -> {
                                if(unsortedTokens.containsKey(token)) {
                                    unsortedTokens.put(token, unsortedTokens.get(token) + 1); //Count it
                                } else {
                                    unsortedTokens.put(token, 1);
                                }
            });
        });
        sortMapByValue(unsortedTokens, stemHandler);
    }
    
    /**
     * Returns the five most common terms of the tweets that belong to this event.
     * @return A List of Strings with the most common terms.
     * @see generateCommonTerms generateCommonTerms() method.
     */
    public final List<String> getCommonTerms() {
        if(!commonTerms.isEmpty()) {
            return commonTerms;
        } else {
            Utilities.printMessageln("No common terms have been calculated yet!");
            Utilities.printMessageln("Run " + PeakFindingEvent.class + "." + "generateCommonTerms() method first.");
            return null;
        }
    }
    
    /**
     * Returns the five most common terms as a single String.
     * @return A String containing the five most common terms.
     * @see getCommonTerms() getCommonTerms() method.
     */
    public final String getCommonTermsAsString() {
        if(commonTerms.isEmpty()) {
            Utilities.printMessageln("No common terms have been calculated yet!");
            Utilities.printMessageln("Run " + PeakFindingEvent.class + "." + "generateCommonTerms() method first.");
            return null;
        }
        String commonTermsString = "";
        for(String term : commonTerms) {
            commonTermsString = commonTermsString + term + " ";
        }
        return commonTermsString;
    }
    
    /**
     * Auxiliary method to sort a Map by value.
     * @param unsortedMap The Map to be sorted.
     * @return A sorted List of the String keys.
     */
    public final void sortMapByValue(HashMap<String, Integer> unsortedMap, StemUtils stemHandler) {
        //Initialize variables
        Entry<String,Integer> entry;
        String currentKey;
        int currentValue;
        
        //Get the 5 greatest tokens by value
        //If the HashMap has less than 5 elements, just sort them
        int size = (unsortedMap.keySet().size() < 5 ? unsortedMap.size() : 5);
        for(int i = 0; i < size; i++) {
            entry = unsortedMap.entrySet().iterator().next();
            currentKey = entry.getKey();
            currentValue = entry.getValue();
            for(String key : unsortedMap.keySet()) {
                if(unsortedMap.get(key) > currentValue) {
                    currentValue = unsortedMap.get(key);
                    currentKey = key;
                }
            }
            commonTerms.add(stemHandler.getOriginalWord(currentKey));
            unsortedMap.remove(currentKey);
        }
    }
    
    /**
     * Prints a specific event along with its tweets.
     */
    public final void printEvent() {
        Utilities.printMessageln("Event '" + getID() + "' contains the following common terms:");
        Utilities.printMessageln(getCommonTermsAsString());
    }
}
