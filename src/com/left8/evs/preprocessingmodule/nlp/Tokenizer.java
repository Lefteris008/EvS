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
package com.left8.evs.preprocessingmodule.nlp;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;

import com.left8.evs.preprocessingmodule.nlp.stopwords.StopWords;
import com.left8.evs.utilities.Config;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2016.03.12_1712
 */
public class Tokenizer {
    
    private final List<String> cleanTokens  = new ArrayList<>();
    private final List<String> cleanTokensAndHashtags = new ArrayList<>();
    private final List<String> hashtags = new ArrayList<>();
    private final List<String> nameHandles = new ArrayList<>();
    private final List<String> urls = new ArrayList<>();
    private final List<String> stopWords = new ArrayList<>();
    private final List<String> symbolsAndNonPrintableChars = new ArrayList<>();
    private final Config config;
    public int numberOfTokens;
    
    /**
     * Constructor with minimum parameters. It only tokenizes a given String
     * without removing stopwords, name handles etc.
     * @param config A Config object.
     * @param text The text to be tokenized.
     */
    public Tokenizer(Config config, String text) {
        this.config = config;
        TokenizerFactory<Word> tf = PTBTokenizer.factory();
        List<Word> tokens = tf.getTokenizer(new StringReader(text)).tokenize();
        for(Word token : tokens) {
            cleanTokens.add(token.toString());
        }
//        String[] tokens = text.split(" ");
//        cleanTokens.addAll(Arrays.asList(tokens));
    }
    
    /**
     * Public constructor. It tokenizes a given String and separates hashtags,
     * name handles, URLs and stopwords and stores them into different lists.
     * @param config A Config object.
     * @param text The text to be tokenized.
     * @param sw A StopWords handle.
     */
    public Tokenizer(Config config, String text, StopWords sw) {
        TokenizerFactory<Word> tf = PTBTokenizer.factory();
        
        List<Word> tokens = tf.getTokenizer(new StringReader(text)).tokenize(); 
        this.config = config;
        numberOfTokens = tokens.size();
        tokens.stream().map((word) -> word.toString()).forEach((token) -> {
            
            if(isHashtag(token)) {
                hashtags.add(token);
                cleanTokensAndHashtags.add(token.replace("#", "")); //Remove '#'
            } else if(isNameHandle(token)) {
                nameHandles.add(token.replace("@", "")); //Remove '@' character
            } else if(isURL(token)) {
                urls.add(token);
            } else if(sw.isStopWord(token)) { //Common stopwords
                stopWords.add(token);
            } else if(isCommonSymbol(token)) { //Common symbolsAndNonPrintableChars not caught before
                symbolsAndNonPrintableChars.add(token);
            } else if (sw.isNonPrintableCharacter("\\u" + Integer.toHexString(token.toCharArray()[0]).substring(1))) { //Non printable characters
                symbolsAndNonPrintableChars.add(token);
            } else {
                cleanTokens.add(token);
                cleanTokensAndHashtags.add(token);
            }
        });
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
     * Returns the symbols and non printable characters that are detected in a tweet as separate tokens.
     * @return A list containing only the symbols and non printable characters of a tweet.
     */
    public final List<String> getSymbols() { return symbolsAndNonPrintableChars; }
    
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
        return config.getURLPattern().matcher(token).find();
    }
    
    /**
     * Returns true if the token is a punctuation symbol.
     * @param token A String containing the token to be checked.
     * @return True if the token is a symbol, false otherwise.
     */
    public final boolean isCommonSymbol(String token) {
        return config.getPunctuationPattern().matcher(token).find();
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
