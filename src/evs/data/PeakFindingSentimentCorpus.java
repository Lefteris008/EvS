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
package evs.data;

import dsretriever.Tweet;
import edmodule.data.PeakFindingCorpus;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2016.03.16_1213
 */
public class PeakFindingSentimentCorpus {
    private final PeakFindingCorpus corpus;
    private final Map<String, SentimentWindowEntity> sentimentsByWindow = new HashMap<>();
    
    public PeakFindingSentimentCorpus(PeakFindingCorpus corpus) {
        this.corpus = corpus;
        calculateSentimentsByWindow();
    }
    
    /**
     * Calculates the sentiments by window. <br/>
     * More formally, it calculates and stores the total amount of positive,
     * negative, neutral and irrelevant annotated tweets and stores the statistics
     * in a SentimentWindowEntity for future use.
     */
    private void calculateSentimentsByWindow() {
        Map<String, ArrayList<Tweet>> tweetsByWindow = corpus.getTweetsByWindow();
        for(String key : tweetsByWindow.keySet()) {
            List<Tweet> tweetsInWindow = tweetsByWindow.get(key);
            int mainSentiment;
            int posCounter = 0;
            int negCounter = 0;
            int neuCounter = 0;
            int irrCounter = 0;
            for(Tweet tweet : tweetsInWindow) {
                if(tweet.getWekaSentiment() == 0) {
                    negCounter++;
                } else if(tweet.getWekaSentiment() == 1) {
                    neuCounter++;
                } else if(tweet.getWekaSentiment() == 2) {
                    posCounter++;
                } else {
                    irrCounter++;
                }
            }
            int max = Math.max(posCounter, 
                Math.max(negCounter, 
                    Math.max(neuCounter, irrCounter)
                )
            );
            if(max == posCounter) {
                mainSentiment = 0;
            } else if(max == negCounter) {
                mainSentiment = 2;
            } else if(max == neuCounter) {
                mainSentiment = 1;
            } else { //Irrelevant
                mainSentiment = -2;
            }
            double posPerc = (double) posCounter / (double) tweetsInWindow.size();
            double negPerc = (double) negCounter / (double) tweetsInWindow.size();
            double neuPerc = (double) neuCounter / (double) tweetsInWindow.size();
            double irrPerc = (double) irrCounter / (double) tweetsInWindow.size();
            SentimentWindowEntity sEntity = new SentimentWindowEntity(posPerc, 
                    negPerc, neuPerc, irrPerc, mainSentiment);
            sentimentsByWindow.put(key, sEntity);
        }
    }
    
    /**
     * Returns a Map containing SentimentWindowEntity objects grouped by window.
     * @return A HashMap containing SentimentWindowEntity objects grouped by window.
     */
    public final Map<String, SentimentWindowEntity> getSentimentsByWindow() {
        return sentimentsByWindow;
    }
    
    /**
     * Returns the original PeakFindingCorpus object from ED analysis.
     * @return A PeakFindingCorpus object.
     */
    public final PeakFindingCorpus getPeakFindingCorpus() { return corpus; }
    
    /**
     * Returns a SentimentWindowEntity object of a specific window.
     * @param key The String key of the window.
     * @return A SentimentWindowEntity object.
     */
    public final SentimentWindowEntity getSentimentEntityOfWindow(String key) {
        return sentimentsByWindow.get(key);
    }
}
