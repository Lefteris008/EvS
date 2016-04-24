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

/**
 *
 * This class provides an easy interface to implement console controls, extend,
 * delete and enhance them, without cluttering the main method or methods that
 * receive the arguments' array.
 * 
 * 
 * @author  Lefteris Paraskevas
 * @version 2016.04.24_1922
 */
public class Console {
    public static int showMongoLogging;
    public static boolean showInlineInfo;
    public static boolean hasExternalCommands = false;
    public static int choice;
    private String[] args;
    
    public Console(String[] args) {
        this.args = args;
        analyzeConsoleInput();
    }
    
    /**
      * Initial version includes 4 flags: <br/>
      * showMonogLogging --> Indicates whether the MongoDB Logging information should
      * be displayed during execution (0) or not (1). <br/>
      * showInlineInfo --> Indicates whether inline information from various sources
      * will be displayed during execution (0) or not (1). <br/>
      * <b>External Commands</b> <br/>
      * -opf --> Run the Sentiment version of Offline Peak Finding. <br/>
      * &nbsp;&nbsp;&nbsp;&nbsp;-nosentiment --> Run the simple version of OPF. <br/>
      * -edcow --> Run the Sentiment version of EDCoW. <br/>
      * &nbsp;&nbsp;&nbsp;&nbsp;-nosentiment --> Run the simple version of EDCoW. <br/>
      * Example: 0 1 -opf --> Show MongoDB Logging information, hides the inline
      * information of the execution and runs the Sentiment version of OFP.
     */
    private void analyzeConsoleInput() {
        int mongoL = Integer.parseInt(args[0]);
        int showInlineInfo = Integer.parseInt(args[1]);
        String extCommands = null;
        if(args.length > 2) { //Has external commands
            hasExternalCommands = true;
            extCommands = args[2];
            switch(extCommands) {
                case "-opf": {
                    if(args.length == 4) { //-nosentiment flag
                        choice = 4;
                        break;
                    }
                    choice = 1;
                    break;
                } case "-edcow" : {
                    if(args.length == 4) { //-nosentiment flag
                        choice = 5;
                        break;
                    }
                    choice = 2;
                    break;
                } default: {
                    Utilities.printMessageln("Wrong arguments. Refer to the online"
                            + "wiki guide.");
                    break;
                }
            }
        }
        
        if(mongoL == 0) {
            showMongoLogging = 0;
        } else {
            showMongoLogging = 1;
        }
        
        if(showInlineInfo == 0) {
            this.showInlineInfo = true;
        } else {
            this.showInlineInfo = false;
        }
    }
}
