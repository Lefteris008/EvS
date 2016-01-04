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
package edmodule.utils;

import preprocessingmodule.language.LanguageCodes;
import preprocessingmodule.nlp.stemming.ArabicStemming;
import preprocessingmodule.nlp.stemming.EnglishStemming;
import preprocessingmodule.nlp.stemming.FrenchStemming;
import preprocessingmodule.nlp.stemming.GermanStemming;
import preprocessingmodule.nlp.stemming.GreekStemming;
import preprocessingmodule.nlp.stemming.ItalianStemming;
import preprocessingmodule.nlp.stemming.PersianStemming;
import preprocessingmodule.nlp.stemming.SpanishStemming;
import preprocessingmodule.nlp.stemming.Stemmer;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2015.11.25_2335_planet2
 */
public class Stemmers {
    
    private static ArabicStemming arStem;
    private static EnglishStemming enStem;
    private static FrenchStemming frStem;
    private static GermanStemming deStem;
    private static GreekStemming grStem;
    private static ItalianStemming itStem;
    private static PersianStemming faStem;
    private static SpanishStemming esStem;
    
    /**
     * Initialize Stemmers
     */
    public static void initStemmers() {
        arStem = new ArabicStemming();
        enStem = new EnglishStemming();
        frStem = new FrenchStemming();
        deStem = new GermanStemming();
        grStem = new GreekStemming();
        itStem = new ItalianStemming();
        faStem = new PersianStemming();
        esStem = new SpanishStemming();
    }
    
    /**
     * Returns a Stemmer according to a given language.
     * @param isoCode A LanguageCodes enumeration containing the ISO code of the text's language.
     * @return A Stemmer
     */
    public static Stemmer getStemmerAccordingToLanguage(LanguageCodes isoCode) {
        if(isoCode.equals(LanguageCodes.ar)) {
            return arStem;
        } else if(isoCode.equals(LanguageCodes.de)) {
            return deStem;
        } else if(isoCode.equals(LanguageCodes.en)) {
            return enStem;
        } else if(isoCode.equals(LanguageCodes.es)) {
            return esStem;
        } else if(isoCode.equals(LanguageCodes.fa)) {
            return faStem;
        } else if(isoCode.equals(LanguageCodes.fr)) {
            return frStem;
        } else if(isoCode.equals(LanguageCodes.gr)) {
            return grStem;
        } else if(isoCode.equals(LanguageCodes.it)) {
            return itStem;
        } else {
            return enStem; //Fall back for unknown languages and english tweets
        }
    }
}
