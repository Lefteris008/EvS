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
package experimenter;

import evs.data.SentimentEDCoWCorpus;
import evs.edcow.SentimentEDCoW;
import evs.edcow.event.SentimentEDCoWEvent;
import evs.evaluator.SentimentEDCoWEvaluator;
import java.util.ArrayList;
import java.util.List;
import utilities.Config;
import utilities.Utilities;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2016.04.09_2052
 */
public class SentimentEDCoWExperimenter {
    
    private final SentimentEDCoWCorpus sCorpus;
    private int delta;
    private final int delta2;
    private int gamma;
    private double minTermSupport;
    private double maxTermSupport;
    private final int timeSliceA;
    private final int timeSliceB;
    private final Config config;
    
    public SentimentEDCoWExperimenter(SentimentEDCoWCorpus sCorpus, int delta,
            int delta2, int gamma, double minTermSupport, double maxTermSupport,
            int timeSliceA, int timeSliceB, Config config) {
        this.sCorpus = sCorpus;
        this.delta = delta;
        this.delta2 = delta2;
        this.gamma = gamma;
        this.minTermSupport = minTermSupport;
        this.maxTermSupport = maxTermSupport;
        this.timeSliceA = timeSliceA;
        this.timeSliceB = timeSliceB;
        this.config = config;
    }
    
    /**
     * Method that exhaustively tests the algorithm, by iterating between the 
     * 'start' and the 'end' value of delta parameter.
     * @param start An integer representing the starting point of delta parameter.
     * @param end An integer representing the ending point of delta parameter.
     * @param step An integer representing the increase step of delta between iterations.
     * @param sentimentSource The source of sentiment. 0 represents SST, 1 Naive
     * Bayes and 2 Bayesian Network.
     * @param showInlineInfo Show or hide inline info during execution.
     * @return A List of String representing the lines to be exported to the file.
     */
    public final List<String> experimentUsingDelta(int start, int end, int step,
            int sentimentSource, boolean showInlineInfo) {
        
        List<String> lines = new ArrayList<>();
        String line;
        for(delta = start; delta < end; delta += step) {
            SentimentEDCoW sEdcow = new SentimentEDCoW(delta, delta2, gamma,
                    minTermSupport, maxTermSupport, timeSliceA, timeSliceB, sCorpus, 
                    sentimentSource);

            sEdcow.apply(); //Apply the algorithm

            SentimentEDCoWEvaluator eval;
            if(sEdcow.events.list.isEmpty()) {
                eval = new SentimentEDCoWEvaluator();
            } else {
                eval = new SentimentEDCoWEvaluator(
                        delta, delta2, gamma, timeSliceA, timeSliceB, minTermSupport,
                        maxTermSupport, sEdcow.events, config,
                        sCorpus.getEDCoWCorpus().getStemsHandler());
                eval.evaluate(showInlineInfo);
            }

            int i = 0;
            line = (sEdcow.events.list.isEmpty() ? 0 : sEdcow.events.list.size()) 
                    + "\t" + delta + "\t" + delta2 + "\t" + gamma 
                    + "\t" + minTermSupport + "\t" + maxTermSupport 
                    + "\t" + eval.getTotalRecall() + "\t" 
                    + sEdcow.getExecutionTime();
            lines.add(line); //Add first line
            for(SentimentEDCoWEvent event : sEdcow.events.list) {
                line = event.getTemporalDescriptionLowerBound()+ "\t" 
                        + eval.getMatchedGroundTruthID(i) + "\t"
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
        }
        return lines;
    }
    
    /**
     * Method that exhaustively tests the algorithm, by iterating between the 
     * 'start' and the 'end' value of gamma parameter.
     * @param start An integer representing the starting point of gamma parameter.
     * @param end An integer representing the ending point of gamma parameter.
     * @param step An integer representing the increase step of gamma between iterations.
     * @param sentimentSource The source of sentiment. 0 represents SST, 1 Naive
     * Bayes and 2 Bayesian Network.
     * @param showInlineInfo Show or hide inline info during execution.
     * @return A List of String representing the lines to be exported to the file.
     */
    public final List<String> experimentUsingGamma(int start, int end, int step,
            int sentimentSource, boolean showInlineInfo) {
        
        List<String> lines = new ArrayList<>();
        String line;
        for(gamma = start; gamma < end; gamma += step) {
            SentimentEDCoW sEdcow = new SentimentEDCoW(delta, delta2, gamma,
                    minTermSupport, maxTermSupport, timeSliceA, timeSliceB, sCorpus, 
                    sentimentSource);

            sEdcow.apply(); //Apply the algorithm

            SentimentEDCoWEvaluator eval;
            if(sEdcow.events.list.isEmpty()) {
                eval = new SentimentEDCoWEvaluator();
            } else {
                eval = new SentimentEDCoWEvaluator(
                        delta, delta2, gamma, timeSliceA, timeSliceB, minTermSupport,
                        maxTermSupport, sEdcow.events, config,
                        sCorpus.getEDCoWCorpus().getStemsHandler());
                eval.evaluate(showInlineInfo);
            }

            int i = 0;
            line = (sEdcow.events.list.isEmpty() ? 0 : sEdcow.events.list.size()) 
                    + "\t" + delta + "\t" + delta2 + "\t" + gamma 
                    + "\t" + minTermSupport + "\t" + maxTermSupport 
                    + "\t" + eval.getTotalRecall() + "\t" 
                    + sEdcow.getExecutionTime();
            lines.add(line); //Add first line
            for(SentimentEDCoWEvent event : sEdcow.events.list) {
                line = event.getTemporalDescriptionLowerBound()+ "\t" 
                        + eval.getMatchedGroundTruthID(i) + "\t"
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
        }
        return lines;
    }
    
     /**
     * Exports a List of String lines into a file.
     * @param filename The name of the file.
     * @param lines The lines to exported.
     */
    public final void exportToFile(String filename, List<String> lines) {
        Utilities.exportToFileUTF_8(config.getResourcesPath() 
            + config.getOutputPath() 
            + config.getEdcowOutputPath()
            + filename, lines, true);
    }
}
