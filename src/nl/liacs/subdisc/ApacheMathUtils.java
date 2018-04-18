package nl.liacs.subdisc;

import java.lang.*;
import java.util.*;

import org.apache.commons.math3.stat.inference.*;

import com.datumbox.framework.common.dataobjects.*;
import com.datumbox.framework.core.statistics.parametrics.independentsamples.*;


public class ApacheMathUtils { //calculate a p-value based on an array
	
	/**
	 * 
	 * @param sample1
	 * @param sample2
	 * @param unequalVar
	 * @return p-value
	 */
	public static float tTest(float[] sample1, float[] sample2, boolean unequalVar) {
		double[] sampel1double = convertFloatsToDoubles(sample1);
		double[] sampel2double = convertFloatsToDoubles(sample2);
		
		if (sample1.length == 0 || sample2.length == 0) {
			return 0;
		}
		else if (sample1.length == 1 && sample2.length == 1) {
			if (sample1[0] == sample2[0]) {
				return 1;
			}
			else {
				return 0;
			}
		}
		else if (sample1.length == 1) {
			return (float) TestUtils.tTest((double) sample1[0],sampel2double);
		}
		else if (sample2.length == 1) {
			return (float) TestUtils.tTest((double) sample2[0],sampel1double);
		}
		else if (unequalVar) {
			return (float) TestUtils.tTest(sampel1double,sampel2double);
		}
		else {
			return (float) TestUtils.homoscedasticTTest(sampel1double,sampel2double);
		}
	}
	
	public static float tTest(float[] sample1, float[] sample2) {
		double[] sampel1double = convertFloatsToDoubles(sample1);
		double[] sampel2double = convertFloatsToDoubles(sample2);
		
		return (float) TestUtils.tTest(sampel1double,sampel2double);
	}

	public static double[] convertFloatsToDoubles(float[] input)
	{
	    if (input == null)
	    {
	        return null; // Or throw an exception - your choice
	    }
	    double[] output = new double[input.length];
	    for (int i = 0; i < input.length; i++)
	    {
	        output[i] = input[i];
	    }
	    return output;
	}
	
}