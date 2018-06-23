package nl.liacs.subdisc;


import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;


import org.ini4j.*;

/**
 * Utility class for JavaScript compatible UTF-8 encoding and decoding.
 * 
 * @see http://stackoverflow.com/questions/607176/java-equivalent-to-javascripts-encodeuricomponent-that-produces-identical-output
 * @author John Topley 
 */
public class ConfigIni
{
	private static String itsIniName = "config.ini";
	private static String itsIniPath;
	private static File itsIniFile;
	private static Ini itsIni;
	
	private static void loadIni() {
		String aJarPath = ".";
		try {
			if (null == itsIniFile) {
				iniIniFile();
			}
			
			if (itsIniFile.isFile() == false) {
				String aConfigContent = JARTextFileLoader.load(itsIniName);
				BufferedWriter writer = null;
			    writer = new BufferedWriter( new FileWriter( itsIniPath ));
			    writer.write( aConfigContent);
			
		        if ( writer != null) {
			        writer.close( );
		        }
		        itsIniFile = new File(itsIniPath);
			}
			itsIni = new Ini(itsIniFile);
		}
		catch (Exception e) {
			Log.logCommandLine("ConfigIni load error: " + e.getMessage());
		}
	}
	
	private static void iniIniFile() {
		try {
			String aJarPath = new File(ConfigIni.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
			itsIniPath = aJarPath + "/" + itsIniName;
			itsIniFile = new File(itsIniPath);
		}
		catch (Exception e) {
			Log.logCommandLine("ConfigIni iniIniPath() error: " + e.getMessage());
		}
	}
	
	public static String get(String aHeader, String aKey) {
		return getString(aHeader, aKey);
	}
	
	public static String get(String aHeader, String aKey, String aDefaultValue) {
		return getString(aHeader, aKey, aDefaultValue);
	}
	
	public static String getString(String aHeader, String aKey) {
		loadIni();
		if (null == itsIni) {
			return null;
		}
		return (String) itsIni.get(aHeader, aKey);
	}
	
	public static String getString(String aHeader, String aKey, String aDefaultValue) {
		String result = getString(aHeader, aKey);
		if (null == itsIni) {
			return aDefaultValue;
		}
		return (result != null ? result: aDefaultValue );
	}
	
	public static boolean getBoolean(String aHeader, String aKey) {
		loadIni();
		if (null == itsIni) {
			return false;
		}
		String aValue = (String) itsIni.get(aHeader, aKey);
		if (null == aValue) {
			return false;
		}
		else {
			aValue = aValue.toLowerCase().trim();
			return (aValue.equals("true") || aValue.equals("t")  
					|| aValue.equals("yes") || aValue.equals("y")
					||aValue.equals("1"));
		}
	}
	
	
	public static int getInt(String aHeader, String aKey, int aDefaultValue) {
		loadIni();
		if (null == itsIni) {
			return aDefaultValue;
		}
		String aValue = (String) itsIni.get(aHeader, aKey);
		return (aValue != null ? Integer.parseInt(aValue): aDefaultValue);
	}
	
	/*
	public static int getInt(String aHeader, String aKey, int aDefaultValue) {
		String result = getInt(aHeader, aKey);
		return (result != null ? result: aDefaultValue );
	}
	*/
	
	public static float getFloat(String aHeader, String aKey, double aDefaultValue) {
		return getFloat(aHeader, aKey, (float) (aDefaultValue));
	}
	
	public static float getFloat(String aHeader, String aKey, float aDefaultValue) {
		loadIni();
		if (null == itsIni) {
			return aDefaultValue;
		}
		String aValue = (String) itsIni.get(aHeader, aKey);
		return (aValue != null ? Float.parseFloat(aValue): aDefaultValue);
	}
	
	public static void set(String aHeader,  String aKey, String aValue) {
		String content = "";
		boolean underHeader = false;
		boolean isKeyFound = false;
		aValue = aValue.replaceAll("\\\\", "\\\\\\\\");
		try {
			if (null == itsIniFile) {
				iniIniFile();
			}
			
			BufferedReader br = new BufferedReader(new FileReader(itsIniFile));
			for(String line; (line = br.readLine()) != null; ) {
		    	
		    	if (content.equals("") == false) {
		    		content = content + "\n";
		    	}
		    	
		        // process the line.
		    	if (false == underHeader) {
		    		if (line.startsWith("[" + aHeader + "]")) {
		    			underHeader = true;
		    		}
		    		content = content + line;
		    	}
		    	else {
		    		if (line.startsWith("[")) {
		    			underHeader = false;
		    			
		    			if (false == isKeyFound) {
		    				content = content + aKey + " = " + aValue + "\n";
		    				isKeyFound = true;
		    			}
		    			
		    			content = content + line;
		    			
		    		}
		    		else {
		    			if (line.replaceAll(" ", "")
		    					.startsWith(aKey + "=")) {
		    				// match key
		    				content = content + aKey + " = " + aValue;
		    				isKeyFound = true;
		    			}
		    			else {
		    				// not match key
		    				content = content + line;
		    			}
		    		}
		    	}
		    }
			
			if (false == isKeyFound) {
				content = content + "\n" + aKey + " = " + aValue;
				isKeyFound = true;
			}
		    
			//Log.logCommandLine("Set config: " + isKeyFound + " \n" + content);
			
		    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(itsIniFile, false)));
			out.println(content);
			out.close();
		}
		catch (Exception e) {
			Log.logCommandLine("ConfigIni.set() error: " + e.getMessage());
		}
	}
}