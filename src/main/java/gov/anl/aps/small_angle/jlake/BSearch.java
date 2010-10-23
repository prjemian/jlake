
//########### SVN repository information ###################
//# $Date$
//# $Author$
//# $Revision$
//# $URL$
//# $Id$
//########### SVN repository information ###################

package gov.anl.aps.small_angle.jlake;

public class BSearch {
	// Search the array "x" for x(iLo) <= z < x(iHi)
	// On exit, iLo and iHi will exactly bracket the datum
	//    and iTest will be the same as iLo.
	private int iLo;
	private int iHi;

    // one constructor
	public BSearch() {
    	iLo = -1;
    	iHi = -1;
    }

	public int getIndex() {
		return(iLo);
	}

	/**
	 * @return the iLo
	 */
	public int getiLo() {
		return iLo;
	}

	/**
	 * @return the iHi
	 */
	public int getiHi() {
		return iHi;
	}

	/**
	 * Search the array "x" for x(iLo) <= z < x(iHi).
	 * <p>
	 * On exit, iLo and iHi will exactly bracket the datum. 
	 * <p>
	 * The return result can be tested. 
	 *
	 * @param  z    datum to search
	 * @param  x    vector to be searched
	 * @return      -1: z < x[0] <br /> +1: z > x[n-1] <br /> 0: otherwise
	 */
	public int bsearch(double z, double[] x) 
	{
		int iTest = -1;            // assume that z < x[0] and test
		int NumPts = x.length;
		if (z < x[0]) return(-1);
		iTest = NumPts;            // assume z > x[n-1] and test
		if (z > x[NumPts-1]) return(1);
		if (iLo < 0 || iHi >= NumPts || iLo >= iHi) {
			iLo = 0;
			iHi = NumPts-1;
		}
		while (z < x[iLo])
			iLo /= 2;
		while (z > x[iHi])         // expand up?
			iHi = (iHi + 1 + NumPts) >>> 1 ;
		iTest = iHi;
		while (iHi - iLo > 1) {
			iTest = (iLo + iHi) >>> 1 ;
			if (z >= x[iTest])
				iLo = iTest;
			else
				iHi = iTest;
		}
		return(0);

	}

	private static void mainReport(double target, double[] x)
	{
		BSearch search = new BSearch();
		int result = search.bsearch(target, x);
		int index = search.getIndex();
		System.out.println(" <areaXY>");
		System.out.println("  <target>" + target + "</target>");
		System.out.println("  <result>" + result + "</result>");
		System.out.println("  <index>"  + index  + "</index>");
		System.out.println("  <iLo>"    + search.getiLo()    + "</iLo>");
		System.out.println("  <iHi>"    + search.getiHi()    + "</iHi>");
		System.out.println(" </areaXY>");
	}

	public static void main(String[] argv)
	{
		int i;
		double[] x = {
				0.047166400, 0.13470064,  0.154969422, 0.413488636, 
				0.567151845, 0.687239362, 0.740807422, 0.975060558};
		System.out.println("<?xml version=\"1.0\"?>");
		System.out.println("<bsearch_unitTest>");
		System.out.println(" <xData>");
		for (i=0; i < x.length; i++) {
			System.out.println("   " + x[i]);
		}
		System.out.println(" </xData>");
		mainReport(0.02,  x);
		mainReport(0.12,  x);
		mainReport(0.2,  x);
		mainReport(0.5,  x);
		mainReport(0.9,  x);
		mainReport(0.99,  x);
		System.out.println("</bsearch_unitTest>");
	}

}
