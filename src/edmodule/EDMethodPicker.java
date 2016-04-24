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

import edmodule.data.Dataset;
import edmodule.data.EDCoWCorpus;
import edmodule.data.PeakFindingCorpus;
import edmodule.utils.BinPair;
import edmodule.peakfinding.BinsCreator;
import experimenter.EDCoWExperimenter;
import experimenter.PeakFindingExperimenter;
import java.util.List;
import java.util.Scanner;
import utilities.Config;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2016.04.24_1922
 */
public class EDMethodPicker {
    
    /**
     * Pick method of Event Detection.
     * @param config A configuration object.
     * @param showInlineInfo A flag that indicates whether or not inline information
     * should be displayed.
     * @param extChoice Supplies an external choice.
     */
    public static void selectEDMethod(Config config, boolean showInlineInfo, 
            int extChoice) {
        int choice;
        if(extChoice == 0) {
            System.out.println("\nPick a method for Event Detection");
            System.out.println("1. EDCoW");
            System.out.println("2. Offline Peak Finding");
            System.out.println("Any other key to exit.");
            System.out.println("");
            System.out.print("Your choice: ");

            Scanner keyboard = new Scanner(System.in);
            choice = keyboard.nextInt();
        } else { //Directly run an algorithm
            choice = extChoice;
        }
        
        switch(choice) {
            case 1: {
                Dataset ds = new Dataset(config);
                EDCoWCorpus corpus = new EDCoWCorpus(config, ds.getTweetList(), ds.getSWH(), 10);
                
                corpus.createCorpus();
                corpus.setDocTermFreqIdList();
                int delta = 4, delta2 = 11, gamma = 26, timeSliceA = 1, 
                        timeSliceB = 154;
                double minTermSupport = 0.001, maxTermSupport = 0.01;
                
                EDCoWExperimenter exper = new EDCoWExperimenter(corpus, delta, 
                        delta2, gamma, minTermSupport, maxTermSupport, timeSliceA, 
                        timeSliceB, config);
                
                //Experiment with delta
                List<String> lines = exper.experimentUsingDelta(1, 20, 1, showInlineInfo);
                exper.exportToFile("edcow_simple.txt", lines);
                
                //Experiment with gamma
                lines = exper.experimentUsingGamma(6, 10, 1, showInlineInfo);
                exper.exportToFile("edcow_simple.txt", lines);
                
                break;
            } case 2: {
                int window = 10;
                double alpha = 0.999;
                int taph = 1;
                int pi = 5;
                Dataset ds = new Dataset(config);
                PeakFindingCorpus corpus = new PeakFindingCorpus(config, ds.getTweetList(), ds.getSWH());
                List<BinPair<String, Integer>> bins = BinsCreator.createBins(corpus, config, window);
                
                PeakFindingExperimenter exper = new PeakFindingExperimenter(corpus, 
                        bins, alpha, taph, pi, window, config);
                
                //Experiment with Taph
                List<String> lines = exper.experimentUsingTaph(1, 10, 1, showInlineInfo);
                exper.exportToFile("peak_finding_simple.txt", lines);
                
                //Experiment with Alpha
                lines = exper.experimentUsingAlpha(0.85, 0.99, 0.01, showInlineInfo);
                exper.exportToFile("peak_finding_simple.txt", lines);
                
                break;
            } default: {
                System.out.println("No method selected. Exiting now...");
                System.exit(0);
            }
        }
    }
}
