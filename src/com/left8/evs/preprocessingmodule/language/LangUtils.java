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
package com.left8.evs.preprocessingmodule.language;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2016.02.19_1713
 */
public class LangUtils {
    
    /**
     * Returns the enumeration language value of the ISO code enumeration.
     * @param isoCode A LanguageCodes enumeration.
     * @return The Language enum value of the ISO code.
     */
    public final static Language getFullLanguageForISOCode(LanguageCodes isoCode) {
        if(isoCode.equals(LanguageCodes.ar)) {
            return Language.arabic;
        } else if(isoCode.equals(LanguageCodes.de)) {
            return Language.german;
        } else if(isoCode.equals(LanguageCodes.en)) {
            return Language.english;
        } else if(isoCode.equals(LanguageCodes.es)) {
            return Language.spanish;
        } else if(isoCode.equals(LanguageCodes.fa)) {
            return Language.persian;
        } else if(isoCode.equals(LanguageCodes.fr)) {
            return Language.french;
        } else if(isoCode.equals(LanguageCodes.gr)) {
            return Language.greek;
        } else if(isoCode.equals(LanguageCodes.it)) {
            return Language.italian;
        } else {
            return Language.english; //Fall back for unknown languages
        }
    }
    
    /**
     * Returns the LanguageCodes enumeration for a given String.
     * @param isoCode A String containing the ISO code.
     * @return A LanguageCodes enumeration.
     */
    public final static LanguageCodes getLangISOFromString(String isoCode) {
        switch (isoCode) {
            case "ar":
                return LanguageCodes.ar;
            case "de":
                return LanguageCodes.de;
            case "en":
                return LanguageCodes.en;
            case "es":
                return LanguageCodes.es;
            case "fa":
                return LanguageCodes.fa;
            case "fr":
                return LanguageCodes.fr;
            case "gr":
                return LanguageCodes.gr;
            case "it":
                return LanguageCodes.it;
            default:
                return LanguageCodes.en; //Fall back for unknown languages
        }
    }
}
