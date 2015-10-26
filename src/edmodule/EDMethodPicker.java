/*
 * Copyright (C) 2015 Lefteris Paraskevas
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

import edmodule.edcow.EDCoW;
import edmodule.lsh.LSH;
import java.util.Scanner;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2015.10.26_1553_wave2
 */
public class EDMethodPicker {
    
    /**
     * Main method to pick your desired method of Event Detection
     * @param args 
     */
    public static void main(String args[]) {
        System.out.println("Pick a method for Event Detection");
        System.out.println("1. EDCoW");
        System.out.println("2. LSH");
        System.out.println("Your choice: ");
        
        Scanner keyboard = new Scanner(System.in);
        int choice = keyboard.nextInt();
        
        if(choice == 1) {
            System.out.println("Selected method: EDCoW");
            EDCoW edcow = new EDCoW();
            edcow.run();
        } else if (choice == 2) {
            System.out.println("Selected method: LSH");
            LSH lsh = new LSH();
            lsh.run();
        }else {
            System.out.println("Exiting...");
            System.exit(0);
        }
        
    }
}
