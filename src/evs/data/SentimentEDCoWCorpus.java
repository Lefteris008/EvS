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

import utilities.dsretriever.Tweet;
import edmodule.data.EDCoWCorpus;
import edmodule.utils.StopWordsHandlers;
import java.util.List;
import utilities.Config;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2016.03.28_0003
 */
public class SentimentEDCoWCorpus {
    private final EDCoWCorpus corpus;
    
    public SentimentEDCoWCorpus(Config config, List<Tweet> tweets, StopWordsHandlers swH, int refreshWindow) {
        this.corpus = new EDCoWCorpus(config, tweets, swH, refreshWindow);
    }
    
    public final EDCoWCorpus getEDCoWCorpus() { return corpus; }
}
