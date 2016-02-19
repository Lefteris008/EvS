/* 
 * Copyright (C) 2016 Adrien Guille <adrien.guille@univ-lyon2.fr>
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
package edmodule.edcow;

import edmodule.edcow.event.Events;
import ch.epfl.lis.jmod.modularity.community.Community;
import edmodule.EDMethod;
import edmodule.data.EDCoWCorpus;
import edmodule.edcow.event.Event;
import java.util.*;

import java.util.logging.Level;
import java.util.logging.Logger;
import utilities.Utilities;

/**
 *
 * @author  Adrien GUILLE, ERIC Lab, University of Lyon 2
 * @email   adrien.guille@univ-lyon2.fr
 * 
 * @author  Lefteris Paraskevas (configurations in EDCoW to omit missing components)
 * @version 2016.02.19_1709 (For EDviaSA project version alignment) 
 */
public class EDCoW implements EDMethod {
    private final int delta; //6
    private final int delta2;
    private final int gamma; //5
    private final double minTermSupport; //0.0001
    private final double maxTermSupport; //0.01
    private HashMap<String, Integer[]> termDocMap;
    private LinkedList<EDCoWEvent> eventList;
    private final int timeSliceA;
    private final int timeSliceB;
    private int countCorpus = 0; //Total number of tweets
    private final EDCoWCorpus corpus;
    public Events events;
    
    /**
     * Default constructor with minimum parameters. <br/>
     * Delta is set to 6, gamma is set to 5, minimum term support is set to
     * 0.0001 and maximum term support is set to 0.01. If you wish to change the
     * aforementioned values use the {@link #EDCoW(int, int, int, double, double,
     * int, int, EDCoWCorpus) second constructor}.
     * @param delta2 Delta2 value. <br/>
     * Prime divisors of the number of documents are required as values. It must
     * be cross-referenced with the number of documents. More specifically, the
     * outcome of the division between the number of documents and this metric
     * should result the number of total windows.
     * @param timeSliceA Beginning timeslice.
     * @param timeSliceB Ending timeslice.
     * @param corpus An EDCoWCorpus object.
     */
    public EDCoW(int delta2, int timeSliceA, int timeSliceB, EDCoWCorpus corpus) {
        this.delta = 6;
        this.delta2 = delta2;
        this.gamma = 5;
        this.minTermSupport = 0.0001;
        this.maxTermSupport = 0.01;
        this.timeSliceA = timeSliceA;
        this.timeSliceB = timeSliceB;
        this.corpus = corpus;
    }
    
    /**
     * Default constructor with the full set of parameters.
     * @param delta1 Delta value (suggested 6). <br/>
     * It directly affects the number of events. Increasing this value, reduces
     * the number of them and vice versa.
     * @param delta2 Delta2 value. <br/>
     * Prime divisors of the number of documents are required as values. It must
     * be cross-referenced with the number of documents. More specifically, the
     * outcome of the division between the number of documents and this metric
     * should result the number of total windows.
     * @param gamma Gamma value (suggested 5). <br/>
     * It affects the quality of the uncovered events. Values greater than 15,
     * seem to increase the number of the uncovered events.
     * @param minTermSupport Minimum term support value (suggested 0.0001). <br/>
     * Changing this value would result in altering the lower bound below which
     * a term should not be included in the keywords list of an event.
     * @param maxTermSupport Maximum term support value (suggested 0.01). <br/>
     * Changing this value would result in altering the upper bound above which
     * a term should not be included in the keywords list of an event.
     * @param timeSliceA Starting timeslice.
     * @param timeSliceB Ending timeslice.
     * @param corpus An EDCoWCorpus object.
     * @see #EDCoW(int, int, int, EDCoWCorpus) EDCoW() minimum constructor.
     */
    public EDCoW(int delta1, int delta2, int gamma, double minTermSupport, 
            double maxTermSupport, int timeSliceA, int timeSliceB, 
            EDCoWCorpus corpus) {
        this.delta = delta1;
        this.delta2 = delta2;
        this.gamma = gamma;
        this.minTermSupport = minTermSupport;
        this.maxTermSupport = maxTermSupport;
        this.timeSliceA = timeSliceA;
        this.timeSliceB = timeSliceB;
        this.countCorpus = 0;
        this.corpus = corpus;
        for (Integer numberOfDocument : corpus.getNumberOfDocuments()) {
            this.countCorpus += numberOfDocument;
        }
    }

    @Override
    public String getName() {
        return "EDCoW";
    }

    @Override
    public String getCitation() {
        return "<li><b>EDCoW:</b> J. Weng and B. Lee (2011) Event Detection in Twitter, In Proceedings of the 2011 AAAI Conference on Weblogs and Social Media (ICWSM), pp. 401-408</li>";
    }
    
    @Override
    public String getAuthors() {
        return "J. Weng and B. Lee";
    }
    
    @Override
    public String getDescription() {
        return "Event detection with clustering of wavelet-based signals";
    }

    @Override
    public void apply() {
        long startTime = System.currentTimeMillis();
        
        double minTermOccur = minTermSupport * countCorpus; //Min support * Message count corpus
        double maxTermOccur = maxTermSupport * countCorpus; //Max support * Message count corpus
    
        int windows = (timeSliceB - timeSliceA) / delta2;
        termDocMap = new HashMap<>();
        eventList = new LinkedList<>();
        
        Utilities.printMessageln("Calculating term frequencies...");
        List<String> terms = corpus.getTerms();
        for(int i = 0; i < terms.size(); i++){
            String term = terms.get(i);
            if(term.length() > 1) { //Stopwords check removed as they are already ommitted when creating the dataset 
                Integer[] frequency = corpus.getDocumentsTermFrequency(i);
                int cf = 0;
                for(int freq : frequency){
                    cf += freq;
                }
                if(cf > minTermOccur && cf < maxTermOccur){
                    termDocMap.put(term, frequency);
                }
            }
        }
        Utilities.printMessageln("Calculating windows...");
        for(int i = 0; i < windows; i++) {
            Utilities.printMessageln("Calculating window " + (i + 1) + "\n");
            try {
                processWindow(i);
            } catch (Exception ex) {
                Logger.getLogger(EDCoW.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        Collections.sort(eventList);
        events = new Events();
        
        eventList.stream().forEach((event) -> {
            //try {
                events.list.add(new Event(
                        event.getKeywordsIDsAsString(), 
                        corpus.getDateFromTimeSlice(
                                (int)event.startSlice) + "," 
                                + corpus.getDateFromTimeSlice((int)event.endSlice - 1), 
                        corpus.getIDsOfWindowAsString(
                                corpus.getDateFromTimeSlice((int)event.startSlice), 
                                corpus.getDateFromTimeSlice((int)event.endSlice - 1))));
//            } catch(ArrayIndexOutOfBoundsException e) {
//                System.out.println("");
//            }
        });
        
        events.setFullList();
        
        long endTime = System.currentTimeMillis();
        Utilities.printExecutionTime(startTime, endTime, EDCoW.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName());
    }
    
    /**
     * Method to run the algorithm and analyze terms and frequencies in a
     * specific window.
     * @param window The window index (0, 1, 2 etc).
     */
    public void processWindow(int window) throws Exception {
    	//try{
            LinkedList<EDCoWKeyword> keyWords = new LinkedList<>();
            Integer[] distributioni = corpus.getNumberOfDocuments();
            double[] distributiond = new double[delta2];
            int startSlice = window * delta2;
            int endSlice = startSlice + delta2 - 1;
            for(int i = startSlice; i < endSlice; i++){
                distributiond[i-startSlice] = (double) distributioni[i]; 
            }
            termDocMap.entrySet().stream().forEach((entry) -> {
                Integer frequencyf[] = entry.getValue();
                double frequencyd[] = new double[delta2];
                for(int i = startSlice; i < endSlice; i++){
                    frequencyd[i-startSlice] = (double) frequencyf[i];
                }
                keyWords.add(new EDCoWKeyword(entry.getKey(), frequencyd, delta, distributiond));
            });
            double[] autoCorrelationValues = new double[keyWords.size()];
            for(int i = 0; i < keyWords.size(); i++){
                autoCorrelationValues[i] = keyWords.get(i).getAutoCorrelation();
            }
            EDCoWThreshold th1 = new EDCoWThreshold();
            double theta1 = th1.theta1(autoCorrelationValues, gamma);

            // Removing trivial keywords based on theta1
            LinkedList<EDCoWKeyword> keyWordsList1 = new LinkedList<>();
            keyWords.stream().filter((k) -> (k.getAutoCorrelation() > theta1)).forEach((k) -> {
                keyWordsList1.add(k);
            });
            
            keyWordsList1.stream().forEach((kw1) -> {
                kw1.computeCrossCorrelation(keyWordsList1);
            });
            
            double[][] bigMatrix = new double[keyWordsList1.size()][keyWordsList1.size()];
            for(int i=0; i < keyWordsList1.size(); i++){
                bigMatrix[i] = keyWordsList1.get(i).getCrossCorrelation();
            }

            //Compute theta2 using the BigMatrix
            double theta2 = th1.theta2(bigMatrix, gamma);        
            for(int i = 0; i < keyWordsList1.size(); i++){
                for(int j = i+1; j < keyWordsList1.size(); j++){
                    bigMatrix[i][j] = (bigMatrix[i][j] < theta2) ? 0 : bigMatrix[i][j];
                }
            }
            EDCoWModularityDetection modularity = new EDCoWModularityDetection(keyWordsList1, bigMatrix, startSlice, endSlice);

            double thresholdE = 0.1;
            ArrayList<Community> finalArrCom = modularity.getCommunitiesFiltered(thresholdE);
            finalArrCom.stream().map((c) -> {
                System.out.println(c.getCommunitySize());
                return c;
                }).forEach((c) -> {
                    modularity.saveEventFromCommunity(c);
                });
            eventList.addAll(modularity.getEvents());
            
        //} catch (NullPointerException e) {
            //Do nothing
        //} catch (IOException | NetworkException e) {
            //Logger.getLogger(EDCoW.class.getName()).log(Level.SEVERE, null, e);
        //} catch (Exception e) {
            //Logger.getLogger(EDCoW.class.getName()).log(Level.SEVERE, null, e);
        //}
    }
}