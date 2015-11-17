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

import org.apache.lucene.analysis.el.GreekStemmer;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2015.11.17_1548_planet1
 */
public class GreekStemming implements Stemmer {
    GreekStemmer grStemmer;

    public GreekStemming() {
        this.grStemmer = new GreekStemmer();
    }

    @Override
    public String stem(String word) {
        char[] wordArray = word.toCharArray();
        return new String(wordArray, 0, grStemmer.stem(wordArray, wordArray.length));
    }
}
