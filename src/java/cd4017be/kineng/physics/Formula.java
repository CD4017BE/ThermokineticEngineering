package cd4017be.kineng.physics;

import java.util.Arrays;
import java.util.Formatter;
import cd4017be.kineng.Main;

/** 
 * @author CD4017BE */
public class Formula {

	static final double PI_2 = Math.PI * 0.5, PI2_3 = Math.PI * 2.0 / 3.0;

	/**@param r [m] radius
	 * @param h [m] height
	 * @return [m^5] the moment of inertia [kg*m²] per density [kg/m³] for a cylinder */
	public static double J_cylinder(double r, double h) {
		r *= r;
		return r * r * h * PI_2;
	}

	/**@param r [m] 
	 * @return [m³] the maximum torsion torque [Nm] per material strength [N/m²] for a circular cross-section */
	public static double torsionStrength_circle(double r) {
		return r * r * r * PI2_3;
	}

	/**@param a
	 * @param b
	 * @return the exponent base 2 of the relative difference between a and b (usually < 0) */
	public static int relDeltaExp(double a, double b) {
		return Math.getExponent(a - b) - Math.getExponent(a + b);
	}

	/** global matrix for linear algebra (only the server main thread should use this)*/
	public static float[][] MATRIX = new float[4][8];
	public static int m, n;

	/** initialize the {@link #MATRIX} with zeroes and increase size if necessary
	 * @param m number of rows
	 * @param n number of columns
	 */
	public static void initMatrix(int m, int n) {
		int oldM = MATRIX.length, oldN = MATRIX[0].length;
		boolean grow = false;
		if (m > oldM) {
			oldM = Math.max(m, oldM << 1);
			grow = true;
		}
		if (n > oldN) {
			oldN = Math.max(n, oldN << 1);
			grow = true;
		}
		if (grow) MATRIX = new float[m][n];
		else for (int i = 0; i < m; i++)
			Arrays.fill(MATRIX[i], 0, n, 0F);
		Formula.m = m;
		Formula.n = n;
	}

	/** solve the system of linear equations previously set up in {@link #MATRIX},
	 * leaving the solution in columns {@link #m} ... {@link #n} - 1.
	 */
	public static void solveMatrix() {
		if (n < m) throw new IllegalStateException("can't solve with less columns than rows!");
		if (Ticking.DEBUG) Main.LOG.info(printMatrix());
		rows: for(int i = 0; i < m; i++) {
			float[] row = MATRIX[i];
			int j = i;
			//find row with non-zero in diagonal
			while(row[i] == 0)
				if (++j >= m) {
					Main.LOG.fatal("div / 0 @ ij = {} in {}", i, printMatrix());
					continue rows;
				} else row = MATRIX[j];
			if (j != i) {
				//swap rows
				MATRIX[j] = MATRIX[i];
				MATRIX[i] = row;
			}
			//normalize row
			float y = row[i];
			if(y != 1F)
				for (int k = i + 1; k < n; k++)
					row[k] /= y;
			//eliminate rows above
			for (int k = 0; k < i; k++) {
				float[] row2 = MATRIX[k];
				float x = row2[i];
				if (x != 0)
					for (int l = i + 1; l < n; l++)
						row2[l] -= row[l] * x;
			}
			//eliminate rows below
			for (int k = j + 1; k < m; k++) {
				float[] row2 = MATRIX[k];
				float x = row2[i];
				if (x != 0)
					for (int l = i + 1; l < n; l++)
						row2[l] -= row[l] * x;
			}
		}
	}

	public static String printMatrix() {
		StringBuilder sb = new StringBuilder();
		Formatter f = new Formatter(sb);
		f.format("%d*%d Matrix:", m, n);
		for (int i = 0; i < m; i++) {
			f.format("\n%2d:[", i);
			float[] row = MATRIX[i];
			for (int j = 0; j < n; j++)
				f.format("%+9.3g ", row[j]);
			sb.setCharAt(sb.length() - 1, ']');
		}
		f.close();
		return sb.toString();
	}

}
