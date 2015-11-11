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
package preprocessingmodule;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2015.11.11_1449_planet1
 */
public class Utils {
    
    /**
     * Method to extract search terms from the 'search_terms.txt' file.
     * @param config The configuration object
     * @return A String array containing the search terms
     */
    public final static String[] extractTermsFromFile(Config config) {
        
        String line;
        ArrayList<String> terms = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(config.getSearchTermsFile()))) {
            while ((line = br.readLine()) != null) {
                terms.add(line); //Open the search terms file and read them
            }
            br.close();
        } catch (IOException e) {
            System.out.println("The file '" + config.getSearchTermsFile() + "' is missing.\nPlace a correct file in classpath and re-run the project");
            Logger.getLogger(PreProcessor.class.getName()).log(Level.SEVERE, null, e);
            System.exit(1);
        }
        
        return terms.toArray(new String[terms.size()]);
    }
    
    /**
     * Method to read the IDs of the tweets that are going to be pulled from Twitter.
     * @param config The configuration object
     * @param filename The folder name in which the .txt files containing the IDs are placed
     * @return A list containing the tweet IDs
     */
    public final static List<String> extractTweetIDsFromFile(Config config, String filename) {
        
        List<String> list = new ArrayList<>();
        
        String path = config.getDatasetLocation() + filename + "\\";
        
        try {
            Files.walk(Paths.get(path)).forEach(filePath -> { //For all files in the current folder
                if (Files.isRegularFile(filePath)) { //Open every single file
                    try (BufferedReader br = new BufferedReader(new FileReader(filePath.toString()))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                           list.add(line); //Store the id to the list
                        }
                    } catch(IOException e) {
                        System.out.println("No filed found in '" + config.getDatasetLocation() + filename + "\\'Place the appropriate files in classpath and re-run the project");
                        Logger.getLogger(PreProcessor.class.getName()).log(Level.SEVERE, null, e);
                        System.exit(1);
                    }
                }
            });     
        } catch (IOException e) {
            System.out.println("Folder '" + config.getDatasetLocation() + filename + "\\' is missing.\nPlace a correct folder in classpath and re-run the project");
            Logger.getLogger(PreProcessor.class.getName()).log(Level.SEVERE, null, e);
            System.exit(1);
        }
        return list;
    }
}
