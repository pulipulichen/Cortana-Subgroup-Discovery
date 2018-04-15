package nl.liacs.subdisc;

import smile.math.special.Beta;

/**
 * Utils with Smile library
 * https://haifengl.github.io/smile/
 * https://github.com/haifengl/smile
 * 
 * @author pudding 20180415
 * 

        double t = 2.302;
        double df = 17;
        double p = SmileUtils.calcTTestPValue(t, df);
        System.out.println(p);
        		
 */
public class SmileUtils {
	public static double calcTTestPValue(double t, double df) {
		return Beta.regularizedIncompleteBetaFunction(0.5 * df, 0.5, df / (df + t * t));
	}
}
