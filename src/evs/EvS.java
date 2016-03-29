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
package evs;

import edmodule.EDMethodPicker;
import edmodule.data.Dataset;
import edmodule.data.PeakFindingCorpus;
import edmodule.peakfinding.BinsCreator;
import edmodule.utils.BinPair;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import evs.data.PeakFindingSentimentCorpus;
import evs.data.SentimentEDCoWCorpus;
import evs.edcow.SentimentEDCoW;
import evs.edcow.event.SentimentEDCoWEvent;
import evs.evaluator.SentimentEDCoWEvaluator;
import experimenter.SentimentPeakFindingExperimenter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import utilities.Config;
import utilities.Console;
import utilities.Utilities;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2016.03.29_1750
 */
public class EvS {
    
    /**
     * Main method that provides a simple console input interface for the user,
     * if she wishes to execute the tool as a .jar executable.
     * @param args A list of arguments.
     */
    public static void main(String[] args) {
        
        Console console = new Console(args); //Read the console
        
        if(!console.showMongoLogging) {
            //Stop reporting logging information
            Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
            mongoLogger.setLevel(Level.SEVERE);
        }
        
        Config config = null;
        try {
            config = new Config();
        } catch (IOException ex) {
            Utilities.printMessageln("Configuration file 'config.properties' "
                    + "not in classpath!");
            Logger.getLogger(EvS.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
        
        int choice;
        System.out.println("\n----EvS----");
        System.out.println("Select one of the following options");
        System.out.println("1. Run Offline Peak Finding experiments.");
        System.out.println("2. Run EDCoW experiments.");
        System.out.println("3. Run each of the above methods without "
                + "the sentiment annotations.");
        System.out.println("");
        System.out.print("Your choice: ");
        
        Scanner keyboard = new Scanner(System.in);
        choice = keyboard.nextInt();
        
        switch (choice) {
            case 1: { //Offline Peak Finding
                int window = 10;
                double alpha = 0.85;
                int taph = 1;
                int pi = 5;
                Dataset ds = new Dataset(config);
                PeakFindingCorpus corpus = new PeakFindingCorpus(config, ds.getTweetList(), ds.getSWH());
                corpus.createCorpus(window);
                
                List<BinPair<String, Integer>> bins = BinsCreator.createBins(corpus, config, window);
                PeakFindingSentimentCorpus sCorpus = new PeakFindingSentimentCorpus(corpus);
                
                SentimentPeakFindingExperimenter exper = 
                        new SentimentPeakFindingExperimenter(sCorpus, bins, alpha, 
                                taph, pi, window, config);
                
                //Taph
                List<String> lines = exper.experimentUsingTaph(1, 10, 1, 0);
                exper.exportToFile("stanford.txt", lines);
                lines = exper.experimentUsingTaph(1, 10, 1, 1);
                exper.exportToFile("naive_bayes.txt", lines);
                lines = exper.experimentUsingTaph(1, 10, 1, 2);
                exper.exportToFile("bayesian_net.txt", lines);
                
                //Alpha
                lines = exper.experimentUsingAlpha(0.85, 0.99, 0.01, 0);
                exper.exportToFile("stanford.txt", lines);
                lines = exper.experimentUsingAlpha(0.85, 0.99, 0.01, 1);
                exper.exportToFile("naive_bayes.txt", lines);
                lines = exper.experimentUsingAlpha(0.85, 0.99, 0.01, 2);
                exper.exportToFile("bayesian_net.txt", lines);
                
                break;
            } case 2: { //EDCoW
                Dataset ds = new Dataset(config);
                List<String> lines;
                String line;
                SentimentEDCoWCorpus corpus = new SentimentEDCoWCorpus(config, 
                        ds.getTweetList(), ds.getSWH(), 10);
                
                corpus.getEDCoWCorpus().createCorpus();
                corpus.getEDCoWCorpus().setDocTermFreqIdList();
                int delta = 5, delta2 = 11, gamma = 6;
                double minTermSupport = 0.001, maxTermSupport = 0.01;
                
                for(gamma = 6; gamma < 15; gamma++) {
                    //Create the EDCoW object -> Naive Bayes
                    SentimentEDCoW sEdcow = new SentimentEDCoW(delta, delta2, gamma,
                            minTermSupport, maxTermSupport, 1, 155, corpus, 2);

                    sEdcow.apply(); //Apply the algorithm

                    SentimentEDCoWEvaluator eval;
                    if(sEdcow.events.list.isEmpty()) {
                        eval = new SentimentEDCoWEvaluator();
                    } else {
                        eval = new SentimentEDCoWEvaluator(
                                delta, delta2, gamma, 1, 155, minTermSupport,
                                maxTermSupport, sEdcow.events, config,
                                corpus.getEDCoWCorpus().getStemsHandler());
                        eval.evaluate();
                    }
                    
                    int i = 0;
                    lines = new ArrayList<>();
                    line = (sEdcow.events.list.isEmpty() ? 0 : sEdcow.events.list.size()) 
                            + "\t" + delta + "\t" + delta2 + "\t" + gamma 
                            + "\t" + minTermSupport + "\t" + maxTermSupport 
                            + "\t" + eval.getTotalRecall() + "\t" 
                            + eval.getTotalPrecision() + "\t"
                            + sEdcow.getExecutionTime();
                    lines.add(line); //Add first line
                    for(SentimentEDCoWEvent event : sEdcow.events.list) {
                        line = event.getTemporalDescriptionLowerBound()+ "\t" 
                                + eval.getMatchedGroundTruthID(i) + "\t"
                                + eval.getRecall(i) + "\t" 
                                + eval.getPrecision(i) + "\t" 
                                + event.getTextualDescription() + "\t"
                                + event.getMainSentimentOfEvent() + "\t"
                                + event.getPositiveSentimentPercentage() + "\t"
                                + event.getNegatineSentimentPercentage() + "\t"
                                + event.getNeutralSentimentPercentage() + "\t"
                                + event.getIrrelevantSentimentPercentage();
                        lines.add(line); //Add every event in a single line
                        i++;
                    }
                    if(lines.size() == 1) { //No events were created
                        line = "No events";
                        lines.add(line);
                    }
                    lines.add(""); //Empty line
                    Utilities.exportToFileUTF_8(config.getResourcesPath() 
                            + config.getOutputPath() + config.getEdcowOutputPath()
                            + "bayesian_net_edcow.txt", lines, true);
                }
                break;
            } case 3: {
                EDMethodPicker.selectEDMethod(config);
            } default: {
                
            }
        }
    }
}
