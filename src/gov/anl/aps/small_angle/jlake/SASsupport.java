
//########### SVN repository information ###################
//# $Date$
//# $Author$
//# $Revision$
//# $URL$
//# $Id$
//########### SVN repository information ###################

package gov.anl.aps.small_angle.jlake;

public class SASsupport {

	/**
	 * amplitude of single particle scattering form factor for spheres
	 * @param Qr  product of Q*r
	 * @return F(Qr) for a sphere
	 */
	public static double sphere(double Qr)
	{
		double F = 3 * (Math.sin(Qr) - Qr*Math.cos(Qr))/(Qr*Qr*Qr);
		return F;
	}

	/**
	 * Chi-Squared goodness of fit statistic
	 * @param calculated
	 * @param measured
	 * @param stdDev
	 * @return Chi-Squared
	 */
	public static double chiSqr(double[] calculated, double[] measured, double[] stdDev) {
	    double sum = 0;
		for (int j = 0; j < calculated.length; j++) {
	    	sum += Math.pow((calculated[j] - measured[j])/stdDev[j], 2);
	    }
		return sum;
	}

	public static void main(String[] argv)
	{
		double Qr = 0.01;
		double F;
		// test the various form factors defined here
		System.out.println("<?xml version=\"1.0\"?>");
		System.out.println("<sphereFormFactor_unitTest>");
		System.out.println("  <column_labels>Qr\tF\tF*F</column_labels>");
		System.out.println("  <data>");
		while (Qr < 8)
		{
			F = SASsupport.sphere(Qr);
			System.out.println("     " + Qr + "\t" + F + "\t" + F*F);
			Qr += 0.1;
			Qr *= 1.021;
		}
		System.out.println("  </data>");
		System.out.println("</sphereFormFactor_unitTest>");
	}

}
