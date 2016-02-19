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

import edmodule.edcow.event.Event;
import edmodule.edcow.event.Events;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import preprocessingmodule.nlp.stemming.StemUtils;
import utilities.Config;
import utilities.Utilities;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2016.02.19_1712
 */
public class EDCoWEvaluator {
    private final int delta;
    private final int delta2;
    private final int gamma;
    private final int timeSliceA;
    private final int timeSliceB;
    private final double minTermSupport;
    private final double maxTermSupport;
    private final List<Event> eventList;
    private final Config config;
    private final HashMap<Integer, HashSet<String>> groundTruthTermsPerEvent = new HashMap<>();
    private final HashMap<Integer, HashSet<String>> groundTruthIDsPerEvent = new HashMap<>();
    private final StemUtils stemsHandler;
    private final List<Double> precisionByEvent = new ArrayList<>();
    private final List<Double> recallByEvent = new ArrayList<>();
    private final HashSet<Integer> assignedEvents = new HashSet<>();
    
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
            Events events, Config config, StemUtils stemsHandler) {
        this.delta = delta;
        this.delta2 = delta2;
        this.gamma = gamma;
        this.timeSliceA = timeSliceA;
        this.timeSliceB = timeSliceB;
        this.minTermSupport = minTermSupport;
        this.maxTermSupport = maxTermSupport;
        this.eventList = events.list;
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
            String[] ids;
            int i = 1;
            while ((line = br.readLine()) != null) {
               terms = line.split("\t")[2].split(",");
               ids = line.split("\t")[3].split(",");
               HashSet<String> termSet = new HashSet<>();
               HashSet<String> idsSet = new HashSet<>();
               for(String term : terms) {
                   termSet.add(term.toLowerCase());
               }
               groundTruthTermsPerEvent.put(i, termSet);
               idsSet.addAll(Arrays.asList(ids));
               groundTruthIDsPerEvent.put(i, idsSet);
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
        List<String> calculatedKeywords;
        List<String> ids;
        HashSet<String> groundTruthKeywords;
        int groundTruthKeywordSize;
        int matchedItems;
        double recall;
        double precision;
        int eventKey;
        for(Event event : eventList) {
            matchedItems = 0;
            ids = new ArrayList<>(Arrays.asList(
                    event.getPhysicalDescription().split(" ")));
            eventKey = -1;
            for(String id : ids) {
                eventKey = findEventById(id);
                if(eventKey != -1) {
                    break;
                }
            }
            calculatedKeywords = new ArrayList<>(
                    Arrays.asList(event.getTextualDescription().split(" ")));
            if(eventKey != -1) {
                Utilities.printMessageln("Event found: " + eventKey);
                
                //Get the ground truth keywords and compare them 1 by 1
                groundTruthKeywords = new HashSet<>(groundTruthTermsPerEvent.get(eventKey));
                groundTruthKeywordSize = groundTruthKeywords.size();
                for(String keyword : calculatedKeywords) {
                    if(matchedItems != groundTruthKeywordSize) {
                        for(String groundTruthKeyword : groundTruthKeywords) {
                            if(stemsHandler.getOriginalWord(keyword).contains(groundTruthKeyword.toLowerCase())) {
                                matchedItems++;
                            }
                        }
                    } else {
                        break; //We matched all terms, so break iteration
                    }
                }
                recall = (double) matchedItems / (double) groundTruthKeywordSize;
                precision = (double) matchedItems / (double) calculatedKeywords.size();
                Utilities.printMessageln("Out of " + calculatedKeywords.size() + " items:");
                Utilities.printMessageln("Matched " + matchedItems + " out of " 
                        + groundTruthKeywordSize + " ground truth terms.");
                Utilities.printMessageln("Recall: " + recall);
                Utilities.printMessageln("Precision: " + precision);
            } else {
                Utilities.printMessageln("Event not found");
                recall = 0;
                precision = 0;
            }
            recallByEvent.add(recall);
            precisionByEvent.add(precision);
        }
        
    }
    
    /**
     * Returns the index of the matched event from the groundTruthTermsPerEvent map.
     * @param term A String representing the term.
     * @return An integer representing the actual event index or -1 if not found.
     */
    private int findEventByTerm(String term) {
        for(Integer key : groundTruthTermsPerEvent.keySet()) {
            HashSet<String> termSet = new HashSet<>(groundTruthTermsPerEvent.get(key));
            for(String _term : termSet) {
                if(term.contains(_term.toLowerCase()) && !assignedEvents.contains(key)) {
                    assignedEvents.add(key);
                    return key;
                }
            }
//            if(termSet.contains(term)) {
//                return key;
//            }
        }
        return -1;
    }
    
    /**
     * Find a ground truth event by a tweet ID.
     * @param id The tweet ID to be searched for.
     * @return The retrieved event key from the analysis.
     */
    private int findEventById(String id) {
        for(Integer key : groundTruthIDsPerEvent.keySet()) {
            HashSet<String> idsSet = new HashSet<>(groundTruthIDsPerEvent.get(key));
            if(idsSet.contains(id) && !assignedEvents.contains(key)) {
                assignedEvents.add(key);
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
    public final double getTotalRecall() {
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
    public final double getTotalPrecision() {
        double totalPrecision = 0;
        totalPrecision = precisionByEvent.stream().mapToDouble((precision) -> precision)
                .reduce(totalPrecision, (accumulator, _item) -> accumulator + _item);
        return totalPrecision;
    }
    
    public final double getRecall(int index) {
        return recallByEvent.get(index);
    }
    
    public final double getPrecision(int index) {
        return precisionByEvent.get(index);
    }
}
