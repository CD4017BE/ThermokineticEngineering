package cd4017be.kineng.physics;

/** @author CD4017BE */
public abstract class Connection {

	public final IShaftPart host;
	public ShaftAxis axis;
	/** [m] force lever radius */
	public final double r;
	public double maxF;
	/** [Nm] last torque on axis (scaled by translation) */
	public double M;

	protected Connection(IShaftPart host, double r) {
		this.host = host;
		this.r = r;
	}

	/** @return [m] translation radius */
	public double translation() {
		return axis.x * r;
	}

	/** @return [Nm] torque limit */
	public double maxTorque() {
		return maxF * Math.abs(translation());
	}

	public void setShaft(ShaftAxis shaft) {
		this.axis = shaft;
		if (shaft != null) shaft.cons.add(this);
		M = 0;
	}

}
