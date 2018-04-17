package nl.liacs.subdisc;

import java.lang.*;
import java.util.*;

import org.apache.commons.math3.stat.inference.*;

import com.datumbox.framework.common.dataobjects.*;
import com.datumbox.framework.core.statistics.parametrics.independentsamples.*;


public class ApacheMathUtils { //calculate a p-value based on an array
	
	public static float tTest(float[] sample1, float[] sample2, boolean unequalVar) {
		double[] sampel1double = convertFloatsToDoubles(sample1);
		double[] sampel2double = convertFloatsToDoubles(sample2);
		
		if (unequalVar) {
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