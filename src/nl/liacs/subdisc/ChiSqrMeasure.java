package nl.liacs.subdisc;

import java.util.*;

public class ChiSqrMeasure
{
	private int itsSampleSize;
	public static QM itsQualityMeasure; // FIXME MM this should not be static
	
	private static String itsFunctionRscript = null; 
	
	private String[] itsIVdata;
	private float[] itsCOVdata;
	private float[] itsDVdata;
	
	private boolean itsIsParametricAncova = true;
	private double itsFstatPval;
	private ArrayList<String> itsPairwiseComparison;

	//make a base model from two columns
	public ChiSqrMeasure(QM theType, Column theIVColumn, Column theCOVColumn, Column theDVColumn)
	{
		itsQualityMeasure = theType;
		

		// 20180616 we have to init ANCOVA result based on theIVColumn, theCOVColumn, and the DVColumn
		//itsPairwiseComparison.add("A > B*");
		
		itsSampleSize = theIVColumn.size();
		itsIVdata = new String[itsSampleSize];
		itsCOVdata = new float[itsSampleSize];
		itsDVdata = new float[itsSampleSize];
		for(int i=0; i<itsSampleSize; i++)
		{
			itsIVdata[i] = theIVColumn.getString(i);
			itsCOVdata[i] = theCOVColumn.getFloat(i);
			itsDVdata[i] = theDVColumn.getFloat(i);
		}
		calc();
	}
	
	public ChiSqrMeasure(ChiSqrMeasure theBase, int sample1True, int sample1False, int sample2True, int sample2False)
	{
		itsQualityMeasure = theBase.itsQualityMeasure;
		itsSampleSize = theIVdata.length;
		itsIVdata = theIVdata;
		itsCOVdata = theCOVdata;
		itsDVdata = theDVdata;
		calc();
	}
	
	private void initFunctionRscript() {
		// Init AncovaMeasure.itsRscriptFoot
		if (null == ChiSqrMeasure.itsFunctionRscript) {
			String aRscript = JARTextFileLoader.load("/r-scripts/ancova.R", "");
			String aSplitor = "print(\"data|script\");";
			int endIndex = aRscript.indexOf(aSplitor);
			ChiSqrMeasure.itsFunctionRscript = aRscript.substring(0, endIndex);
			Log.logCommandLine("itsFunctionRscript: " + ChiSqrMeasure.itsFunctionRscript);
		}
	}
	
	private void setNullResult() {
		itsFstatPval = 1;
		itsPairwiseComparison = new ArrayList<String>( Arrays.asList( new String[]{"null"} ) );
	}
	
	private void calc() {
		
		if (itsSampleSize < 4) {
			setNullResult();
			return;
		}
		
		HashMap<String, Integer> ivLevelCount =  new HashMap<String, Integer>();
		
		String aIVData = "ancova_sm_ancova(data.frame(iv = c(";
		String aCOVData = "),cov = c(";
		String aDVData = "),dv = c(";
		String anEndData = ")));";
		for(int i=0; i<itsSampleSize; i++)
		{
			if (i > 0) {
				aIVData += ",";
				aCOVData += ",";
				aDVData += ",";
			}
			
			if (false == ivLevelCount.containsKey(itsIVdata[i])) {
				ivLevelCount.put(itsIVdata[i], 1);
			}
			else {
				ivLevelCount.put(itsIVdata[i], (ivLevelCount.get(itsIVdata[i])+1));
			}
			
			aIVData += "'" + itsIVdata[i] + "'";
			aCOVData += itsCOVdata[i];
			aDVData += itsDVdata[i];
		}
		
		boolean isDataValided = true;
		// 1. 至少要有2種key
		Set<String> ivKeys = ivLevelCount.keySet();
		if (ivKeys.size() < 2) {
			isDataValided = false;
		}
		
		// 2. 每種key都要有2個以上
		for (String key: ivKeys) {
			if (ivLevelCount.get(key) < 3) {
				isDataValided = false;
			}
		}
		
		if (false == isDataValided) {
			setNullResult();
			return;
		}
		
		// ----------------------
		
		//String aData = "iv = c(1,1,1,1,1,1,1,1,1,1,2,2,2,2,2,2,2,2,2,2),cov = c(11,12,19,13,17,15,17,14,13,16,11,14,10,12,12,13,10,15,14,11),dv = c(21,23,25,23,23,24,24,20,22,24,21,24,21,20,23,24,23,21,25,24)";
		String aDataScript = aIVData + aCOVData + aDVData + anEndData;
		Log.logCommandLine("aDataScript: " + aDataScript);
		
		// Init AncovaMeasure.itsRscriptFoot
		initFunctionRscript();
		
		//Log.logCommandLine("aRscript: " + aRscript);
		
		
		String aReturn = RserveUtil.runScript("TRIPLE_ANCOVA", aDataScript, ChiSqrMeasure.itsFunctionRscript);
		
		String[] aReturnParts = aReturn.split(",");
		
		if (aReturnParts[0].equals("FALSE")) {
			itsIsParametricAncova = false;
		}
		itsFstatPval = Double.parseDouble(aReturnParts[1]);
		//Log.logCommandLine("ANCOVA Result: " + aReturnParts[1]);
		
		String[] aPairwiseComparison = aReturnParts[2].split(";");
		//itsPairwiseComparison.add(aReturnParts[1] + " (" + aReturnParts[0] + ")");
		itsPairwiseComparison = new ArrayList<String>( Arrays.asList( aPairwiseComparison ) );
	}
	
	public boolean isParametricAncova() {
		return itsIsParametricAncova;
	}
	

	public double getFstatPval() {
		return itsFstatPval;
	}
	public double getFstatPvalInvert() {
		return (1 - itsFstatPval);
	}
	
	public String getFormatPairwiseComparison() {
		String output = "";
		for (String p : itsPairwiseComparison) {
			if (false == output.equals("")) {
				output = output + "; ";
			}
			output = output + p;
		}
		
		if (false == itsIsParametricAncova) {
			output = "[NP] " + output;
		}
		
		return output;
	}
}
