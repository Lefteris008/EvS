/* 
 * Copyright (C) 2016 Adrien Guille <adrien.guille@univ-lyon2.fr>
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
package com.left8.evs.edmodule.edcow;

////////////////////////////////////////////////////////////////////////////////

import com.left8.evs.utilities.Utilities;

//  This file is part of SONDY.                                               //
//                                                                            //
//  SONDY is free software: you can redistribute it and/or modify             //
//  it under the terms of the GNU General Public License as published by      //
//  the Free Software Foundation, either version 3 of the License, or         //
//  (at your option) any later version.                                       //
//                                                                            //
//  SONDY is distributed in the hope that it will be useful,                  //
//  but WITHOUT ANY WARRANTY; without even the implied warranty of            //
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the             //
//  GNU General Public License for more details.                              //
//                                                                            //
//  You should have received a copy of the GNU General Public License         //
//  along with SONDY.  If not, see <http://www.gnu.org/licenses/>.            //
////////////////////////////////////////////////////////////////////////////////

/**
 *      This class serves for computing the cross correlation function (CCF).
 *      The computing is done using the FFT.
 *
 *      @author siniwitt
 * 
 * @author  Lefteris Paraskevas
 * @version 2016.01.31_1921
 */
public class EDCoWCrossCorrelationZeroTime {       	
	
    public double autoCorrelationZeroTime(double[] sign){
        double sum = 0.0;
        for(double x:sign)sum += Math.pow(x, 2); 
        return (sum<0.00001)?0:sum;
    }

    public double correlationZeroTime(double[] sign1, double[] sign2){
        double sum = 0.0;
        if(sign1.length == sign2.length) {
            for(int i=0; i < sign1.length; i++) {
                sum += (sign1[i] * sign2[i]);
            }
        }
        else {
            Utilities.printMessageln("The length of sign1 and sign2 is not the same.");
        }
        return (sum<0.00001)?0:sum;
    }
}       
