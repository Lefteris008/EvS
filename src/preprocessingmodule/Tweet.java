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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package preprocessingmodule;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2015.11.11_1752_planet1
 */
public class Tweet {
    
    private final long id;
    private final String username;
    private final String text;
    private final String date;
    private final long latitude;
    private final long longitude;
    private final int numberOfRetweets;
    private final int numberOfFavorites;
    private final boolean retweet;
    private final boolean favorited;
    private final boolean retweeted;
    private final String language;
    private final String groundTruthEvent;
    
    public Tweet(long id, String username, String text, String date, 
            long latitude, long longitude, int numberOfRetweets, 
            int numberOfFavorites, boolean retweet, boolean favorited, 
            boolean retweeted, String language, String groundTruthEventh) {
        
        this.id = id;
        this.username = username;
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
        this.groundTruthEvent = groundTruthEventh;
    }
    
    public long getID() { return id; }
    
    public String getUsername() { return username; }
    
    public String getText() { return text; }
    
    public String getDate() { return date; }
    
    public long getLatitude() { return latitude; }
            
    public long getLongitude() { return longitude; }
    
    public int getNumberOfRetweets() { return numberOfRetweets; }
    
    public int getNumberOfFavorites() { return numberOfFavorites; }
    
    public boolean isRetweet() { return retweet; }
    
    public boolean isFavorited() { return favorited; }
    
    public boolean isRetweeted() { return retweeted; }
    
    public String getLanguage() { return language; }
    
    public String getGroundTruthEvent() { return groundTruthEvent; }
    
    public void printTweetData() {
        System.out.println("Tweet with ID '" + id + "'");
        System.out.println("--------------------------------------------\n");
        System.out.println("Username: @" + username);
        System.out.println("Text: " + (retweet ? "RT" : "") + text);
        System.out.println("Created at: " + date);
        System.out.println("Latitude: " + latitude);
        System.out.println("Longitude: " + longitude);
        if(retweeted) {
            System.out.println("Retweeted " + numberOfRetweets + " times");
        }
        if(favorited) {
            System.out.println("Favorited " + numberOfFavorites + " times");
        }
        System.out.println("Language: " + language);
        System.out.println("Ground truth event: " + groundTruthEvent);
        System.out.println("--------------------------------------------");
    }
}
