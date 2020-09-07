package cd4017be.kineng.physics;


/** 
 * @author CD4017BE */
public class Formula {

	/**@param r [m] radius
	 * @param h [m] height
	 * @return [m^5] the moment of inertia per density for a cylinder */
	public static double J_cylinder(double r, double h) {
		r *= r;
		return r * r * h * Math.PI * 0.5;
	}

}
