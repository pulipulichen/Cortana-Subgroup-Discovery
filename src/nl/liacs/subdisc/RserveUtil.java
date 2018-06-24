package nl.liacs.subdisc;


import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;

import org.rosuda.REngine.*;
import org.rosuda.REngine.Rserve.*;

import java.util.HashMap;
import java.util.concurrent.*;

/**
 * Utility class for JavaScript compatible UTF-8 encoding and decoding.
 * 
 * @see http://stackoverflow.com/questions/607176/java-equivalent-to-javascripts-encodeuricomponent-that-produces-identical-output
 * @author John Topley 
 */
public class RserveUtil
{
	//private static String RPath = "D:\\Program Files\\R\\R-3.4.0\\bin\\R.exe";
	private static RConnection connection = null;
	private static String itsRPath = null;
	private static String itsRscriptStartup = null;
	private static String itsRscriptShutdown = null;
	private static boolean itsLazyStartup = false;
	private static Runtime rt;
	
	private static void initRscript() {
		// Init AncovaMeasure.itsRscriptFoot
		if (null == itsRscriptStartup) {
			//return;
			
			itsRPath = ConfigIni.get("rserve", "RPath");
			
			// Check the RPath
			if (tryRPath(itsRPath) == false) {
				itsRPath = null;
			}
			
			if (null == itsRPath) {
				// Guess the RPath
				String[] aRPathCandidate = {
						"R"
				};
				
				for (int i = 0; i < aRPathCandidate.length; i++) {
					if (tryRPath(aRPathCandidate[i])) {
						itsRPath = aRPathCandidate[i];
						break;
					}
				}
				
				// Finding R in directory
				if (null == itsRPath) {
					String[] aRPathFolderCandidate = {
							"C:\\Program Files\\R",
							"D:\\Program Files\\R",
							"C:\\Program Files (x86)\\R",
							"D:\\Program Files (x86)\\R"
					};
					
					for (int i = 0; i < aRPathFolderCandidate.length; i++) {
						File aRfolder = new File(aRPathFolderCandidate[i]);
						if (aRfolder.exists()) {
							// find the first sub folder
							for (final File fileEntry : aRfolder.listFiles()) {
						        if (fileEntry.isDirectory() && fileEntry.getName().startsWith("R-")) {
						        	itsRPath = aRPathFolderCandidate[i] + "\\" + fileEntry.getName() + "\\bin\\R.exe";

						        	if (new File(itsRPath).exists()) {
						        		Log.logCommandLine("Find RPath: " + itsRPath);
						        		ConfigIni.set("global", "RPath", itsRPath);
						        		break;
						        	}
						        }
						    }
						}
					}
				}
				
				if (null == itsRPath) {
					//throw new FileNotFoundException("R path is not correct. May you have not install R.");
					Log.logCommandLine("R path is not correct. May you have not install R.");
					

					int dialogButton = JOptionPane.YES_NO_OPTION;
					int dialogResult = JOptionPane.showConfirmDialog(null, "R has not been installed. \n" 
							+ "Pleasse check the config.ini setting. \n"
							+ "Subgroup result would not be analysed correctly. \n"
							+ "Do you want to open R's download page?", "R is not found", dialogButton);
					if(dialogResult == 0) {
						//System.out.println("Yes option");
						try {
							Desktop.getDesktop().browse(new URI("https://cloud.r-project.org/"));
						}
						catch (Exception e) {
							Log.logCommandLine("Open URI error: " + e.getMessage());
						}
					}
					return;
				}
			}
			
			
			itsRscriptStartup = JARTextFileLoader.load("/r-scripts/r_serve_startup.R", "").replaceAll("\"","\\\\\"");
			itsRscriptShutdown = JARTextFileLoader.load("/r-scripts/r_serve_shutdown.R", "").replaceAll("\"","\\\\\"");
		}
	}
	
	public static boolean tryRPath(String theRPath) {
		if (rt == null) {
			rt = Runtime.getRuntime();
		}
		try {
			rt.exec("\"" + theRPath + "\"");
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}
	
	public static void startup() {
		itsLazyStartup = true;
	}
	
	public static void startup(boolean isForce) {

		initRscript();
		
		if (connection != null) {
			return;
		}
		
		/*
		try (Socket ignored = new Socket("localhost", 6311)) {
	        //return false;
			//Log.logCommandLine("RserveUtil.startup() 6311 is not occupied.");
	    } catch (IOException ignored) {
	    	Log.logCommandLine("RserveUtil.startup() Socket failed: " + ignored.getMessage());
	        return;
	    }
	    */
		
		if (rt == null) {
			rt = Runtime.getRuntime();
		}
		
		try {
			rt.exec("\"" + itsRPath + "\" -e \"" + itsRscriptStartup + "\"");
			Log.logCommandLine("RserveUtil.startup(): \"" + itsRPath + "\" -e \"" + itsRscriptStartup + "\"");
			
			connect();
		}
		catch (Exception e) {
			Log.logCommandLine("RserveUtil.startup() failed: " + e.getMessage());
		}
	}
	
	public static void connect() {
		if (connection != null) {
			return;
		}
		try {
			connection = new RConnection();
			connection.setStringEncoding("utf8");
			Log.logCommandLine("Rserve connectioned. " + (connection != null));
		}
		catch (Exception e) {
			String message = "Rserve connection failed: " + e.getMessage();
			JOptionPane.showMessageDialog(null, message);
			connection = null;
			Log.logCommandLine("Rserve connection failed: " + e.getMessage());
		}
	}
	
	public static void shutdown() {
		itsLazyStartup = false;
		
		if (connection == null) {
			return;
		}
		
		disconnect();
		if (rt == null) {
			rt = Runtime.getRuntime();
		}
		try {
			rt.exec("\"" + itsRPath + "\" -e \"" + itsRscriptShutdown + "\"");
			Log.logCommandLine("RserveUtil.shutdown(): \"" + itsRPath + "\" -e \"" + itsRscriptShutdown + "\"");
			//connection = null;
		}
		catch (Exception e) {
			
		}
		finally {
			runScriptConnectCount = 0;
		}
	}
	
	public static void disconnect() {
		if (connection != null) {
			connection.close();
			connection = null;
			declaredFunctions =  new ArrayList<String>();
			Log.logCommandLine("Rserve disconnectioned. " + (connection != null));
		}
	}
	
	public static void taskkill() {
		if (rt == null) {
			rt = Runtime.getRuntime();
		}
		try {
			rt.exec("taskkill /f /im Rserve.exe");
			Log.logCommandLine("Rserve processes are killed.");
			//connection = null;
		}
		catch (Exception e) {
			
		}
	}
	
	// --------------------
	
	public static String run(String theRscript) {
		startup(true);
		String result;
		try {
			result = connection.eval(theRscript).asString();
			//connection.setStringEncoding(arg0);
		}
		catch (Exception e) {
			result = e.getMessage();
		}
		return result; 
	}
	
	
	private static HashMap<String, String> runScriptCache =  new HashMap<String, String>();
	private static ArrayList<String> declaredFunctions =  new ArrayList<String>();
	private static int runScriptConnectCount = 0;
	private static boolean itsLock = false;
	
	@SuppressWarnings("finally")
	public static String runScript(String aScriptKey, String aDataScript, String aFunctionScript) {

		//if (true == itsLazyStartup) {
		startup(true);
		//}
		
		if (null == itsRPath) {
			Log.logCommandLine("No itsRPath.");
			return null;
		}
		
		String aReturn = null;
		String key = aScriptKey + "_" + aDataScript;
		
		if (runScriptCache.containsKey(key)) {
			return runScriptCache.get(key);
		}
		
		/*
		if (runScriptConnectCount % 100 == 99) {
			try {
				shutdown();
				Thread.sleep(1000);
				startup();
			}
			catch (Exception e) {}
		}
		*/
		
		// ------------------------------------
		
		if (null == connection) {
			Log.logCommandLine("No connection. Please excute RserveUtil.startup() first.");
			return null;
		}
		
		try {
			//Thread.sleep(1000);	
			
			if (declaredFunctions.contains(aScriptKey) == false) {
				Log.logCommandLine("Run Function Script: " + aFunctionScript);
				connection.eval(aFunctionScript);
				
				declaredFunctions.add(aScriptKey);
				runScriptConnectCount++;
				//Thread.sleep(1000);	
			}
			
			runScriptConnectCount++;
			//Log.logCommandLine("Conntect: " + runScriptConnectCount);
          
			//Log.logCommandLine("Conntect: " + connection.eval(aDataScript));
			while (itsLock == true) {
				Thread.sleep(3000 + ThreadLocalRandom.current().nextInt(0, 10 + 1));
			}
			
			itsLock = true;
			//connection.eval(aFunctionScript);
			aReturn = connection.eval(aDataScript).asString();
			itsLock = false;
			runScriptCache.put(key, aReturn);
			return aReturn;
		} catch (Exception e) {
			try {
				Log.logCommandLine("declaredFunctions: " + aFunctionScript);
				connection.eval(aFunctionScript);
				aReturn = connection.eval(aDataScript).asString();
				itsLock = false;
				runScriptCache.put(key, aReturn);
				return aReturn;
			}
			catch (Exception e2) {
				Log.logCommandLine("R script error: " + e2.getMessage());
				Log.logCommandLine(aFunctionScript);
				Log.logCommandLine(aDataScript);
				Log.logCommandLine("" + (connection == null));
				Log.logCommandLine("" + declaredFunctions.toString());
				
				declaredFunctions.remove(aScriptKey);
				itsLock = false;
				//return null;
				
				
				e2.printStackTrace();
				//throw new Exception("Stop Rserve");
				
				try {
					Thread.sleep(10000);
					//disconnect();
					shutdown();
					Thread.sleep(10000);
					//startup();
					//connect();
					return runScript(aScriptKey, aDataScript, aFunctionScript);
				}
				catch (Exception e3) {
					e2.printStackTrace();
					return aReturn;
				}
			}
		}
	}
}