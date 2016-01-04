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
package edmodule;

import edmodule.dataset.Dataset;
import edmodule.edcow.EDCoW;
import edmodule.lsh.LSH;
import edmodule.peakfinding.BinsCreator;
import edmodule.peakfinding.OfflinePeakFinding;
import java.util.List;
import java.util.Scanner;
import utilities.Config;
import utilities.Utilities;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2016.01.04_1914_gargantua
 */
public class EDMethodPicker {
    
    /**
     * Pick method of Event Detection.
     * @param config A configuration object
     */
    public static void selectEDMethod(Config config) {
        System.out.println("\nPick a method for Event Detection");
        System.out.println("1. EDCoW");
        System.out.println("2. LSH");
        System.out.println("3. Offline Peak Finding");
        System.out.print("Your choice: ");
        
        Scanner keyboard = new Scanner(System.in);
        int choice = keyboard.nextInt();
        
        switch(choice) {
            case 1: {
                Dataset ds = new Dataset(config);
                ds.createCorpus(config);
                ds.setDocTermFreqIdList();
                
                EDCoW edcow = new EDCoW(14, 100, ds); //Create the EDCoW object
                Utilities.printInfoMessage("Selected method: " + edcow.getName());
                Utilities.printInfoMessage("Now applying algorithm...");
                
                edcow.apply(); //Apply the algorithm
                Utilities.printInfoMessage("Succesfully applied EDCoW algorithm");
                break;
            } case 2: {
                System.out.println("Selected method: LSH");
                LSH lsh = new LSH();
                lsh.apply();
                break;
            } case 3: {
                List<Integer> bins = BinsCreator.createBins(1);
                OfflinePeakFinding opf = new OfflinePeakFinding(bins, 0.125, 2, 5);
                Utilities.printInfoMessage("Selected method: " + opf.getName());
                Utilities.printInfoMessage("Now applying algorithm...");
                break;
            } default: {
                System.out.println("No method selected. Exiting now...");
                System.exit(0);
            }
        }
    }
}
