package nl.liacs.subdisc;

import java.util.*;

public class ChiSqrMeasure
{
	private int itsSampleSize;
	public static QM itsQualityMeasure; // FIXME MM this should not be static
	
	private static String itsFunctionRscript = null;
	
	private int[] itsCrossTable;
	private int itsRowNum;
	
	private boolean itsIsFisherTest = false;
	private double itsChiSqrPval;
	private double itsAdjStdRes;

	//make a base model from two columns
	public ChiSqrMeasure(int[] theCrossTable, int theRowNum)
	{
		itsCrossTable = theCrossTable;
		itsRowNum = theRowNum;
		calc();
	}
	
	public ChiSqrMeasure(float[] theCrossTable, int theRowNum)
	{
		itsCrossTable = new int[theCrossTable.length];
		for (int i = 0; i < itsCrossTable.length; i++) {
			itsCrossTable[i] = Math.round(theCrossTable[i]);
		}
		itsRowNum = theRowNum;
		calc();
	}
	
	public ChiSqrMeasure(ChiSqrMeasure theBase, int[] theCrossTable, int theRowNum)
	{
		itsQualityMeasure = theBase.itsQualityMeasure;
		itsCrossTable = theCrossTable;
		itsRowNum = theRowNum;
		calc();
	}
	
	private void initFunctionRscript() {
		// Init AncovaMeasure.itsRscriptFoot
		if (null == ChiSqrMeasure.itsFunctionRscript) {
			String aRscript = JARTextFileLoader.load("/r-scripts/cortana_chisq.R", "");
			String aSplitor = "print(\"script|data\");";
			int endIndex = aRscript.indexOf(aSplitor);
			ChiSqrMeasure.itsFunctionRscript = aRscript.substring(0, endIndex);
			Log.logCommandLine("itsFunctionRscript: " + ChiSqrMeasure.itsFunctionRscript);
		}
	}
	
	private void setNullResult() {
		itsIsFisherTest = false;
		itsChiSqrPval = 0d;
		itsAdjStdRes = 0d;
	}
	
	private void calc() {
		
		if (itsCrossTable.length < 4) {
			setNullResult();
			return;
		}
		
		String aDataHeader = "cortana_chisq(c(";
		String aDataFooter = "), " + itsRowNum + ");";
		
		String aData = "" + itsCrossTable[0];
		for (int i = 1; i < itsCrossTable.length; i++) {
			aData = aData + "," + itsCrossTable[i];
		}
		
		String aDataScript = aDataHeader + aData + aDataFooter;
		Log.logCommandLine("aDataScript: " + aDataScript);
		
		// Init AncovaMeasure.itsRscriptFoot
		initFunctionRscript();
		
		//Log.logCommandLine("aRscript: " + aRscript);
		String className = this.getClass().getSimpleName();
		String aReturn = RserveUtil.runScript(className, aDataScript, ChiSqrMeasure.itsFunctionRscript);
		if (null == aReturn) {
			return;
		}
		
		String[] aReturnParts = aReturn.split(",");
		
		if (aReturnParts[2].equals("FALSE")) {
			itsIsFisherTest = false;
		}
		
		itsChiSqrPval = Double.parseDouble(aReturnParts[0]);
		itsAdjStdRes = Double.parseDouble(aReturnParts[1]);
	}
	
	public boolean isFisherTest() {
		return itsIsFisherTest;
	}

	public double getChiSqrPval() {
		return (1 - itsChiSqrPval);
	}
	
	public double getAdjStdRes() {
		return itsAdjStdRes;
	}
	
	public double getQualityMeasure() {
		return getChiSqrPval() * getAdjStdRes();
	}
}
