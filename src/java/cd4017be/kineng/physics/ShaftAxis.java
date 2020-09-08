package cd4017be.kineng.physics;

import static cd4017be.kineng.physics.ShaftStructure.*;
import java.util.ArrayList;
import cd4017be.lib.util.IndexedSet;

/** @author CD4017BE */
public class ShaftAxis extends IndexedSet.Element {

	public ShaftStructure struct;
	public final ArrayList<IShaftPart> parts = new ArrayList<>();
	public final ArrayList<Connection> cons = new ArrayList<>();
	public final ShaftRenderInfo renderInfo;
	/** [kg*m²] moment of inertia */
	public double J;
	/** gear translation factor: how fast this part rotates compared to the core part */
	public double x;
	/** [Nm] torque limit of weakest shaft */
	private double M_weak;
	boolean invalid;

	public ShaftAxis(boolean client) {
		this.struct = new ShaftStructure(client);
		this.renderInfo = client ? new ShaftRenderInfo() : null;
		struct.add(this);
		x = 1.0;
	}

	public double ω() {
		return struct.ω * x;
	}

	public double φ() {
		return struct.φ * x;
	}

	/** @param L [N*m*s] impulse to add to the shaft */
	public void pulse(double L) {
		struct.ω += L * x / struct.J;
	}

	/** must be called after modifying {@link #parts} */
	public void refreshParts() {
		cons.clear();
		M_weak = Double.POSITIVE_INFINITY;
		double J1 = 0, L = 0;
		for (IShaftPart part : parts) {
			double J = part.J();
			J1 += J;
			L += J * part.setShaft(this);
			M_weak = Math.min(M_weak, part.maxTorque());
		}
		double ax = Math.abs(x);
		M_weak *= ax;
		struct.ω = (struct.ω * (struct.J -= J * ax) + L * x) / (struct.J += J1 * ax);
		J = J1;
		if(renderInfo != null) renderInfo.invalidate();
		struct.flowMat = null;
		if (parts.isEmpty()) struct.markDirty(INV_STRUC);
		else struct.register();
		invalid = false;
	}

	public void markInvalid() {
		invalid = true;
		struct.markDirty(INV_AXES);
	}

	void rescan() {
		//remove invalid parts at the beginning
		int i = 0, l = parts.size();
		while (i < l && parts.get(i).getShaft() != this) i++;
		if (i > 0) parts.subList(0, i).clear();
		//find end of first valid section
		i = 1; l = parts.size();
		while (i < l && parts.get(i).getShaft() == this) i++;
		//split off remaining valid sections
		ShaftAxis shaft = null;
		for (int j = i; j < l; j++) {
			IShaftPart part = parts.get(j);
			if (part.getShaft() == this) {
				if (shaft == null) shaft = new ShaftAxis(struct.client());
				shaft.parts.add(part);
			} else if (shaft != null) {
				shaft.refreshParts();
				shaft = null;
			}
		}
		if (shaft != null) shaft.refreshParts();
		//only keep first valid section
		if (i < l) parts.subList(i, l).clear();
		refreshParts();
	}

	@Override
	public void setIdx(int idx) {
		if (idx == getIdx()) return;
		super.setIdx(idx);
		struct.flowMat = null;
	}

	public void onSpeedSync(double ω, double φ) {
		if (!struct.client()) throw new IllegalStateException("Shaft.onSpeedSync() called server side!");
		struct.ω = ω / x;
		struct.φ = φ / x;
	}

	/**@param α [r/s²] shaft acceleration */
	public void checkOverload(double α) {
		// fast upper bound check
		double ΣM = 0;
		for (Connection con : cons)
			ΣM += Math.abs(con.M);
		if (ΣM < M_weak) return;
		// overload possible: do exact check
		α *= x;
		ΣM = 0;
		double J = 0;
		int j = 0;
		IShaftPart next = cons.get(j).host;
		for (IShaftPart part : parts) {
			double J_ = part.J();
			J += J_;
			double M = J_ * α * 0.5, maxM = part.maxTorque();
			ΣM -= M;
			if (ΣM > maxM || ΣM < -maxM)
				Ticking.overloads.add(part);
			if (part == next) {
				do {
					ΣM += cons.get(j).M;
					next = ++j < cons.size() ? cons.get(j).host : null;
				} while(next == part);
				if (ΣM > maxM || ΣM < -maxM)
					Ticking.overloads.add(part);
			}
			ΣM -= M;
		}
		//TODO the last axis sometimes has torque left over for unknown reasons!
		/*if (Math.getExponent(ΣM) > 0)
			Main.LOG.warn("left over torque in overload check: {} Nm, J = {} should be {}, acceleration: {} Nm", ΣM, this.J, J, J * α);
		*/
	}

}
