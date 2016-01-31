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
package evaluator;

import edmodule.edcow.EDCoWEvent;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import preprocessingmodule.nlp.stemming.StemUtils;
import utilities.Config;
import utilities.Utilities;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2016.01.31_1921
 */
public class EDCoWEvaluator {
    private final int delta;
    private final int delta2;
    private final int gamma;
    private final int timeSliceA;
    private final int timeSliceB;
    private final double minTermSupport;
    private final double maxTermSupport;
    private final LinkedList<EDCoWEvent> eventList;
    private final Config config;
    private final HashMap<Integer, HashSet<String>> groundTruthEvents = new HashMap<>();
    private final StemUtils stemsHandler;
    private final List<Double> precisionByEvent = new ArrayList<>();
    private final List<Double> recallByEvent = new ArrayList<>();
    
    /**
     * Public constructor.
     * @param delta An integer representing the delta value.
     * @param delta2 An integer representing the delta2 value.
     * @param gamma An integer representing the gamma value.
     * @param timeSliceA An integer representing the starting time slice of the dataset.
     * @param timeSliceB An integer representing the ending time slice of the dataset.
     * @param minTermSupport A double representing the minimum term support of the terms in an event.
     * @param maxTermSupport A double representing the maximum term support of the terms in an event.
     * @param eventList A list containing the events after the application of EDCoW algorithm.
     * @param config A configuration object.
     */
    public EDCoWEvaluator(int delta, int delta2, int gamma, int timeSliceA, 
            int timeSliceB, double minTermSupport, double maxTermSupport, 
            LinkedList<EDCoWEvent> eventList, Config config, StemUtils stemsHandler) {
        this.delta = delta;
        this.delta2 = delta2;
        this.gamma = gamma;
        this.timeSliceA = timeSliceA;
        this.timeSliceB = timeSliceB;
        this.minTermSupport = minTermSupport;
        this.maxTermSupport = maxTermSupport;
        this.eventList = eventList;
        this.config = config;
        this.stemsHandler = stemsHandler;
        loadGroundTruthDataset();
    }
    
    /**
     * Method to load a ground truth dataset for future use.
     * More formally it creates a HashMap that contains integer IDs as keys
     * and a HashSet of the terms of a specific event, as values.
     */
    private void loadGroundTruthDataset() {
        try (BufferedReader br = new BufferedReader(new FileReader(
                config.getResourcesPath() + config.getGroundTruthDataFile()))) {
            String line;
            String[] terms;
            int i = 1;
            while ((line = br.readLine()) != null) {
//               String[] temp = line.split("\t");
//               terms = temp[2].split(",");
               terms = line.split("\t")[2].split(",");
               HashSet<String> termSet = new HashSet<>();
               for(String term : terms) {
                   termSet.add(term.toLowerCase());
               }
               groundTruthEvents.put(i, termSet);
               i++;
            }
        } catch (FileNotFoundException e) {
            Logger.getLogger(EDCoWEvaluator.class.getName()).log(Level.SEVERE, null, e);
        } catch (IOException e) {
            Logger.getLogger(EDCoWEvaluator.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
    /**
     * Method to evaluate an extracted EDCoW dataset.
     * More formally, it calculates the recall and precision of a EDCoW dataset
     * that is calculated after the application of the algorithm and exports this
     * data into a file, along with other useful metrics.
     */
    public void evaluate() {
        LinkedList<String> calculatedKeywords;
        HashSet<String> groundTruthKeywords;
        int groundTruthKeywordSize;
        int matchedItems;
        double recall;
        double precision;
        int eventKey;
        for(EDCoWEvent event : eventList) {
            matchedItems = 0;
            recall = 0;
            precision = 0;
            calculatedKeywords = new LinkedList<>(event.keywords);
            eventKey = -1;
            for(String keyoword : calculatedKeywords) {
                eventKey = findEventByTerm(stemsHandler.getOriginalWord(keyoword)
                        .toLowerCase());
                if(eventKey != -1) {
                    break;
                }
            }
            if(eventKey != -1) {
                Utilities.printMessageln("Found: " + eventKey);
                groundTruthKeywords = new HashSet<>(groundTruthEvents.get(eventKey));
                groundTruthKeywordSize = groundTruthKeywords.size();
                for(String keyword : calculatedKeywords) {
                    if(groundTruthKeywords.contains(stemsHandler
                            .getOriginalWord(keyword).toLowerCase())) {
                        matchedItems++;
                    }
                }
                recall = matchedItems / groundTruthKeywordSize;
                precision = matchedItems / calculatedKeywords.size();
                recallByEvent.add(recall);
                precisionByEvent.add(precision);
            } else {
                Utilities.printMessageln("Event not found");
            }
        } 
    }
    
    /**
     * Returns the index of the matched event from the groundTruthEvents map.
     * @param term A String representing the term.
     * @return An integer representing the actual event index or -1 if not found.
     */
    private int findEventByTerm(String term) {
        for(Integer key : groundTruthEvents.keySet()) {
            HashSet<String> termSet = new HashSet<>(groundTruthEvents.get(key));
            if(termSet.contains(term)) {
                return key;
            }
        }
        return -1;
    }
    
    /**
     * Calculates and returns the total recall of the calculated dataset.
     * @return A double containing the total recall of the calculated dataset
     * compared with the ground truth data.
     */
    public final double getRecall() {
        double totalRecall = 0;
        totalRecall = recallByEvent.stream().mapToDouble((recall) -> recall)
                .reduce(totalRecall, (accumulator, _item) -> accumulator + _item);
        return totalRecall;
    }
    
    /**
     * Calculates and returns the total precision of the calculated dataset.
     * @return A double containing the total precision of the calculated dataset
     * compared with the ground truth data.
     */
    public final double getPrecision() {
        double totalPrecision = 0;
        totalPrecision = precisionByEvent.stream().mapToDouble((precision) -> precision)
                .reduce(totalPrecision, (accumulator, _item) -> accumulator + _item);
        return totalPrecision;
    }
}
