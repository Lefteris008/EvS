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
 * @author  Lefteris Paraskevas
 * @version 2016.04.02_1326
 */
public class Console {
    public static int showMongoLogging;
    public static boolean showInlineInfo;
    
    public Console(String[] args) {
        int mongoL = Integer.parseInt(args[0]);
        int showInlineInfo = Integer.parseInt(args[1]);
        
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
