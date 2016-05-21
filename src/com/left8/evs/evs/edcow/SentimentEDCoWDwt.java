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
package com.left8.evs.evs.edcow;

import math.transform.jwave.handlers.DiscreteWaveletTransform;
import math.transform.jwave.handlers.wavelets.Haar02;
import math.transform.jwave.handlers.wavelets.WaveletInterface;

////////////////////////////////////////////////////////////////////////////////
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
 *   @author yue HE, Falitokiniaina RABEARISON, Département Informatique et Statistiques, Université Lumière Lyon 2
 *   @author Adrien GUILLE, Laboratoire ERIC, Université Lumière Lyon 2
 */
public class SentimentEDCoWDwt {
	
	int level;
	double[] signal;
	double[] arrHilb;	
	double[] probaVector;
	double shannonEntropy;
	double swemax, hMeasure;
	
	WaveletInterface wI;
	DiscreteWaveletTransform dwt;		
	
	SentimentEDCoWDwt(double[] signal1,int level1){    					
		signal = signal1;
		level = level1;
		wI = new Haar02();		
		dwt = new DiscreteWaveletTransform(wI,level);				
		arrHilb = dwt.forward( signal );		
	}

	public int nbCoeffAtLevel(int level1){		
		return (int)((arrHilb.length)/Math.pow(2, level1));
	}
	
	/** Get coefficients of a level*/
	public double[] getCoefficients(int atLev) {
		// TODO Auto-generated method stub	           			
			int rightPart = 0;
			for (int i=1; i<=atLev; i++){
				rightPart = rightPart + nbCoeffAtLevel(i);
			}
			int totalSize = arrHilb.length;
			int beginIndice = totalSize - rightPart;
			int nbCoeff = nbCoeffAtLevel(atLev);
			double[] coeff = new double[nbCoeff];
			
			for(int i=0; i<nbCoeff; i++){
				coeff[i] = arrHilb[beginIndice+i];
			}
			return coeff;
	}					
    	
	/** compute proba vector*/
	public void probaVector() {
		double sSquare = 0;
		probaVector = new double[level];
		
		for(int i=0; i < level; i++){
			double[] coeff = getCoefficients(i+1);						
			SentimentEDCoWVector vec = new SentimentEDCoWVector(coeff);
			sSquare = sSquare + Math.pow(vec.getNorm(),2);												
		}
		for(int i = 0; i < level; i++){
			double[] coeff = getCoefficients(i+1);						
			SentimentEDCoWVector vec = new SentimentEDCoWVector(coeff);
			if (sSquare == 0) {
                            probaVector[i] = 0;
                        } else {
                            probaVector[i] = Math.pow(vec.getNorm(), 2) / sSquare;
                        }											
		}				
//		for(double x:probaVector){
//			System.out.println("p = "+x);
//		}		
	}	
    
	public void shannonEntropy() {
		shannonEntropy = 0;
		for(double x : probaVector) {			
			shannonEntropy = shannonEntropy - (x * (Math.log(x) / Math.log(2)));
			//System.out.println("shannonEntropy " + shannonEntropy);
		}
		//System.out.println("shannon Entropy :"+shannonEntropy);		
	}
    
	public void hMeasure(){
            probaVector();
            shannonEntropy();
            swemax = Math.log(level) / Math.log(2);
            hMeasure = shannonEntropy / swemax;
	}
	
	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public double[] getSignal() {
		return signal;
	}

	public void setSignal(double[] signal) {
		this.signal = signal;
	}

	public double[] getArrHilb() {
		return arrHilb;
	}

	public void setArrHilb(double[] arrHilb) {
		this.arrHilb = arrHilb;
	}

	public double gethMeasure() {
		return hMeasure;
	}			
}
