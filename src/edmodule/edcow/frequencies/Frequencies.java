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
package edmodule.edcow.frequencies;

import java.util.HashMap;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2015.11.02_1617_planet1
 */
public class Frequencies {
    
    public HashMap<String, Integer> frequencies;
    
    public Frequencies() {
        frequencies = new HashMap<>();
    }
    
    /**
     * Updates the HashMap that contains the frequencies of the terms.
     * If the HashMap contains the lemma, it increases its frequency by 1.
     * If it doesn't, it creates a new key-pair value, with the frequency set to 1.
     * @param lemma The lemma for which the frequencies are going to be updated
     */
    public void updateFrequency(String lemma) {
        if(frequencies.containsKey(lemma)) {
            frequencies.put(lemma, frequencies.get(lemma) + 1);
        } else {
            frequencies.put(lemma, 1);
        }
    }
    
    public int getTermFrequency(String lemma) {
        return frequencies.get(lemma);
    }
    
}
