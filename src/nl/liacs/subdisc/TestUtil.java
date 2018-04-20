package nl.liacs.subdisc;

//import smile.math.Math;
//import smile.math.Math;
//import java.lang.Math;
import java.lang.*;

import org.rosuda.JRI.*;
import org.rosuda.JRI.REXP;
import org.rosuda.REngine.*;
import org.rosuda.REngine.Rserve.*;


import nl.liacs.subdisc.gui.*;


public class TestUtil { //calculate a p-value based on an array
	    
    public static void start() {

		// --------------------------------
		
		//double[] sample1 = {5,4,3};
		//double[] sample2 = {7,6,16,5,4,3};
		//Log.logCommandLine("" + TestUtils.tTest(sample1,sample2));
		
		//TransposeDataCollection transposeDataCollection = new TransposeDataCollection();
        //transposeDataCollection.put(0, new FlatDataCollection(Arrays.asList(new Object[]{5,4,3})));
        //transposeDataCollection.put(1, new FlatDataCollection(Arrays.asList(new Object[]{7,6,16,5,4,3})));
		
		//Log.logCommandLine("" + LevenesIndependentSamples.testVariances(transposeDataCollection, 0.5));
		
    	/*
		RConnection connection = null;
		try {
            connection = new RConnection();

            String vector = "c(1,2,3,4)";
            connection.eval("meanVal=mean(" + vector + ")");
            double mean = connection.eval("meanVal").asDouble();
            Log.logCommandLine("The mean of given vector is = " + mean);
        } catch (RserveException e) {
            //e.printStackTrace();            
        } catch (REXPMismatchException e) {
            //e.printStackTrace();
        } finally {
        	if (connection != null) {
        		connection.close();
        	}
        }
		*/
    	
    	//int[] sample1 = new int[]{2,0};
    	//int[] sample2 = new int[]{2,2};
    	//Log.logCommandLine("" + RserveUtil.chiSquareTest(sample1, sample2));
    	//MiningWindow.showMessageBox("test");
    	/*
    	Rengine r = new Rengine(new String[]{"--no-save"}, false, null);
		System.out.println("Rengine created, waiting for R");
		 
        // the engine creates R is a new thread, so we should wait until it's
        // ready
        if (!r.waitForR()) {
            System.out.println("Cannot load R");
        }
	    r.eval("library(Hmisc)");
	    r.eval("yy <- describe(rnorm(200))");
	    REXP exp = r.eval("zz <- yy$counts[5:11]");
	    REXP names = r.eval("names(zz)");
	    String[] strExp = exp.asStringArray();
	    System.out.println("result:" + exp);
	 
	    r.eval("histval <- hist(rnorm(100), plot=FALSE)");
	    REXP xvalExp = r.eval("histval$mids");
	    REXP yvalExp = r.eval("histval$counts");
	    
	    Log.logCommandLine("histval$mids:" + xvalExp + " histval$counts:" + yvalExp);
		*/
		// --------------------------------
    }

}