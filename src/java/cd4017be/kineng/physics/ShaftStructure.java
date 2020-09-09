package cd4017be.kineng.physics;

import static cd4017be.kineng.physics.Ticking.Δt;
import java.util.*;
import cd4017be.kineng.Main;
import cd4017be.lib.util.IndexedSet;

/** @author CD4017BE */
public class ShaftStructure extends IndexedSet<ShaftAxis> {
	public static final byte INV_STRUC = 1, INV_AXES = 2;
	public static final double FRICTION_V0 = 0.2, SYNC_THRESHOLD = 0.1, LOOP = 720 * Math.PI;
	
	final ArrayList<DynamicForce> forces;
	final ArrayList<GearLink> links;
	/** [r] angle */
	public double φ;
	/** [r/s] angular velocity */
	public double ω, old_ω;
	/** [kg*m²] moment of inertia */
	public double J;
	/** 1: structure invalid, 2: axes invalid, 4: flowMat invalid, 8:needs register */
	private byte state = 8;
	/** stores a row major ({@link #links}.size() + 1) * {@link #size() - 1} matrix
	 * where the first row stores the excess axis torque vector and
	 * the remaining rows, when multiplied with this vector, produce the gear link torques */
	double[] flowMat;
	/** total shaft and gear friction */
	private double M_fr;

	public ShaftStructure(boolean client) {
		super(new ShaftAxis[4]);
		this.forces = client ? null : new ArrayList<>();
		this.links = client ? null : new ArrayList<>();
	}

	public void markDirty(int mode) {
		if (state == 0)
			Ticking.of(forces == null).updateStruct.add(this);
		state |= mode & 7;
	}

	public void update() {
		if((state & INV_AXES) != 0) {
			for(int i = 0; i < count && i < array.length; i++)
				if(array[i].invalid)
					array[i].rescan();
		}
		if((state & INV_STRUC) != 0) {
			rescan(this);
			return;
		}
		state = 0;
	}

	public void tickClient() {
		φ += ω * Δt;
		if(φ > LOOP || φ < -LOOP) φ %= LOOP;
	}

	public void tickServer(boolean checkOverload) {
		if(Double.isNaN(J) || J == 0) return;
		int l = count - 1;
		if (flowMat == null) recalculateFlowMat();
		else Arrays.fill(flowMat, 0, l, 0F);
		// sum up kinetic forces
		double M = 0, Mdω = 0;
		for(DynamicForce f : forces) {
			double M_ = f.F * f.r, Mdω_ = f.Fdv * f.r * f.r;
			M += M_;
			Mdω += Mdω_;
			M_ += Mdω_ * ω;
			int i = f.con.updateTorque(M_);
			if (i < l) flowMat[i] += M_;
		}
		// subtract axis inertia from excess axis torques
		double α = (Mdω * ω + M) / J;
		for (int i = 0; i < l; i++) {
			ShaftAxis axis = array[i];
			flowMat[i] -= α * axis.J * axis.x;
		}
		// compute force flow though the shaft system via matrix vector multiplication
		// to compute friction and part overload
		double M_fr = this.M_fr;
		for (int j = 0, k = l; k < flowMat.length; j++) {
			float M_ = 0;
			for (int i = 0; i < l; i++, k++)
				M_ += flowMat[i] * flowMat[k];
			M_fr += links.get(j).dynamicFriction(M_);
		}
		Mdω -= M_fr / (Math.abs(ω) + FRICTION_V0);
		if (checkOverload)
			for (ShaftAxis axis : this)
				axis.checkOverload(α);
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
			ShaftAxis shaft = array[Ticking.RAND.nextInt(size())];
			shaft.parts.get(Ticking.RAND.nextInt(shaft.parts.size())).syncToClient();
		}
	}

	public static void merge(ShaftStructure A, ShaftStructure B, double xA, double xB) {
		if (A == B) {
			if (Math.getExponent(xA / xB - 1.0) < -24) return;
			A.J = Double.NaN;
			A.ω = 0;
			return;
		}
		//merge smaller structure into larger one
		int la = A.size(), lb = B.size();
		if (la > lb || la == lb && A.J > B.J)
			A.merge(B, xA / xB);
		else B.merge(A, xB / xA);
	}

	/** @param other ShaftStructure to merge into this one
	 * @param x gear translation factor: how fast the other shaft should rotate
	 *        compared to this one. */
	private void merge(ShaftStructure other, double x) {
		Main.LOG.info("merging {} <- {}", this, other);
		for(ShaftAxis axis : other) {
			axis.struct = this;
			axis.x *= x;
		}
		addAll(other);
		if(forces != null) {
			for(DynamicForce force : other.forces)
				force.r *= x;
			forces.addAll(other.forces);
			other.forces.clear();
		}
		double L = other.J * other.ω * x + this.J * this.ω;
		this.J += other.J * Math.abs(x);
		ω = L / J;
		flowMat = null;
		markDirty(other.state);
		other.markDirty(0);
	}

	@Override
	public String toString() {
		return String.format("ShaftStructure(J=%.4g kg*m², ω=%.4g r/s, φ=%.4g r, %d Axes, F=%s)", J, ω, φ, size(), forces);
	}

	public boolean client() {
		return forces == null;
	}

	public void register() {
		if((state & 8) == 0 || isEmpty()) return;
		Ticking t = Ticking.of(forces == null);
		t.structs.add(this);
		if ((state ^= 8) != 0)
			t.updateStruct.add(this);
		if(forces == null)
			Main.LOG.info("Structure added on client with {} parts", size());
		else Main.LOG.info("Structure added on server with {} parts and {} forces", size(), forces.size());
	}

	public static void rescan(ShaftStructure struct) {
		Main.LOG.info("rescanning {}", struct);
		boolean client = struct.client();
		ArrayList<ShaftAxis> stack = Ticking.of(client).AXIS_STACK;
		stack.clear(); //in case someone else used this and forgot to clear.
		while(!struct.isEmpty()) {
			ShaftAxis axis = struct.remove(0);
			if(axis.struct != struct || axis.parts.isEmpty()) continue;
			ShaftStructure nstruct = new ShaftStructure(client);
			nstruct.add(axis);
			axis.struct = nstruct;
			double J = axis.J;
			double L = axis.J * axis.struct.ω * axis.x;
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
							J += axis.J * Math.abs(x);
							L += axis.struct.ω * axis.J * axis.x;
							axis.x = x;
							axis.struct.remove(axis);
							axis.struct.markDirty(INV_STRUC);
							axis.struct = nstruct;
							nstruct.add(axis);
							stack.add(axis);
						} else if(Math.getExponent(axis.x / x - 1.0) > -24)
							nstruct.J = Double.NaN;
					}
			nstruct.J = J;
			nstruct.ω = L / J;
			if(Double.isNaN(nstruct.ω)) nstruct.ω = 0;
			nstruct.register();
		}
		if(struct.forces != null)
			struct.forces.clear();
	}

	private void recalculateFlowMat() {
		//collect gear connections
		links.clear();
		M_fr = 0;
		for (ShaftAxis axis : this) {
			for (Connection con : axis.cons) {
				if (!(con instanceof GearLink)) continue;
				GearLink link = (GearLink)con;
				if (link.other == null || axis.getIdx() >= link.other.axis.getIdx()) continue;
				links.add(link);
				M_fr += link.staticFriction();
			}
			//TODO maybe add a bearing friction as well
		}
		
		/* We want to solve the following system of linear equations:
		 * 0 = Ml[i] + s[links.get(i).axis.getIdx()] * links.get(i).translation()
		 *   - s[links.get(i).other.axis.getIdx()] * links.other.get(i).translation();
		 * Mx[j] = sum[i](Ml[i]) where links.get(i).axis.getIdx() == j;
		 * Here Mx[i] is the excess torque of axis i (the thing we know),
		 * Ml[i] is the torque transmitted over links.get(i) (the thing we want to compute)
		 * and s[i] is something like a potential on axis i representing elastic deformation in the gears (don't care).
		 * 
		 * This can be represented as block matrix equation:
		 * [ 1  S ] \/ [Ml] = [0 ]  = [ 0 ] \/ [Mx]  <=>  [Ml] = [ 1  S ]^-1 \/ [ 0 ] \/ [Mx]
		 * [ F  0 ] /\ [s ] = [Mx]  = [ 1 ] /\       <=>  [s ] = [ F  0 ]    /\ [ 1 ] /\
		 * This can be simplified to:
		 * [Ml] = [ 1   S  ]^-1 \/ [ 0 ] \/ [Mx] = [ 1  S ]^-1 \/ [     0    ] \/ [Mx]
		 * [s ] = [ 0  F*S ]    /\ [ 1 ] /\      = [ 0  1 ]    /\ [ inv(F*S) ] /\
		 *
		 *  = [ 1  0 ]^-1 \/ [ S*inv(F*S) ] \/ [Mx] = [ S*inv(F*S) ] \/ [Mx]
		 *  = [ 0  1 ]    /\ [  inv(F*S)  ] /\      = [  inv(F*S)  ] /\
		 *
		 * Since we don't care about computing 's', the bottom rows can be dropped and we finally get:
		 * Ml = A * Mx with A = S * inv(F * S)
		 * If we take the transpose, we can use Gauss-algorithm to compute inverse and multiplication in one go:
		 * tr(A) = inv_tr(F * S) * tr(S)
		 * 
		 * F is n * m Matrix:
		 *  F[links.get(i).axis.getIdx(), i] = 1,
		 *  F[links.get(i).other.axis.getIdx(), i] = -1
		 * S is m * n Matrix:
		 *  S[i, links.get(i).axis.getIdx()] = links.get(i).translation()
		 *  S[i, links.get(i).other.axis.getIdx()] = -links.get(i).other.translation()
		 * tr(F * S)[j, i] = (F * S)[i, j] = sum[k](F[i, k] * S[k, j])
		 * 
		 * Note: The rows and columns corresponding to the last axis index are redundant and therefore not included.
		 * Because the excess torque of the last axis is basically determined by what is left over in the end.
		 */
		int n = size() - 1, m = links.size();
		Formula.initMatrix(n, n + m);
		//fill matrix with [ tr(F * S)  tr(S) ]
		for (int i = 0; i < m; i++) {
			GearLink con1 = links.get(i), con2 = con1.other;
			
			int j1 = con1.axis.getIdx(), j2 = con2.axis.getIdx();
			if (j1 < n) {
				float t = (float)con1.translation();
				float[] row = Formula.MATRIX[j1];
				row[n + i] = t;
				row[j1] += t;
				if (j2 < n) row[j2] -= t;
			}
			if (j2 < n) {
				float t = (float)con2.translation();
				float[] row = Formula.MATRIX[j2];
				row[n + i] = -t;
				row[j2] += t;
				if (j1 < n) row[j1] -= t;
			}
		}
		Formula.solveMatrix();
		//copy transposed result into flowMat
		flowMat = new double[n * (m + 1)];
		for (int i = 0; i < n; i++) {
			float[] row = Formula.MATRIX[i];
			for (int j = 0, k = n + i; j < m; j++, k+=n)
				flowMat[k] = row[j + n];
		}
	}

}
