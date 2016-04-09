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
package evaluator;

/**
 *
 * Interface for implementing an evaluator class for a desired Event Detection
 * method.
 * @author  Lefteris Paraskevas
 * @version 2016.04.09_2016
 */
public interface AbstractEvaluator {
    
    abstract void loadGroundTruthDataset();
    
    abstract void evaluate(boolean showInlineInfo);
    
    abstract int findEventById(String id);
    
    abstract int findEventByTerm(String term);
    
    abstract double getTotalRecall();
    
    abstract double getRecall(int index);
    
    abstract double getPrecision(int index);
    
}
