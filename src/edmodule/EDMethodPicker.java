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
import preprocessingmodule.nlp.stopwords.StopWords;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2015.11.17_1852_planet1
 */
public class EDMethodPicker {
    
    /**
     * Constructor to pick method of Event Detection 
     * @param config A configuration object
     */
    public EDMethodPicker(Config config) {
        System.out.println("Pick a method for Event Detection");
        System.out.println("1. EDCoW");
        System.out.println("2. LSH");
        System.out.print("Your choice: ");
        
        Scanner keyboard = new Scanner(System.in);
        int choice = keyboard.nextInt();
        
        Dataset corpus = new Dataset();
        switch(choice) {
            case 1: {
                System.out.println("Selected method: EDCoW");
                EDCoW edcow = new EDCoW(1, 1, 1, corpus); //Test object
                StopWords stopwordsHandler = new StopWords();
                stopwordsHandler.loadStopWords(config); //Load the stopwords
                edcow.apply(stopwordsHandler); //Apply the algorithm
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
