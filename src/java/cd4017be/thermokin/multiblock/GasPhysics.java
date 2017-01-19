package cd4017be.thermokin.multiblock;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import cd4017be.thermokin.Objects;
import cd4017be.thermokin.multiblock.GasContainer.HeatWrapper;
import cd4017be.thermokin.physics.GasState;
import cd4017be.thermokin.physics.ThermodynamicUtil;
import cd4017be.thermokin.recipe.Substances;
import cd4017be.thermokin.recipe.Substances.Environment;
import cd4017be.lib.templates.SharedNetwork;

/**
 * This class represents a network of connected gas reservoirs and combines them into one large reservoir.
 * @author CD4017BE
 */
public class GasPhysics extends SharedNetwork<GasContainer, GasPhysics> {

	public final GasState gas;
	public float heatCond, refTemp;
	public HashMap<Long, HeatExchCon> heatExch = new HashMap<Long, HeatExchCon>();
	public HashSet<GasContainer> spread = new HashSet<GasContainer>();
	
	static class HeatExchCon {
		HeatExchCon(HeatWrapper pipe, IHeatReservoir other) {
			this.pipe = pipe;
			this.other = other;
		}
		final HeatWrapper pipe;
		final IHeatReservoir other;
	}
	
	public GasPhysics(GasContainer core, GasState gas) {
		super(core);
		this.gas = gas;
		this.heatCond = core.heatCond;
		this.refTemp = core.refTemp;
	}
	
	public GasPhysics(HashMap<Long, GasContainer> comps, GasState gas) {
		super(comps);
		this.gas = gas;
	}

	@Override
	public GasPhysics onSplit(HashMap<Long, GasContainer> comps) {
		float V = 0, C = 0, T = 0;
		for (GasContainer c : comps.values()) {
			V += c.V; C += c.heatCond; T += c.refTemp;
		}
		this.heatCond -= C;
		this.refTemp -= T;
		GasPhysics gp = new GasPhysics(comps, gas.split(V));
		gp.heatCond = C;
		gp.refTemp = T;
		for (Iterator<Entry<Long, HeatExchCon>> it = heatExch.entrySet().iterator(); it.hasNext();) {
			Entry<Long, HeatExchCon> e = it.next();
			if (e.getValue().pipe.owner().network == gp) {
				gp.heatExch.put(e.getKey(), e.getValue());
				it.remove();
			}
		}
		return gp;
	}

	@Override
	public void onMerged(GasPhysics network) {
		super.onMerged(network);
		gas.merge(network.gas);
		this.heatCond += network.heatCond;
		this.refTemp += network.refTemp;
		this.heatExch.putAll(network.heatExch);
	}

	@Override
	public void remove(GasContainer comp) {
		if (this.components.containsKey(comp.getUID())) gas.split(comp.V);
		this.heatCond -= comp.heatCond;
		this.refTemp -= comp.refTemp;
		for (int i = 0; i < 6; i+=2) this.heatExch.remove(SharedNetwork.SidedPosUID(comp.getUID(), i));
		super.remove(comp);
	}

	public static interface IGasCon {
		public boolean conGas(byte side);
	}

	@Override
	public void updateCompCon(GasContainer comp) {
		for (byte i : sides()) {
			GasContainer obj;
			if (comp.canConnect(i) && (obj = comp.getNeighbor(i)) != null) {
				if (obj.network.gas.type != gas.type) {
					obj.network.spread.add(comp);
					spread.add(obj);
				} else add(obj);
			}
		}
		comp.updateCon = false;
		ICapabilityProvider te;
		TileEntity tile = (TileEntity)comp.tile;
		World world = tile.getWorld();
		BlockPos pos = tile.getPos();
		Environment env = Substances.getEnvFor(world);
		float dC = -comp.heatCond;
		for (EnumFacing s : EnumFacing.VALUES) {
			int i = s.ordinal();
			IHeatReservoir hr = comp.tile.getCapability(Objects.HEAT_CAP, s);
			if (!(hr instanceof HeatWrapper)) continue;
			HeatWrapper gh = (HeatWrapper)hr;
			if ((te = comp.tile.getTileOnSide(s)) == null || (hr = te.getCapability(Objects.HEAT_CAP, s.getOpposite())) == null) {
				dC += env.getCond(world.getBlockState(pos), comp.R[i]);
			} else if ((i & 1) == 0 && !(hr instanceof HeatWrapper && ((HeatWrapper)hr).owner().network == this)) {
				heatExch.put(SharedNetwork.SidedPosUID(comp.getUID(), i), new HeatExchCon(gh, hr));
			}
		}
		comp.heatCond += dC;
		this.heatCond += dC;
		float dT = env.getTemp(world, pos) * comp.heatCond - comp.refTemp;
		comp.refTemp += dT;
		this.refTemp += dT;
	}

	@Override
	protected void updatePhysics() {
		if (heatCond > 0) {
			float T = refTemp / heatCond;
			if (heatCond > gas.nR) gas.T = T;
			else gas.T -= (gas.T - T) * heatCond / gas.nR;
		}
		if (!heatExch.isEmpty())
			for (Iterator<HeatExchCon> it = heatExch.values().iterator(); it.hasNext();) {
				HeatExchCon hc = it.next();
				if (hc.other.invalid() || (hc.other instanceof HeatWrapper && ((HeatWrapper)hc.other).owner().network == this)) {
					it.remove();
					continue;
				}
				HeatReservoir.exchangeHeat(hc.pipe, hc.other);
			}
		if (!spread.isEmpty())
			for (Iterator<GasContainer> it = spread.iterator(); it.hasNext();) {
				GasContainer cont = it.next();
				if (((TileEntity)cont.tile).isInvalid()) {
					it.remove();
					continue;
				}
				double dV = cont.V;
				if (ThermodynamicUtil.shouldSpread(gas, cont.network.gas, dV)) {
					it.remove();
					cont.evacuate().type = gas.type;
					add(cont);
				}
			}
	}

}
