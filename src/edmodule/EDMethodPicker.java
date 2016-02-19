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
import edmodule.edcow.EDCoW;
import edmodule.edcow.event.Event;
import edmodule.utils.BinPair;
import edmodule.peakfinding.BinsCreator;
import edmodule.peakfinding.OfflinePeakFinding;
import evaluator.EDCoWEvaluator;
import evaluator.PeakFindingEvaluator;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import utilities.Config;
import utilities.Utilities;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2016.02.19_1708
 */
public class EDMethodPicker {
    
    /**
     * Pick method of Event Detection.
     * @param config A configuration object
     */
    public static void selectEDMethod(Config config) throws FileNotFoundException {
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
                List<String> lines;
                String line;
                EDCoWCorpus corpus = new EDCoWCorpus(config, ds.getTweetList(), ds.getSWH(), 10);
                
                corpus.createCorpus();
                corpus.setDocTermFreqIdList();
                int delta = 4, delta2 = 11, gamma = 26;
                double minTermSupport = 0.0001, maxTermSupport = 0.001;
                
                for(delta = 4; delta < 15; delta++) {
                    EDCoW edcow = new EDCoW(delta, delta2, gamma, minTermSupport, 
                            maxTermSupport, 1, 155, corpus); //Create the EDCoW object

                    edcow.apply(); //Apply the algorithm

                    EDCoWEvaluator eval = new EDCoWEvaluator(delta, delta2, 
                            gamma, 1, 155, minTermSupport, maxTermSupport, 
                            edcow.events, config, corpus.getStemsHandler());
                    eval.evaluate();
                    
                    int i = 0;
                    lines = new ArrayList<>();
                    line = (edcow.events.list.isEmpty() ? 0 : edcow.events.list.size()) 
                            + "\t" + delta + "\t" + delta2 + "\t" + gamma 
                            + "\t" + minTermSupport + "\t" + maxTermSupport 
                            + "\t" + eval.getTotalRecall() + "\t" + eval.getTotalPrecision();
                    lines.add(line); //Add first line
                    for(Event event : edcow.events.list) {
                        line = event.getTemporalDescriptionLowerBound()+ "\t" 
                                + eval.getRecall(i) + "\t" 
                                + eval.getPrecision(i) + "\t" 
                                + event.getTextualDescription();
                        lines.add(line); //Add every event in a single line
                        i++;
                    }
                    if(lines.size() == 1) { //No events were created
                        line = "No events";
                        lines.add(line);
                    }
                    lines.add(""); //Empty line
//                    Utilities.printMessageln("Total events found: " + edcow.events.list.size());
//                    Utilities.printMessageln("Total recall: " + eval.getTotalRecall());
//                    Utilities.printMessageln("Total precision: " + eval.getTotalPrecision());
                    Utilities.exportToFileUTF_8(config.getResourcesPath() 
                            + config.getEDCoWEventFileName(), lines, true);
                }
                break;
            } case 2: {
                Utilities.printMessageln("LSH not implemented yet!");
                break;
            } case 3: {
                int window = 10;
                double alpha = 0.999;
                int taph = 1;
                int pi = 5;
                Dataset ds = new Dataset(config);
                PeakFindingCorpus corpus = new PeakFindingCorpus(config, ds.getTweetList(), ds.getSWH());
                List<BinPair<String, Integer>> bins = BinsCreator.createBins(corpus, config, window);
                Utilities.printMessageln("Selected method: Offline Peak Finding");
                Utilities.printMessageln("Now applying algorithm...");
                ArrayList<String> lines = new ArrayList<>();
                String line;
                for(alpha = 0.85; alpha < 0.999; alpha += 0.01) {
                    OfflinePeakFinding opf = new OfflinePeakFinding(bins, 0.999, 1, 5, window, corpus);
                    opf.apply();
                    PeakFindingEvaluator eval = new PeakFindingEvaluator(alpha, taph, 
                            pi, opf.getEventList(), config);
                    eval.evaluateWithAllTerms(false);
                    int i = 0;
                    lines = new ArrayList<>();
                    line = (opf.getEventList().isEmpty() ? 0 : opf.getEventList().size()) 
                            + "\t" + alpha + "\t" + taph + "\t" + pi 
                            + "\t" + eval.getTotalRecall() + "\t" + eval.getTotalPrecision();
                    lines.add(line); //Add first line
                    for(edmodule.peakfinding.event.Event event : opf.getEventList()) {
                        line = bins.get(event.getWindowLowerBound()).getBin()+ "\t" 
                                + eval.getMatchedGroundTruthID(i) + "\t"
                                + eval.getRecallOfEvent(i) + "\t" 
                                + eval.getPrecisionOfEvent(i) + "\t" 
                                + event.getCommonTermsAsString();
                        lines.add(line); //Add every event in a single line
                        i++;
                    }
                    if(lines.size() == 1) { //No events were created
                        line = "No events";
                        lines.add(line);
                    }
                    lines.add(""); //Empty line
                    Utilities.exportToFileUTF_8(config.getResourcesPath() 
                            + config.getPeakFindingEventsFileName(), lines, true);
                }
                
                
                break;
            } default: {
                System.out.println("No method selected. Exiting now...");
                System.exit(0);
            }
        }
    }
}
