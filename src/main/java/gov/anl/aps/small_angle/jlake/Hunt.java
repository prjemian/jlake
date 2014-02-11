
package gov.anl.aps.small_angle.jlake;
public class Hunt {

	// returns: -1 if x < xx[0], n if x >= xx[n], 
	// otherwise jlo where x[jlo] <= x   and  jhi-jlo = 1

	private static int jhi = -1;

	public static int hunt(double[] xx, double x, int jloInitial) {
		int jm, inc;
		int ascnd = 0; // assume xx[] are in descending order
		int jlo = jloInitial;
		int n = xx.length;

		if (xx[n - 1] > xx[0])
			ascnd = 1; // found xx[] to be in ascending order
		if (jlo < 0 || jlo >= n) { 
			// jlo & jhi are out of range? reset them
			jlo = -1;
			jhi = n;
		} else { 
			// optimization: last solution may be near this one
			inc = 1;
			if (((ascnd == 1) && (x >= xx[jlo]))
					|| ((ascnd == 0) && (x <= xx[jlo]))) {
				// expand range adjusting jhi towards n
				if (jlo == n)
					return jlo;
				jhi = jlo + 1;
				while (((ascnd == 1) && (x >= xx[jhi]))
						|| ((ascnd == 0) && (x <= xx[jhi]))) {
					jlo = jhi;
					inc += inc;
					jhi = jlo + inc;
					if (jhi > n) {
						jhi = n + 1;
						break;
					}
				}
			} else {
				if (jlo == 0) {
					jlo = -1;
					return jlo;
				}
				// expand range adjusting jlo towards 0
				jhi = jlo--;
				while ((ascnd == 1) && (x < xx[jlo])
						|| ((ascnd == 0) && (x > xx[jlo]))) {
					jhi = jlo;
					inc *= 2;
					if (inc > jhi) {
						jlo = 0;
						break;
					} else
						jlo = jhi - inc;
				}
			}
		}
		while (jhi - jlo != 1) {
			jm = (jhi + jlo) >>> 1 ;
			if ((ascnd == 1) && (x > xx[jm]) || (ascnd == 0) && (x < xx[jm]))
				jlo = jm;
			else
				jhi = jm;
		}
		// x[jlo] <= x   and  jhi-jlo = 1
		return jlo;
	}

	private static void mainReport(double[] xx, double x, int jloInitial, int expected)
	{
		int result;
		result = hunt(xx, x, jloInitial);
		System.out.println(" <test>");
		System.out.print("  <x>");
		System.out.print(x);
		System.out.println("</x>");
		System.out.print("  <expected>");
		System.out.print(expected);
		System.out.println("</expected>");
		System.out.print("  <index>");
		System.out.print(result);
		System.out.println("</index>");
		System.out.println(" </test>");
	}

	public static void main(String[] argv)
	{
		double[] x = {
				0.657004356, 0.772901448, 0.891232378, 1.012048258, 
				1.135401272, 1.261344698, 1.389932937, 1.521221529};
		int i;
		System.out.println("<?xml version=\"1.0\"?>");
		System.out.println("<hunt_unitTest>");
		System.out.println(" <test_data>");
		for (i = 0; i < x.length; i++) {
			System.out.print("    ");
			System.out.println(x[i]);
		}
		System.out.println(" </test_data>");
		mainReport(x, 1.2, -1, 4);
		mainReport(x, 0.2, -1, -1);
		mainReport(x, 2.2, -1, 7);
		System.out.println("</hunt_unitTest>");
	}

}
