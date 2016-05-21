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
package com.left8.evs.samodule;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.left8.evs.utilities.Config;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2016.03.12_1712
 */
public class EmoticonsAnnotator {
    
    private Set<String> positiveEmoticons = new HashSet<>();
    private Set<String> negativeEmoticons = new HashSet<>();
    private Config config;
    
    public EmoticonsAnnotator(Config config) {
        this.config = config;
        initializeSets();
    }
    
    private void initializeSets() {
        String positivePath = config.getResourcesPath() + config.getEmoticonsPath() + config.getPositiveEmoticonsFile();
        String negativePath = config.getResourcesPath() + config.getEmoticonsPath() + config.getNegativeEmoticonsFile();
        try {
            BufferedReader in = new BufferedReader(new FileReader(positivePath));
            String line;
            while((line = in.readLine()) != null) {
                positiveEmoticons.add(line);
            }
            
            in = new BufferedReader(new FileReader(negativePath));
            while((line = in.readLine()) != null) {
                negativeEmoticons.add(line);
            }
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(EmoticonsAnnotator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(EmoticonsAnnotator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Returns true if a given token is a positive emoticon.
     * @param tokens A List containing String already tokenized.
     * @return True if there at least one positive token, false otherwise.
     */
    public boolean containsPositiveEmoticon(List<String> tokens) {
        for(String token : tokens) {
            if(positiveEmoticons.contains(token)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns true if a given token is a negative emoticon.
     * @param tokens A List containing String already tokenized.
     * @return True if there at least one negative token, false otherwise.
     */
    public boolean containsNegativeEmoticon(List<String> tokens) {
        for(String token : tokens) {
            if(negativeEmoticons.contains(token)) {
                return true;
            }
        }
        return false;
    }
}
