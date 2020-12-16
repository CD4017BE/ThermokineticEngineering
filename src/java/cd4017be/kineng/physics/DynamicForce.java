package cd4017be.kineng.physics;

import cd4017be.math.cplx.CplxD;

/**Abstract implementation of {@link IForce} that continuously applies a force of
 * {@link #F} + {@link #Fdv} * {@link ShaftStructure#v} on a connected {@link ShaftStructure}.
 * The power added to the shaft is the applied force times the shafts velocity.
 * @author CD4017BE */
public abstract class DynamicForce implements IForce {

	/** [N] current static force component */
	public double F;
	/** [N/m*s] current dynamic force component (force per velocity) */
	public double Fdv;
	/** [m] gear translation radius */
	public double r;
	/** the connection this is linked to */
	public ForceCon con;

	/** update physics on this device. called from the shaft every tick. 
	 * @param dE [J] amount of energy added to the shaft
	 * @param ds [m] effective distance moved
	 * @param v [m/s] new shaft velocity */
	public void work(double dE, double ds, double v) {}

	@Override
	public void move(double dan, double av_, double av1) {
		double ds = dan * r;
		work((av_ * r * Fdv + F) * ds, ds, av1 * r);
	}

	@Override
	public ForceCon getM(CplxD M, double av) {
		M.set(F * r, Fdv * r * r);
		return con;
	}

	@Override
	public void onStructureChanged() {
		r = con.translation();
	}

	@Override
	public void connect(ForceCon con) {
		this.con = con;
	}

	@Override
	public String toString() {
		return String.format("%.4g N %+.4g N/m*s", F, Fdv);
	}

}
