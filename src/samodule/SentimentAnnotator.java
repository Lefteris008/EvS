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
package samodule;

import dsretriever.MongoHandler;
import dsretriever.Tweet;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import utilities.Config;
import utilities.Utilities;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2016.03.16_1214  
 */
public class SentimentAnnotator {
    
    private final Config config;
    private final List<Tweet> tweets;
    private final MongoHandler mongo;
    private final List<Integer> sentimenAnnotations = new ArrayList<>();
    
    public SentimentAnnotator(Config config, List<Tweet> tweets, MongoHandler mongo) {
        this.config = config;
        this.tweets = new ArrayList<>(tweets);
        this.mongo = mongo;
        loadSentimentAnnotations();
    }
    
    private void loadSentimentAnnotations() {
        try {
            Files.walk(Paths.get(config.getResourcesPath() 
                    + config.getSentimentFilesPath())).forEach(filePath -> {
                if(Files.isRegularFile(filePath)) {
                    Utilities.printMessageln("Reading " + filePath.getFileName() + " file...");
                    String line;
                    String[] temp;
                    try {
                        BufferedReader in = new BufferedReader(new FileReader(filePath.toString()));
                        
                        boolean firstLine = true;
                        while((line = in.readLine()) != null) {
                            if(firstLine) {
                                firstLine = false;
                                continue;
                            }
                            line = line.replaceAll("        ", " ");
                            line = line.replaceAll("       ", " ");
                            line = line.replaceAll("      ", " ");
                            line = line.replaceAll("     ", " ");
                            line = line.replaceAll("    ", " ");
                            line = line.replaceAll("   ", " ");
                            line = line.replaceAll("  ", " ");
                            temp = line.split(" ");
                            sentimenAnnotations.add(Integer.parseInt(temp[3].substring(2)));
                        }
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(SentimentAnnotator.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(SentimentAnnotator.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
        } catch (IOException ex) {
            Logger.getLogger(SentimentAnnotator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Annotates a list of tweets by their 
     */
    public final void annotateWithSentiment() {
        int i = 0;
        for(Tweet tweet : tweets) {
            mongo.updateSentiment(tweet.getID(), sentimenAnnotations.get(i),
                    "sentimentPrediction");
            i++;
        }
    }
}
