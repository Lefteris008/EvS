/* 
 * Copyright (C) 2015 Adrien Guille <adrien.guille@univ-lyon2.fr>
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
import edmodule.dataset.Dataset;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author  Adrien GUILLE, ERIC Lab, University of Lyon 2
 * @email   adrien.guille@univ-lyon2.fr
 * 
 * @author  Lefteris Paraskevas (changes to omit missing classes)
 * @version 2015.12.06_1935_planet3 (For EDviaSA project version alignment) 
 */
public class EDCoW implements EDMethod {
    private final int delta = 1;
    private final int delta2 = 48;
    private final int gamma = 10;  
    private final double minTermSupport = 0.01; //0.0001
    private final double maxTermSupport = 0.9; //0.01
    private HashMap<String,Short[]> termDocMap;
    public LinkedList<EDCoWEvent> eventList;
    private final int timeSliceA;
    private final int timeSliceB;
    private final int countCorpus; //Total number of tweets
    private final Dataset ds;
    public Events events;
    
    public EDCoW(int timeSliceA, int timeSliceB, Dataset ds){
        this.timeSliceA = timeSliceA;
        this.timeSliceB = timeSliceB;
        this.countCorpus = ds.getNumberOfDocuments()[0];
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
        double minTermOccur = minTermSupport * countCorpus; //Min support * Message count corpus
        double maxTermOccur = maxTermSupport * countCorpus; //Max support * Message count corpus
        //Deltas and gammas already configured
    
        int windows = (timeSliceB-timeSliceA)/delta2;
        termDocMap = new HashMap<>();
        eventList = new LinkedList<>();
        
        for(int i = 0; i < ds.getTerms().size(); i++){
            String term = ds.getTerms().get(i);
            if(term.length() > 1) { //Stopwords check removed as they are already ommited when creating the dataset 
                Short[] frequency = ds.getDocumentsTermFrequency(i);
                int cf = 0;
                for(short freq : frequency){
                    cf += freq;
                }
                if(cf > minTermOccur && cf < maxTermOccur){
                    termDocMap.put(term, frequency);
                }
            }
        }
        for(int i = 0; i < windows; i++){
            processWindow(i);
        }
        Collections.sort(eventList);
        events = new Events();
        eventList.stream().forEach((event) -> {
            //events.list.add(new Event(event.getKeywordsAsString(),AppParameters.dataset.corpus.convertTimeSliceToDay((int)event.startSlice)+","+AppParameters.dataset.corpus.convertTimeSliceToDay((int)event.endSlice)));
        });
        events.setFullList();
    }
    
    public void processWindow(int window){
    	try{
            LinkedList<EDCoWKeyword> keyWords = new LinkedList<>();
            Integer[] distributioni = ds.getNumberOfDocuments();
            double[] distributiond = new double[delta2];
            int startSlice = window*delta2;
            int endSlice = startSlice+delta2-1;
            for(int i = startSlice; i < endSlice;  i++){
                distributiond[i-startSlice] = (double) distributioni[i]; 
            }
            for(Entry<String, Short[]> entry : termDocMap.entrySet()) {
                Short frequencyf[] = entry.getValue();
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

            //compute theta2 using the bigmatrix
            double theta2 = th1.theta2(bigMatrix, gamma);        
            for(int i = 0; i < keyWordsList1.size(); i++){
                for(int j = i+1; j < keyWordsList1.size(); j++){
                    bigMatrix[i][j] = (bigMatrix[i][j] < theta2) ? 0 : bigMatrix[i][j];
                }
            }
            EDCoWModularityDetection modularity = new EDCoWModularityDetection(keyWordsList1, bigMatrix, startSlice, endSlice);

            double thresholdE = 0.1;
            ArrayList<Community> finalArrCom= modularity.getCommunitiesFiltered(thresholdE);
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
        } catch (IOException ex) {
            Logger.getLogger(EDCoW.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(EDCoW.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
