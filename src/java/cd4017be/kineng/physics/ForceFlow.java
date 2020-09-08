package cd4017be.kineng.physics;

import java.util.ArrayList;
import java.util.BitSet;

/** Used compute intermediate forces on individual parts in a shaft structure.
 * ForceFlow elements act as instructions that are executed in series and use a
 * double array to pass on force values between them.
 * @author CD4017BE */
public interface ForceFlow {

	/** @param vars [N] variable table:
	 *        <br>vars[0] accumulates all gear friction [N*m].
	 *        <br>vars[1] holds current shaft velocity [r/s].
	 *        <br>vars[2] holds shaft acceleration [r/s²].
	 *        <br>remaining entries hold propagated torque [N*m]. */
	void process(double[] vars);


	class Null implements ForceFlow {
		final int i;

		public Null(int i) {
			this.i = i;
		}

		@Override
		public void process(double[] vars) {
			vars[i] = 0;
		}

		@Override
		public String toString() {
			return "f" + i + " = 0";
		}
	}


	class Source implements ForceFlow {

		final ForceCon c;
		final int i;

		/** sets vars[i] to the current force applied by f.
		 * @param c force connection
		 * @param i variable index */
		public Source(ForceCon c, int i) {
			this.c = c;
			this.i = i;
		}

		@Override
		public void process(double[] vars) {
			DynamicForce f = c.force;
			if (f == null) return;
			double F = vars[1] * f.r * f.Fdv + f.F, max = c.maxF;
			if (Math.abs(F) > max)
				c.host.onOverload(F, max);
			vars[i] += F * f.r;
		}

		@Override
		public String toString() {
			return "f" + i + "<- F(v):" + c.force;
		}
	}


	class Inertia implements ForceFlow {

		final double J;
		final int i;

		/** subtracts acceleration inertia for the given mass from vars[i].
		 * @param J [kg*m²] segment's moment of inertia
		 * @param i variable index */
		public Inertia(double J, int i) {
			this.J = J;
			this.i = i;
		}

		@Override
		public void process(double[] vars) {
			vars[i] -= vars[2] * J;
		}

		@Override
		public String toString() {
			return String.format("f%d <- a * %.3g kg*m", i, J);
		}
	}


	class Check implements ForceFlow {

		final double limit;
		final OverloadHandler handler;
		final int i;

		/** checks whether vars[i] exceeds the given torque limit and notifies the given
		 * handler in case of overload.
		 * @param handler
		 * @param limit [N*m] torque limit
		 * @param i variable index */
		public Check(OverloadHandler handler, double limit, int i) {
			this.handler = handler;
			this.limit = limit;
			this.i = i;
		}

		@Override
		public void process(double[] vars) {
			double F = Math.abs(vars[i]);
			if(F > limit) handler.onOverload(F, limit);
		}

		@Override
		public String toString() {
			return String.format("lim f%d < %.3g N*m", i, limit);
		}

	}


	class Merge implements ForceFlow {

		final int i, j;

		/** combines the forces vars[i] and vars[j] into vars[i].
		 * @param i variable index
		 * @param j variable index */
		public Merge(int i, int j) {
			this.i = i;
			this.j = j;
		}

		@Override
		public void process(double[] vars) {
			vars[i] += vars[j];
		}

		@Override
		public String toString() {
			return "f" + i + " <- f" + j;
		}
		
	}


	class Friction implements ForceFlow {

		final int i;
		final double stat, dyn;

		/** adds a friction force of (stat + |dyn * vars[i]|) onto vars[0].
		 * @param stat [N*m] static friction component
		 * @param dyn [1] dynamic friction component
		 * @param i variable index */
		public Friction(double stat, double dyn, int i) {
			this.stat = stat;
			this.dyn = dyn;
			this.i = i;
		}

		@Override
		public void process(double[] vars) {
			vars[0] -= Math.abs(vars[i] * dyn) + stat;
		}

		@Override
		public String toString() {
			return String.format("f0 <- %.3g N*m + |%.3g f%d|", stat, dyn, i);
		}

	}


	interface OverloadHandler {

		/** gets called in case a port is overloaded
		 * @param F [N|N*m] the force/torque applied on/through the part
		 * @param lim [N|N*m] the limit that was specified for the part */
		void onOverload(double F, double lim);
	}

	FlowCompiler COMPILER = new FlowCompiler();

	class FlowCompiler {
		final ArrayList<ForceFlow> instr = new ArrayList<>();
		final BitSet used = new BitSet();
		int max;

		private int next() {
			int i = used.nextClearBit(0);
			used.set(i);
			if (i >= max) max = i + 1;
			instr.add(new Null(i));
			return i;
		}

		private void merge(int i, int j) {
			instr.add(new Merge(i, j));
			used.clear(j);
		}

		public void compile(ShaftStructure struc) {
			used.set(0, 3);
			max = 3;
			compileShaft(struc.axes.get(0), null);
			struc.stack = new double[max];
			struc.flow = instr.toArray(new ForceFlow[instr.size()]);
			used.clear();
			instr.clear();
		}

		private int compileShaft(ShaftAxis shaft, GearLink from) {
			boolean isFirst = from != null && from.host == shaft.parts.get(0);
			boolean isLast = from != null && from.host == shaft.parts.get(shaft.parts.size() - 1);
			int i = next();
			if (isFirst && isLast) {
				for (Connection con : shaft.cons) {
					if (con == from) continue;
					if (con instanceof ForceCon)
						instr.add(new Source((ForceCon)con, i));
					else if (con instanceof GearLink)
						merge((GearLink)con, i);
				}
				instr.add(new Inertia(from.host.J(), i));
				return i;
			}
			traverse(shaft, true, from, i);
			if (from == null || isLast) return i;
			int i1 = i;
			if (!isFirst) i = next();
			traverse(shaft, false, from, i);
			if (i == i1) return i;
			merge(i1, i);
			return i1;
		}

		//TODO regular shaft overload checks
		private void traverse(ShaftAxis shaft, boolean forward, GearLink end, int i) {
			int p1 = shaft.parts.size() - 1;
			int p = forward ? 0 : p1, d = forward ? 1 : -1;
			IShaftPart part = shaft.parts.get(p);
			double m = part.J();
			for (Connection con : shaft.cons) {
				while(part != con.host) {
					m += part.J();
					part = shaft.parts.get(p += d);
					m += part.J();
					if (forward && p == p1)
						m += part.J();
				}
				if (m > 0 && p != 0 && (forward || p != p1)) {
					instr.add(new Inertia(m * 0.5 * shaft.x, i));
					m = 0;
				}
				if (con == end) break;
				else if (con instanceof ForceCon)
					instr.add(new Source((ForceCon)con, i));
				else if (con instanceof GearLink)
					merge((GearLink)con, i);
			}
		}

		private void merge(GearLink con, int i) {
			GearLink link = con.other;
			if (link == null) return;
			int j = compileShaft(link.axis, link);
			double l0 = link.maxTorque(), l1 = con.maxTorque();
			if (l0 < l1) instr.add(new Check(link.host, l0, j));
			else instr.add(new Check(con.host, l1, j));
			instr.add(new Friction(con.staticFriction(), link.fricD + con.fricD, j));
			merge(i, j);
		}
	}

}
