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
package edmodule;

import edmodule.utils.Dataset;
import edmodule.edcow.EDCoW;
import edmodule.lsh.LSH;
import java.util.Scanner;
import preprocessingmodule.Config;
import preprocessingmodule.language.LanguageCodes;
import preprocessingmodule.nlp.stopwords.StopWords;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2015.11.25_2358_planet2
 */
public class EDMethodPicker {
    
    /**
     * Constructor to pick method of Event Detection 
     * @param config A configuration object
     */
    public EDMethodPicker(Config config) {
        System.out.println("\nPick a method for Event Detection");
        System.out.println("1. EDCoW");
        System.out.println("2. LSH");
        System.out.print("Your choice: ");
        
        Scanner keyboard = new Scanner(System.in);
        int choice = keyboard.nextInt();
        
        switch(choice) {
            case 1: {
                StopWords stopwordsHandler = new StopWords(config);
                stopwordsHandler.loadStopWords(LanguageCodes.en); //Load the stopwords for the english language
                Dataset ds = new Dataset(config, stopwordsHandler); //Load the dataset
                ds.setDocTermFreqIdList(config);
                
                System.out.println("Selected method: EDCoW");
                System.out.println("Now applying EDCoW algorithm...");
                
                EDCoW edcow = new EDCoW(452, 500, ds); //Create the EDCoW object
                edcow.apply(stopwordsHandler); //Apply the algorithm
                
                System.out.println("Succesfully applied EDCoW algorithm");
            } 
            case 2: {
                System.out.println("Selected method: LSH");
                LSH lsh = new LSH();
                lsh.run();
            }
            default: {
                System.out.println("No method selected. Exiting now...");
                System.exit(0);
            }
        }
    }
}
