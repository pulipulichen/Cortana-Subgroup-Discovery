package nl.liacs.subdisc;


import java.io.*;
import java.net.*;
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
			itsIni = new Ini(new File(aJarPath + "/config.ini"));
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
		return (String) itsIni.get(aHeader, aKey);
	}
	
	public static String getString(String aHeader, String aKey, String aDefaultValue) {
		String result = getString(aHeader, aKey);
		return (result != null ? result: aDefaultValue );
	}
	
	public static boolean getBoolean(String aHeader, String aKey) {
		loadIni();
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
		String aValue = (String) itsIni.get(aHeader, aKey);
		return (aValue != null ? Float.parseFloat(aValue): aDefaultValue);
	}
}