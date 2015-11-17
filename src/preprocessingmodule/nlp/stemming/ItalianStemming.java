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
package preprocessingmodule.nlp.stemming;

import org.tartarus.snowball.ext.ItalianStemmer;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2015.11.17_1555_planet1
 */
public class ItalianStemming implements Stemmer {
    ItalianStemmer itStemmer;

    public ItalianStemming() {
        itStemmer = new ItalianStemmer();
    }
    
    @Override
    public String stem(String word) {
        itStemmer.setCurrent(word);
        if(itStemmer.stem()){
            return itStemmer.getCurrent();
        }else{
            return word;
        }
    }
}
