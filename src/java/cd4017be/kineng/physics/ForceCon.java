package cd4017be.kineng.physics;

public class ForceCon extends Connection {

	public IForce force;

	/**
	 * @param host
	 * @param r [m] lever radius
	 */
	public ForceCon(IShaftPart host, double r) {
		super(host, r);
	}

	public void link(IForce link) {
		if (link == this.force) return;
		M = 0;
		if (this.force != null) {
			this.force.connect(null);
			if (axis != null && axis.struct.forces != null)
				axis.struct.forces.remove(this.force);
		}
		this.force = link;
		if (link != null) link.connect(this);
		relink();
	}

	public void relink() {
		if (force == null || axis == null) return;
		if (axis.struct.forces != null)
			axis.struct.forces.add(force);
		force.onStructureChanged();
	}

	@Override
	public void setShaft(ShaftAxis axis) {
		if (axis == this.axis) {
			super.setShaft(axis);
			return;
		}
		if (this.axis != null && force != null && this.axis.struct.forces != null)
			this.axis.struct.forces.remove(force);
		super.setShaft(axis);
		relink();
	}

	public void updateTorque(double M, double[] axesM, int n) {
		this.M = M;
		if (Math.abs(M) > maxTorque())
			Ticking.overloads.add(host);
		int i = axis.getIdx();
		if (i < n) axesM[i] += M; 
	}

}