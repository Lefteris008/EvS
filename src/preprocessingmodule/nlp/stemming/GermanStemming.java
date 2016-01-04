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

import org.tartarus.snowball.ext.GermanStemmer;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2015.11.17_1557_planet1
 */
public class GermanStemming implements Stemmer {
    GermanStemmer deStemmer;

    public GermanStemming() {
        deStemmer = new GermanStemmer();
    }
    
    @Override
    public String stem(String word) {
        deStemmer.setCurrent(word);
        if(deStemmer.stem()){
            return deStemmer.getCurrent();
        }else{
            return word;
        }
    }
}
