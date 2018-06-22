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
	private static Ini itsIni = null;
	
	private static void loadIni() {
		String aJarPath = ".";
		try {
			aJarPath = new File(ConfigIni.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
			String anIniPath = aJarPath + "/config.ini";
			File f = new File(anIniPath);
			if (f.isFile() == false) {
				String aConfigContent = JARTextFileLoader.load("config.ini");
				BufferedWriter writer = null;
			    writer = new BufferedWriter( new FileWriter( anIniPath ));
			    writer.write( aConfigContent);
			
		        if ( writer != null) {
			        writer.close( );
		        }
			}
			itsIni = new Ini(f);
		}
		catch (Exception e) {
			Log.logCommandLine("ConfigIni load error: " + e.getMessage());
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
	
	public static float getFloat(String aHeader, String aKey, float aDefaultValue) {
		loadIni();
		if (null == itsIni) {
			return aDefaultValue;
		}
		String aValue = (String) itsIni.get(aHeader, aKey);
		return (aValue != null ? Float.parseFloat(aValue): aDefaultValue);
	}
}