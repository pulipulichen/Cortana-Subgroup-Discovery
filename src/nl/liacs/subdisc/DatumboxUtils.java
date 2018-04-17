package nl.liacs.subdisc;

//import smile.math.Math;
//import smile.math.Math;
//import java.lang.Math;
import java.lang.*;
import java.util.*;

import com.datumbox.framework.common.dataobjects.*;
import com.datumbox.framework.core.statistics.parametrics.independentsamples.*;


public class DatumboxUtils { //calculate a p-value based on an array
	
	/**
	 * DatumboxUtils.LevenesIndependentSamplesTestVariances
	 * @param sample1
	 * @param sample2
	 * @return
	 */
	public static boolean LevenesIndependentSamplesTestVariances(float[] sample1, float[] sample2) {
		List sample1List = Arrays.asList(convertFloatsToObjects(sample1));
		List sample2List = Arrays.asList(convertFloatsToObjects(sample2));
		
		TransposeDataCollection transposeDataCollection = new TransposeDataCollection();
        transposeDataCollection.put(0, new FlatDataCollection(sample1List));
        transposeDataCollection.put(1, new FlatDataCollection(sample2List));
        
        return LevenesIndependentSamples.testVariances(transposeDataCollection, 0.05);
	}
	
	public static Object[] convertFloatsToObjects (float[] input)
	{
	    if (input == null)
	    {
	        return null; // Or throw an exception - your choice
	    }
	    Object[] output = new Object[input.length];
	    for (int i = 0; i < input.length; i++)
	    {
	        output[i] = input[i];
	    }
	    return output;
	}
}