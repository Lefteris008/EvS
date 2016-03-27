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

import edmodule.data.PeakFindingCorpus;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2016.03.28_0003
 */
public class PeakFindingSentimentCorpus {
    private final PeakFindingCorpus corpus;
    private final Map<String, SentimentWindowEntity> sentimentsByWindow = new HashMap<>();
    
    public PeakFindingSentimentCorpus(PeakFindingCorpus corpus) {
        this.corpus = corpus;
    }
    
    /**
     * Returns the original PeakFindingCorpus object from ED analysis.
     * @return A PeakFindingCorpus object.
     */
    public final PeakFindingCorpus getPeakFindingCorpus() { return corpus; }
    
    /**
     * Returns a SentimentWindowEntity object of a specific window.
     * @param key The String key of the window.
     * @return A SentimentWindowEntity object.
     */
    public final SentimentWindowEntity getSentimentEntityOfWindow(String key) {
        return sentimentsByWindow.get(key);
    }
}
