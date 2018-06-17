package nl.liacs.subdisc;

import java.awt.geom.*;
import java.util.*;
import Jama.*;

public class AncovaMeasure
{
	private int itsSampleSize;
	private double itsXSum; //The sum of all X-values
	private double itsYSum; // The sum of all Y-values
	private double itsXYSum;// SUM(x*y)
	private double itsXSquaredSum;// SUM(x*x)
	private double itsYSquaredSum;// SUM(y*y)

	private double itsErrorTermSquaredSum;//Sum of all the squared error terms, changes whenever the regression function is updated
	private double itsComplementErrorTermSquaredSum;//Sum of all the squared error terms of this complement, changes whenever the regression function is updated

	private double itsSlope; //The slope-value of the regression function
	private double itsIntercept;//The intercept-value of the regression function
	private double itsComplementSlope; //The slope-value of the regression function
	private double itsComplementIntercept;//The intercept-value of the regression function

	private double itsCorrelation;

	private List<Point2D.Float> itsData;//Stores all the datapoints for this measure
	private List<Point2D.Float> itsComplementData = new ArrayList<Point2D.Float>();//Stores all the datapoints for the complement

	public static QM itsQualityMeasure; // FIXME MM this should not be static
	private AncovaMeasure itsBase = null;

	private Matrix itsDataMatrix;
	private Matrix itsBetaHat;
	private Matrix itsHatMatrix;
	private Matrix itsResidualMatrix;

	private double itsP;
	private double itsSSquared;
	private double[] itsRSquared;
	private double[] itsT;
	private double[] itsSVP;

	private int itsBoundSevenCount;
	private int itsBoundSixCount;
	private int itsBoundFiveCount;
	private int itsBoundFourCount;
	private int itsRankDefCount;
	
	private String itsRscriptHead = "input <- data.frame(";
	private String itsRscriptFoot = ");reg.model <- aov(dv~iv*cov,data=input);reg.interaction <- summary(reg.model)[[1]][[\"Pr(>F)\"]][3];if (is.null(reg.interaction) || is.na(reg.interaction)) {    p.value <- 1;} else if (reg.interaction > 0.05) {    ancova.model <- aov(dv~as.factor(iv)+cov,data=input);    library(car);    ancova.model.type3 <- Anova(ancova.model, type=3);    p.value <- ancova.model.type3[\"Pr(>F)\"][[1]][2];    library(emmeans);    ancova.compare.summary <- summary((emmeans(ancova.model, pairwise ~ iv, adjust = \"none\"))$contrasts);    ancova.compare.summary$contrast <- ifelse(ancova.compare.summary$estimate > 0, gsub(\" - \", \" > \", ancova.compare.summary$contrast), ifelse(ancova.compare.summary$estimate < 0, gsub(\" - \", \" < \", ancova.compare.summary$contrast), gsub(\" - \", \" = \", ancova.compare.summary$contrast)));    ancova.compare.summary$contrast <- ifelse(ancova.compare.summary$p.value < 0.05, paste0(ancova.compare.summary$contrast, \"*\"), ancova.compare.summary$contrast);    pairwise.result <- ancova.compare.summary$contrast;    } else {        library(gtools);    library(plyr);    iv.comb <- combinations(n=as.integer(summary(iv.levels)[\"Length\"]), r=2, v=iv.levels, repeats.allowed=F);    iv.comb.freq <- sum(count(iv.comb)$freq);    iv.comb <- cbind(iv.comb, c(rep(\"=\",iv.comb.freq)));    iv.comb <- cbind(iv.comb, c(rep.int(NA,iv.comb.freq)));    iv.comb <- cbind(iv.comb, c(rep(\"\",iv.comb.freq)));    library(fANCOVA);    p.value <- 1;    for (i in 1:iv.comb.freq) {        comb <- iv.comb[i,];        input.comb <- input[input$iv %in% comb, ];        input.comb <- input.comb[with(input.comb, order(iv)), ];        Taov.result <- T.aov(input.comb[,\"cov\"], input.comb[,\"dv\"], input.comb[,\"iv\"], plot=TRUE, data.points=TRUE);        loess.result <- loess.ancova(input.comb[,\"cov\"], input.comb[,\"dv\"], input.comb[,\"iv\"], plot=TRUE, data.points=TRUE);        fitted.data <- data.frame(loess.result$smooth.fit$fitted, input.comb[,\"iv\"]);        colnames(fitted.data)<- c(\"fitted\",\"iv\");        fitted.data1 <- fitted.data[fitted.data$iv %in% comb[1], ];        fitted.data1.mean <- mean(fitted.data1[,\"fitted\"]);        fitted.data2 <- fitted.data[fitted.data$iv %in% comb[2], ];        fitted.data2.mean <- mean(fitted.data2[,\"fitted\"]);        iv.comb[i,3] <- Taov.result$p.value;        p.value <- ifelse(Taov.result$p.value < p.value, Taov.result$p.value, p.value);                if (fitted.data1.mean > fitted.data2.mean) {            iv.comb[i,4] <- \">\"        } else if (fitted.data1.mean < fitted.data2.mean) {            iv.comb[i,4] <- \"<\"        } else {            iv.comb[i,4] <- \"=\"        };        iv.comb[i,5] <- paste(iv.comb[i,1], iv.comb[i,4], iv.comb[i,2]);        if (iv.comb[i,3] < 0.05) {            iv.comb[i,5] <- paste0(iv.comb[i,5], \"*\")        };    };    pairwise.result <- iv.comb[,5];};paste(sprintf(\"%.5f\", p.value), paste(pairwise.result, collapse=\";\"), sep=\",\");";
	
	private String[] itsIVdata;
	private float[] itsCOVdata;
	private float[] itsDVdata;
	private double itsFstatPval;
	private ArrayList<String> itsPairwiseComparison = new ArrayList<String>();

	//make a base model from two columns
	public AncovaMeasure(QM theType, Column theIVColumn, Column theCOVColumn, Column theDVColumn)
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
	
	public AncovaMeasure(AncovaMeasure theBase, String[] theIVdata, float[] theCOVdata, float[] theDVdata)
	{
		itsQualityMeasure = theBase.itsQualityMeasure;
		itsSampleSize = theIVdata.length;
		itsIVdata = theIVdata;
		itsCOVdata = theCOVdata;
		itsDVdata = theDVdata;
		calc();
	}

	//constructor for non-base RM. It derives from a base-RM
	public AncovaMeasure(AncovaMeasure theBase, BitSet theMembers)
	{
		itsQualityMeasure = theBase.itsQualityMeasure;
		itsBase = theBase;

		/*
		//Create an empty measure
		itsSampleSize = 0;
		itsXSum = 0;
		itsYSum = 0;
		itsXYSum = 0;
		itsXSquaredSum = 0;
		itsYSquaredSum = 0;

		itsData = new ArrayList<Point2D.Float>(theMembers.cardinality());
		itsComplementData =
			new ArrayList<Point2D.Float>(itsBase.getSampleSize() - theMembers.cardinality()); //create empty one. will be filled after update()

		for (int i=0; i<itsBase.getSampleSize(); i++)
		{
			Point2D.Float anObservation = itsBase.getObservation(i);
			if (theMembers.get(i))
				addObservation(anObservation);
			else //complement
				itsComplementData.add(anObservation);
		}
		*/
	}
	
	private void calc() {
		
		if (itsSampleSize < 4) {
			itsFstatPval = 1;
			return;
		}
		
		String aIVData = "iv = c(";
		String aCOVData = "),cov = c(";
		String aDVData = "),dv = c(";
		for(int i=0; i<itsSampleSize; i++)
		{
			if (i > 0) {
				aIVData += ",";
				aCOVData += ",";
				aDVData += ",";
			}
			
			aIVData += "'" + itsIVdata[i] + "'";
			aCOVData += itsCOVdata[i];
			aDVData += itsDVdata[i];
		}
		
		
		
		//String aData = "iv = c(1,1,1,1,1,1,1,1,1,1,2,2,2,2,2,2,2,2,2,2),cov = c(11,12,19,13,17,15,17,14,13,16,11,14,10,12,12,13,10,15,14,11),dv = c(21,23,25,23,23,24,24,20,22,24,21,24,21,20,23,24,23,21,25,24)";
		String aData = aIVData + aCOVData + aDVData + ")";
		Log.logCommandLine("aData: " + aData);
		
		String aRscript = itsRscriptHead + aData + itsRscriptFoot;
		
		
		String aReturn = RserveUtil.runScript("TRIPLE_ANCOVA", aData, aRscript);
		
		String[] aReturnParts = aReturn.split(",");
		
		itsFstatPval = Double.parseDouble(aReturnParts[0]);
		//Log.logCommandLine("ANCOVA Result: " + aReturnParts[1]);
		
		String[] aPairwiseComparison = aReturnParts[1].split(";");
		//itsPairwiseComparison.add(aReturnParts[1] + " (" + aReturnParts[0] + ")");
		itsPairwiseComparison = new ArrayList<String>( Arrays.asList( aPairwiseComparison ) );
	}
	
	public String getPairwiseComparison() {
		String output = "";
		for (String p : itsPairwiseComparison) {
			if (false == output.equals("")) {
				output = output + "; ";
			}
			output = output + p;
		}
		return output;
	}
	
	public double getFstatPval() {
		return itsFstatPval;
	}
	public double getFstatPvalInvert() {
		return (1 - itsFstatPval);
	}
}
