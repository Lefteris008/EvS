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

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2016.04.30_1828
 * 
 * Sentiment Entity class represents the main sentiment information wrapper.
 * <b>0 --&gt;</b> Negative sentiment. <br>
 * <b>1 --&gt;</b> Neutral sentiment. <br>
 * <b>2 --&gt;</b> Positive sentiment. <br>
 * <b>Anything different --&gt;</b> Irrelevant sentiment.
 */
public class SentimentWindowEntity {
    private final double positivePercentage;
    private final double negativePercentage;
    private final double neutralPercentage;
    private final double irrelevantPercentage;
    private final int mainSentiment;
    
    public SentimentWindowEntity(double pos, double neg, double neut, double irrel, 
            int mainSentiment) {
        this.positivePercentage = pos;
        this.negativePercentage = neg;
        this.neutralPercentage = neut;
        this.irrelevantPercentage = irrel;
        this.mainSentiment = mainSentiment;
    }

    /**
     * Returns the percentage of the positive annotated tweets against the total
     * amount of them.
     * @return A double representing the positive percentage of tweets.
     */
    public double getPositivePercentage() {
        return positivePercentage;
    }

    /**
     * Returns the percentage of the negative annotated tweets against the total
     * amount of them.
     * @return A double representing the negative percentage of tweets.
     */
    public double getNegativePercentage() {
        return negativePercentage;
    }

    /**
     * Returns the percentage of the neutral annotated tweets against the total
     * amount of them.
     * @return A double representing the neutral percentage of tweets.
     */
    public double getNeutralPercentage() {
        return neutralPercentage;
    }

    /**
     * Returns the percentage of the irrelevant annotated tweets against the total
     * amount of them.
     * @return A double representing the irrelevant percentage of tweets.
     */
    public double getIrrelevantPercentage() {
        return irrelevantPercentage;
    }

    /**
     * Returns the main sentiment of the window entity.
     * @return An integer representing the sentiment (refer to class header).
     */
    public int getMainSentiment() {
        return mainSentiment;
    }
    
    /**
     * Returns the description of the main sentiment of the window entity.
     * @return The description of the main sentiment.
     */
    public String getMainSentimentDescription() {
        if(mainSentiment == 0) {
            return "Negative";
        } else if(mainSentiment == 1) {
            return "Neutral";
        } else if(mainSentiment == 2) {
            return "Positive";
        } else {
            return "Irrelevant";
        }
    }
    
}
