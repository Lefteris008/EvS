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
import java.util.List;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2016.01.15_0249_planet3
 */
public class PeakFindingEvent {
    
    private final int id;
    private final Window<Integer, Integer> window;
    private final List<String> tweets;
    
    /**
     * Public constructor. It creates an event.
     * @param name A String with the name of the event.
     * @param window A Window object with the window of the event.
     * @param tweets A List of String containing the corresponding tweets of the
     * event.
     */
    public PeakFindingEvent(int id, Window<Integer, Integer> window, List<String> tweets) {
        this.id = id;
        this.window = window;
        this.tweets = new ArrayList<>(tweets);
    }
    
    /**
     * Get the name of the event.
     * @return A String with event's name.
     */
    public final int getID() { return id; }
    
    /**
     * Get the window of the event.
     * @return Returns a Window object with the event's window.
     */
    public final Window<Integer, Integer> getWindow() { return window; }
    
    /**
     * Get the tweets of the event.
     * @return A List of String with the event's tweets.
     */
    public final List<String> getTweets() { return tweets; }
}
