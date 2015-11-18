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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import preprocessingmodule.nlp.stopwords.StopWords;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2015.11.18_2017_planet1
 */
public class Tokenizer {
    
    private static final List<String> cleanTokens  = new ArrayList<>();
    private static final List<String> hashtags = new ArrayList<>();
    private static final List<String> nameHandles = new ArrayList<>();
    private static final List<String> urlsAndNumberAbbreviations = new ArrayList<>();
    private static final List<String> stopWords = new ArrayList<>();
    public static int numberOfTokens;
    
    /**
     * Public constructor. It tokenizes a given String and separates hashtags,
     * name handles, URLs and stopwords and stores them into different lists.
     * @param text The text to be tokenized.
     * @param sw A StopWords handle.
     */
    public Tokenizer(String text, StopWords sw) {
        String[] temp = text.split(" "); //Split them according to white spaces
        for (String temp1 : temp) {
            if(isHashtag(temp1)) {
                hashtags.add(temp1.replace("#", "")); //Remove '#' character
            } else if(isNameHandle(temp1)) {
                nameHandles.add(temp1.replace("@", "")); //Remove '@' character
            } else if(isURLOrNumberAbbreviation(temp1)) {
                urlsAndNumberAbbreviations.add(temp1);
            } else if(sw.isStopWord(temp1)) {
                stopWords.add(temp1);
            } else {
                cleanTokens.add(temp1);
            }
            numberOfTokens++;
        }
    }
    
    /**
     * Returns the clean tokens of the phrase.
     * @return A String list with the clean tokens of the initial phrase -excluding the URLs, hashtags and name handles
     */
    public static List<String> getCleanTokensFromText() { return cleanTokens; }
    
    /**
     * Returns the stopwords of the phrase.
     * @return A list containing only the stopwords of the original phrase.
     */
    public static final List<String> getStopWordsFromText() { return stopWords; }
    
    /**
     * Returns the hashtags of the phrase.
     * @return A list containing only the hashtags of the tweet, without the '#' character.
     */
    public static final List<String> getHashtags() { return hashtags; }
    
    /**
     * Returns the name handles of the phrase.
     * @return A list containing only the name handles of the tweet, without the '@' character.
     */
    public static final List<String> getNameHandles() { return nameHandles; }
    
    /**
     * Returns the URLs and the number abbreviations (e.g. current time) of the phrase.
     * @return A list containing only the URLs of the tweet.
     */
    public final List<String> getURLsAndNumberAbbreviation() { return urlsAndNumberAbbreviations; }
    
    /**
     * Returns true if the token is a hashtag. 
     * @param token A String containing the token to be checked.
     * @return True if the token is a hashtag, false otherwise.
     */
    public final static boolean isHashtag(String token) {
        return token.startsWith("#");
    }
    
    /**
     * Returns true if the token is a name handle.
     * @param token A String containing the token to be checked.
     * @return True if the token is a name handle, false otherwise.
     */
    public final static boolean isNameHandle(String token) {
        return token.startsWith("@");
    }
    
    /**
     * Returns true if the token is a URL or a number abbreviation (e.g. current time).
     * @param token A String containing the token to be checked.
     * @return True if the token is a URL, false otherwise.
     */
    public final static boolean isURLOrNumberAbbreviation(String token) {
        
        Pattern pattern = Pattern.compile("(@)?(href=')?(HREF=')?(HREF=\")?(href=\")?(http://)?[a-zA-Z_0-9\\-]+(\\.\\w[a-zA-Z_0-9\\-]+)+(/[#&\\n\\-=?\\+\\%/\\.\\w]+)?");
        Matcher matcher = pattern.matcher(token);
        
        return matcher.matches();
    }
    
    /**
     * Tokenizes given phrase and prints results.
     */
    public final void textTokenizingTester() {
        System.out.println("Given text contains " + numberOfTokens + " tokens of which:");
        
        System.out.println("\n" + getCleanTokensFromText().size() + " are clean tokens. Printing them now...");
        getCleanTokensFromText().stream().forEach((token) -> {
            System.out.println("\t" + token);
        });
        
        System.out.println("\n" + getHashtags().size() + " are hashtags. Printing them now...");
        getHashtags().stream().forEach((token) -> {
            System.out.println("\t" + token);
        });
        
        System.out.println("\n" + getNameHandles().size() + " are name handles. Printing them now...");
        getNameHandles().stream().forEach((token) -> {
            System.out.println("\t" + token);
        });
        
        System.out.println("\n" + getURLsAndNumberAbbreviation().size() + " are URLs. Printing them now...");
        getURLsAndNumberAbbreviation().stream().forEach((token) -> {
            System.out.println("\t" + token);
        });
        
        System.out.println("\n" + getStopWordsFromText().size() + " are stopwords. Printing them now...");
        getStopWordsFromText().stream().forEach((token) -> {
            System.out.println("\t" + token);
        });
    }
}
