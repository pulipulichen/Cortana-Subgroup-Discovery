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
	
	private ArrayList<String> itsPairwiseComparison = new ArrayList<String>();

	//make a base model from two columns
	public AncovaMeasure(QM theType, Column theIVColumn, Column theCOVColumn, Column theDVColumn)
	{
		itsQualityMeasure = theType;
		

		// 20180616 we have to init ANCOVA result based on theIVColumn, theCOVColumn, and the DVColumn
		itsPairwiseComparison.add("A > B*");
		
		RserveUtil.startup();
		RserveUtil.connect();
		String aReturn = RserveUtil.runScript("test", "1,2", "print(paste(1+2,3+4, sep=','))");
		RserveUtil.disconnect();
		RserveUtil.shutdown();
		//RserveUtil.shutdown();
		Log.logCommandLine("File writer error: " + aReturn);
		itsPairwiseComparison.add("B > C ");
		
		/*
		itsSampleSize = theIVColumn.size();
		itsData = new ArrayList<Point2D.Float>(itsSampleSize);
		for(int i=0; i<itsSampleSize; i++)
		{
			itsXSum += theCOVColumn.getFloat(i);
			itsYSum += theDVColumn.getFloat(i);
			itsXYSum += theCOVColumn.getFloat(i)*theDVColumn.getFloat(i);
			itsXSquaredSum += theCOVColumn.getFloat(i)*theCOVColumn.getFloat(i);
			itsYSquaredSum += theDVColumn.getFloat(i)*theDVColumn.getFloat(i);

			itsData.add(new Point2D.Float(theCOVColumn.getFloat(i), theDVColumn.getFloat(i)) );
		}
		*/
		

		/*
		switch (itsQualityMeasure)
		{
			case LINEAR_REGRESSION:
			{
				itsBase = null; //this *is* the base
				itsComplementData = null; //will remain empty for the base RM
				updateRegressionFunction();
				updateErrorTerms();
				break;
			}
		}
		*/
	}

	//constructor for non-base RM. It derives from a base-RM
	public AncovaMeasure(AncovaMeasure theBase, BitSet theMembers)
	{
		itsQualityMeasure = theBase.itsQualityMeasure;
		itsBase = theBase;

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
	}

	//TODO test and verify method
	public double getEvaluationMeasureValue()
	{
		updateRegressionFunction();
		updateErrorTerms();
		return getSSD();
	}

	//TODO turn this t-value into a p-value.
	public double getSSD()
	{
		//determine the sums for the complement
		double aComplementXSum = itsBase.getXSum()-itsXSum;
		double aComplementYSum = itsBase.getYSum()-itsYSum;
		double aComplementXSquaredSum = itsBase.getXSquaredSum()-itsXSquaredSum;
		double aComplementXYSum = itsBase.getXYSum()-itsXYSum;
		double aComplementSampleSize = itsBase.getSampleSize()-itsSampleSize;
		double aComplementXMean = aComplementXSum / aComplementSampleSize;
		double aComplementYMean = aComplementYSum / aComplementSampleSize;

		//determine variance for the distribution
		double aSubgroupNumerator = getErrorTermVariance(itsErrorTermSquaredSum, itsSampleSize);
		double aSubgroupDenominator = itsXSquaredSum - 2*itsXSum*itsXSum/itsSampleSize + itsXSum*itsXSum/itsSampleSize;
		double aSubgroupVariance = aSubgroupNumerator / aSubgroupDenominator;

		//if we divided by zero along the way, we are considering a degenerate candidate subgroup, hence quality=0
		if (itsSampleSize==0 || itsSampleSize==2 || aSubgroupDenominator==0) {
			return 0;
		}
		//else if (itsSampleSize==1) {
		//	return -1;
		//}


		//calculate the difference between slopes of this measure and its complement
		double aSlope = getSlope(itsXSum, itsYSum, itsXSquaredSum, itsXYSum, itsSampleSize);
		double aComplementSlope = getSlope(aComplementXSum, aComplementYSum, aComplementXSquaredSum, aComplementXYSum, aComplementSampleSize);
		double aSlopeDifference = Math.abs(aComplementSlope - aSlope);
		
		itsComplementSlope = aComplementSlope;
		itsComplementIntercept = getIntercept(aComplementXMean, aComplementYMean, itsComplementSlope);
		updateComplementErrorTerms();
		
		//determine variance for the complement distribution
		double aComplementNumerator = getErrorTermVariance(itsComplementErrorTermSquaredSum, aComplementSampleSize);
		double aComplementDenominator = aComplementXSquaredSum - 2*aComplementXSum*aComplementXSum/aComplementSampleSize + aComplementXSum*aComplementXSum/aComplementSampleSize;
		double aComplementVariance = aComplementNumerator/aComplementDenominator;

		//if we divided by zero along the way, we are considering a degenerate candidate subgroup complement, hence quality=0
		if (aComplementSampleSize==0 || aComplementSampleSize==2 || aComplementDenominator==0)
			return 0;
		else if (aComplementSampleSize==1) {
			return -1;
		}


		//Log.logCommandLine("\n  itsSampleSize: " + itsSampleSize);
		Log.logCommandLine("\n   subgroup slope: " + aSlope);
		Log.logCommandLine("  subgroup variance: " + aSubgroupVariance);
		
		Log.logCommandLine("complement slope: " + aComplementSlope);
		//Log.logCommandLine("complement itsComplementErrorTermSquaredSum: " + itsComplementErrorTermSquaredSum);
		//Log.logCommandLine("complement aComplementSampleSize: " + aComplementSampleSize);
		//Log.logCommandLine("complement aNumerator: " + aComplementNumerator);
		//Log.logCommandLine("complement aDenominator: " + aComplementDenominator);
		Log.logCommandLine("complement variance: " + aComplementVariance + "\n");

		if (aSubgroupVariance+aComplementVariance==0) {
			return 0;
		}
		else {
			//TODO turn this t-value into a p-value.
			double aTStat = aSlopeDifference / Math.sqrt(aSubgroupVariance+aComplementVariance);
			double aDFNumerator = (aSubgroupVariance+aSubgroupVariance)*(aSubgroupVariance+aSubgroupVariance);
			double aDFDenominatorSubgroup = aSubgroupVariance*aSubgroupVariance/(itsSampleSize-2);
			double aDFDenominatorComplement = aComplementVariance*aComplementVariance/(aComplementSampleSize-2);
			double aDF = aDFNumerator / (aDFDenominatorSubgroup + aDFDenominatorComplement);
			try {
				double pValue = StatUtils.PValue(aTStat, aDF);
				//double pValue = SmileUtils.calcTTestPValue(aTStat, aDF);
				return (1-pValue);
			}
			catch (Exception e) {
				return 0;
			}
		}
	}

	public double calculate(Subgroup theNewSubgroup)
	{
		BitSet aMembers = theNewSubgroup.getMembers();
		int aSampleSize = aMembers.cardinality();

		//filter out rank deficient model that crash matrix multiplication library
		if (aSampleSize < 2)
		{
			itsRankDefCount++;
			return Double.MIN_VALUE;
		}

		//calculate the upper bound values. Before each bound, only the necessary computations are done.
		double aT = itsT[aSampleSize];
		double aRSquared = itsRSquared[aSampleSize];

		double aBoundSeven = computeBoundSeven(aSampleSize, aT, aRSquared);
		if (aBoundSeven>Double.MIN_VALUE)
		{
			Log.logCommandLine("                   Bound 7: " + aBoundSeven);
			itsBoundSevenCount++;
		}

		int[] anIndices = new int[aSampleSize];
		int[] aRemovedIndices = new int[itsSampleSize-aSampleSize];
		int anIndex=0;
		int aRemovedIndex=0;
		for (int i=0; i<itsSampleSize; i++)
		{
			if (aMembers.get(i))
			{
				anIndices[anIndex] = i;
				anIndex++;
			}
			else
			{
				aRemovedIndices[aRemovedIndex] = i;
				aRemovedIndex++;
			}
		}

		Matrix aRemovedResiduals = itsResidualMatrix.getMatrix(aRemovedIndices,0,0);
		double aSquaredResidualSum = squareSum(aRemovedResiduals);
		double aBoundSix = computeBoundSix(aSampleSize, aT, aSquaredResidualSum);
		if (aBoundSix>Double.MIN_VALUE)
		{
			Log.logCommandLine("                   Bound 6: " + aBoundSix);
			itsBoundSixCount++;
		}

		Matrix aRemovedHatMatrix = itsHatMatrix.getMatrix(aRemovedIndices,aRemovedIndices);
		double aRemovedTrace = aRemovedHatMatrix.trace();
		double aBoundFive = computeBoundFive(aSampleSize, aRemovedTrace, aRSquared);
		if (aBoundFive>Double.MIN_VALUE)
		{
			Log.logCommandLine("                   Bound 5: " + aBoundFive);
			itsBoundFiveCount++;
		}

		double aBoundFour = computeBoundFour(aSampleSize, aRemovedTrace, aSquaredResidualSum);
		if (aBoundFour>Double.MIN_VALUE)
		{
			Log.logCommandLine("                   Bound 4: " + aBoundFour);
			itsBoundFourCount++;
		}

		//compute estimate based on projection of single influence values
		double anSVPDistance = computeSVPDistance(itsSampleSize-aSampleSize, aRemovedIndices);
		Log.logCommandLine("                   SVP est: " + anSVPDistance);

		//start computing Cook's Distance
		Matrix aNewDataMatrix = itsDataMatrix.getMatrix(anIndices,0,1);

		//filter out rank-deficient cases; these regressions cannot be computed, hence low quality
		LUDecomposition itsDecomp = new LUDecomposition(aNewDataMatrix);
		if (!itsDecomp.isNonsingular())
		{
			itsRankDefCount++;
			return Double.MIN_VALUE;
		}

		//make submatrices
		double[][] anXValues = new double[aSampleSize][2];
		for (int i=0; i<aSampleSize; i++)
		{
			anXValues[i][0]=1;
			anXValues[i][1]=aNewDataMatrix.get(i,0);
		}
		double[][] aYValues = new double[aSampleSize][1];
		for (int i=0; i<aSampleSize; i++)
			aYValues[i][0]=aNewDataMatrix.get(i,1);
		Matrix anXMatrix = new Matrix(anXValues);
		Matrix aYMatrix = new Matrix(aYValues);

		//compute regression
		Matrix aBetaHat = (anXMatrix.transpose().times(anXMatrix)).inverse().times(anXMatrix.transpose()).times(aYMatrix);
		Matrix aHatMatrix = anXMatrix.times((anXMatrix.transpose().times(anXMatrix)).inverse()).times(anXMatrix.transpose());
		Matrix aResidualMatrix = (Matrix.identity(aSampleSize,aSampleSize).minus(aHatMatrix)).times(aYMatrix);

		//compute Cook's distance
		double aP = aBetaHat.getRowDimension();
		double anSSquared = (aResidualMatrix.transpose().times(aResidualMatrix)).get(0,0)/((double) itsSampleSize-aP);
		double[][] aParentValues = {{itsIntercept},{itsSlope}};
		Matrix aParentBetaHat = new Matrix(aParentValues);

		double aQuality = aBetaHat.minus(aParentBetaHat).transpose().times(anXMatrix.transpose()).times(anXMatrix).times(aBetaHat.minus(aParentBetaHat)).get(0,0)/(aP*anSSquared);
		//N.B.: Temporary line for fetching Cook's experimental statistics
		Log.logRefinement(""+aQuality+","+anSVPDistance+","+aSampleSize);
		return aQuality;
	}

	private double computeBoundSeven(int theSampleSize, double theT, double theRSquared)
	{
		if (theT>=1)
			return Double.MIN_VALUE;
		return theT/((1-theT)*(1-theT))*theRSquared/(itsP*itsSSquared);
	}

	private double computeBoundSix(int theSampleSize, double theT, double theSquaredResidualSum)
	{
		if (theT>=1)
			return Double.MIN_VALUE;
		return theT/((1-theT)*(1-theT))*theSquaredResidualSum/(itsP*itsSSquared);
	}

	private double computeBoundFive(int theSampleSize, double theRemovedTrace, double theRSquared)
	{
		if (theRemovedTrace>=1)
			return Double.MIN_VALUE;
		return theRemovedTrace/((1-theRemovedTrace)*(1-theRemovedTrace))*theRSquared/(itsP*itsSSquared);
	}

	private double computeBoundFour(int theSampleSize, double theRemovedTrace, double theSquaredResidualSum)
	{
		if (theRemovedTrace>=1)
			return Double.MIN_VALUE;
		return theRemovedTrace/((1-theRemovedTrace)*(1-theRemovedTrace))*theSquaredResidualSum/(itsP*itsSSquared);
	}

	private double computeSVPDistance(int theNrRemoved, int[] theIndices)
	{
		double result = 0.0;
		for (int i=0; i<theNrRemoved; i++)
			result += itsSVP[theIndices[i]];
		return result;
	}

	private double squareSum(Matrix itsMatrix)
	{
		int aSampleSize = itsMatrix.getRowDimension();
		double[] itsValues = itsMatrix.getRowPackedCopy();
		double aSum = 0.0;
		for (int i=0; i<aSampleSize; i++)
			aSum += itsValues[i]*itsValues[i];
		return aSum;
	}

	/**
	 * Updates the slope and intercept of the regression function.
	 * Function used to determine slope:
	 * b = SUM( (x_n - x_mean)*(y_n - y_mean) ) / SUM( (x_n - x_mean)*(x_n - x_mean) )
	 * this can be rewritten to
	 * b = ( SUM(x_n*y_n) - x_mean*y_sum - y_mean*x_sum + n*x_mean*y_mean ) / ( SUM(x_n*x_n) - 2*x_mean*x_sum + n*x_mean*x_mean )
	 *
	 */
	private void updateRegressionFunction()
	{
		double aXMean = itsXSum / itsSampleSize;
		double aYMean = itsYSum / itsSampleSize;
		itsSlope = getSlope(itsXSum, itsYSum, itsXSquaredSum, itsXYSum, itsSampleSize);
		itsIntercept = aYMean - itsSlope*aXMean;
	}

	private double getSlope(double theXSum, double theYSum, double theXSquaredSum, double theXYSum, double theSampleSize)
	{
		double aXMean = theXSum / theSampleSize;
		double aYMean = theYSum / theSampleSize;
		double aNumerator = theXYSum - aXMean*theYSum - aYMean*theXSum + theSampleSize*aXMean*aYMean;
		double aDenominator = theXSquaredSum - 2*aXMean*theXSum + theXSum*aXMean;
		return aNumerator/aDenominator;
	}
	
	private double getIntercept(double aXMean, double aYMean, double aSlope) {
		return aYMean - aSlope*aXMean;
	}

	/**
	 * Add a new datapoint to this measure, where the Y-value is the target variable.
	 * Always call update() after all datapoints have been added.
	 * @param theY the Y-value, the target
	 * @param theX the X-value
	 */
	// never used
	@Deprecated
	private void addObservation(float theY, float theX)
	{
		//adjust the sums
		itsSampleSize++;
		itsXSum += theX;
		itsYSum += theY;
		itsXYSum += theX*theY;
		itsXSquaredSum += theX*theX;
		itsYSquaredSum += theY*theY;

		//Add to its own lists
		Point2D.Float aDataPoint = new Point2D.Float(theX,theY);
		itsData.add(aDataPoint);
	}

	private void addObservation(Point2D.Float theObservation)
	{
		float anX = (float) theObservation.getX();
		float aY = (float) theObservation.getY();

		//adjust the sums
		itsSampleSize++;
		itsXSum += anX;
		itsYSum += aY;
		itsXYSum += anX*aY;
		itsXSquaredSum += anX*anX;
		itsYSquaredSum += aY*aY;

		//Add to its own lists
		itsData.add(theObservation);
	}

	private Point2D.Float getObservation(int theIndex)
	{
		return itsData.get(theIndex);
	}

	/**
	 * calculates the error terms for the distribution and recomputes the
	 * sum of the squared error term
	 *
	 */
	private void updateErrorTerms()
	{
		itsErrorTermSquaredSum = 0;
		for(int i=0; i<itsSampleSize; i++)
		{
			double anErrorTerm = getErrorTerm(itsData.get(i));
			itsErrorTermSquaredSum += anErrorTerm*anErrorTerm;
		}

		//update the error terms of the complement of this measure, if present
		/*
		if(itsBase!=null)
		{
			itsComplementErrorTermSquaredSum=0;
			for(int i=0; i<(itsBase.getSampleSize()-itsSampleSize); i++)
			{
				if(itsComplementData.size()!=itsBase.getSampleSize()-itsSampleSize) {
					System.err.println("incorrect computation of complement!");
				}
				double anErrorTerm = getComplementErrorTerm(itsComplementData.get(i));
				Log.logCommandLine("                   i: " + i);
				Log.logCommandLine("                   itsComplementData: " + itsComplementData.get(i));
				Log.logCommandLine("                   anErrorTerm: " + anErrorTerm);
				itsComplementErrorTermSquaredSum += anErrorTerm*anErrorTerm;
			}
		}
		*/
	}
	
	/**
	 * @author pudding 20180415
	 */
	private void updateComplementErrorTerms() {
		itsComplementErrorTermSquaredSum=0;
		if(itsBase!=null)
		{	
			for(int i=0; i<(itsBase.getSampleSize()-itsSampleSize); i++)
			{
				if(itsComplementData.size()!=itsBase.getSampleSize()-itsSampleSize) {
					System.err.println("incorrect computation of complement!");
				}
				double anErrorTerm = getComplementErrorTerm(itsComplementData.get(i));
				//Log.logCommandLine("                   i: " + i);
				//Log.logCommandLine("                   itsComplementData: " + itsComplementData.get(i));
				//Log.logCommandLine("                   anErrorTerm: " + anErrorTerm);
				itsComplementErrorTermSquaredSum += anErrorTerm*anErrorTerm;
			}
		}
	}

	/**
	 * Determine the error term for a given point
	 *
	 * @param theX the x-value
	 * @param theY the y-value
	 * @return the error term
	 */
	private double getErrorTerm(double theX, double theY)
	{
		return theY - (itsSlope*theX+itsIntercept);
	}

	private double getErrorTerm(Point2D.Float theDataPoint)
	{
		return getErrorTerm(theDataPoint.getX(), theDataPoint.getY());
	}

	private double getErrorTermVariance(double theErrorTermSquaredSum, double theSampleSize)
	{
		return theErrorTermSquaredSum / (theSampleSize - 2 );
	}
	
	/**
	 * Determine the error term for a given point
	 *
	 * @param theX the x-value
	 * @param theY the y-value
	 * @return the error term
	 */
	private double getComplementErrorTerm(double theX, double theY)
	{
		return theY - (itsComplementSlope*theX+itsComplementIntercept);
	}

	private double getComplementErrorTerm(Point2D.Float theDataPoint)
	{
		return getComplementErrorTerm(theDataPoint.getX(), theDataPoint.getY());
	}

	private int getSampleSize()
	{
		return itsSampleSize;
	}

	private double getXSum()
	{
		return itsXSum;
	}

	private double getYSum()
	{
		return itsYSum;
	}

	private double getXYSum()
	{
		return itsXYSum;
	}

	private double getXSquaredSum()
	{
		return itsXSquaredSum;
	}

	// never used
	@Deprecated
	private double getYSquaredSum()
	{
		return itsYSquaredSum;
	}

	/**
	 * Computes and returns the correlation given the observations contained
	 * by CorrelationMeasure.
	 *
	 * @return the computed correlation
	 */
	// never used
	@Deprecated
	private double getCorrelation()
	{
		itsCorrelation = (itsSampleSize*itsXYSum - itsXSum*itsYSum)/Math.sqrt((itsSampleSize*itsXSquaredSum - itsXSum*itsXSum) * (itsSampleSize*itsYSquaredSum - itsYSum*itsYSum));
		//itsCorrelationIsOutdated = false; //set flag to false, so subsequent calls to getCorrelation don't need anymore computation.
		return itsCorrelation;
	}

	public double getSlope()
	{
		return itsSlope;
	}

	public double getIntercept()
	{
		return itsIntercept;
	}

	public double getBaseFunctionValue(double theX)
	{
		return theX*itsSlope + itsIntercept;
	}

	public int getNrBoundSeven() { return itsBoundSevenCount; }
	public int getNrBoundSix() { return itsBoundSixCount; }
	public int getNrBoundFive() { return itsBoundFiveCount; }
	public int getNrBoundFour() { return itsBoundFourCount; }
	public int getNrRankDef() { return itsRankDefCount; }
	
	public String getPairwiseComparison() {
		String output = "";
		for (String p : itsPairwiseComparison) {
			if (false == output.equals("")) {
				output = output + " ";
			}
			output = output + p;
		}
		return output;
	}
}