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
package edmodule;

/**
 * Abstract interface that once implemented, provides the basic structure of
 * an Event Detection algorithm. Specialized sub-classes my need to be created
 * but every separate ED technique has to implement these 6 methods.
 * 
 * @author  Lefteris Paraskevas
 * @version 2016.04.09_1945
 */
public interface AbstractEDMethod {
    
    /**
     * Get the name of the the Event Detection algorithm used.
     * @return A String containing the name of the algorithm, e.g. "EDCoW".
     */
    public abstract String getName();
    
    /**
     * Get the citation of the paper that introduced the used Event Detection algorithm.
     * @return A String containing the citation. The String must start and end
     * with '<li>' tags.
     */
    public abstract String getCitation();
    
    /**
     * Get the name of the author that published the paper that introduced 
     * the used Event Detection algorithm.
     * @return A String with the name of the author. If multiple authors
     * contributed to the published paper, their names must be separated with a comma.
     */
    public abstract String getAuthors();
    
    /**
     * Return a short description of the Event Detection algorithm used.
     * @return A String containing the description.
     */
    public abstract String getDescription();
    
    /**
     * Main method that starts the execution of the Event Detection algorithm.
     */
    public abstract void apply() throws Exception;
    
    /**
     * Method that returns the execution time of the main algorithm in seconds.
     * @return A long containing the time in seconds.
     */
    public abstract long getExecutionTime();
}
