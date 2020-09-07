package cd4017be.kineng.physics;

import java.util.ArrayList;

/** @author CD4017BE */
public class ShaftAxis {

	public ShaftStructure struct;
	public final ArrayList<IShaftPart> parts = new ArrayList<>();
	public final ArrayList<Connection> cons = new ArrayList<>();
	public final ShaftRenderInfo renderInfo;
	/** [kg*m²] moment of inertia */
	public double J;
	/** gear translation factor: how fast this part rotates compared to the core part */
	public double x;
	boolean invalid;

	public ShaftAxis(boolean client) {
		this.struct = new ShaftStructure(client);
		this.renderInfo = client ? new ShaftRenderInfo() : null;
		struct.axes.add(this);
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
		double J1 = 0, L = 0;
		for (IShaftPart part : parts) {
			double J = part.J();
			J1 += J;
			L += J * part.setShaft(this);
		}
		double ax = Math.abs(x);
		struct.ω = (struct.ω * (struct.J -= J * ax) + L * x) / (struct.J += J1 * ax);
		J = J1;
		struct.flow = null;
		if(renderInfo != null) renderInfo.invalidate();
		if (parts.isEmpty()) struct.invalidStruc = true;
		else struct.register();
		invalid = false;
	}

	public void markInvalid() {
		invalid = true;
		struct.invalidAxes = true;
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

	public void onSpeedSync(double ω, double φ) {
		if (!struct.client()) throw new IllegalStateException("Shaft.onSpeedSync() called server side!");
		struct.ω = ω / x;
		struct.φ = φ / x;
	}

}
