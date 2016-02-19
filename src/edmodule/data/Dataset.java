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
package edmodule.data;

import java.util.List;
import utilities.Config;
import dsretriever.MongoHandler;
import dsretriever.Tweet;
import edmodule.utils.Stemmers;
import edmodule.utils.StopWordsHandlers;
import utilities.Utilities;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2016.02.19_1709
 */
public final class Dataset {
    
    private final StopWordsHandlers swH;
    private final List<Tweet> tweets;

    /**
     * It retrieves a dataset from the already stored MongoDB collection.
     * @param config A Configuration object.
     * @param sw A StopWords handler
     */
    public Dataset(Config config) {
        long startTime = System.currentTimeMillis(); //Start time
        
        MongoHandler mongo = new MongoHandler(config);
        mongo.connectToMongoDB();
 
        //Initialize stopwords and stemmers
        swH = new StopWordsHandlers(config);
        Stemmers.initStemmers();
        
        //Load all tweets from MongoDB Store
        tweets = mongo.retrieveAllTweetsFromMongoDBStore();
        mongo.closeMongoConnection();
 
        long endTime = System.currentTimeMillis();
        Utilities.printExecutionTime(startTime, endTime, Dataset.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName());
    }
    
    /**
     * Method to return previously initialized StopWordsHanders object.
     * @return A StopWordsHandlers object.
     */
    public final StopWordsHandlers getSWH() { return swH; }
    
    /**
     * Method to return a list containing all retrieved tweets from database.
     * @return A List containing tweets.
     */
    public final List<Tweet> getTweetList() { return tweets; }
}
