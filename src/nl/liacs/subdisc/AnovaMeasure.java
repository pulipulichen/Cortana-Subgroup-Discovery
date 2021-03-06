package nl.liacs.subdisc;

import java.util.*;
import java.lang.*;

public class AnovaMeasure
{
	private int itsSampleSize;
	public static QM itsQualityMeasure; // FIXME MM this should not be static
	
	//private String itsRscriptHead = "input <- data.frame(";
	//private String itsRscriptFoot = ");reg.model <- aov(dv~iv*cov,data=input);reg.interaction <- summary(reg.model)[[1]][[\"Pr(>F)\"]][3];if (is.null(reg.interaction) || is.na(reg.interaction)) {    p.value <- 1;} else if (reg.interaction > 0.05) {    ancova.model <- aov(dv~as.factor(iv)+cov,data=input);    library(car);    ancova.model.type3 <- Anova(ancova.model, type=3);    p.value <- ancova.model.type3[\"Pr(>F)\"][[1]][2];    library(emmeans);    ancova.compare.summary <- summary((emmeans(ancova.model, pairwise ~ iv, adjust = \"none\"))$contrasts);    ancova.compare.summary$contrast <- ifelse(ancova.compare.summary$estimate > 0, gsub(\" - \", \" > \", ancova.compare.summary$contrast), ifelse(ancova.compare.summary$estimate < 0, gsub(\" - \", \" < \", ancova.compare.summary$contrast), gsub(\" - \", \" = \", ancova.compare.summary$contrast)));    ancova.compare.summary$contrast <- ifelse(ancova.compare.summary$p.value < 0.05, paste0(ancova.compare.summary$contrast, \"*\"), ancova.compare.summary$contrast);    pairwise.result <- ancova.compare.summary$contrast;    } else {        library(gtools);    library(plyr);    iv.comb <- combinations(n=as.integer(summary(iv.levels)[\"Length\"]), r=2, v=iv.levels, repeats.allowed=F);    iv.comb.freq <- sum(count(iv.comb)$freq);    iv.comb <- cbind(iv.comb, c(rep(\"=\",iv.comb.freq)));    iv.comb <- cbind(iv.comb, c(rep.int(NA,iv.comb.freq)));    iv.comb <- cbind(iv.comb, c(rep(\"\",iv.comb.freq)));    library(fANCOVA);    p.value <- 1;    for (i in 1:iv.comb.freq) {        comb <- iv.comb[i,];        input.comb <- input[input$iv %in% comb, ];        input.comb <- input.comb[with(input.comb, order(iv)), ];        Taov.result <- T.aov(input.comb[,\"cov\"], input.comb[,\"dv\"], input.comb[,\"iv\"], plot=TRUE, data.points=TRUE);        loess.result <- loess.ancova(input.comb[,\"cov\"], input.comb[,\"dv\"], input.comb[,\"iv\"], plot=TRUE, data.points=TRUE);        fitted.data <- data.frame(loess.result$smooth.fit$fitted, input.comb[,\"iv\"]);        colnames(fitted.data)<- c(\"fitted\",\"iv\");        fitted.data1 <- fitted.data[fitted.data$iv %in% comb[1], ];        fitted.data1.mean <- mean(fitted.data1[,\"fitted\"]);        fitted.data2 <- fitted.data[fitted.data$iv %in% comb[2], ];        fitted.data2.mean <- mean(fitted.data2[,\"fitted\"]);        iv.comb[i,3] <- Taov.result$p.value;        p.value <- ifelse(Taov.result$p.value < p.value, Taov.result$p.value, p.value);                if (fitted.data1.mean > fitted.data2.mean) {            iv.comb[i,4] <- \">\"        } else if (fitted.data1.mean < fitted.data2.mean) {            iv.comb[i,4] <- \"<\"        } else {            iv.comb[i,4] <- \"=\"        };        iv.comb[i,5] <- paste(iv.comb[i,1], iv.comb[i,4], iv.comb[i,2]);        if (iv.comb[i,3] < 0.05) {            iv.comb[i,5] <- paste0(iv.comb[i,5], \"*\")        };    };    pairwise.result <- iv.comb[,5];};paste(sprintf(\"%.5f\", p.value), paste(pairwise.result, collapse=\";\"), sep=\",\");";
	private static String itsFunctionRscript = null; 
	
	private String[] itsIVdata;
	private float[] itsDVdata;
	
	private String itsMethod;
	private double itsFstatPval = 0;
	private ArrayList<String> itsPairwiseComparison = new ArrayList<String>();
	
	private String itsRscriptKey = "ANOVA_MEASURE";
	private String itsRscriptFileName = "cortana_anova.R";

	//make a base model from two columns
	public AnovaMeasure(QM theType, Column theIVColumn, Column theDVColumn)
	{
		itsQualityMeasure = theType;
		

		// 20180616 we have to init ANCOVA result based on theIVColumn, theCOVColumn, and the DVColumn
		//itsPairwiseComparison.add("A > B*");
		
		if (theIVColumn == null 
				|| theDVColumn == null) {
			return;
		}
		
		itsSampleSize = theIVColumn.size();
		itsIVdata = new String[itsSampleSize];
		itsDVdata = new float[itsSampleSize];
		for(int i=0; i<itsSampleSize; i++)
		{
			itsIVdata[i] = theIVColumn.getString(i);
			itsDVdata[i] = theDVColumn.getFloat(i);
		}
		calc();
	}
	
	public AnovaMeasure(AnovaMeasure theBase, String[] theIVdata, float[] theDVdata)
	{
		itsQualityMeasure = theBase.itsQualityMeasure;
		itsSampleSize = theIVdata.length;
		itsIVdata = theIVdata;
		itsDVdata = theDVdata;
		calc();
	}
	
	private void initFunctionRscript() {
		// Init AncovaMeasure.itsRscriptFoot
		if (null == AnovaMeasure.itsFunctionRscript) {
			String aRscript = JARTextFileLoader.load("/r-scripts/" + itsRscriptFileName, "");
			String aSplitor = "print(\"script|data\");";
			int endIndex = aRscript.indexOf(aSplitor);
			AnovaMeasure.itsFunctionRscript = aRscript.substring(0, endIndex);
			//Log.logCommandLine("itsFunctionRscript: " + AncovaMeasure.itsFunctionRscript);
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
		
		String aIVData = "cortana_anova(data.frame(iv = c(";
		String aDVData = "),dv = c(";
		String anEndData = ")));";
		for(int i=0; i<itsSampleSize; i++)
		{
			if (i > 0) {
				aIVData += ",";
				aDVData += ",";
			}
			
			if (false == ivLevelCount.containsKey(itsIVdata[i])) {
				ivLevelCount.put(itsIVdata[i], 1);
			}
			else {
				ivLevelCount.put(itsIVdata[i], (ivLevelCount.get(itsIVdata[i])+1));
			}
			
			aIVData += "'" + itsIVdata[i] + "'";
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
		String aDataScript = aIVData + aDVData + anEndData;
		//Log.logCommandLine("aDataScript: " + aDataScript);
		
		// Init AncovaMeasure.itsRscriptFoot
		initFunctionRscript();
		
		//Log.logCommandLine("aRscript: " + aRscript);
		
		
		String aReturn = RserveUtil.runScript(itsRscriptKey, aDataScript, AnovaMeasure.itsFunctionRscript);
		if (null == aReturn) {
			return;
		}
		
		String[] aReturnParts = aReturn.split(",");
		
		itsMethod = aReturnParts[0];
		itsFstatPval = Double.parseDouble(aReturnParts[1]);
		//Log.logCommandLine("ANCOVA Result: " + aReturnParts[1]);
		
		String[] aPairwiseComparison = aReturnParts[2].split(";");
		//itsPairwiseComparison.add(aReturnParts[1] + " (" + aReturnParts[0] + ")");
		itsPairwiseComparison = new ArrayList<String>( Arrays.asList( aPairwiseComparison ) );
	}
		
	public String getMethod() {
		return itsMethod;
	}
	

	public double getFstatPval() {
		return itsFstatPval;
	}
	
	public double getFstatPvalInvert() {
		return (Math.round( (1 - itsFstatPval) * 100) / 100);
	}
	
	public String getFormatPairwiseComparison() {
		String output = "";
		if (itsPairwiseComparison == null) {
			return output;
		}
		
		for (String p : itsPairwiseComparison) {
			if (false == output.equals("")) {
				output = output + "; ";
			}
			output = output + p;
		}
		
		return output;
	}
}
