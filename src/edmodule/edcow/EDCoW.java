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
import ch.epfl.lis.networks.NetworkException;
import edmodule.EDMethod;
import edmodule.dataset.Dataset;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import java.util.logging.Level;
import java.util.logging.Logger;
import utilities.Utilities;

/**
 *
 * @author  Adrien GUILLE, ERIC Lab, University of Lyon 2
 * @email   adrien.guille@univ-lyon2.fr
 * 
 * @author  Lefteris Paraskevas (configurations in EDCoW to omit missing components)
 * @version 2015.12.21_2001_gargantua (For EDviaSA project version alignment) 
 */
public class EDCoW implements EDMethod {
    private final int delta = 10; //6
    private final int delta2 = 43; //48
    private final int gamma = 4; //5
    private final double minTermSupport = 0.0001; //0.0001
    private final double maxTermSupport = 0.01; //0.01
    private HashMap<String, Integer[]> termDocMap;
    public LinkedList<EDCoWEvent> eventList;
    private final int timeSliceA;
    private final int timeSliceB;
    private int countCorpus = 0; //Total number of tweets
    private final Dataset ds;
    public Events events;
    
    public EDCoW(int timeSliceA, int timeSliceB, Dataset ds){
        this.timeSliceA = timeSliceA;
        this.timeSliceB = timeSliceB;
        this.countCorpus = 0;
        for (Integer numberOfDocument : ds.getNumberOfDocuments()) {
            this.countCorpus += numberOfDocument;
        }
        this.ds = ds;
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
        //Deltas and gammas already configured
    
        int windows = (timeSliceB - timeSliceA) / delta2;
        termDocMap = new HashMap<>();
        eventList = new LinkedList<>();
        
        Utilities.printInfoMessage("Now calculating term frequencies...");
        List<String> terms = ds.getTerms();
        for(int i = 0; i < terms.size(); i++){
            String term = terms.get(i);
            if(term.length() > 1) { //Stopwords check removed as they are already ommitted when creating the dataset 
                Integer[] frequency = ds.getDocumentsTermFrequency(i);
                int cf = 0;
                for(int freq : frequency){
                    cf += freq;
                }
                if(cf > minTermOccur && cf < maxTermOccur){
                    termDocMap.put(term, frequency);
                }
            }
        }
        Utilities.printInfoMessage("Now calculating windows...");
        for(int i = 0; i < windows; i++) {
            Utilities.printInfoMessage("Calculating window " + (i + 1) + "\n");
            processWindow(i);
        }
        Collections.sort(eventList);
        events = new Events();
        eventList.stream().forEach((event) -> {
            //events.list.add(new Event(event.getKeywordsAsString(),AppParameters.dataset.corpus.convertTimeSliceToDay((int)event.startSlice)+","+AppParameters.dataset.corpus.convertTimeSliceToDay((int)event.endSlice)));
        });
        events.setFullList();
        
        long endTime = System.currentTimeMillis();
        Utilities.printExecutionTime(startTime, endTime, EDCoW.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName());
    }
    
    public void processWindow(int window) {
    	try{
            LinkedList<EDCoWKeyword> keyWords = new LinkedList<>();
            Integer[] distributioni = ds.getNumberOfDocuments();
            double[] distributiond = new double[delta2];
            int startSlice = window * delta2;
            int endSlice = startSlice + delta2;
            for(int i = startSlice; i < endSlice; i++){
                distributiond[i-startSlice] = (double) distributioni[i]; 
            }
            for(Entry<String, Integer[]> entry : termDocMap.entrySet()) {
                Integer frequencyf[] = entry.getValue();
                double frequencyd[] = new double[delta2];
                for(int i = startSlice; i < endSlice; i++){
                    frequencyd[i-startSlice] = (double) frequencyf[i];
                }
                keyWords.add(new EDCoWKeyword(entry.getKey(), frequencyd, delta, distributiond));
            }
            double[] autoCorrelationValues = new double[keyWords.size()];
            for(int i = 0; i < keyWords.size(); i++){
                autoCorrelationValues[i] = keyWords.get(i).getAutoCorrelation();
            }
            EDCoWThreshold th1 = new EDCoWThreshold();
            double theta1 = th1.theta1(autoCorrelationValues, gamma);

            // Removing trivial keywords based on theta1
            LinkedList<EDCoWKeyword> keyWordsList1 = new LinkedList<>();
            for(EDCoWKeyword k : keyWords) {
                if(k.getAutoCorrelation() > theta1) {
                    keyWordsList1.add(k);
                }
            }
//            keyWords.stream().filter((k) -> (k.getAutoCorrelation() > theta1)).forEach((k) -> {
//                keyWordsList1.add(k);
//            });     
            for(EDCoWKeyword kw1 : keyWordsList1) {
                kw1.computeCrossCorrelation(keyWordsList1);
            }
//            keyWordsList1.stream().forEach((kw1) -> {
//                kw1.computeCrossCorrelation(keyWordsList1);
//            });
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
            for(Community c : finalArrCom) {
                 System.out.println(c.getCommunitySize());
                 modularity.saveEventFromCommunity(c);
            }
            eventList.addAll(modularity.getEvents());
//            finalArrCom.stream().map((c) -> {
//                System.out.println(c.getCommunitySize());
//                return c;
//                }).forEach((c) -> {
//                    modularity.saveEventFromCommunity(c);
//                });
//            eventList.addAll(modularity.getEvents());
        } catch (NullPointerException e) {
            //Do nothing
        } catch (IOException | NetworkException e) {
            Logger.getLogger(EDCoW.class.getName()).log(Level.SEVERE, null, e);
        } catch (Exception e) {
            Logger.getLogger(EDCoW.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}
