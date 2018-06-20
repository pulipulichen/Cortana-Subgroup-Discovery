package nl.liacs.subdisc;


import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;

import org.rosuda.REngine.*;
import org.rosuda.REngine.Rserve.*;

import java.util.HashMap;

/**
 * Utility class for JavaScript compatible UTF-8 encoding and decoding.
 * 
 * @see http://stackoverflow.com/questions/607176/java-equivalent-to-javascripts-encodeuricomponent-that-produces-identical-output
 * @author John Topley 
 */
public class RserveUtil
{
	//private static String RPath = "D:\\Program Files\\R\\R-3.4.0\\bin\\R.exe";
	private static RConnection connection;
	private static String itsRPath = null;
	private static String itsRscriptStartup = null;
	private static String itsRscriptShutdown = null;
	
	private static void initRscript() {
		// Init AncovaMeasure.itsRscriptFoot
		if (null == itsRscriptStartup) {
			itsRPath = ConfigIni.get("global", "RPath", "D:\\Program Files\\R\\R-3.4.0\\bin\\R.exe");
			itsRscriptStartup = JARTextFileLoader.load("/r-scripts/r_serve_startup.R", "");
			itsRscriptShutdown = JARTextFileLoader.load("/r-scripts/r_serve_shutdown.R", "");
		}
	}
	
	public static void startup() {
		
		try (Socket ignored = new Socket("localhost", 6311)) {
	        //return false;
	    } catch (IOException ignored) {
	    	Log.logCommandLine("RserveUtil.startup() Socket failed: " + ignored.getMessage());
	        return;
	    }
		
		if (connection != null) {
			return;
		}
		
		initRscript();
		
		final Runtime rt = Runtime.getRuntime();
		try {
			rt.exec("\"" + itsRPath + "\" -e \"" + itsRscriptStartup + "\"");
			Log.logCommandLine("RserveUtil.startup(): \"" + itsRPath + "\" -e \"" + itsRscriptStartup + "\"");
		}
		catch (Exception e) {
			Log.logCommandLine("RserveUtil.startup() failed: " + e.getMessage());
		}
	}
	
	public static void shutdown() {
		if (connection == null) {
			return;
		}
		
		final Runtime rt = Runtime.getRuntime();
		try {
			disconnect();
			rt.exec("\"" + itsRPath + "\" -e \"" + itsRscriptShutdown + "\"");
			Log.logCommandLine("RserveUtil.shutdown(): \"" + itsRPath + "\" -e \"" + itsRscriptShutdown + "\"");
			connection = null;
		}
		catch (Exception e) {
			
		}
		finally {
			runScriptConnectCount = 0;
		}
	}
	
	public static void connect() {
		if (connection != null) {
			return;
		}
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
	
	// -------------------------------------
	
	private static HashMap<String, Float> chiSquareTestCache;
	private static int chiSquareTestCount;
	
	private static String chiSquareTestRScript = "tbl <-matrix(data, nrow = 2);expDat <- tbl;for (i in 1:2){expDat[i,1] <- (sum(tbl[i,]) * sum(tbl[,1])) / sum(tbl);  expDat[i,2] <- (sum(tbl[i,]) * sum(tbl[,2])) / sum(tbl);};if (sum(data) <= 20 && length(which(expDat <= 5))>0) {result <- fisher.test(tbl)} else {result <- chisq.test(tbl)};(1-result$p.value)";
	
	public static float chiSquareTest(int sample1True, int sample1False, int sample2True, int sample2False) {
		float aReturn = 0;
		String key = sample1True + ", " + sample1False + ", " + sample2True + ", " + sample2False;
		
		if (chiSquareTestCache == null) {
			chiSquareTestCache = new HashMap<String, Float>();
		}
		else if (chiSquareTestCache.containsKey(key)) {
			return chiSquareTestCache.get(key);
		}
		
		
		if (connection == null) {
			return aReturn;
		}
		
		try {
			/*
			if (chiSquareTestCount % 10 == 9 && false) {
				chiSquareTestCount = 0;
				disconnect();
				//Thread.sleep(1000);
				connect();
			}
			*/
			Thread.sleep(100);	
			
			
			String rScript = "data <- c("+key+");" 
        		  + chiSquareTestRScript;
			chiSquareTestCount++;
			Log.logCommandLine("Conntect: " + chiSquareTestCount);
          
			aReturn = (float) connection.eval(rScript).asDouble();
			chiSquareTestCache.put(key, aReturn);
          
		//} catch (RserveException e) {
			//e.printStackTrace();            
		//} catch (REXPMismatchException e) {
			//e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			
			try {
				Thread.sleep(1000);
				disconnect();
				shutdown();
				Thread.sleep(1000);
				startup();
				connect();
				return chiSquareTest(sample1True, sample1False, sample2True, sample2False);
			}
			catch (Exception e2) {
				e2.printStackTrace();
			}
		} finally {
	        //Log.logCommandLine("" + chiSquareTestRScript);
			return aReturn;
		}
	}

	public static float chiSquareTest(float sample1True, float sample1False, float sample2True, float sample2False) {
		return chiSquareTest((int) sample1True, (int) sample1False, (int) sample2True, (int) sample2False);
	}
	
	// --------------------
	
	
	private static HashMap<String, String> runScriptCache =  new HashMap<String, String>();
	private static int runScriptConnectCount = 0;
	
	@SuppressWarnings("finally")
	public static String runScript(String aScriptKey, String aDataKey, String aScript) {
		String aReturn = null;
		String key = aScriptKey + "_" + aDataKey;
		
		if (runScriptCache.containsKey(key)) {
			return runScriptCache.get(key);
		}
		
		// ------------------------------------
		
		if (connection == null) {
			Log.logCommandLine("No connection. Please excute RserveUtil.startup() first.");
			return aReturn;
		}
		
		try {
			Thread.sleep(100);	
			
			runScriptConnectCount++;
			Log.logCommandLine("Conntect: " + runScriptConnectCount);
          
			aReturn = (String) connection.eval(aScript).asString();
			runScriptCache.put(key, aReturn);
			return aReturn;
		} catch (Exception e) {
			e.printStackTrace();
			
			try {
				Thread.sleep(1000);
				disconnect();
				shutdown();
				Thread.sleep(1000);
				startup();
				connect();
				return runScript(aScriptKey, aDataKey, aScript);
			}
			catch (Exception e2) {
				e2.printStackTrace();
			}
		} finally {
	        //Log.logCommandLine("" + chiSquareTestRScript);
			return aReturn;
		}
	}
}