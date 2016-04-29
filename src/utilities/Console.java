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
package utilities;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *
 * This class provides an easy interface to implement console controls, extend,
 * delete and enhance them, without cluttering the main method or methods that
 * receive the arguments' array.
 * 
 * 
 * @author  Lefteris Paraskevas
 * @version 2016.04.29_1429
 */
public class Console {
    private static Options options;
    private static CommandLine cmd;
    private final Config config;
    private String[] args;
    
    public Console(String[] args, Config config) throws ParseException {
        
        this.args = args;
        this.config = config;
        setOptions();
        
        CommandLineParser parser = new DefaultParser();
        cmd = parser.parse(options, this.args);
    }
    
    /**
     * Method that defines the console input flags.
     */
    private void setOptions() {
        options = new Options();
        
        options.addOption(config.getMongoLoggingArgName(), false, "Show or hide Mongo Logging Information");
        options.addOption(config.getInlineInfoArgName(), false, "Show or hide inline information");
        options.addOption(config.getOpfArgName(), false, "Non Sentiment version of Offline Peak Finding");
        options.addOption(config.getNoSentimentArgName(), false, "Sentiment version of previous algorithm");
        options.addOption(config.getEdcowArgName(), false, "Non Sentiment version of EDCoW");
    }
    
    public final CommandLine getCmd() { return cmd; }
    
    /**
     * Determine whether the console has external commands to run the ED algorithms
     * or not.
     * @return true if the console has external commands, false otherwise. 
     */
    public final boolean hasExternalCommands() {
        return (args.length > 2) ? true : false;
    }
    
    /**
     * Determines whether the user has ordered to hide the MongoDB Logging Information.
     * @return If the user set the flag returns true, false otherwise.
     */
    public final boolean showMongoLogging() {
        return cmd.hasOption(config.getMongoLoggingArgName());
    }
    
    /**
     * Determines whether the user has ordered to hide the various inline info.
     * @return If the user set the flag returns true, false otherwise.
     */
    public final boolean showInlineInfo() {
        return cmd.hasOption(config.getInlineInfoArgName());
    }
    
    /**
     * Handles the rest of the console and returns the correct value for switch-
     * case scenarios.
     * @return The appropriate value or -1 for error.
     */
    public final int getChoiceValue() {
        if(cmd.hasOption(config.getEdcowArgName())) {
            if(cmd.hasOption(config.getNoSentimentArgName())) {
                return 5;
            }
            return 2;
        }
        
        if(cmd.hasOption(config.getOpfArgName())) {
            if(cmd.hasOption(config.getNoSentimentArgName())) {
                return 4;
            }
            return 1;
        }
        
        return -1;
    }
}
