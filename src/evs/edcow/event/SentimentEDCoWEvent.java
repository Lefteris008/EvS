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
package evs.edcow.event;

import dsretriever.Tweet;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author  Adrien GUILLE, Laboratoire ERIC, Université Lumière Lyon 2
 * @author  Lefteris Paraskevas
 * @version 2016.03.28_0004
 */
public class SentimentEDCoWEvent implements Serializable {
    private SimpleStringProperty textualDescription;
    private SimpleStringProperty temporalDescription;
    private SimpleStringProperty physicalDescription;
    private List<Tweet> tweetsOfEvent;
    private SimpleDoubleProperty score;
    private int mainSentiment;
    private double positiveSentimentPerc;
    private double negativeSentimentPerc;
    private double neutralSentimentPerc;
    private double irrelevantSentimentPerc;
    private int uniqueUsers;

    public SentimentEDCoWEvent(String text, String temp, String physical, List<Tweet> tweets, 
            int sentimentSource) {
        this(text, temp, physical, tweets, sentimentSource, 0);
    }

    public SentimentEDCoWEvent(String text, String temp, String physical, 
            List<Tweet> tweets, int sentimentSource, double score) {
        textualDescription = new SimpleStringProperty(text);
        temporalDescription = new SimpleStringProperty(temp);
        physicalDescription = new SimpleStringProperty(physical);
        this.tweetsOfEvent = tweets;
        this.score = new SimpleDoubleProperty(score);
        calculateSentimentStatistics(sentimentSource);
        calculateUniqueUsers();
    }

    public String getTextualDescription() {
        return textualDescription.get();
    }

    public String getTemporalDescription() {
        return temporalDescription.get();
    }
    
    public String getPhysicalDescription() {
        return physicalDescription.get();
    }
    
    public String getTemporalDescriptionLowerBound() {
        return temporalDescription.getValue().split(",")[0];
    }
    
    public String getTemporalDescriptionUpperBound() {
        return temporalDescription.getValue().split(",")[1];
    }

    public void setTextualDescription(String newText) {
        textualDescription.set(newText);
    }

    public void setTemporalDescription(String newTemp) {
        temporalDescription.set(newTemp);
    }

    public double getScore() {
        return score.getValue();
    }

    public void setScore(double score) {
        this.score.set(score);
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeObject(textualDescription.get());
        out.writeObject(temporalDescription.get());
        out.writeDouble(score.get());
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException {
        try {
            textualDescription = new SimpleStringProperty((String)in.readObject());
            temporalDescription = new SimpleStringProperty((String)in.readObject());
            score = new SimpleDoubleProperty(in.readDouble());
        } catch (ClassNotFoundException ignored) {
            throw new IOException(ignored);
        }
    }
    
    private void calculateSentimentStatistics(int sentimentSource) {
        int positiveCounter = 0;
        int negativeCounter = 0;
        int neutralCounter = 0;
        int irrelevantCounter = 0;
        int returnedSentiment;
        
        for(Tweet tweet : tweetsOfEvent) {
            if(sentimentSource == 0) { //Stanford
                returnedSentiment = tweet.getStanfordSentiment();
                if(returnedSentiment < 2) {
                    negativeCounter++;
                } else if(returnedSentiment > 2) {
                    positiveCounter++;
                } else { //2
                    neutralCounter++;
                }
            } else { //Weka
                if(sentimentSource == 1) {
                    returnedSentiment = tweet.getNaiveBayesSentiment();
                } else {
                    returnedSentiment = tweet.getBayesianNetSentiment();
                }
                if(returnedSentiment == 0) {
                    negativeCounter++;
                } else if(returnedSentiment == 1) {
                    neutralCounter++;
                } else if(returnedSentiment == 2) {
                    positiveCounter++;
                } else { //-2
                    irrelevantCounter++;
                }
            }   
        }
        int max = Math.max(positiveCounter, 
                Math.max(negativeCounter, 
                    Math.max(neutralCounter, irrelevantCounter)
                )
            );
        if(max == positiveCounter) {
            mainSentiment = 0;
        } else if(max == negativeCounter) {
            mainSentiment = 2;
        } else if(max == neutralCounter) {
            mainSentiment = 1;
        } else { //Irrelevant
            mainSentiment = -2;
        }
        positiveSentimentPerc = (double) positiveCounter / (double) tweetsOfEvent.size();
        negativeSentimentPerc = (double) negativeCounter / (double) tweetsOfEvent.size();
        neutralSentimentPerc = (double) neutralCounter / (double) tweetsOfEvent.size();
        irrelevantSentimentPerc = (double) irrelevantCounter / (double) tweetsOfEvent.size();
    }
    
    private void calculateUniqueUsers() {
        Set<Long> users = new HashSet<>();
        for(Tweet tweet : tweetsOfEvent) {
            users.add(tweet.getUserId());
        }
        uniqueUsers = users.size();
    }
    
    public int getMainSentimentOfEvent() { return mainSentiment; }
    
    public double getPositiveSentimentPercentage() { return positiveSentimentPerc; }
    
    public double getNegatineSentimentPercentage() { return negativeSentimentPerc; }
    
    public double getNeutralSentimentPercentage() { return neutralSentimentPerc; }
    
    public double getIrrelevantSentimentPercentage() { return irrelevantSentimentPerc; }
    
    public int getUniqueUsers() { return uniqueUsers; }
}