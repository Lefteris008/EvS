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

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2015.11.02_1609_planet1
 */
public class Tokenizer {
    
    /**
     * Tokenizes a given String phrase into the words that it consists of.
     * @param phrase The String phrase that is going to be tokenized
     * @return A String list with the tokens of the initial phrase
     */
    public static List<String> tokenize(String phrase) {
        String[] temp;
        temp = phrase.split(" ");
        
        ///Code for stopwords and other optimizations
        
        return Arrays.asList(temp);
    }
    
}
