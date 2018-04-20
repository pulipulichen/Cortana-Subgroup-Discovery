package nl.liacs.subdisc;


import java.util.*;

import javax.swing.*;

import org.rosuda.REngine.*;
import org.rosuda.REngine.Rserve.*;

/**
 * Utility class for JavaScript compatible UTF-8 encoding and decoding.
 * 
 * @see http://stackoverflow.com/questions/607176/java-equivalent-to-javascripts-encodeuricomponent-that-produces-identical-output
 * @author John Topley 
 */
public class RserveUtil
{
	public static RConnection connection;
	
	public static void connect() {
		try {
			connection = new RConnection();
		}
		catch (Exception e) {
			String message = "Rserve connection failed.";
			JOptionPane.showMessageDialog(null, message);
			connection = null;
		}
	}
	
	public static void disconnect() {
		if (connection != null) {
			connection.close();
			connection = null;
		}
	}
	
	private static String chiSquareTestRScript = "tbl <-matrix(data, nrow = 2);expDat <- tbl;for (i in 1:2){expDat[i,1] <- (sum(tbl[i,]) * sum(tbl[,1])) / sum(tbl);  expDat[i,2] <- (sum(tbl[i,]) * sum(tbl[,2])) / sum(tbl);};if (sum(data) <= 20 && length(which(expDat <= 5))>0) {result <- fisher.test(tbl)} else {result <- chisq.test(tbl)};(1-result$p.value)";
	
	public static float chiSquareTest(int sample1True, int sample1False, int sample2True, int sample2False) {
		float aReturn = 0;
		
		if (connection == null) {
			return aReturn;
		}
		
		try {
			String rScript = "data <- c(" + sample1True + ", " + sample1False + ", " + sample2True + ", " + sample2False + ");" 
        		  + chiSquareTestRScript;
          
			aReturn = (float) connection.eval(rScript).asDouble();
          
		//} catch (RserveException e) {
			//e.printStackTrace();            
		//} catch (REXPMismatchException e) {
			//e.printStackTrace();
		} catch (Exception e) {
			
		}

        //Log.logCommandLine("" + chiSquareTestRScript);
		return aReturn;
	}

	public static float chiSquareTest(float sample1True, float sample1False, float sample2True, float sample2False) {
		return chiSquareTest((int) sample1True, (int) sample1False, (int) sample2True, (int) sample2False);
	}
}