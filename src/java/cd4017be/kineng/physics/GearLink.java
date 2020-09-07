package cd4017be.kineng.physics;

public class GearLink extends Connection {

	public GearLink other;
	public double fricS, fricD;

	public GearLink(IShaftPart host, double r) {
		super(host, r);
	}

	public void disconnect() {
		if (other != null) {
			other.other = null;
			if (axis != null) axis.struct.invalidStruc = true;
		}
		other = null;
	}

	public void connect(GearLink other) {
		if (other == this.other) return;
		this.disconnect();
		if (other != null) {
			other.disconnect();
			this.other = other;
			other.other = this;
			if (this.axis != null && other.axis != null)
				ShaftStructure.merge(this.axis.struct, other.axis.struct, this.translation(), other.translation());
		}
	}

	@Override
	public void setShaft(ShaftAxis axis) {
		if (axis == this.axis || other == null) {
			super.setShaft(axis);
			return;
		}
		if (this.axis != null)
			this.axis.struct.invalidStruc = true;
		super.setShaft(axis);
		if (other == null || this.axis == null || other.axis == null) return;
		ShaftStructure.merge(this.axis.struct, other.axis.struct, this.translation(), other.translation());
	}

	public double staticFriction() {
		return fricS * translation() + other.fricS * other.translation();
	}

}