package nl.liacs.subdisc;

import java.io.*;

public class JARTextFileLoader
{
	private static Class<? extends JARTextFileLoader> itsClass;
	
	public static String load(String theFileName) {
		return load(theFileName, "\n");
	}
	
	public static String load(String theFileName, String theSeperator) {
		if (null == itsClass) {
			itsClass = (new JARTextFileLoader()).getClass();
		}
		
		if (false == theFileName.startsWith("/")) {
			theFileName = "/" + theFileName;
		}
		
		theFileName = "/config" + theFileName;
		
		StringBuffer sb = new StringBuffer();
		
		try {
			BufferedReader txtReader = new BufferedReader(new InputStreamReader(itsClass.getResourceAsStream(theFileName)));
			String s;
		    while ((s=txtReader.readLine())!=null) {
	            sb.append(s);
	            if (null != theSeperator) {
	            	sb.append(theSeperator); //if you want the newline
	            }
		    }
		    return sb.toString();
		}
		catch (Exception e) {
			return null;
		}
	}
}
