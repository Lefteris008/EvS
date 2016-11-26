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
package com.left8.evs.evs.evaluator;

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

import com.left8.evs.evs.edcow.event.SentimentEDCoWEvent;
import com.left8.evs.evs.edcow.event.SentimentEDCoWEvents;
import com.left8.evs.preprocessingmodule.nlp.stemming.StemUtils;
import com.left8.evs.utilities.Config;
import com.left8.evs.utilities.PrintUtilities;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2016.11.26_1304
 */
public class SentimentEDCoWEvaluator implements AbstractSentimentEvaluator {
    private int delta;
    private int delta2;
    private int gamma;
    private int timeSliceA;
    private int timeSliceB;
    private double minTermSupport;
    private double maxTermSupport;
    private List<SentimentEDCoWEvent> eventList;
    private Config config;
    private final HashMap<Integer, HashSet<String>> groundTruthTermsPerEvent = new HashMap<>();
    private final HashMap<Integer, HashSet<String>> groundTruthIDsPerEvent = new HashMap<>();
    private final HashMap<Integer, Integer> matchedGroundTruthEventIDs = new HashMap<>();
    private StemUtils stemsHandler;
    private final List<Double> precisionByEvent = new ArrayList<>();
    private final List<Double> recallByEvent = new ArrayList<>();
    private final HashSet<Integer> assignedEvents = new HashSet<>();
    private SentimentEDCoWEvents events;
    
    public SentimentEDCoWEvaluator() {
        ///
    }
    
    /**
     * Public constructor.
     * @param delta An integer representing the delta value.
     * @param delta2 An integer representing the delta2 value.
     * @param gamma An integer representing the gamma value.
     * @param timeSliceA An integer representing the starting time slice of the dataset.
     * @param timeSliceB An integer representing the ending time slice of the dataset.
     * @param minTermSupport A double representing the minimum term support of the terms in an event.
     * @param maxTermSupport A double representing the maximum term support of the terms in an event.
     * @param events A list containing the events after the application of EDCoW algorithm.
     * @param config A configuration object.
     * @param stemsHandler A StemUtils object.
     */
    public SentimentEDCoWEvaluator(int delta, int delta2, int gamma, int timeSliceA, 
            int timeSliceB, double minTermSupport, double maxTermSupport, 
            SentimentEDCoWEvents events, Config config, StemUtils stemsHandler) {
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
        this.events = events;
        loadGroundTruthDataset();
    }
    
    /**
     * Method to load a ground truth dataset for future use.
     * More formally it creates a HashMap that contains integer IDs as keys
     * and a HashSet of the terms of a specific event, as values.
     */
    @Override
    public final void loadGroundTruthDataset() {
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
            Logger.getLogger(SentimentEDCoWEvaluator.class.getName()).log(Level.SEVERE, null, e);
        } catch (IOException e) {
            Logger.getLogger(SentimentEDCoWEvaluator.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
    /**
     * Method to evaluate an extracted EDCoW dataset.
     * More formally, it calculates the recall and precision of a EDCoW dataset
     * that is calculated after the application of the algorithm and exports this
     * data into a file, along with other useful metrics.
     * @param showInlineInfo A switch to determine whether inline information 
     * should be printed
     */
    @Override
    public void evaluate(boolean showInlineInfo) {
        List<String> calculatedKeywords;
        List<String> ids;
        HashSet<String> groundTruthKeywords;
        int groundTruthKeywordSize;
        int matchedItems;
        double recall;
        double precision;
        int eventKey;
        int eventCounter = 0;
        for(SentimentEDCoWEvent event : eventList) {
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
            matchedGroundTruthEventIDs.put(eventCounter, eventKey);
            eventCounter++;
            if(eventKey != -1) {
                
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
                if(showInlineInfo) {
                    PrintUtilities.printInfoMessageln("Event found: " + eventKey);
                    PrintUtilities.printInfoMessageln("Out of " + calculatedKeywords.size() + " items:");
                    PrintUtilities.printInfoMessageln("Matched " + matchedItems + " out of " 
                            + groundTruthKeywordSize + " ground truth terms.");
                    PrintUtilities.printInfoMessageln("Recall: " + recall);
                    PrintUtilities.printInfoMessageln("Precision: " + precision);
                }
            } else {
                if(showInlineInfo) {
                    PrintUtilities.printInfoMessageln("Event not found.");
                }
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
    @Override
    public int findEventByTerm(String term) {
        for(Integer key : groundTruthTermsPerEvent.keySet()) {
            HashSet<String> termSet = new HashSet<>(groundTruthTermsPerEvent.get(key));
            for(String _term : termSet) {
                if(term.contains(_term.toLowerCase()) && !assignedEvents.contains(key)) {
                    assignedEvents.add(key);
                    return key;
                }
            }
        }
        return -1;
    }
    
    /**
     * Find a ground truth event by a tweet ID.
     * @param id The tweet ID to be searched for.
     * @return The retrieved event key from the analysis.
     */
    @Override
    public int findEventById(String id) {
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
     * compared with the ground truth data. If no events were present, -1 is 
     * returned instead.
     */
    @Override
    public final double getTotalRecall() {
        return assignedEvents.size() / groundTruthIDsPerEvent.size();
    }
    
    @Override
    public final double getRecall(int index) {
        return recallByEvent.get(index);
    }
    
    @Override
    public final double getPrecision(int index) {
        return precisionByEvent.get(index);
    }
    
    /**
     * Method to return the ground truth event ID that the 'eventID' event was
     * matched to.
     * @param eventID The event to search for.
     * @return A integer representing the ground event ID.
     */
    public final int getMatchedGroundTruthID(int eventID) {
        return matchedGroundTruthEventIDs.get(eventID);
    }
    
    /**
     * Returns the value of delta parameter.
     * @return An integer representing the delta parameter.
     */
    public int getDeltaValue() { return delta; }

    /**
     * Returns the value of delta2 parameter.
     * @return An integer representing the delta2 parameter.
     */
    public int getDelta2Value() { return delta2; }

    /**
     * Returns the value of gamma parameter.
     * @return An integer representing the gamma parameter.
     */
    public int getGammaValue() { return gamma; }

    /**
     * Returns the value of timeSliceA parameter.
     * @return An integer representing the timeSliceA parameter.
     */
    public int getTimeSliceAValue() { return timeSliceA; }

    /**
     * Returns the value of timeSliceB parameter.
     * @return An integer representing the timeSliceB parameter.
     */
    public int getTimeSliceBValue() { return timeSliceB; }

    /**
     * Returns the value of minTermSupport parameter.
     * @return An integer representing the minTermSupport parameter.
     */
    public double getMinTermSupportValue() { return minTermSupport; }

    /**
     * Returns the value of maxTermSupport parameter.
     * @return An integer representing the maxTermSupport parameter.
     */
    public double getMaxTermSupportValue() { return maxTermSupport; }
}
