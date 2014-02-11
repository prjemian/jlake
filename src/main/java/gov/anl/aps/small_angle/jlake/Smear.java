
package gov.anl.aps.small_angle.jlake;

/**
	 *  Smear the data of Isas(Qsas) into Ismr(Qsas) using the slit-length
     *    weighting function "Plengt" and an extrapolation
     *    of the data to avoid truncation errors.  Assume that
     *    Plengt goes to zero for l > lo (the slit length).
     *  Also assume that the slit length  function is symmetrical
     *    about l = zero.
 * @author Pete Jemian
 *
 */
public class Smear {

	private double  sFinal = 1.0;	// to start evaluating the constants for extrapolation
	private String  extrapName = "flat background";		// model final data as a constant
	private ExtrapolateFunction  extrap;
	private double  fSlope;			// linear coefficient of data fit
	private double  fIntercept;		// constant coefficient of data fit
	private double[] Ismr;			// re-smeared intensity
	private double  slit_length = 1;	// slit length, as defined by Lake

	/**
	 * 
	 */
	public Smear() {
		this( 1.0, "flat background", 1.0 );
	}

	/**
	 * @param qStart
	 * @param extrapForm
	 * @param slitLength
	 */
	public Smear(double qStart, String extrapForm, double slitLength) {
		setSFinal( qStart );
		extrap = new ExtrapolateFunction();
		setExtrapName( extrapForm );
		setSlit_length( slitLength );
	}

	/**
	 * 
     *  This routine is written so that if "Plengt" is changed
     *    (for example) to a Gaussian, that no further modification
     *    is necessary to the integration procedure.  That is,
     *    this routine will integrate the data out to "lo".
     *    
	 * @param Qsas
	 * @param Isas
	 * @param Idev
	 * @return vector containing smeared intensities
	 */
	public double[] smear(double[] Qsas, double[] Isas, double[] Idev) {
		extrap.select(extrapName);
		// this was Prep() but not needed now
		// Calculate the constants for an extrapolation fit from all the data that
		// satisfy x(i) >= sFinal
		extrap.fitData(Qsas, Isas, Idev, sFinal);
		int NumPts = Qsas.length;
		double qLo = Qsas[0];
		double ratio = slit_length / (Qsas[NumPts - 1] - qLo);
		double[] x = new double[NumPts];	// slit-length direction
		double[] w = new double[NumPts];	// probability weighting function
		for (int i = 0; i < Qsas.length; i++) {
			x[i] = ratio * (Qsas[i] - qLo);	// take x step-size from Q steps
			w[i] = Plengt(x[i]);
		}
		double[] y = new double[NumPts];	// integrand
		Ismr = new double[NumPts];
		for (int i = 0; i < NumPts; i++) {	// evaluate each integral ...
			double qNow = Qsas[i];
			for (int k = 0; k < NumPts; k++)
				y[k] = lookupIsas(qNow, x[k], Qsas, Isas);
			double area = AreaXY.areaXY(x, y);
			Ismr[i] = area / slit_length;	// integral pre-factor
		}
		return Ismr;
	}

	/**
	 * called after extrapolation fit
	 */
	public String reportFit() {
		return extrap.toString();
	}

	/**
     *  Determine the "corrected" intensity at u = SQRT (x*x + y*y).
     *  Note that only positive values of "u" will be searched!
	 * @param q
	 * @param l
	 * @param qSAS
	 * @param iSAS
	 * @return
	 */
	private double lookupIsas(double q, double l, double[] qSAS,
			double[] iSAS) {
		double u = Math.sqrt(q*q + l*l); /* circularly symmetric */
		BSearch search = new BSearch();
		int iTest = search.bsearch(u, qSAS); /* find index */
		int index = search.getIndex();
		double value = 0;
		switch (iTest) {
		case -1:
			System.out.printf("lookupIsas: Bad value of u or array Q\n");
			return (0);

		case 0:
			// find intensity by interpolation within existing data
			if (u == qSAS[index])
				value = iSAS[index]; /* exactly! */
			else {
				/* power law interpolation */
				value = powerInterpolate(u, qSAS[index], iSAS[index], qSAS[index + 1], iSAS[index + 1]);
			}
			break;

		case 1:
			// find intensity from extrapolation model
			value = extrap.valueOf(u);
			break;
		default:
			System.out
					.printf("lookupIsas: unexpected return result from bsearch\n");
			break;
		}
		return value;
	}


	/**
	 * power law interpolation: y(x) given (x1, y1) & (x2, y2)
	 * @param x
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	private double powerInterpolate(double x, double x1, double y1, double x2,
			double y2) {
		double value = interpolate(Math.log(x), 
				Math.log(x1), Math.log(y1), 
				Math.log(x2), Math.log(y2));
		return Math.exp(value);
	}

	/**
	 * Linear interpolation: y(x) given (x1, y1) & (x2, y2)
	 * @param x
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	private double interpolate(double x, double x1, double y1, double x2,
			double y2) {
		return (y1 + (y2 - y1) * (x - x1) / (x2 - x1));
	}

	/**
	 * Plengt is the slit-length weighting function. It is defined for a
	 * rectangular slit of length 2*sLengt and probability 1/(2*sLengt). It is
	 * zero elsewhere. It is not necessary to change the limit of the
	 * integration if the functional form here is changed.
	 * 
	 * @param x
	 * @return (|x| <= slit_length) ? 1/(2*sLengt) : 0
	 */
	private double Plengt(double x) {
		return (Math.abs(x) > slit_length) ? 0.0 : 0.5 / slit_length;
	}

	/* ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ */


	/**
	 * @return the sFinal
	 */
	public double getSFinal() {
		return sFinal;
	}


	/**
	 * @param value the sFinal to set
	 */
	public void setSFinal(double value) {
		sFinal = value;
	}


	/**
	 * @return the extrapName
	 */
	public String getExtrapName() {
		return extrapName;
	}


	/**
	 * @param value the extrapName to set
	 */
	public void setExtrapName(String value) {
		extrapName = value;
		extrap.select(value);
	}


	/**
	 * @return the fSlope
	 */
	public double getFSlope() {
		return fSlope;
	}


	/**
	 * @param value the fSlope to set
	 */
	public void setFSlope(double value) {
		fSlope = value;
	}


	/**
	 * @return the fIntercept
	 */
	public double getFIntercept() {
		return fIntercept;
	}


	/**
	 * @param value the fIntercept to set
	 */
	public void setFIntercept(double value) {
		fIntercept = value;
	}


	/**
	 * @return the ismr
	 */
	public double[] getIsmr() {
		return Ismr;
	}


	/**
	 * @param value the ismr to set
	 */
	public void setIsmr(double[] value) {
		Ismr = value;
	}


	/**
	 * @return the slit_length
	 */
	public double getSlit_length() {
		return slit_length;
	}


	/**
	 * @param value the slit_length to set
	 */
	public void setSlit_length(double value) {
		slit_length = value;
	}


	/* ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ */


	/**
	 * @param args from the command-line
	 */
	public static void main(String[] args) {
		// test data from bimodal-test.xml
		double[] Qsas = { 0.004015714, 0.004540865, 0.005009597, 0.005523349,
				0.005984948, 0.006530898, 0.007069772, 0.007642068,
				0.008159884, 0.008650528, 0.009354105, 0.010037672,
				0.010495212, 0.011477862, 0.011993325, 0.013163305,
				0.014130834, 0.015375826, 0.016275363, 0.01708254, 0.018146453,
				0.01896742, 0.020318232, 0.022038655, 0.023703925, 0.024299407,
				0.025016839, 0.026155734, 0.027319571, 0.028558794,
				0.030457612, 0.031377606, 0.033449266, 0.035090096,
				0.037364624, 0.037523746, 0.040189631, 0.04227563, 0.044563353,
				0.04535573, 0.047021858, 0.049001731, 0.051083747, 0.051397134,
				0.052541237, 0.054909293, 0.056582063, 0.057632517,
				0.059833772, 0.06468869, 0.067026652, 0.072305635, 0.075132929,
				0.078039028, 0.079841085, 0.085349523, 0.087941781,
				0.093215562, 0.093915127, 0.1005312, 0.1078508, 0.1122292,
				0.1207168, 0.1274587, 0.1361404, 0.1395714, 0.1447186,
				0.1544717, 0.1581253, 0.1650496, 0.1717408, 0.1717076,
				0.1732336, 0.1817141, 0.1934383, 0.204874, 0.2085548, 0.216523,
				0.2240186, 0.2419596, 0.2485349, 0.2630818, 0.274137,
				0.2869201, 0.3078441, 0.319146, 0.3486647, 0.349388, 0.3675496,
				0.3753445, 0.3850296 };
		double[] Isas = { 3497.473, 3340.003, 3322.474, 2983.234, 2737.171,
				2598.76, 2233.901, 2080.964, 1882.877, 1549.716, 1458.494,
				1225.962, 1136.555, 888.0701, 799.5388, 647.1413, 529.7873,
				420.6537, 347.7079, 320.9346, 271.4893, 245.919, 206.0616,
				167.7822, 144.4464, 133.7931, 121.9826, 109.0163, 99.10448,
				85.42811, 71.82733, 66.32532, 50.51509, 42.95596, 33.62893,
				33.32085, 23.27343, 17.22542, 13.60951, 11.31936, 9.468609,
				7.68958, 5.605547, 5.563108, 4.893671, 3.973402, 3.41373,
				2.985957, 2.539224, 2.017291, 1.78293, 1.496331, 1.282017,
				1.27591, 1.096026, 0.8326781, 0.7886078, 0.586588, 0.651387,
				0.5250364, 0.3965718, 0.3887897, 0.2906151, 0.2321665,
				0.2235141, 0.1860456, 0.2007678, 0.1832369, 0.1549768,
				0.1425524, 0.1437128, 0.1568291, 0.1520312, 0.1152461,
				0.1154749, 0.1241597, 0.1112878, 0.1336105, 0.1289867,
				0.098495856, 0.1145, 0.1329978, 0.1089815, 0.097936325,
				0.088699348, 0.1104923, 0.1128228, 0.094258696, 0.07961268,
				0.082723245, 0.110684 };
		double[] Idev = { 90.72816, 84.95314, 79.63133, 73.72543, 68.43955,
				62.31092, 56.48292, 50.61856, 45.65575, 41.2854, 35.61156,
				30.77334, 27.89533, 22.62078, 20.3025, 16.00751, 13.28712,
				10.62196, 9.141067, 8.055664, 6.899693, 6.175018, 5.218031,
				4.293956, 3.601686, 3.388158, 3.149926, 2.80829, 2.498103,
				2.203721, 1.81182, 1.644182, 1.3135, 1.093143, 0.8404584,
				0.8248547, 0.5991439, 0.4641992, 0.3505869, 0.3183173,
				0.2605782, 0.2072131, 0.1653795, 0.1601304, 0.1429478,
				0.1157004, 0.1017323, 0.094667964, 0.083097622, 0.067339174,
				0.062345441, 0.053552937, 0.049515471, 0.04563617, 0.043358441,
				0.037035827, 0.034431241, 0.029896911, 0.029371019,
				0.025180873, 0.02189042, 0.020405844, 0.018200388, 0.016876817,
				0.015540876, 0.015103163, 0.014521016, 0.013634013,
				0.013357825, 0.012909681, 0.012551193, 0.01255288, 0.012478063,
				0.012119967, 0.011744127, 0.011446702, 0.011385638,
				0.011229753, 0.011106365, 0.010926177, 0.010834897,
				0.010744881, 0.010655032, 0.010593238, 0.010534982,
				0.010501204, 0.010450368, 0.010450677, 0.010421322,
				0.010433678, 0.010393647 };
		Smear smr = new Smear(0.2, "flat background", 0.2);
		double[] smrI = smr.smear(Qsas, Isas, Idev);
		for (int i = 0; i < Qsas.length; i++)
			System.out.printf("%g\t%g\t%g\n", Qsas[i], Isas[i], smrI[i]);
		System.out.println("the end.");
	}

}
