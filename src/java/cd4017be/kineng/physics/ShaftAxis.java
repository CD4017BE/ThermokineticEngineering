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
	/** [1/r] speed limit of weakest shaft */
	private double av_weak;
	/** 0: valid, 1:update parts, 2:rescan */
	byte state;

	public ShaftAxis(boolean client) {
		this.struct = new ShaftStructure(client);
		this.renderInfo = client ? new ShaftRenderInfo() : null;
		struct.add(this);
		x = 1.0;
	}

	public double av() {
		return struct.av * x;
	}

	public double ang() {
		return struct.ang * x;
	}

	/** @param L [N*m*s] impulse to add to the shaft */
	public void pulse(double L) {
		struct.av += L * x / struct.J;
	}

	/** must be called after modifying {@link #parts} */
	public void refreshParts() {
		cons.clear();
		M_weak = av_weak = Double.POSITIVE_INFINITY;
		double J1 = 0, L = 0;
		for (IShaftPart part : parts) {
			double J;
			L += part.setShaft(this) * (J = part.J());
			J1 += J;
			M_weak = Math.min(M_weak, part.maxTorque());
			av_weak = Math.min(av_weak, part.maxSpeed());
		}
		L *= Math.signum(x);
		double ax = Math.abs(x);
		M_weak *= ax;
		struct.J -= J * ax;
		struct.av = (struct.av * struct.J + L) / (struct.J += J1 * ax);
		J = J1;
		if(renderInfo != null) renderInfo.invalidate();
		struct.flowMat = null;
		if (parts.isEmpty()) struct.markDirty(INV_STRUC);
		else struct.register();
		state = 0;
	}

	public void markInvalid(boolean rescan) {
		state |= rescan ? 2 : 1;
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
		boolean wasSplit = false;
		ShaftAxis shaft = null;
		for (int j = i; j < l; j++) {
			IShaftPart part = parts.get(j);
			if (part.getShaft() == this) {
				if (shaft == null) {
					shaft = new ShaftAxis(struct.client());
					wasSplit = true;
				}
				shaft.parts.add(part);
			} else if (shaft != null) {
				shaft.refreshParts();
				shaft = null;
			}
		}
		if (shaft != null) shaft.refreshParts();
		//only keep first valid section
		if (i < l) parts.subList(i, l).clear();
		if (wasSplit) {
			//evacuate this axis entirely
			shaft = new ShaftAxis(struct.client());
			shaft.parts.addAll(parts);
			shaft.refreshParts();
			parts.clear();
		}
		refreshParts();
	}

	@Override
	public void setIdx(int idx) {
		if (idx == getIdx()) return;
		super.setIdx(idx);
		struct.flowMat = null;
	}

	public void onSpeedSync(double av, double ang) {
		if (!struct.client()) throw new IllegalStateException("Shaft.onSpeedSync() called server side!");
		struct.av = Double.isNaN(av) ? 0 : av / x;
		struct.ang = Double.isNaN(ang) ? 0 : ang / x;
	}

	/**@param aacc [r/s²] shaft acceleration */
	public void checkOverload(double aacc) {
		// fast upper bound check
		double M = 0, av = Math.abs(struct.av * x);
		for (Connection con : cons)
			M += Math.abs(con.M);
		if (M < M_weak && av < av_weak) return;
		// overload possible: do exact check
		aacc *= x;
		M = 0;
		int j = 0;
		IShaftPart next = cons.get(j).host;
		for (IShaftPart part : parts) {
			double dM = part.J() * aacc * 0.5, maxM = part.maxTorque();
			M -= dM;
			if (M > maxM || M < -maxM || av > part.maxSpeed())
				Ticking.overloads.add(part);
			if (part == next) {
				do {
					M += cons.get(j).M;
					next = ++j < cons.size() ? cons.get(j).host : null;
				} while(next == part);
				if (M > maxM || M < -maxM)
					Ticking.overloads.add(part);
			}
			M -= dM;
		}
		//TODO the last axis sometimes has torque left over for unknown reasons!
		/*if (Math.getExponent(ΣM) > 0)
			Main.LOG.warn("left over torque in overload check: {} Nm, J = {} should be {}, acceleration: {} Nm", ΣM, this.J, J, J * α);
		*/
	}

}
