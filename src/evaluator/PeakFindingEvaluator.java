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

import edmodule.peakfinding.event.PeakFindingEvent;
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
import utilities.Config;
import utilities.Utilities;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2016.04.30_1827
 */
public class PeakFindingEvaluator implements AbstractEvaluator {
    private final double alpha;
    private final int taph;
    private final int pi;
    private final Config config;
    private List<PeakFindingEvent> eventList;
    private final HashMap<Integer, HashSet<String>> groundTruthTermsPerEvent = new HashMap<>();
    private final HashMap<Integer, HashSet<String>> groundTruthTweetIDsPerEvent = new HashMap<>();
    private final HashMap<Integer, Integer> matchedGroundTruthEventIDs = new HashMap<>();
    private final List<Double> precisionByEvent = new ArrayList<>();
    private final List<Double> recallByEvent = new ArrayList<>();
    private final HashSet<Integer> assignedEvents = new HashSet<>();
    
    /**
     * Public constructor.
     * @param alpha A double value representing alpha parameter.
     * @param taph An integer value representing taph parameter.
     * @param pi An integer value representing pi parameter.
     * @param eventList A List containing the events found by the algorithm.
     * @param config A configuration object.
     */
    public PeakFindingEvaluator(double alpha, int taph, int pi, 
            List<PeakFindingEvent> eventList, Config config) {
        this.alpha = alpha;
        this.taph = taph;
        this.pi = pi;
        this.config = config;
        this.eventList = new ArrayList<>(eventList);
        loadGroundTruthDataset();
    }
    
    /**
     * Method to load a ground truth dataset for future use.
     * More formally it creates a HashMap that contains integer IDs as keys
     * and a HashSet of the terms of a specific event, as values.
     */
    @Override
    public void loadGroundTruthDataset() {
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
               groundTruthTweetIDsPerEvent.put(i, idsSet);
               i++;
            }
        } catch (FileNotFoundException e) {
            Logger.getLogger(PeakFindingEvaluator.class.getName()).log(Level.SEVERE, null, e);
        } catch (IOException e) {
            Logger.getLogger(PeakFindingEvaluator.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
    @Override
    public void evaluate(boolean showInlineInfo) {
        ///
    }
    
    /**
     * Run the evaluation using only the 5 most common terms of every event.
     * @param showInlineInfo Boolean flag that indicates whether to show or hide
     * inline information during execution.
     */
    public void evaluateWithCommonTerms(boolean showInlineInfo) {
        HashSet<String> groundTruthKeywords;
        ArrayList<String> calculatedKeywords;
        int groundTruthKeywordSize;
        double recall, precision;
        int eventKey = -1;
        int matchedItems = 0;
        for(PeakFindingEvent event : eventList) {
            matchedItems = 0;
            for(String id : event.getTweetIDs()) {
                if((eventKey = findEventById(id)) != -1) {
                    break;
                }
            }
            calculatedKeywords = new ArrayList<>(event.getCommonTerms());
            matchedGroundTruthEventIDs.put(event.getID(), eventKey);
            if(eventKey != -1) {
                //Get the ground truth keywords and compare them 1 by 1
                groundTruthKeywords = new HashSet<>(groundTruthTermsPerEvent.get(eventKey));
                groundTruthKeywordSize = groundTruthKeywords.size();
                for(String keyword : calculatedKeywords) {
                    if(matchedItems != groundTruthKeywordSize) {
                        for(String groundTruthKeyword : groundTruthKeywords) {
                            if(keyword.toLowerCase().contains(groundTruthKeyword.toLowerCase())) {
                                matchedItems++;
                            }
                        }
                    } else {
                        break; //We matched all terms, so break iteration
                    }
                }
                recall = (double) matchedItems / (double) groundTruthKeywordSize;
                precision = (double) matchedItems / (double) calculatedKeywords.size();
                
                //Show inline info during execution if the user chose so
                if(showInlineInfo) {
                    Utilities.printMessageln("Event found: " + eventKey);
                    Utilities.printMessageln("Out of " + calculatedKeywords.size() + " items:");
                    Utilities.printMessageln("Matched " + matchedItems + " out of " 
                            + groundTruthKeywordSize + " ground truth terms.");
                    Utilities.printMessageln("Recall: " + recall);
                    Utilities.printMessageln("Precision: " + precision);
                }
            } else {
                recall = 0;
                precision = 0;
                if(showInlineInfo) {
                    Utilities.printMessageln("Event not found");
                }
            }
            recallByEvent.add(recall);
            precisionByEvent.add(precision);
        }
    }
    
    /**
     * Run the evaluation method using all the generated terms in a specific event.
     * @param showInlineInfo Boolean flag that indicates whether to show or hide
     * inline information during execution.
     */
    public void evaluateWithAllTerms(boolean showInlineInfo) {
        HashSet<String> groundTruthKeywords;
        ArrayList<String> calculatedKeywords;
        int groundTruthKeywordSize;
        double recall, precision;
        int eventKey = -1;
        int matchedItems = 0;
        for(PeakFindingEvent event : eventList) {
            matchedItems = 0;
            for(String id : event.getTweetIDs()) {
                if((eventKey = findEventById(id)) != -1) {
                    break;
                }
            }
            calculatedKeywords = new ArrayList<>(event.getAllTerms());
            matchedGroundTruthEventIDs.put(event.getID(), eventKey);
            if(eventKey != -1) {
                //Get the ground truth keywords and compare them 1 by 1
                groundTruthKeywords = new HashSet<>(groundTruthTermsPerEvent.get(eventKey));
                groundTruthKeywordSize = groundTruthKeywords.size();
                for(String keyword : calculatedKeywords) {
                    if(matchedItems != groundTruthKeywordSize) {
                        for(String groundTruthKeyword : groundTruthKeywords) {
                            if(event.getStemsHandler().getOriginalWord(keyword).toLowerCase().contains(groundTruthKeyword.toLowerCase())) {
                                matchedItems++;
                            }
                        }
                    } else {
                        break; //We matched all terms, so break iteration
                    }
                }
                recall = (double) matchedItems / (double) groundTruthKeywordSize;
                precision = (double) matchedItems / (double) calculatedKeywords.size();
                //Show inline info during execution if the user chose so
                if(showInlineInfo) {
                    Utilities.printMessageln("Event found: " + eventKey);
                    Utilities.printMessageln("Out of " + calculatedKeywords.size() + " items:");
                    Utilities.printMessageln("Matched " + matchedItems + " out of " 
                            + groundTruthKeywordSize + " ground truth terms.");
                    Utilities.printMessageln("Recall: " + recall);
                    Utilities.printMessageln("Precision: " + precision);
                }
            } else {
                recall = 0;
                precision = 0;
                if(showInlineInfo) {
                    Utilities.printMessageln("Event not found");
                }
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
                if(term.toLowerCase().contains(_term.toLowerCase()) && !assignedEvents.contains(key)) {
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
        for(Integer key : groundTruthTweetIDsPerEvent.keySet()) {
            HashSet<String> idsSet = new HashSet<>(groundTruthTweetIDsPerEvent.get(key));
            if(idsSet.contains(id) && !assignedEvents.contains(key)) {
                assignedEvents.add(key);
                return key;
            }
        }
        return -1;
    }
    
    /**
     * Method to return the alpha parameter that this instance of evaluation used.
     * @return A double representing the alpha value.
     */
    public final double getAlphaValue() { return alpha; }
    
    /**
     * Method to return the taph parameter that this instance of evaluation used.
     * @return An integer representing the taph value.
     */
    public final int getTaphValue() { return taph; }
    
    /**
     * Method to return the pi parameter that this instance of evaluation used.
     * @return An integer representing the pi value.
     */
    public final int getPiValue() { return pi; }
    
    /**
     * Calculates and returns the total recall of the calculated dataset.
     * @return A double containing the total recall of the calculated dataset
     * compared with the ground truth data.
     */
    @Override
    public final double getTotalRecall() {
        return assignedEvents.size() / groundTruthTweetIDsPerEvent.size();
    }
    
   /**
     * Method to return the recall value of a specific event.
     * @param index The index of the event.
     * @return A double value representing the recall of the event.
     */
    @Override
    public final double getRecall(int index) {
        return recallByEvent.get(index);
    }
    
    /**
     * Method to return the precision value of a specific event.
     * @param index The index of the event.
     * @return A double value representing the precision of the event.
     */
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
}
