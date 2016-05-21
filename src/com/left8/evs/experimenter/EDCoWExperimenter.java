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
package com.left8.evs.experimenter;

import java.util.ArrayList;
import java.util.List;

import com.left8.evs.edmodule.data.EDCoWCorpus;
import com.left8.evs.edmodule.edcow.EDCoW;
import com.left8.evs.edmodule.edcow.event.EDCoWEvent;
import com.left8.evs.evaluator.EDCoWEvaluator;
import com.left8.evs.utilities.Config;
import com.left8.evs.utilities.Utilities;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2016.04.09_2021
 */
public class EDCoWExperimenter {
    private final EDCoWCorpus corpus;
    private int delta;
    private final int delta2;
    private int gamma;
    private double minTermSupport;
    private double maxTermSupport;
    private final int timeSliceA;
    private final int timeSliceB;
    private final Config config;
    
    public EDCoWExperimenter(EDCoWCorpus corpus, int delta, int delta2, int gamma, 
            double minTermSupport, double maxTermSupport, int timeSliceA, 
            int timeSliceB, Config config) {
        this.corpus = corpus;
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
     * @param showInlineInfo Show or hide inline info during execution.
     * @return A List of String representing the lines to be exported to the file.
     */
    public final List<String> experimentUsingDelta(int start, int end, int step, 
            boolean showInlineInfo) {
        
        List<String> lines = new ArrayList<>();
        String line;
        for(delta = start; delta < end; delta += step) {
            EDCoW edcow = new EDCoW(delta, delta2, gamma, minTermSupport, maxTermSupport, 
                    timeSliceA, timeSliceB, corpus);

            edcow.apply(); //Apply the algorithm

            EDCoWEvaluator eval;
            if(edcow.events.list.isEmpty()) {
                eval = new EDCoWEvaluator();
            } else {
                eval = new EDCoWEvaluator(
                        delta, delta2, gamma, timeSliceA, timeSliceB, minTermSupport,
                        maxTermSupport, edcow.events, config, corpus.getStemsHandler());
                eval.evaluate(showInlineInfo);
            }

            int i = 0;
            line = (edcow.events.list.isEmpty() ? 0 : edcow.events.list.size()) 
                    + "\t" + delta + "\t" + delta2 + "\t" + gamma 
                    + "\t" + minTermSupport + "\t" + maxTermSupport 
                    + "\t" + eval.getTotalRecall() + "\t" 
                    + edcow.getExecutionTime();
            lines.add(line); //Add first line
            for(EDCoWEvent event : edcow.events.list) {
                line = event.getTemporalDescriptionLowerBound()+ "\t" 
                        + event.getTextualDescription();
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
     * @param showInlineInfo Show or hide inline info during execution.
     * @return A List of String representing the lines to be exported to the file.
     */
    public final List<String> experimentUsingGamma(int start, int end, int step,
            boolean showInlineInfo) {
        
        List<String> lines = new ArrayList<>();
        String line;
        for(gamma = start; gamma < end; gamma += step) {
            EDCoW edcow = new EDCoW(delta, delta2, gamma,
                    minTermSupport, maxTermSupport, timeSliceA, timeSliceB, corpus);

            edcow.apply(); //Apply the algorithm

            EDCoWEvaluator eval;
            if(edcow.events.list.isEmpty()) {
                eval = new EDCoWEvaluator();
            } else {
                eval = new EDCoWEvaluator(
                        delta, delta2, gamma, timeSliceA, timeSliceB, minTermSupport,
                        maxTermSupport, edcow.events, config, corpus.getStemsHandler());
                eval.evaluate(showInlineInfo);
            }

            int i = 0;
            line = (edcow.events.list.isEmpty() ? 0 : edcow.events.list.size()) 
                    + "\t" + delta + "\t" + delta2 + "\t" + gamma 
                    + "\t" + minTermSupport + "\t" + maxTermSupport 
                    + "\t" + eval.getTotalRecall() + "\t" 
                    + edcow.getExecutionTime();
            lines.add(line); //Add first line
            for(EDCoWEvent event : edcow.events.list) {
                line = event.getTemporalDescriptionLowerBound()+ "\t" 
                        + event.getTextualDescription();
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
