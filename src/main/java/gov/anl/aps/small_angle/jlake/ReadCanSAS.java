
package gov.anl.aps.small_angle.jlake;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import net.smallangles.cansas1d.FloatUnitType;
import net.smallangles.cansas1d.IdataType;
import net.smallangles.cansas1d.SASdataType;
import net.smallangles.cansas1d.SASdetectorType;
import net.smallangles.cansas1d.SASentryType;
import net.smallangles.cansas1d.SASrootType;
import net.smallangles.cansas1d.demo.Reader;

/**
 * Read the canSAS1d/1.0 XML data file.
 * This class uses the cansas1d/1.0 JAXB 2.1 binding to the XML file.
 * @author Pete Jemian
 */
public class ReadCanSAS {

    private String xmlFile;
    private ArrayList<SASentry> entries;

    /** directory containing resources files */
	private static final String RES_DIR = "/jlake/";

    /**
     * Constructor
     * @param theXmlFile
     * @throws JAXBException 
     */
	public ReadCanSAS(String theXmlFile) throws JAXBException {
		xmlFile = theXmlFile;

		// make a new reader
		Reader reader = new Reader();

		// open the XML file and load contents
		// into a Java data structure
		SASrootType sasRoot = reader.loadXML(xmlFile);

		// import all SASentry elements into an ArrayList
		entries = new ArrayList<SASentry>();

		// walk the SASentry nodes
		for (SASentryType sasEntryNode : sasRoot.getSASentry()) {
			SASentry entry = readSasEntryNode(sasEntryNode);
			if (entry != null)
				entries.add(entry);
		}
	}

    /**
     * Creates a new instance of SASentry.
     * Parse the in-memory structure created by the JAXB binding
     * and pull out just the data we need for desmearing
     * @param node of the SASentry from the XML file
     * @return SASentry Object or null if not valid for desmearing
     */
    private SASentry readSasEntryNode(SASentryType node) {
		List <SASdetectorType> detectorList = 
			node.getSASinstrument().getSASdetector();
		// need to check for number of detectors >= 1
		if (detectorList.size() == 0) return null;

		// cannot proceed if no slit_length is defined
		SASdetectorType firstDetector = detectorList.get(0);
		FloatUnitType slitLengthPtr = firstDetector.getSlitLength();
		if (slitLengthPtr == null) return null;

		// start gathering the info
		SASentry entry = new SASentry();
		entry.setTitle(node.getTitle());
		entry.setSlitLength(slitLengthPtr.getValue());
		entry.setSampleName(node.getSASsample().getID());
		entry.setInstrumentName(node.getSASinstrument().getName());
		entry.setDetectorName(firstDetector.getName());

		for (SASdataType sasData : node.getSASdata()) {
			// selects SASdata[@name='slit-smeared']
			if (!sasData.getName().equals("slit-smeared")) continue;
			entry.setDataName(sasData.getName());

            List<IdataType> iDataList = sasData.getIdata();
			int numPts = iDataList.size();
			double[] Qsas = new double[numPts];
            double[] Isas = new double[numPts];
            double[] Idev = new double[numPts];

            // no other way but brute force ...
            // walk the Idata nodes for the Qsas, Isas, Idev data
            int i = 0;
            for (IdataType iDataNode : iDataList) {
            	// check the units
            	if (!iDataNode.getQ().getUnit().equals("1/A")) return null;
            	if (!iDataNode.getI().getUnit().equals("1/cm")) return null;
            	if (!iDataNode.getIdev().getUnit().equals("1/cm")) return null;
            	Qsas[i] = iDataNode.getQ().getValue();
            	Isas[i] = iDataNode.getI().getValue();
            	Idev[i] = iDataNode.getIdev().getValue();
            	i++;
            }
            entry.setQsas(Qsas);
            entry.setIsas(Isas);
            entry.setIdev(Idev);
        }

		return entry;
    }

    /**
     * @param sasEntry
     */
    private static void reportTest(SASentry sasEntry) {
    	System.out.println("=========================");
		System.out.println("title:\t" + sasEntry.getTitle());
		System.out.println("dataName:\t" + sasEntry.getDataName());
		int numPts = sasEntry.getNumPts();
		System.out.println("numPts:\t" + numPts);
		System.out.println("sample:\t" + sasEntry.getSampleName());
		System.out.println("instrument:\t" + sasEntry.getInstrumentName());
		System.out.println("detector:\t" + sasEntry.getDetectorName());
		System.out.println("slit_length:\t" + sasEntry.getSlitLength());
        System.out.println("number of points:\t" + sasEntry.getNumPts());
		//System.out.println("index\tQsas\tIsas\tIdev");
        //double[] Qsas = sasEntry.getQsas();
        //double[] Isas = sasEntry.getIsas();
        //double[] Idev = sasEntry.getIdev();
		//for (int i = 0; i < numPts; i++) {
		//	System.out.printf("%d\t%g\t%g\t%g\n", 
		//			i + 1, Qsas[i], Isas[i], Idev[i]);
		//}
	}


    /**
	 * @return the xmlFile
	 */
	public String getXmlFile() {
		return xmlFile;
	}

	/**
	 * @param xmlFile the xmlFile to set
	 */
	public void setXmlFile(String xmlFile) {
		this.xmlFile = xmlFile;
	}

	/**
	 * @return the entries
	 */
	public ArrayList<SASentry> getEntries() {
		return entries;
	}

	/**
	 * @param entries the entries to set
	 */
	public void setEntries(ArrayList<SASentry> entries) {
		this.entries = entries;
	}

    /**
     * demo of the ReadCanSAS class
     */
    private static void testStandard() {
		ArrayList<ReadCanSAS> db = new ArrayList<ReadCanSAS>();
		String[] testFiles = {
				"bb_af1410.xml",
				"s81-polyurea.xml",
				"s8-latex.xml",
				"1998spheres255.xml",
				"1998spheres460.xml",
				"1998spheres.xml",
				"gc14-dls-i22.xml"
		};
		ReadCanSAS cs;
		for (String xmlFile : testFiles) {
			try {
				cs = new ReadCanSAS(RES_DIR + xmlFile);
				if (cs.entries != null) 
					if (cs.entries.size() > 0) {
						db.add(cs);
						for (SASentry entry : cs.entries)
							reportTest(entry);
					}
			} catch (JAXBException e) {
				Throwable le = e.getLinkedException();
				if (le != null) {
					// FileNotFoundException
					System.out.println(le.getLocalizedMessage());
				} else {
					e.printStackTrace();
				}
			} catch (Throwable e) {
				//e.printStackTrace();
				String msg = "Could not open file: %s\n\n";
				System.out.println("\n========================");
				System.out.printf(msg, RES_DIR + xmlFile);
			}
		}
    }

	/**
	 * @param args
	 *            the command line arguments
	 */
    public static void main(String[] args) {
		testStandard();
	}
    
}
