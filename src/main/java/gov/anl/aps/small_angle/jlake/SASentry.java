
package gov.anl.aps.small_angle.jlake;
/**
 * @author Pete Jemian
 *
 */
public class SASentry {

	private double[] Qsas;
	private double[] Isas;
	private double[] Idev;
	private String title;
	private String dataName;
	private String sampleName;
	private String instrumentName;
	private String detectorName;
	private double slitLength;

	/**
	 * 
	 */
	public void clearEntry() {
		title = "";
		dataName = "";
		sampleName = "";
		instrumentName = "";
		detectorName = "";
		slitLength = 1.0;
	}

	/**
	 * 
	 */
	public SASentry() {
		clearEntry();
	}

	/**
	 * @return the qsas
	 */
	public double[] getQsas() {
		return Qsas;
	}

	/**
	 * @param i index
	 * @return Qsas[i]
	 */
	public double getQsas(int i) {
		if (Qsas == null) return Double.NaN;
		if (i < 0) return Double.NaN;
		if (i >= Qsas.length) return Double.NaN;
		return Qsas[i];
	}

	/**
	 * @param qsas the qsas to set
	 */
	public void setQsas(double[] qsas) {
		Qsas = qsas;
	}

	/**
	 * @return Isas
	 */
	public double[] getIsas() {
		return Isas;
	}

	/**
	 * @param i index
	 * @return Isas[i]
	 */
	public double getIsas(int i) {
		if (Isas == null) return Double.NaN;
		if (i < 0) return Double.NaN;
		if (i >= Isas.length) return Double.NaN;
		return Isas[i];
	}

	/**
	 * @param isas the isas to set
	 */
	public void setIsas(double[] isas) {
		Isas = isas;
	}

	/**
	 * @return Idev
	 */
	public double[] getIdev() {
		return Idev;
	}

	/**
	 * @param i index
	 * @return Idev[i]
	 */
	public double getIdev(int i) {
		if (Idev == null) return Double.NaN;
		if (i < 0) return Double.NaN;
		if (i >= Idev.length) return Double.NaN;
		return Idev[i];
	}

	/**
	 * @param idev the idev to set
	 */
	public void setIdev(double[] idev) {
		Idev = idev;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the dataName
	 */
	public String getDataName() {
		return dataName;
	}

	/**
	 * @param dataName the dataName to set
	 */
	public void setDataName(String dataName) {
		this.dataName = dataName;
	}

	/**
	 * @return the sampleName
	 */
	public String getSampleName() {
		return sampleName;
	}

	/**
	 * @param sampleName the sampleName to set
	 */
	public void setSampleName(String sampleName) {
		this.sampleName = sampleName;
	}

	/**
	 * @return the instrumentName
	 */
	public String getInstrumentName() {
		return instrumentName;
	}

	/**
	 * @param instrumentName the instrumentName to set
	 */
	public void setInstrumentName(String instrumentName) {
		this.instrumentName = instrumentName;
	}

	/**
	 * @return the detectorName
	 */
	public String getDetectorName() {
		return detectorName;
	}

	/**
	 * @param detectorName the detectorName to set
	 */
	public void setDetectorName(String detectorName) {
		this.detectorName = detectorName;
	}

	/**
	 * @return the slitLength
	 */
	public double getSlitLength() {
		return slitLength;
	}

	/**
	 * @param slitLength the slitLength to set
	 */
	public void setSlitLength(double slitLength) {
		this.slitLength = slitLength;
	}

	/**
	 * report the number of points in the Qsas array
	 */
	public int getNumPts() {
		if (Qsas == null) return 0;
		return this.Qsas.length;
	}

}
