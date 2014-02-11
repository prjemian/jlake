
package gov.anl.aps.small_angle.jlake;

import gov.anl.aps.small_angle.utils.StatsRegisters;

import java.text.DecimalFormat;

import net.smallangles.cansas1d.FloatUnitType;
import net.smallangles.cansas1d.SASentryType;

/**
 * Main class of jlake.  This one does the desmearing
 */
public class Desmear {

	private double slit_length;
	private String extrapName;
	private int entryIndex;
	private int detectorIndex;
	private double startEvalQ;
	private double[] Qsas; // input Q
	private double[] Isas; // input I (slit-smeared)
	private double[] Idev; // input Idev (slit-smeared)
	private double[] Ismr; // calculated I (slit-smeared)
	private double[] Idsm; // calculated I (desmeared)
	private double[] IdsmDev; // calculated Idev (desmeared)
	private double[] stdResid; // standardized residuals (Ismr - Isas)/Idev
	private int maxIterations;
	private GetDesmearingInfo db;
	private SASentryType entry;
	private double ChiSqr;
	private double ChiSqrStarting;
	private Smear smr;

	/** directory containing resources files */
	private static final String RES_DIR = "/jlake/";

	/**
	 * Constructor
	 * @param xmlPropertyFile
	 * @throws Throwable 
	 */
	public Desmear(String xmlPropertyFile) throws Throwable {
		prepare(xmlPropertyFile);

		/*
		 * To start Lake's method, assume that the 0-th approximation of the
		 * corrected intensity is the measured intensity.
		 */
		for (int i = 0; i < db.getQsas().length; i++) {
			Idsm[i] = Isas[i];
			IdsmDev[i] = Idev[i];
		}
		System.out.flush();
		smr = new Smear(startEvalQ, extrapName, slit_length);
		Ismr = smr.smear(Qsas, Idsm, IdsmDev);
	    ChiSqr = SASsupport.chiSqr(Ismr, Isas, Idev);
	    ChiSqrStarting = ChiSqr;                        // remember the first one
	    stdResid = new double[Qsas.length];
	    String message = reportStart();
        System.out.printf(message);

		for (int iteration = 0; iteration < maxIterations; iteration++) {
			Idsm = methodOfLake(Idsm, Isas, Ismr);		// THIS is Lake's method
	        IdsmDev = FixErr (Qsas, Isas, Idev, Idsm);
	        Ismr = smr.smear(Qsas, Idsm, IdsmDev);
		    for (int j = 0; j < Qsas.length; j++)
	        	stdResid[j] = (Ismr[j] - Isas[j]) / Idev[j];
	        ChiSqr = SASsupport.chiSqr(Ismr, Isas, Idev);
	        message = reportIteration(iteration, ChiSqr);
	        System.out.printf(message);
		}
		System.out.printf(reportDone());
	}

	/**
	 * Report the conditions before desmearing
	 */
	public String reportStart() {
		String text = "first smearing done\n";
		text = text.concat("starting ChiSqr=");
		DecimalFormat fmt = new DecimalFormat("0.#####E0\n");
		text = text.concat(fmt.format(ChiSqrStarting));
		text = text.concat("\n");
		return text;
	}

	/**
	 * Default report after each iteration of desmearing
	 * @param iteration number
	 */
	public String reportIteration(int iteration, double ChiSqr) {
		smr.reportFit();
		Integer iterCount = iteration+1;
		String text = "iteration ";
		text = text.concat(iterCount.toString());
		text = text.concat("  ChiSqr=");
		DecimalFormat fmt = new DecimalFormat("0.#####E0\n");
		text = text.concat(fmt.format(ChiSqr));
		//text = text.concat(Double.toString(ChiSqr));
		return text;
	}

	/**
	 * 
	 */
	public String reportDone() {
		return "desmearing complete\n";
	}

	/**
	 * retrieve desmearing properties from data structure
	 * 
	 * @param xmlPropertyFile
	 * @throws Throwable 
	 */
	private void prepare(String xmlPropertyFile) throws Throwable {
		db = new GetDesmearingInfo(xmlPropertyFile);
		if (db == null) {
			throw new Throwable("Could not open property file: " 
					+ xmlPropertyFile);
		} else {
			db.inputReporter();
			Qsas = db.getQsas(); // input Q
			Isas = db.getIsas(); // input I (slit-smeared)
			Idev = db.getIdev(); // input Idev (slit-smeared)
			Ismr = db.getIsmr(); // calculated I (slit-smeared)
			Idsm = db.getIdsm(); // calculated I (desmeared)
			IdsmDev = db.getIdsmDev(); // calculated Idev (desmeared)
			maxIterations = db.getDt().getIterations();
			extrapName = db.getDt().getExtrapolationForm();
			gov.anl.aps.small_angle.jlake.properties.FloatUnitType 
				xStartNode = db.getDt().getXStartExtrapolationEvaluation();
			GetDesmearingInfo.confirm_units(
					xStartNode.getUnit(), 
							"1/A", 
							"x_start_extrapolation_evaluation"
							);
			startEvalQ = xStartNode.getValue();
			entryIndex = db.getEntryIndex();
			detectorIndex = db.getDetectorIndex();
			entry = db.getSasRoot().getSASentry().get(entryIndex);
			FloatUnitType slt = entry.getSASinstrument().getSASdetector().get(
					detectorIndex).getSlitLength();
			GetDesmearingInfo.confirm_units(
					slt.getUnit(), "1/A", "slit_length");
			slit_length = slt.getValue();
		}
	}

	/**
	 * This is the method outlined by Lake <br />
	 * next Idsm = Idsm * ( Isas - Ismr )
	 * @param Idsm
	 * @param Isas
	 * @param Ismr
	 * @return
	 */
	private double[] methodOfLake(double[] Idsm, double[] Isas, double[] Ismr) {
		double[] newIdsm = new double[Isas.length];
		for (int j = 0; j < Qsas.length; j++)
			newIdsm[j] = Idsm[j] * Isas[j] / Ismr[j]; // THIS is Lake's method
		return newIdsm;
	}

	/**
	 * Estimate the error on Z based on data point scatter and
	 *  previous error values and smooth that estimate.
	 * @param x array of abscissae
	 * @param y array of measured ordinates
	 * @param dy array of estimated uncertainties of measured ordinates
	 * @param z array of computed ordinates
	 * @return dz array of estimated uncertainties of computed ordinates
	 */
	private double[] FixErr (double[] x, double[] y, double[] dy, double[] z)
	/*
	 *  Estimate the error on Z based on data point scatter and
	 *  previous error values and smooth that estimate.
	 */
	{
	    int n = x.length;
	    double[] dz = new double[n];

	    /* Error proportional to input error */
	    for (int i = 0; i < n; i++) 
	        dz[i] = z[i] * dy[i] / y[i];

	    /*
	     *  Error based on scatter of desmeared data points.
	     *    Determine this by fitting a line to the points
	     *    i-1, i, i+1 and take the difference.  Add this to dz.
	     */
		StatsRegisters sr = new StatsRegisters();
	    sr.sumAdd(x[0], z[0]);
	    sr.sumAdd(x[1], z[1]);
	    sr.sumAdd(x[2], z[2]);
	    double intercept = sr.lr_constant();
	    double slope = sr.lr_slope();
	    dz[0] += Math.abs(intercept + slope*x[0] - z[0]);
	    dz[1] += Math.abs(intercept + slope*x[1] - z[1]);
	    for (int i = 2; i < n-1; i++) {
	    	sr.sumClr ();
	    	sr.sumAdd(x[i-1], z[i-1]);
	    	sr.sumAdd(x[i],   z[i]);
	    	sr.sumAdd(x[i+1], z[i+1]);
	    	intercept = sr.lr_constant();
	    	slope = sr.lr_slope();
	    	double zNew = intercept + slope * x[i];
	    	dz[i] += Math.abs(zNew - z[i]);
	    }
	    dz[n-1] += Math.abs(intercept + slope*x[n-1] - z[n-1]);

	    /*
	     *  Smooth the error by a 3-point moving average filter.
	     *    Do this 5 times.  Don't smooth the end points.
	     *    Weight the data points by distance^2 (as a penalty)
	     *    using the function weight(u,v)=(1 - |1 - u/v|)**2
	     *    By its definition, weight(x0,x0) == 1.0.  I speed
	     *    computation using this definition.  Why I can't use
	     *    this definition of weight as a statement function
	     *    with some compilers is beyond me!
	     *  Smoothing is necessary to increase the error estimate
	     *    for some grossly under-estimated errors.
	     */
	    double w1;
	    double w2;
	    for (int j = 0; j < 5; j++) {
	        for (int i = 1; i < n-1; i++) {
	            w1 = Math.pow(1 - Math.abs(1 - (x[i-1]/x[i])), 2);
	            w2 = Math.pow(1 - Math.abs(1 - (x[i+1]/x[i])), 2);
	            dz[i] = (w1 * dz[i-1] + dz[i] + w2 * dz[i+1])
	                    / (w1 + 1.0 + w2);
	        }
	    }
	    return dz;
	}


	/**
	 * Example routine (but not the command-line interface)
	 * @param args
	 */
	public static void main(String[] args) {
		Desmear dsm;
		try {
			dsm = new Desmear(Desmear.RES_DIR + "test-desmearing.xml");
			String labels = "Qsas\t"
				+ "Isas\t"
				+ "Idev\t"
				+ "Ismr\t"
				+ "Idsm\t"
				+ "IdsmDev\t"
				+ "stdResid\n";
			System.out.printf(labels);
			

			DecimalFormat Qfmt = new DecimalFormat("0.#########\t");
			DecimalFormat Ifmt = new DecimalFormat("0.#####E00\t");
			DecimalFormat Residfmt = new DecimalFormat("0.####\n");
			
			for (int i = 0; i < dsm.Qsas.length; i++) {
				String text = Qfmt.format(dsm.Qsas[i])
					+ Ifmt.format(dsm.Isas[i])
					+ Ifmt.format(dsm.Idev[i])
					+ Ifmt.format(dsm.Ismr[i])
					+ Ifmt.format(dsm.Idsm[i])
					+ Ifmt.format(dsm.IdsmDev[i])
					+ Residfmt.format(dsm.stdResid[i]);
				System.out.printf(text);
			}
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("the end.");
	}

	/**
	 * @return the slit_length
	 */
	public double getSlit_length() {
		return slit_length;
	}

	/**
	 * @return the extrapName
	 */
	public String getExtrapName() {
		return extrapName;
	}

	/**
	 * @return the entryIndex
	 */
	public int getEntryIndex() {
		return entryIndex;
	}

	/**
	 * @return the detectorIndex
	 */
	public int getDetectorIndex() {
		return detectorIndex;
	}

	/**
	 * @return the startEvalQ
	 */
	public double getStartEvalQ() {
		return startEvalQ;
	}

	/**
	 * @return the qsas
	 */
	public double[] getQsas() {
		return Qsas;
	}

	/**
	 * @return the isas
	 */
	public double[] getIsas() {
		return Isas;
	}

	/**
	 * @return the idev
	 */
	public double[] getIdev() {
		return Idev;
	}

	/**
	 * @return the ismr
	 */
	public double[] getIsmr() {
		return Ismr;
	}

	/**
	 * @return the idsm
	 */
	public double[] getIdsm() {
		return Idsm;
	}

	/**
	 * @return the idsmDev
	 */
	public double[] getIdsmDev() {
		return IdsmDev;
	}

	/**
	 * @return the stdResid
	 */
	public double[] getStdResid() {
		return stdResid;
	}

	/**
	 * @return the maxIterations
	 */
	public int getMaxIterations() {
		return maxIterations;
	}

	/**
	 * @return the db
	 */
	public GetDesmearingInfo getDb() {
		return db;
	}

	/**
	 * @return the entry
	 */
	public SASentryType getEntry() {
		return entry;
	}

	/**
	 * @return the chiSqr
	 */
	public double getChiSqr() {
		return ChiSqr;
	}

	/**
	 * @return the chiSqrStarting
	 */
	public double getChiSqrStarting() {
		return ChiSqrStarting;
	}

	/**
	 * @return the smr
	 */
	public Smear getSmr() {
		return smr;
	}

}
