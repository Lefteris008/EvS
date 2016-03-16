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
package evs;

import edmodule.data.Dataset;
import edmodule.data.PeakFindingCorpus;
import edmodule.peakfinding.BinsCreator;
import edmodule.utils.BinPair;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import evs.data.PeakFindingSentimentCorpus;
import evs.peakfinding.SentimentPeakFinding;
import utilities.Config;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2016.03.16_1213
 */
public class EvS {
            
    public EvS(Config config) {
        int window = 10;
        double alpha = 0.999;
        int taph = 1;
        int pi = 5;
        Dataset ds = new Dataset(config);
        PeakFindingCorpus corpus = new PeakFindingCorpus(config, ds.getTweetList(), ds.getSWH());
        List<BinPair<String, Integer>> bins = BinsCreator.createBins(corpus, config, window);
        PeakFindingSentimentCorpus sCorpus = new PeakFindingSentimentCorpus(corpus);
        
        SentimentPeakFinding spf = new SentimentPeakFinding(bins, alpha, taph, 
                pi, window, sCorpus);
        try {
            spf.apply();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(EvS.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
