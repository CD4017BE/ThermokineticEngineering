package cd4017be.thermokin.multiblock;

import java.util.ArrayList;
import java.util.HashMap;
import cd4017be.thermokin.physics.GasState;
import cd4017be.thermokin.physics.LiquidState;
import cd4017be.lib.templates.SharedNetwork;

/**
 * This class represents a network of connected pressurized liquid reservoirs.
 * Liquid is automatically redistributed to achieve equal pressure.
 * @author CD4017BE
 */
public class LiquidPhysics extends SharedNetwork<LiquidComponent, LiquidPhysics> {

	/** A list of all connected LiquidContainers */
	public ArrayList<LiquidContainer> nodes = new ArrayList<LiquidContainer>();
	/** This is a summary of all liquid stored in this network */
	public LiquidState content;

	public static interface ILiquidCon {
		public boolean conLiquid(byte side);
	}

	public LiquidPhysics(LiquidComponent core) {
		super(core);
		if (core instanceof LiquidContainer) {
			nodes.add((LiquidContainer)core);
			content = ((LiquidContainer)core).liquid.copy();
		} else content = new LiquidState(0);
	}

	protected LiquidPhysics(HashMap<Long, LiquidComponent> comps) {
		super(comps);
		content = new LiquidState(0);
		for (LiquidComponent comp : comps.values())
			if (comp instanceof LiquidContainer) {
				nodes.add((LiquidContainer)comp);
				content.merge(((LiquidContainer)comp).liquid);
			}
	}

	@Override
	public LiquidPhysics onSplit(HashMap<Long, LiquidComponent> comps) {
		LiquidPhysics phys = new LiquidPhysics(comps);
		nodes.removeAll(phys.nodes);
		content.remove(phys.content);
		return phys;
	}

	@Override
	public void onMerged(LiquidPhysics network) {
		super.onMerged(network);
		nodes.addAll(network.nodes);
		content.merge(network.content);
	}

	@Override
	public void remove(LiquidComponent comp) {
		super.remove(comp);
		if (comp instanceof LiquidContainer) {
			nodes.remove(comp);
			content.remove(((LiquidContainer)comp).liquid);
		}
	}

	@Override
	public void add(LiquidComponent comp) {
		if (content.type == null || comp.network == null || comp.network.content.type == null || content.type == comp.network.content.type)
			super.add(comp);
	}

	@Override
	protected void updatePhysics() {
		//To do the liquid exchange, we need at least 2 nodes and some room for moving
		if (nodes.size() > 1 && content.V > 0 && content.V < content.Vmax) {
			//First calculate the target liquid pressure and total space for moving
			double Eg = 0, Vg = 0, Vtot = 0, Vrem = 0;
			for (LiquidContainer node : nodes) {
				GasState gas = node.getBufferGas();
				Eg += gas.E();
				Vg += gas.V;
				Vtot += node.liquid.V;
				Vrem += node.liquid.Vmax;
			}
			content.V = Vtot; content.Vmax = Vrem;
			Vrem -= Vtot;
			//Then calculate for each node the transfer necessary to reach target pressure.
			//But only perform the draining yet and combine all drained liquid together.
			int i = 0, n = 0;
			double[] dV = new double[nodes.size()];
			LiquidState[] targets = new LiquidState[dV.length];
			LiquidState out = new LiquidState(content.type, 0, 0, 0);
			for (LiquidContainer node : nodes) {
				GasState gas = node.getBufferGas();
				LiquidState liq = node.liquid;
				double d;
				if (++i == dV.length) {
					d = Vtot - liq.V;
					gas.adiabat(gas.V - d);
				} else {
					Vtot -= liq.V;
					Vrem -= liq.Vrem();
					double V = gas.V,
						Vmax = V + Math.min(Vrem, liq.V),
						Vmin = V - Math.min(Vtot, liq.Vrem());
					V *= gas.E() / Eg * Vg;
					if (V >= Vmax * Vmax) V = Vmax;
					else if (V <= Vmin * Vmin) V = Vmin;
					else V = Math.sqrt(V);
					d = gas.V - V;
					gas.adiabat(V);
					Eg -= gas.E();
					Vg -= gas.V;
					Vtot -= d;
					Vrem += d;
				}
				if (d < 0) out.merge(liq.drain(-d));
				else if (d > 0) {
					targets[n] = liq;
					dV[n++] = d;
				}
			}
			//Finally as we know what temperature the drained liquid has, we can perform the filling part
			for (i = 0; i < n; i++)
				targets[i].insert(out.drain(dV[i]));
		}
	}

	/**
	 * Tries to drain the given liquid out of this network
	 * @param liq : <br>
	 * <b>liq.Vmax</b> defines maximum amount to drain, <br>
	 * <b>liq.type</b> will be set to the actually drained type and <br>
	 * <b>liq.V</b> will be set to the actually drained amount.
	 * @param doDrain true will actually do it, false will just simulate
	 * @return [J] mechanical energy required for this operation (usually negative)
	 */
	public double drainLiquid(LiquidState liq, boolean doDrain) {
		double E = 0;
		if (nodes.isEmpty() || content.V == 0) return E;
		liq.V = 0;
		liq.type = content.type;
		double rem = content.V, Vg = 0, V = 0;
		boolean nall = liq.Vmax < content.V;
		if (nall) {
			V = liq.Vmax;
			rem -= V;
			for (LiquidContainer node : nodes) {
				GasState gas = node.getBufferGas();
				Vg += gas.V;
			}
		} else {
			V = rem;
		}
		for (LiquidContainer node : nodes) {
			LiquidState liquid = node.liquid;
			GasState gas = node.getBufferGas();
			double dV = liquid.V;
			if (nall) {
				double dV1 = gas.V / Vg * V;
				if (dV1 < dV) {
					rem -= dV;
					dV = Math.max(dV1, -rem);
					rem += dV;
				}
				Vg -= gas.V;
				V -= dV;
			}
			double ngV = gas.V + dV;
			E -= gas.E() * (1.0 - gas.V / ngV);
			if (doDrain) {
				liq.insert(liquid.drain(dV));
				gas.adiabat(ngV);
			} else liq.insert(liquid.copy(dV));
		}
		if (doDrain) {
			content.V -= liq.V;
			if (content.V == 0) content.type = null;
		}
		return E;
	}

	/**
	 * Tries to fill the given liquid into this network
	 * @param liq : <br>
	 * <b>liq.Vmax</b> defines maximum amount to fill, <br>
	 * <b>liq.type</b> defines the type of liquid to fill and <br>
	 * <b>liq.V</b> will be set to the amount actually filled.
	 * @param doFill true will actually do it, false will just simulate
	 * @return [J] mechanical energy required for this operation
	 */
	public double fillLiquid(LiquidState liq, boolean doFill) {
		double E = 0;
		if (nodes.isEmpty() || (content.V > 0 && content.type != liq.type)) return E;
		liq.V = 0;
		double rem = content.Vrem(), Vg = 0, V = 0;
		boolean nall = liq.Vmax < rem;
		if (nall) {
			V = liq.Vmax;
			rem -= V;
			for (LiquidContainer node : nodes) {
				GasState gas = node.getBufferGas();
				Vg += gas.V;
			}
		} else if (rem == 0) return 0;
		for (LiquidContainer node : nodes) {
			LiquidState liquid = node.liquid;
			GasState gas = node.getBufferGas();
			double dV = liquid.Vrem();
			if (nall) {
				double dV1 = gas.V / Vg * V;
				if (dV1 < dV) {
					rem -= dV;
					dV = Math.max(dV1, -rem);
					rem += dV;
				}
				Vg -= gas.V;
				V -= dV;
			}
			double ngV = gas.V - dV;
			E -= gas.E() * (1.0 - gas.V / ngV);
			if (doFill) {
				liq.V += liquid.insert(liq.copy(dV));
				gas.adiabat(ngV);
			} else liq.V += dV;
		}
		if (doFill) {
			content.type = liq.type;
			content.V += liq.V;
		}
		return E;
	}

}
