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
package preprocessingmodule.nlp;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import preprocessingmodule.nlp.stopwords.StopWords;
import utilities.Config;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2015.12.16_2101_planet3
 */
public class Tokenizer {
    
    private final List<String> cleanTokens  = new ArrayList<>();
    private final List<String> cleanTokensAndHashtags = new ArrayList<>();
    private final List<String> hashtags = new ArrayList<>();
    private final List<String> nameHandles = new ArrayList<>();
    private final List<String> urls = new ArrayList<>();
    private final List<String> stopWords = new ArrayList<>();
    private final Config config;
    public int numberOfTokens;
    
    /**
     * Public constructor. It tokenizes a given String and separates hashtags,
     * name handles, URLs and stopwords and stores them into different lists.
     * @param text The text to be tokenized.
     * @param sw A StopWords handle.
     */
    public Tokenizer(Config config, String text, StopWords sw) {
        this.config = config;
        String[] temp = text.split(" "); //Split them according to white spaces
        numberOfTokens = temp.length;
        for (String temp1 : temp) {
            if(isHashtag(temp1)) {
                hashtags.add(temp1.replace("#", "")); //Remove '#' character and all other punctuation
                cleanTokensAndHashtags.add(temp1.replace("#", ""));
            } else if(isNameHandle(temp1)) {
                nameHandles.add(temp1.replace("@", "")); //Remove '@' character
            } else if(isURL(temp1)) {
                urls.add(temp1);
            } else if(sw.isStopWord(temp1)) {
                stopWords.add(temp1);
            } else {
                //Remove punctuation that was bound to the token and then store it
                String[] temp_ = removeMissingPunctuationAndSeparate(temp1);
                cleanTokens.addAll(Arrays.asList(temp_));
                cleanTokensAndHashtags.addAll(Arrays.asList(temp_));
                numberOfTokens += temp_.length - 1; //Entire listing already add initially
            }
        }
    }
    
    /**
     * Removes punctuation bound to an input word.
     * @param word The input word.
     * @return A String with punctuation omitted.
     */
    public final String removeMissingPunctuation(String word) {
        return config.getPunctuationPattern().matcher(word).replaceAll("");
    }
    
    /**
     * Removes punctuation bound to an input word and separates using white space.
     * @param word The input word.
     * @return A String array with punctuation omitted. Since the input word is
     * split in the spot where the original punctuation was located, an array of
     * at least two elements is returned.
     */
    public final String[] removeMissingPunctuationAndSeparate(String word) {
        return config.getPunctuationPattern().matcher(word).replaceAll(" ").split(" ");
    }
    
    /**
     * Returns the clean tokens of the phrase.
     * @return A String list with the clean tokens of the initial phrase -excluding the URLs, number abbreviations, hashtags and name handles
     */
    public List<String> getCleanTokens() { return cleanTokens; }
    
    /**
     * Returns the clean tokens and the hashtags of the phrase.
     * @return A String list with the clean tokens of the initial phrase and its hashtags -excluding the URLs, number abbreviations and name handles.
     */
    public List<String> getCleanTokensAndHashtags() { return cleanTokensAndHashtags; }
    
    /**
     * Returns the stopwords of the phrase.
     * @return A list containing only the stopwords of the original phrase.
     */
    public final List<String> getStopWords() { return stopWords; }
    
    /**
     * Returns the hashtags of the phrase.
     * @return A list containing only the hashtags of the tweet, without the '#' character.
     */
    public final List<String> getHashtags() { return hashtags; }
    
    /**
     * Returns the name handles of the phrase.
     * @return A list containing only the name handles of the tweet, without the '@' character.
     */
    public final List<String> getNameHandles() { return nameHandles; }
    
    /**
     * Returns the URLs and the number abbreviations (e.g. current time) of the phrase.
     * @return A list containing only the URLs of the tweet.
     */
    public final List<String> getURLs() { return urls; }
    
    /**
     * Returns true if the token is a hashtag. 
     * @param token A String containing the token to be checked.
     * @return True if the token is a hashtag, false otherwise.
     */
    public final boolean isHashtag(String token) {
        return token.startsWith("#");
    }
    
    /**
     * Returns true if the token is a name handle.
     * @param token A String containing the token to be checked.
     * @return True if the token is a name handle, false otherwise.
     */
    public final boolean isNameHandle(String token) {
        return token.startsWith("@");
    }
    
    /**
     * Returns true if the token is a URL.
     * @param token A String containing the token to be checked.
     * @return True if the token is a URL, false otherwise.
     */
    public final boolean isURL(String token) {
        return config.getURLPattern().matcher(token).matches();
    }
    
    /**
     * Tokenizes given phrase and prints results.
     */
    public final void textTokenizingTester() {
        System.out.println("Given text contains " + numberOfTokens + " tokens of which:");
        
        System.out.println("\n" + getCleanTokens().size() + (getCleanTokens().size() == 1 ? " is clean token. Printing it now..." : " are clean tokens. Printing them now..."));
        getCleanTokens().stream().forEach((token) -> {
            System.out.println("\t" + token);
        });
        
        System.out.println("\n" + getHashtags().size() + (getHashtags().size() == 1 ? " is hashtag. Printing it now..." : " are hashtags. Printing them now..."));
        getHashtags().stream().forEach((token) -> {
            System.out.println("\t" + token);
        });
        
        System.out.println("\n" + getNameHandles().size() + (getNameHandles().size() == 1 ? " is name handle. Printing it now..." : " are name handles. Printing them now..."));
        getNameHandles().stream().forEach((token) -> {
            System.out.println("\t" + token);
        });
        
        System.out.println("\n" + getURLs().size() + (getURLs().size() == 1 ? " is URL. Printing it now..." : " are URLs. Printing them now..."));
        getURLs().stream().forEach((token) -> {
            System.out.println("\t" + token);
        });
        
        System.out.println("\n" + getStopWords().size() + (getStopWords().size() == 1 ? " is stopword. Printing it now..." : " are stopwords. Printintg them now..."));
        getStopWords().stream().forEach((token) -> {
            System.out.println("\t" + token);
        });
    }
}
