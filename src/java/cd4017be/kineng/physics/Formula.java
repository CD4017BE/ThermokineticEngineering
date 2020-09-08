package cd4017be.kineng.physics;


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

}
