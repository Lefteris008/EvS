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
package preprocessingmodule.nlp.stemming;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2016.01.20_1828_gargantua
 */
public class StemUtils {
    
    private final HashMap<String, String> stemsMap = new HashMap<>();
    
    /**
     * Transforms a list of tokens into their stems.
     * @param tokens A String list with the tokens to be transformed.
     * @param stemmer An EnglishStemming handle.
     * @return A String list with the stems of the original terms.
     */
    public final List<String> getStemsAsList(List<String> tokens, Stemmer stemmer) {
        List<String> stemmedTokens = new ArrayList<>();
        tokens.stream().forEach((token) -> {
            stemmedTokens.add(stemmer.stem(token));
            stemsMap.put(stemmer.stem(token), token);
        });
        return stemmedTokens;
    }
    
    /**
     * Returns the stems/original words map.
     * @return A HashMap containing pairs of stems, original words.
     */
    public final HashMap<String, String> getStemsMap() { return stemsMap; }
    
    /**
     * Auxiliary method to retrieve original words from its stem.
     * @param stem The stem for which we want the original word.
     * @return A String with the original word.
     */
    public final String getOriginalWord(String stem) {
        if(stemsMap.containsKey(stem)) {
            return stemsMap.get(stem);
        } else {
            return null;
        }
    }
 }
