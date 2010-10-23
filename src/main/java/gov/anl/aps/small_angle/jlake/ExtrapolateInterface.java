
//########### SVN repository information ###################
//# $Date$
//# $Author$
//# $Revision$
//# $URL$
//# $Id$
//########### SVN repository information ###################

package gov.anl.aps.small_angle.jlake;

/**
 * @author Pete Jemian
 * Extrapolation function used to extend SAS data for smearing <br />
 * I(Q) = instance.valueOf(double qNow);
 */
public interface ExtrapolateInterface {

	/**
	 * Fit (Qsas, Isas, Idev) for all Qsas[i] >= qStart
	 * @param Qsas
	 * @param Isas
	 * @param Idev
	 * @param qStart
	 */
	public void fitData(double[] Qsas, double[] Isas, double[] Idev, double qStart);

	/**
	 * extrapolation evaluated by fit 
	 * @param qNow
	 * @return extrapolated value
	 */
	public double valueOf(double qNow);

	/**
	 * @return String representing fitted equation
	 */
	public String toString();

}
