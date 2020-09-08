package cd4017be.kineng.physics;

/**
 * Continuously applies a force of {@link #F} + {@link #Fdv} * {@link ShaftStructure#v} on a connected {@link ShaftStructure}.
 * The power added to the shaft is the applied force times the shafts velocity.
 * @author CD4017BE */
public abstract class DynamicForce {

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
	public abstract void work(double dE, double ds, double v);

	@Override
	public String toString() {
		return String.format("%.4g N %+.4g N/m*s", F, Fdv);
	}

}
