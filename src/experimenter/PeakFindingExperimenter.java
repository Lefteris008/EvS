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

import edmodule.data.PeakFindingCorpus;
import edmodule.peakfinding.OfflinePeakFinding;
import edmodule.peakfinding.event.PeakFindingEvent;
import edmodule.utils.BinPair;
import evaluator.PeakFindingEvaluator;
import java.util.ArrayList;
import java.util.List;
import utilities.Config;
import utilities.Utilities;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2016.04.30_1830
 */
public class PeakFindingExperimenter {
    private final PeakFindingCorpus corpus;
    private final List<BinPair<String, Integer>> bins;
    private double alpha;
    private int taph;
    private final int pi;
    private final Config config;
    
    public PeakFindingExperimenter(PeakFindingCorpus corpus, List<BinPair<String, Integer>> bins, double alpha, int taph, int pi, Config config) {
        this.corpus = corpus;
        this.bins = bins;
        this.alpha = alpha;
        this.taph = taph;
        this.pi = pi;
        this.config = config;
    }
    
    /**
     * Method that exhaustively tests the algorithm, by iterating between the 
     * 'start' and the 'end' value of taph parameter.
     * @param start An integer representing the starting point of taph parameter.
     * @param end An integer representing the ending point of taph parameter.
     * @param step An integer representing the increase step of taph between iterations.
     * @param showInlineInfo A boolean flag that indicates whether to show or hide
     * inline information during execution.
     * @return A List of String representing the lines to be exported to the file.
     */
    public final List<String> experimentUsingTaph(int start, int end, int step,
            boolean showInlineInfo) {
        List<String> lines = new ArrayList<>();
        String line;
        for(taph = start; taph < end; taph += step) {
            OfflinePeakFinding opf = new OfflinePeakFinding(bins, alpha, taph, 
                    pi, corpus);

            opf.apply();
            PeakFindingEvaluator eval = new PeakFindingEvaluator(alpha, taph, 
                        pi, opf.getEventList(), config);
            eval.evaluateWithAllTerms(showInlineInfo);
            int i = 0;
            line = (opf.getEventList().isEmpty() ? 0 : opf.getEventList().size()) 
                    + "\t" + alpha + "\t" 
                    + taph + "\t" 
                    + pi + "\t" 
                    + eval.getTotalRecall() + "\t" 
                    + opf.getExecutionTime();
            lines.add(line); //Add first line
            for(PeakFindingEvent event : opf.getEventList()) {
                line = bins.get(event.getWindowLowerBound()).getBin()+ "\t" 
                        + eval.getMatchedGroundTruthID(i) + "\t"
                        + event.getCommonTermsAsString();
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
     * 'start' and the 'end' value of taph parameter.
     * @param start An integer representing the starting point of taph parameter.
     * @param end An integer representing the ending point of taph parameter.
     * @param step An integer representing the increase step of taph between iterations.
     * @param showInlineInfo Boolean flag that indicates whether to show or hide
     * inline information during execution.
     * @return A List of String representing the lines to be exported to the file.
     */
    public final List<String> experimentUsingAlpha(double start, double end, double step,
            boolean showInlineInfo) {
        List<String> lines = new ArrayList<>();
        String line;
        for(alpha = start; alpha < end; alpha += step) {
            OfflinePeakFinding opf = new OfflinePeakFinding(bins, alpha, taph, 
                    pi, corpus);

            opf.apply();
            PeakFindingEvaluator eval = new PeakFindingEvaluator(alpha, taph, 
                        pi, opf.getEventList(), config);
            eval.evaluateWithAllTerms(showInlineInfo);
            int i = 0;
            line = (opf.getEventList().isEmpty() ? 0 : opf.getEventList().size()) 
                    + "\t" + alpha + "\t" 
                    + taph + "\t" 
                    + pi + "\t" 
                    + eval.getTotalRecall() + "\t" 
                    + opf.getExecutionTime();
            lines.add(line); //Add first line
            for(PeakFindingEvent event : opf.getEventList()) {
                line = bins.get(event.getWindowLowerBound()).getBin()+ "\t" 
                        + eval.getMatchedGroundTruthID(i) + "\t"
                        + event.getCommonTermsAsString();
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
            + config.getPeakFindingOutputPath()
            + filename, lines, true);
    }
}
