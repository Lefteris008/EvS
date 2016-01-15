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
package edmodule.peakfinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import utilities.Utilities;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2016.01.15_0249_planet3
 */
public class PeakFindingEvents {
    
    private final HashMap<String, ArrayList<String>> tweetsByWindow;
    private final List<BinPair<String, Integer>> bins;
    private final List<Window<Integer, Integer>> eventWindows;
    private final ArrayList<ArrayList<String>> eventsTweets = new ArrayList<>();
    private final List<PeakFindingEvent> events = new ArrayList<>();
    
    /**
     * Public constructor.
     * @param tweetsByWindow A HashMap containing all tweets in a specific
     * window interval.
     * @param bins A List of BinPair objects, containing all bins.
     * @param events A List of Window objects, containing the generated eventsTweets.
     */
    public PeakFindingEvents(HashMap<String, ArrayList<String>> tweetsByWindow, 
            List<BinPair<String, Integer>> bins, List<Window<Integer, Integer>> eventWindows) {
        this.tweetsByWindow = new HashMap<>(tweetsByWindow);
        this.bins = new ArrayList<>(bins);
        this.eventWindows = new ArrayList<>(eventWindows);
        generateEvents();
    }
    
    /**
     * Returns the tweets that belong to a certain event. <br/>
     * More formally, it parses the auxiliary tweetsByWindow HashMap and appends
     * the relevant tweets into a String list.
     * @param window A Window object, the actual event.
     * @return A List with the relevant tweets.
     */
    public final ArrayList<String> getTweetsOfEvent(Window<Integer, Integer> window) {
        
        int start = window.getStart();
        int end = window.getEnd();
        String key;
        ArrayList<String> tweetsOfEvent = new ArrayList<>();
        
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
    public final void generateEvents() {
        for(Window<Integer, Integer> window : eventWindows) {
            ArrayList<String> tweets = new ArrayList<>(getTweetsOfEvent(window));
            eventsTweets.add(tweets);
        }
        int i = 0;
        for(Window<Integer, Integer> window : eventWindows) {
            PeakFindingEvent event = new PeakFindingEvent(i, window, eventsTweets.get(i));
            events.add(event);
            i++;
        }
    }
    
    /**
     * Returns the actual events. 
     * @return A List with PeakFindingEvent object.
     */
    public final List<PeakFindingEvent> getEvents() { return events; }
    
    /**
     * Prints a specific event along with its tweets.
     * @param index The index of the event in the events list.
     */
    public final void printEvent(int index) {
        PeakFindingEvent event = events.get(index);
        Utilities.printInfoMessage("----------------------------");
        Utilities.printInfoMessage("Event '" + event.getID() + "' contains the following tweets:");
        event.getTweets().stream().forEach((tweet) -> {
            Utilities.printInfoMessage(tweet);
        });
    }
    
    /**
     * TODO
     */
    public final void printEvents() {
        ///TODO
    }
}
