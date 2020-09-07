package cd4017be.kineng.physics;

public class ForceCon extends Connection {

	public DynamicForce force;

	/**
	 * @param host
	 * @param r [m] lever radius
	 */
	public ForceCon(IShaftPart host, double r) {
		super(host, r);
	}

	public void link(DynamicForce link) {
		if (link == this.force) return;
		if (this.force != null && axis != null && axis.struct.forces != null)
			axis.struct.forces.remove(this.force);
		this.force = link;
		relink();
	}

	public void relink() {
		if (force == null || axis == null) return;
		if (axis.struct.forces != null)
			axis.struct.forces.add(force);
		force.r = translation();
	}

	@Override
	public void setShaft(ShaftAxis axis) {
		if (axis == this.axis) {
			super.setShaft(axis);
			return;
		}
		if (this.axis != null && force != null)
			this.axis.struct.forces.remove(force);
		super.setShaft(axis);
		relink();
	}

}