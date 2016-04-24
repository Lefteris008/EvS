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
import experimenter.SentimentEDCoWExperimenter;
import experimenter.SentimentPeakFindingExperimenter;
import java.io.IOException;
import java.util.Scanner;
import utilities.Config;
import utilities.Console;
import utilities.Utilities;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2016.04.24_1922
 */
public class EvS {
    
    private static int showMongoLogging = -1;
    private static boolean showInlineInfo = false;
    private static boolean hasExtCommands = false;
    private static int choice;
    
    /**
     * Method to manually set mongoLoggingFlag if the tool is executed as a JAR
     * library.
     * @param value 0 if the user wishes to show Mongo Logging information,
     * 1 otherwise.
     */
    public static void setShowMongoLoggingFlag(int value) {
        showMongoLogging = value;
    }
    
    /**
     * Main method that provides a simple console input interface for the user,
     * if she wishes to execute the tool as a .jar executable.
     * @param args A list of arguments.
     */
    public static void main(String[] args) {
        
        if(args.length != 0) { //If the user supplied arguments
            Console console = new Console(args); //Read the console
            showMongoLogging = console.showMongoLogging; //Set the mongo logging flag.
            showInlineInfo = console.showInlineInfo; //Set the inline info flag.
            hasExtCommands = console.hasExternalCommands;
            choice = console.choice;
        }
        
        if(showMongoLogging == 1) {
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
        System.out.println("\n----EvS----");
        
        if(!hasExtCommands) {
            System.out.println("Select one of the following options");
            System.out.println("1. Run Offline Peak Finding experiments.");
            System.out.println("2. Run EDCoW experiments.");
            System.out.println("3. Run each of the above methods without "
                    + "the sentiment annotations.");
            System.out.println("Any other key to exit.");
            System.out.println("");
            System.out.print("Your choice: ");

            Scanner keyboard = new Scanner(System.in);
            choice = keyboard.nextInt();
        }
        
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
                
                //Experiment with Taph
                List<String> lines = exper.experimentUsingTaph(1, 10, 1, 0, showInlineInfo);
                exper.exportToFile("stanford.txt", lines);
                lines = exper.experimentUsingTaph(1, 10, 1, 1, showInlineInfo);
                exper.exportToFile("naive_bayes.txt", lines);
                lines = exper.experimentUsingTaph(1, 10, 1, 2, showInlineInfo);
                exper.exportToFile("bayesian_net.txt", lines);
                
                //Experiment with Alpha
                lines = exper.experimentUsingAlpha(0.85, 0.99, 0.01, 0, showInlineInfo);
                exper.exportToFile("stanford.txt", lines);
                lines = exper.experimentUsingAlpha(0.85, 0.99, 0.01, 1, showInlineInfo);
                exper.exportToFile("naive_bayes.txt", lines);
                lines = exper.experimentUsingAlpha(0.85, 0.99, 0.01, 2, showInlineInfo);
                exper.exportToFile("bayesian_net.txt", lines);
                
                break;
            } case 2: { //EDCoW
                Dataset ds = new Dataset(config);
                SentimentEDCoWCorpus corpus = new SentimentEDCoWCorpus(config, 
                        ds.getTweetList(), ds.getSWH(), 10);
                
                corpus.getEDCoWCorpus().createCorpus();
                corpus.getEDCoWCorpus().setDocTermFreqIdList();
                int delta = 5, delta2 = 11, gamma = 6;
                double minTermSupport = 0.001, maxTermSupport = 0.01;
                
                SentimentEDCoWExperimenter exper = new SentimentEDCoWExperimenter(
                        corpus, delta, delta2, gamma, minTermSupport, maxTermSupport, 
                        choice, choice, config);
                
                //Experiment with delta
                List<String> lines = exper.experimentUsingDelta(1, 20, 1, 0, showInlineInfo);
                exper.exportToFile("stanford.txt", lines);
                lines = exper.experimentUsingDelta(1, 20, 1, 1, showInlineInfo);
                exper.exportToFile("naive_bayes.txt", lines);
                lines = exper.experimentUsingDelta(1, 20, 1, 2, showInlineInfo);
                exper.exportToFile("bayesian_net.txt", lines);
                
                //Experiment with gamma
                lines = exper.experimentUsingGamma(6, 10, 1, 0, showInlineInfo);
                exper.exportToFile("stanford.txt", lines);
                lines = exper.experimentUsingGamma(6, 10, 1, 1, showInlineInfo);
                exper.exportToFile("naive_bayes.txt", lines);
                lines = exper.experimentUsingGamma(6, 10, 1, 2, showInlineInfo);
                exper.exportToFile("bayesian_net.txt", lines);
                
                break;
            } case 3: {
                EDMethodPicker.selectEDMethod(config, showInlineInfo, 0);
                break;
            } case 4: { //Directly run OPF
                EDMethodPicker.selectEDMethod(config, showInlineInfo, 1);
                break;
            } case 5: { //Directly run EDCoW
                EDMethodPicker.selectEDMethod(config, showInlineInfo, 2);
                break;
            } default: {
                System.out.println("Exiting now...");
                System.exit(0);
            }
        }
    }
}
