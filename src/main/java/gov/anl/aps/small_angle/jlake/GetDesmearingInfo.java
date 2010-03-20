
//########### SVN repository information ###################
//# $Date$
//# $Author$
//# $Revision$
//# $URL$
//# $Id$
//########### SVN repository information ###################

package gov.anl.aps.small_angle.jlake;

import gov.anl.aps.small_angle.jlake.properties.DesmearingType;

import java.io.InputStream;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;


import net.smallangles.cansas1d.IdataType;
import net.smallangles.cansas1d.SASdataType;
import net.smallangles.cansas1d.SASdetectorType;
import net.smallangles.cansas1d.SASentryType;
import net.smallangles.cansas1d.SASinstrumentType;
import net.smallangles.cansas1d.SASrootType;
import net.smallangles.cansas1d.SASentryType.Run;


/**
 * @author Pete Jemian
 * 
 */
public class GetDesmearingInfo {

	/** desmearing gov.anl.aps.small_angle.jlake.properties */
	private static DesmearingType dt;

	/** SAS data (from cansas1d/1.0 XML file) */
	private static SASrootType sasRoot;

	/** input Q */
	private static double[] Qsas;

	/** input I (slit-smeared) */
	private static double[] Isas;

	/** input Idev (slit-smeared) */
	private static double[] Idev;

	/** calculated I slit-smeared */
	private static double[] Ismr;

	/** calculated I desmeared */
	private static double[] Idsm;

	/** calculated Idev desmeared */
	private static double[] IdsmDev;

	/** to correct the collimation broadening 
	 * (the whole point of this software!) 
	 */
	private static double   slit_length;

	/** package context for desmearing info JAXB support */
	private static final String DESMEARING_PROPERTIES_PKG = 
		"gov.anl.aps.small_angle.jlake.properties";

	/** package context for cansas1d Java support */
	private static final String CANSAS_JAXB = 
		"net.smallangles.cansas1d";

	/** directory containing resources files */
	private static final String RES_DIR = "/jlake/";

	/** default value in XSD is not handled by JAXB */
	private int entryIndex = 0;

	/** default value in XSD is not handled by JAXB */
	private int dataIndex = 0;

	/** default value in XSD is not handled by JAXB */
	private int detectorIndex = 0;

	/**
	 * Constructor
	 * @param xmlPropertyFile
	 * @throws Throwable 
	 */
	public GetDesmearingInfo(String xmlPropertyFile) throws Throwable
	{
		try {
			dt = (DesmearingType) loadXML(
					DESMEARING_PROPERTIES_PKG, xmlPropertyFile);
		} catch (JAXBException e) {
			e.printStackTrace();
			System.out.println(
					"ERROR: " +
					"Cannot find or interpret XML property sheet:\t" 
					+ xmlPropertyFile);
			return;
		}
		// get the SAS data file name
		String xmlDataFile = dt.getDataFile().trim();
		// load SAS data into memory
		try {
			sasRoot = (SASrootType) loadXML(
					GetDesmearingInfo.CANSAS_JAXB, xmlDataFile);
		} catch (JAXBException e) {
			e.printStackTrace();
			String msg = 
				"ERROR: " + 
				"Cannot find or interpret SAS XML data file:\t" 
				+ xmlDataFile;
			System.out.println(msg);
			return;
		}

		// properties & data are loaded
		// grab the SAS data to be desmeared
		if (dt.getEntryIndex() != null)
			entryIndex = dt.getEntryIndex()-1;
		if (dt.getDataIndex() != null)
			dataIndex = dt.getDataIndex()-1;
		if (dt.getDetectorIndex() != null)
			detectorIndex = dt.getDetectorIndex()-1;
		SASentryType entry = (SASentryType) sasRoot.getSASentry().get(entryIndex);
		SASdataType sdt = (SASdataType) entry.getSASdata().get(dataIndex);
		if (sdt.getName().trim().compareTo("slit-smeared") != 0) {
			System.out.println("selected SASdata element must start: <SASdata name=\"slit-smeared\">");
			// throw something (an exception) here?
			return;
		}
		int numPoints = sdt.getIdata().size();
		Qsas = new double[numPoints];	// input Q
		Isas = new double[numPoints];	// input I (slit-smeared)
		Idev = new double[numPoints];	// input Idev (slit-smeared)
		int i = 0;
		for (IdataType iDataNode : sdt.getIdata()) {
			confirm_units(iDataNode.getQ().getUnit(), "1/A", "Q");
			confirm_units(iDataNode.getI().getUnit(), "1/cm", "I");
			confirm_units(iDataNode.getIdev().getUnit(), "1/cm", "Idev");
			Qsas[i] = iDataNode.getQ().getValue();
			Isas[i] = iDataNode.getI().getValue();
			Idev[i] = iDataNode.getIdev().getValue();
			i++;
		}
		Ismr = new double[numPoints];		// calculated I slit-smeared
		Idsm = new double[numPoints];		// calculated I desmeared
		IdsmDev = new double[numPoints];	// calculated Idev desmeared
		SASinstrumentType instrument = (SASinstrumentType) entry.getSASinstrument();
		SASdetectorType detector = (SASdetectorType) instrument.getSASdetector().get(detectorIndex);
		confirm_units(detector.getSlitLength().getUnit(), 
				"1/A", "slit_length");
		slit_length = detector.getSlitLength().getValue();
	}

	/**
	 * apply the test for correct units and throw an exception if not as expected
	 * @param unitsVar a String variable holding the defined value for the units
	 * @param expected a String with the expected value for the units
	 * @param varName a String for display in the exception message
	 * @throws Throwable
	 */
	public static void confirm_units(String unitsVar, String expected, String varName) throws Throwable {
		if (!unitsVar.equals(expected))
			throw new Throwable(varName + " units are not " + expected);
	}

	/**
	 * @param (String) pkg Java package containing XML Schema bound to Java data structures
	 * @param (String) xmlFile XML file to be opened
	 * @return (Object) root object of Java data structure from XML file 
	 * @throws JAXBException 
	 */
	@SuppressWarnings("unchecked")
	private  Object loadXML(String pkg, String xmlFile) throws JAXBException {
		// use the $(pkg) schema that is bound to a Java structure
		JAXBContext jc = JAXBContext.newInstance(pkg);
		Unmarshaller unmarshaller = jc.createUnmarshaller();

		// find the XML file as a resource in the JAR
		InputStream in = this.getClass().getResourceAsStream(xmlFile);
		if (in == null) {
			throw new IllegalArgumentException("InputStream cannot be null");
		}

		// open the XML file into a Java data structure
		Object obj = unmarshaller.unmarshal(in);
		obj = ((JAXBElement<Object>) obj).getValue();

		//obj = (Object) ((JAXBElement<Object>) unmarshaller
		//		.unmarshal(in)).getValue();
		return obj;
	}


	/**
	 */
	public void inputReporter()
	{
		System.out.println("dataFile:\t" + dt.getDataFile().trim());
		System.out.printf("dataset selected:\t/SASroot/SASentry[%d]/SASdata[%d]\n", 
				entryIndex+1, dataIndex+1);
		System.out.printf("detector selected:\t/SASroot/SASentry[%d]/SASinstrument/SASdetector[%d]\n", 
				entryIndex+1, detectorIndex+1);
		System.out.println("extrapolation_form:\t" + dt.getExtrapolationForm().trim());
		System.out.println(
				"x_start_extrapolation_evaluation:\t" 
				+ dt.getXStartExtrapolationEvaluation().getValue()
				+ "  "
				+ dt.getXStartExtrapolationEvaluation().getUnit());
		System.out.println("iterations:\t" + dt.getIterations());
		System.out.println("iterative_weight_method:\t" + dt.getIterativeWeightMethod().trim());

		System.out.println("#---------------------------------------");

		int numEntries = sasRoot.getSASentry().size();
		System.out.println("SASentry elements: " + numEntries);
		for( int i = 0; i < numEntries; i++ ) {
			System.out.println("SASentry");
			SASentryType entry = sasRoot.getSASentry().get(i);
			System.out.printf("Title:\t%s\n", entry.getTitle());
			List<SASentryType.Run> runs = entry.getRun();
			System.out.printf("#Runs:\t%d\n", runs.size());
			for( int j = 0; j < runs.size(); j++ ) {
				Run run = (Run) runs.get(j);
				System.out.printf("Run@name:\t%s\n", run.getName());
				System.out.printf("Run:\t%s\n", run.getValue());
			}
			List<SASdataType> datasets = entry.getSASdata();
			System.out.printf("#SASdata:\t%d\n", datasets.size());
			for( int j = 0; j < datasets.size(); j++ ) {
				SASdataType sdt = (SASdataType) datasets.get(j);
				System.out.printf("SASdata@name:\t%s\n", sdt.getName());
				System.out.printf("#points:\t%d\n", sdt.getIdata().size());
			}
			List<SASdetectorType> detectors = entry.getSASinstrument().getSASdetector();
			System.out.printf("#SASdetector:\t%d\n", detectors.size());
			for( int j = 0; j < detectors.size(); j++ ) {
				SASdetectorType det = (SASdetectorType) detectors.get(j);
				System.out.printf("SASdata@name:\t%s\n", det.getName());
				try {
					System.out.printf("SDD:\t%g\t(%s)\n", det.getSDD()
							.getValue(), det.getSDD().getUnit());
				} catch (Exception e) {
					System.out.println("SDD:\tundefined");
				}
				try {
					System.out.printf("slit_length:\t%g\t(%s)\n", det
							.getSlitLength().getValue(), det.getSlitLength()
							.getUnit());
				} catch (Exception e) {
					System.out.println("slit_length:\tundefined");
				}
			}
			System.out.println();
		}
	}

	/**
	 * @return the dt
	 */
	public DesmearingType getDt() {
		return dt;
	}

	/**
	 * @param dt the dt to set
	 */
	public void setDt(DesmearingType dt) {
		GetDesmearingInfo.dt = dt;
	}

	/**
	 * @return the sasRoot
	 */
	public SASrootType getSasRoot() {
		return sasRoot;
	}

	/**
	 * @param sasRoot the sasRoot to set
	 */
	public void setSasRoot(SASrootType sasRoot) {
		GetDesmearingInfo.sasRoot = sasRoot;
	}

	/**
	 * @return the qsas
	 */
	public double[] getQsas() {
		return Qsas;
	}

	/**
	 * @param qsas the qsas to set
	 */
	public void setQsas(double[] qsas) {
		Qsas = qsas;
	}

	/**
	 * @return the isas
	 */
	public double[] getIsas() {
		return Isas;
	}

	/**
	 * @param isas the isas to set
	 */
	public void setIsas(double[] isas) {
		Isas = isas;
	}

	/**
	 * @return the idev
	 */
	public double[] getIdev() {
		return Idev;
	}

	/**
	 * @param idev the idev to set
	 */
	public void setIdev(double[] idev) {
		Idev = idev;
	}

	/**
	 * @return the ismr
	 */
	public double[] getIsmr() {
		return Ismr;
	}

	/**
	 * @param ismr the ismr to set
	 */
	public void setIsmr(double[] ismr) {
		Ismr = ismr;
	}

	/**
	 * @return the idsm
	 */
	public double[] getIdsm() {
		return Idsm;
	}

	/**
	 * @param idsm the idsm to set
	 */
	public void setIdsm(double[] idsm) {
		Idsm = idsm;
	}

	/**
	 * @return the idsmDev
	 */
	public double[] getIdsmDev() {
		return IdsmDev;
	}

	/**
	 * @param idsmDev the idsmDev to set
	 */
	public void setIdsmDev(double[] idsmDev) {
		IdsmDev = idsmDev;
	}

	/**
	 * @return the slit length
	 */
	public double getSlitLength() {
		return slit_length;
	}

	/**
	 * @return the slit_length
	 */
	public static double getSlit_length() {
		return slit_length;
	}

	/**
	 * @return the entryIndex
	 */
	public int getEntryIndex() {
		return entryIndex;
	}

	/**
	 * @return the dataIndex
	 */
	public int getDataIndex() {
		return dataIndex;
	}

	/**
	 * @return the detectorIndex
	 */
	public int getDetectorIndex() {
		return detectorIndex;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// load test desmearing properties and data into memory
		String testFile = GetDesmearingInfo.RES_DIR + "test-desmearing.xml";
		GetDesmearingInfo info;
		try {
			info = new GetDesmearingInfo(testFile);
			if (info == null) {
				System.out.println(
						"Could not open info file: " 
						+ testFile
						);
			} else {
				info.inputReporter();
				System.out.println("the end.");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

}
