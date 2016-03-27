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
package evs.peakfinding.event;

import edmodule.utils.BinPair;
import dsretriever.Tweet;
import edmodule.peakfinding.Window;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import preprocessingmodule.nlp.stemming.StemUtils;
import evs.data.PeakFindingSentimentCorpus;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2016.03.28_0009
 */
public class SentimentPeakFindingEvents {
    
    private final HashMap<String, ArrayList<Tweet>> tweetsByWindow;
    private final List<BinPair<String, Integer>> bins;
    private final List<Window<Integer, Integer>> eventWindows;
    private final ArrayList<ArrayList<Tweet>> eventsTweets = new ArrayList<>();
    private final List<SentimentPeakFindingEvent> events = new ArrayList<>();
    private final PeakFindingSentimentCorpus corpus;
    
    /**
     * Public constructor.
     * @param tweetsByWindow A HashMap containing all tweets in a specific
     * window interval.
     * @param bins A List of BinPair objects, containing all bins.
     * @param eventWindows A List of Window objects, containing the generated eventsTweets.
     * @param corpus A PeakFindingCorpus object.
     */
    public SentimentPeakFindingEvents(HashMap<String, ArrayList<Tweet>> tweetsByWindow, 
            List<BinPair<String, Integer>> bins, 
            List<Window<Integer, Integer>> eventWindows, 
            PeakFindingSentimentCorpus corpus, StemUtils stemsHandler,
            int sentimentSouce) throws FileNotFoundException {
        this.tweetsByWindow = new HashMap<>(tweetsByWindow);
        this.bins = new ArrayList<>(bins);
        this.eventWindows = new ArrayList<>(eventWindows);
        this.corpus = corpus;
        generateEvents(sentimentSouce);
    }
    
    /**
     * Returns the tweets that belong to a certain event. <br/>
     * More formally, it parses the auxiliary tweetsByWindow HashMap and appends
     * the relevant tweets into a String list.
     * @param window A Window object, the actual event.
     * @return A List with the relevant tweets.
     */
    public final ArrayList<Tweet> getTweetsOfEvent(Window<Integer, Integer> window) {
        
        int start = window.getStart();
        int end = window.getEnd();
        String key;
        ArrayList<Tweet> tweetsOfEvent = new ArrayList<>();
        
        for(int i = start; i < end; i++) {
            key = bins.get(i).getBin();
            if(tweetsByWindow.containsKey(key)) {
                tweetsOfEvent.addAll(tweetsByWindow.get(key));
            }
        }
        return tweetsOfEvent;    
    }
    
    /**
     * Method to generate all subsequent events and create a list of them for
     * future use.
     */
    public final void generateEvents(int sentimentSouce) {
        eventWindows.stream().map((window) -> new ArrayList<>(getTweetsOfEvent(window))).forEach((tweets) -> {
            eventsTweets.add(tweets);
        });
        int i = 0;
        for(Window<Integer, Integer> window : eventWindows) {
            SentimentPeakFindingEvent event = new SentimentPeakFindingEvent(i, window, eventsTweets.get(i),
                    corpus, sentimentSouce);
            events.add(event);
            i++;
        }
    }
    
    /**
     * Returns the actual events. 
     * @return A List with Event object.
     */
    public final List<SentimentPeakFindingEvent> getEvents() { return events; }
}
