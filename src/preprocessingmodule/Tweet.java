/*
 * Copyright (C) 2015 Lefteris Paraskevas
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
 * aString with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package preprocessingmodule;

import java.util.Date;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2015.11.25_2359_planet2
 */
public class Tweet {
    
    private final long id;
    private final String username;
    private final String displayName;
    private final long userId;
    private final String text;
    private final Date date;
    private final double latitude;
    private final double longitude;
    private final int numberOfRetweets;
    private final int numberOfFavorites;
    private final boolean retweet;
    private final boolean favorited;
    private final boolean retweeted;
    private final String language;
    
    public Tweet(long id, String username, String displayName, long userId, String text, Date date, 
            double latitude, double longitude, int numberOfRetweets, 
            int numberOfFavorites, boolean retweet, boolean favorited, 
            boolean retweeted, String language) {
        
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.userId = userId;
        this.text = text;
        this.date = date;
        this.latitude = latitude;
        this.longitude = longitude;
        this.numberOfRetweets = numberOfRetweets;
        this.numberOfFavorites = numberOfFavorites;
        this.retweet = retweet;
        this.favorited = favorited;
        this.retweeted = retweeted;
        this.language = language;
    }
    
    /**
     * Returns the ID of the tweet.
     * @return A String containing the ID
     */
    public long getID() { return id; }
    
    /**
     * Returns the username of the creator of the tweet.
     * @return A String containing the username.
     */
    public String getUsername() { return username; }
    
    /**
     * Returns the display name of the creator of the tweet.
     * @return A String containing the display name.
     */
    public String getDisplayName() { return displayName; }
    
    /**
     * Returns the user ID of the creator of the tweet.
     * @return A long containing the ID of the creator of the tweet.
     */
    public long getUserId() { return userId; }
    
    /**
     * Returns the actual text of the tweet.
     * @return A String containing the text of the tweet
     */
    public String getText() { return text; }
    
    /**
     * Returns the date the tweet was created at.
     * @return A date object
     */
    public Date getDate() { return date; }
    
    /**
     * Returns the latitude of the location where the tweet was created.
     * @return A String containing the tweet's latitude or '-1' in case there's no location information
     */
    public double getLatitude() { return latitude; }
            
    /**
     * Returns the longitude of the location where the tweet was created.
     * @return A long containing the tweet's longitude or '-1' in case there's no location information
     */
    public double getLongitude() { return longitude; }
    
    /**
     * Returns the number of times the tweet was retweeted.
     * @return An integer containing the number of the tweet's retweets.
     */
    public int getNumberOfRetweets() { return numberOfRetweets; }
    
    /**
     * Returns the number of times the tweet was favorited.
     * @return An integer containing the number of the tweet's favorites.
     */
    public int getNumberOfFavorites() { return numberOfFavorites; }
    
    /**
     * Informs whether the tweet was actually a retweet itself.
     * @return True if the tweet is a retweet, false otherwise.
     */
    public boolean isRetweet() { return retweet; }
    
    /**
     * Informs whether the tweet was favorited at all.
     * @return True if the tweet is favorited at least once, false otherwise.
     */
    public boolean isFavorited() { return favorited; }
    
    /**
     * Informs whether the tweet was retweeted at all.
     * @return True if the tweet is retweeted at least once, false otherwise.
     */
    public boolean isRetweeted() { return retweeted; }
    
    /**
     * Returns the language the tweet was written in.
     * @return A String containing the abbreviation of the tweet's language.
     */
    public String getLanguage() { return language; }
    
    /**
     * Returns the ground truth event the tweet is referring to.
     * @return A String containing the event's name.
     * @deprecated Ground truth events removed since Planet2.
     */
    public String getGroundTruthEvent() { return "None"; }
    
    /**
     * Prints the tweet metadata along with its text.
     */
    public void printTweetData() {
        System.out.println("Tweet with ID '" + getID() + "'");
        System.out.println("--------------------------------------------");
        System.out.println("@" + getUsername());
        System.out.println("Display name: " + getDisplayName());
        System.out.println("User ID '" + getUserId() + "'");
        System.out.println(getText());
        System.out.println("Created at: " + getDate().toString());
        System.out.println("Location");
        if(getLatitude() == -1) {
            System.out.println("\tNo location info provided.");
        } else {
            System.out.println("\tLatitude: " + getLatitude());
            System.out.println("\tLongitude: " + getLongitude());
        }
        if(isRetweeted()) {
            System.out.println("Retweeted " + getNumberOfRetweets() + " times");
        }
        if(isFavorited()) {
            System.out.println("Favorited " + getNumberOfFavorites() + " times");
        }
        System.out.println("Language: " + getLanguage());
        System.out.println("--------------------------------------------");
    }
}
