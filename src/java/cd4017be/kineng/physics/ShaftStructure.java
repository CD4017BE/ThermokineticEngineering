package cd4017be.kineng.physics;

import static cd4017be.kineng.physics.Ticking.Δt;
import java.util.ArrayList;
import java.util.Random;
import cd4017be.kineng.Main;

/** @author CD4017BE */
public class ShaftStructure {

	public static final Random RAND = new Random();
	public static final double FRICTION_V0 = 0.2, SYNC_THRESHOLD = 0.1, LOOP = 720 * Math.PI;
	
	final ArrayList<ShaftAxis> axes;
	final ArrayList<DynamicForce> forces;
	/** [r] angle */
	public double φ;
	/** [r/s] angular velocity */
	public double ω, old_ω;
	/** [kg*m²] moment of inertia */
	public double J;
	boolean invalidStruc, invalidAxes, registered;
	ForceFlow[] flow;
	double[] stack;

	public ShaftStructure(boolean client) {
		this.axes = new ArrayList<>();
		this.forces = client ? null : new ArrayList<>();
	}

	public void tick() {
		// maintenance stuff
		if(invalidAxes) rescanAxes();
		if(invalidStruc) {
			rescan(this);
			return;
		}
		if(Double.isNaN(J) || J == 0 || axes.isEmpty()) return;
		if(forces == null) {
			// client update
			φ += ω * Δt;
			if(φ > LOOP || φ < -LOOP) φ %= LOOP;
			return;
		}
		if(flow == null) ForceFlow.COMPILER.compile(this);
		// sum up kinetic forces
		double M = 0, Mdω = 0;
		for(DynamicForce f : forces) {
			M += f.F * f.r;
			Mdω += f.Fdv * f.r * f.r;
		}
		// compute force flow though the shaft system to handle part overload and gear
		// friction
		stack[0] = 0;
		stack[1] = ω;
		stack[2] = (M + Mdω * ω) / J;
		for(ForceFlow ff : flow)
			ff.process(stack);
		Mdω += stack[0] / (Math.abs(ω) + FRICTION_V0);
		// compute new velocity after applying force, average velocity and average force
		double ω0 = ω, ωμ;
		if(Mdω == 0) {
			// apply static force (needs special case to avoid NaN)
			ω += M / J * Δt;
			ωμ = (ω0 + ω) * .5;
		} else {
			// apply dynamic force
			ω += Math.expm1(Mdω / J * Δt) * (M / Mdω + ω);
			ωμ = (ω0 + ω) * .5;
			M += ωμ * Mdω;
		}
		// computing actual distance moved and integrating forces would be very complicated
		// so instead just approximate using average force and maintaining energy conservation.
		double Δωsq = ω * ω - ω0 * ω0, Δφ;
		if(M == 0 || Math.getExponent(ωμ) * 2 - Math.getExponent(Δωsq) > 32)
			Δφ = ωμ * Δt; // special case for tiny velocity changes to avoid precision issues
		else Δφ = Δωsq * .5 * J / M;
		for(DynamicForce f : forces) {
			double Δs = Δφ * f.r;
			f.work((ωμ * f.r * f.Fdv + f.F) * Δs, Δs, ω * f.r);
		}
		φ += Δφ;
		if(φ > LOOP || φ < -LOOP) φ %= LOOP;
		if(Double.isNaN(ω)) ω = 0;
		if(Double.isNaN(φ)) φ = 0;
		if(Math.abs((ω - old_ω) / (ω + old_ω)) > SYNC_THRESHOLD) {
			old_ω = ω;
			ShaftAxis shaft = axes.get(RAND.nextInt(axes.size()));
			shaft.parts.get(ShaftStructure.RAND.nextInt(shaft.parts.size())).syncToClient();
		}
	}

	public static void merge(ShaftStructure A, ShaftStructure B, double xA, double xB) {
		if (A == B) {
			A.J = Double.NaN;
			A.ω = 0;
			return;
		}
		//merge smaller structure into larger one
		int la = A.axes.size(), lb = B.axes.size();
		if (la > lb || la == lb && A.J > B.J)
			A.merge(B, xA / xB);
		else B.merge(A, xB / xA);
	}

	/** @param other ShaftStructure to merge into this one
	 * @param x gear translation factor: how fast the other shaft should rotate
	 *        compared to this one. */
	private void merge(ShaftStructure other, double x) {
		Main.LOG.info("merging {} <- {}", this, other);
		for(ShaftAxis axis : other.axes) {
			axis.struct = this;
			axis.x *= x;
		}
		axes.addAll(other.axes);
		other.axes.clear();
		if(forces != null) {
			for(DynamicForce force : other.forces)
				force.r *= x;
			forces.addAll(other.forces);
			other.forces.clear();
		}
		double L = other.J * other.ω * x + this.J * this.ω;
		this.J += other.J * Math.abs(x);
		ω = L / J;
		flow = null;
		invalidAxes |= other.invalidAxes;
		invalidStruc |= other.invalidStruc;
	}

	@Override
	public String toString() {
		return String.format("ShaftStructure(J=%.4g kg*m², ω=%.4g r/s, φ=%.4g r, %d Axes, F=%s)", J, ω, φ, axes.size(), forces);
	}

	public boolean client() {
		return forces == null;
	}

	private void rescanAxes() {
		// copy array
		for(ShaftAxis axis : axes.toArray(new ShaftAxis[axes.size()]))
			if(axis.invalid)
				axis.rescan();
		invalidAxes = false;
	}

	public void register() {
		if(registered || axes.isEmpty()) return;
		Ticking.of(forces == null).structs.add(this);
		registered = true;
		if(forces == null)
			Main.LOG.info("Structure added on client with {} parts", axes.size());
		else Main.LOG.info("Structure added on server with {} parts and {} forces", axes.size(), forces.size());
	}

	public static void rescan(ShaftStructure struct) {
		Main.LOG.info("rescanning {}", struct);
		boolean client = struct.client();
		ArrayList<ShaftAxis> stack = Ticking.of(client).AXIS_STACK;
		stack.clear(); //in case someone else used this and forgot to clear.
		for(ShaftAxis axis : struct.axes) {
			if(axis.struct != struct || axis.parts.isEmpty()) continue;
			ShaftStructure nstruct = new ShaftStructure(client);
			nstruct.axes.add(axis);
			double J = axis.J;
			double L = axis.J * axis.struct.ω * axis.x;
			axis.struct = nstruct;
			axis.x = 1.0;
			stack.add(axis);
			while(!stack.isEmpty())
				for(Connection con : stack.remove(stack.size() - 1).cons)
					if(con instanceof ForceCon)
						((ForceCon)con).relink();
					else if(con instanceof GearLink) {
						GearLink gear = (GearLink)con, link = gear.other;
						if(link == null) continue;
						double x = gear.translation() / link.r;
						axis = link.axis;
						if(axis.struct != nstruct) {
							nstruct.axes.add(axis);
							J += axis.J * Math.abs(x);
							L += axis.struct.ω * axis.J * axis.x;
							axis.x = x;
							axis.struct.invalidStruc = true;
							axis.struct = nstruct;
							stack.add(axis);
						} else if(Math.getExponent(axis.x / x - 1.0) > -24)
							nstruct.J = Double.NaN;
					}
			nstruct.J = J;
			nstruct.ω = L / J;
			if(Double.isNaN(nstruct.ω)) nstruct.ω = 0;
			nstruct.register();
		}
		struct.invalidStruc = false;
		struct.axes.clear();
		if(struct.forces != null)
			struct.forces.clear();
	}

}
