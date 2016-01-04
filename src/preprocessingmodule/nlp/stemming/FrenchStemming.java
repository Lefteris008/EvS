/*
 * Copyright (C) 2016 Adrien Guille <adrien.guille@univ-lyon2.fr>, Lefteris Paraskevas
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

import org.tartarus.snowball.ext.FrenchStemmer;

/**
 *
 * @author  Adrien GUILLE, Laboratoire ERIC, Université Lumière Lyon 2
 * @author  Lefteris Paraskevas
 * @version 2015.11.17_1553_planet1
 */
public class FrenchStemming implements Stemmer {
    FrenchStemmer frStemmer;
    
    public FrenchStemming(){
        frStemmer = new FrenchStemmer();
    }
    
    @Override
    public String stem(String word) {
        frStemmer.setCurrent(word);
        if(frStemmer.stem()){
            return frStemmer.getCurrent();
        }else{
            return word;
        }
    }
}
