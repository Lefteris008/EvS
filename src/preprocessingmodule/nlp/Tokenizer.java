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

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2015.11.17_1524_planet1
 */
public class Tokenizer {
    
    /**
     * Tokenizes a given String phrase into the words that it consists of.
     * This method also filters out URLs, hashtags and name handles that the tweet might contains.
     * @param text The String phrase that is going to be tokenized
     * @return A String list with the clean tokens of the initial phrase -excluding the URLs, hashtags and name handles
     */
    public static List<String> getCleanTokensFromText(String text) {        
        
        String[] temp = text.split(" ");
        List<String> cleanTokens = new ArrayList<>();
        
        for (String temp1 : temp) {
            if(!isHashtag(temp1) && !isURL(temp1) && !isNameHandle(temp1)) { //Filter out hashtags, URLs and name handles
                cleanTokens.add(temp1);
            }
        }
        return cleanTokens;
    }
    
    /**
     * Extracts the hashtags of a given tweet.
     * @param text The actual tweet.
     * @return A list containing only the hashtags of the tweet, without the '#' character.
     */
    public static final List<String> getHashtags(String text) {
        
        String[] temp = text.split(" ");
        List<String> hashtags = new ArrayList<>();
        
        for(String temp1 : temp) {
            if(isHashtag(temp1)) {
                hashtags.add(temp1.replace("#", "")); //Add the hashtag, excluding the '#' character
            }
        }
        return hashtags;
    }
    
    /**
     * Extracts the name handles of a given tweet.
     * @param text The actual tweet.
     * @return A list containing only the name handles of the tweet, without the '@' character.
     */
    public static final List<String> getNameHandles(String text) {
        String[] temp = text.split(" ");
        List<String> nameHandles = new ArrayList<>();
        
        for(String temp1 : temp) {
            if(isNameHandle(temp1)) {
                nameHandles.add(temp1.replace("@", "")); //Add the name handle, excluding the '@' character
            }
        }
        return nameHandles;
    }
    
    /**
     * Extracts the URLs of a given tweet.
     * @param text The actual tweet.
     * @return A list containing only the URLs of the tweet.
     */
    public static final List<String> getURLs(String text) {
        String[] temp = text.split(" ");
        List<String> urls = new ArrayList<>();
        
        for(String temp1 : temp) {
            if(isURL(temp1)) {
                urls.add(temp1);
            }
        }
        return urls;
    }
    
    /**
     * Returns true if the token is a hashtag. 
     * @param token A String containing the token to be checked.
     * @return True if the token is a hashtag, false otherwise.
     */
    public static final boolean isHashtag(String token) {
        return token.startsWith("#");
    }
    
    /**
     * Returns true if the token is a name handle.
     * @param token A String containing the token to be checked.
     * @return True if the token is a name handle, false otherwise.
     */
    public static final boolean isNameHandle(String token) {
        return token.startsWith("@");
    }
    
    /**
     * Returns true if the token is a URL.
     * @param token A String containing the token to be checked.
     * @return True if the token is a URL, false otherwise.
     */
    public static final boolean isURL(String token) {
        
        Pattern pattern = Pattern.compile("(@)?(href=')?(HREF=')?(HREF=\")?(href=\")?(http://)?[a-zA-Z_0-9\\-]+(\\.\\w[a-zA-Z_0-9\\-]+)+(/[#&\\n\\-=?\\+\\%/\\.\\w]+)?");
        Matcher matcher = pattern.matcher(token);
        
        return matcher.matches();
    }
}
