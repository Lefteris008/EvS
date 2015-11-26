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
package preprocessingmodule.nlp.stopwords;

import preprocessingmodule.language.Language;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import preprocessingmodule.Config;
import preprocessingmodule.PreProcessor;
import preprocessingmodule.language.LangUtils;
import preprocessingmodule.language.LanguageCodes;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2015.11.26_1717_planet2
 */
public final class StopWords {
    
    private final HashSet<String> stopwords = new HashSet<>();
    private final Config config;
    
    /**
     * Public constructor that initializes a HashSet.
     * @param config
     */
    public StopWords(Config config) {
        this.config = config;
    }
    
    /**
     * Method that loads all stopwords contained in various files into a HashSet.
     * @param isoCode The ISO code of the language of the stopwords
     * @return True if the process succeeds, false otherwise.
     */
    public final boolean loadStopWords(LanguageCodes isoCode) {
        
        Language lan = LangUtils.getFullLanguageForISOCode(isoCode);
        
        try {
            Files.walk(Paths.get(config.getStopwordsPath())).forEach(filePath -> {
                if (Files.isRegularFile(filePath) && 
                        (filePath.toString().contains(lan.toString()) || 
                        filePath.toString().contains("punctuation.txt"))) { //Open stopwords files in appropriate language and punctuation
                    try (BufferedReader br = new BufferedReader(new FileReader(filePath.toString()))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                           stopwords.add(line); 
                        }
                    } catch(IOException e) {
                        System.out.println("No filed found in '" + config.getStopwordsPath() + "\\'Place the appropriate files in classpath and re-run the project");
                        Logger.getLogger(PreProcessor.class.getName()).log(Level.SEVERE, null, e);
                    }
                }
            });       
            return true;
        } catch (IOException ex) {
            System.out.println("Cannot locate the stopwords directory.\nPlease, place the appropriate files in classpath and re-run the project.");
            Logger.getLogger(StopWords.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    /**
     * True if the 'stopwords' HashSet contains 'word'.
     * @param word A String to be searched.
     * @return True if 'stopwords' contains 'word', false otherwise.
     */
    public final boolean isStopWord(String word) {
        return stopwords.contains(word);
    }
    
}
