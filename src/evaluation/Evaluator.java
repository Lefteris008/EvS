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
package evaluation;

import java.util.Scanner;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2015.10.12_1906_wave2
 * From this class, every single evaluator will run
 */
public class Evaluator {
    
    /**
     * Public constructor
     */
    public Evaluator() {
        ///
    }
    
    /**
     * Method that calls the desired evaluators.
     * @param args 
     */
    public static void evaluator(String args[]) {
        System.out.println("Choose your desired evaluation method\n");
        System.out.println("1. Offline Peak-Finding Algorithm.\n");
        //**TODO**
        System.out.println("0. Exit\n");
        System.out.print("Your choice: ");
        Scanner keyboard = new Scanner(System.in);
        int choice = keyboard.nextInt();
        
        switch(choice) {
            case 1: {
                OfflinePeakFinding eval = new OfflinePeakFinding();
                eval.createBins();
                //eval.findPeakWindow();
                break;
            }
            default: {
                System.exit(0);
            }
        }
    }
    
}
