
package gov.anl.aps.small_angle.jlake;
public class AreaXY {

	/**
	 * @param x array of abcissae
	 * @param y array of ordinates
	 * @return integral of y(x) using trapezoid rule
	 */
	public static double areaXY(double[] x, double[] y) {
		int i;
		double area = 0;
		for (i = 1; i < x.length; i++) {
			area += 0.5 * (y[i] + y[i-1]) * (x[i] - x[i-1]);
		}
		return area;
	}

	/**
	 * report of unit test for AreaXY
	 * @param x array of abcissae
	 * @param y array of ordinates
	 * @param expected
	 */
	private static void mainReport(double[] x, double[] y, double expected)
	{
		double result;
		double difference;
		int i;
		result = areaXY(x, y);
		difference = result - expected;
		System.out.println(" <areaXY>");
		System.out.println("  <expected>"   + expected   + "</expected>");
		System.out.println("  <result>"     + result     + "</result>");
		System.out.println("  <difference>" + difference + "</difference>");
		System.out.println("  <xyData>");
		for (i=0; i < x.length; i++) {
			System.out.println("   " + x[i] + "\t" + y[i]);
		}
		System.out.println("  </xyData>");
		System.out.println(" </areaXY>");
	}

	/**
	 * Unit test for AreaXY
	 * @param argv command-line parameters
	 */
	public static void main(String[] argv)
	{
		double[] x = {
				0.047166400, 0.13470064,  0.154969422, 0.413488636, 
				0.567151845, 0.687239362, 0.740807422, 0.975060558};
		double[] y = {
				0.657004356, 0.772901448, 0.891232378, 1.012048258, 
				1.135401272, 1.261344698, 1.389932937, 1.521221529};
		double[] xx = {1, 2.98, 3, 4, 4.005, 6, 7, 8};
		double[] yy = {0.01, 0.011, .9, 0.986093532338308, 0.002, 0.0044, 0.011, 0.01};

		System.out.println("<?xml version=\"1.0\"?>");
		System.out.println("<areaXY_unitTest>");
		mainReport(x, y, 1.04635221);
		mainReport(xx, yy, 1.000001);
		System.out.println("</areaXY_unitTest>");
	}

}
