package cd4017be.api.heat;

import cd4017be.api.IBlockModule;
import cd4017be.api.registry.Environment;
import cd4017be.lib.TickRegistry;
import cd4017be.lib.TickRegistry.IUpdatable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

/**
 * More advanced implementation of {@link IHeatReservoir} for use in TileEntities.<br>
 * This already handles heat exchange with surrounding blocks all on it's own using the CommutativeTick system. The hosting TileEntity only needs to expose it's {@link #getCapability heat capability} and {@link #markUpdate notify block changes}.
 * @author CD4017BE
 */
public class SidedHeatReservoir implements IHeatAccess, IBlockModule, IUpdatable {

	/** [J/K] heat capacity */
	public final float C;
	/** [K] temperature */
	public float T = Float.NaN;
	protected TileEntity tile;
	public Environment env;
	protected float envTemp, Renv;
	private boolean check;
	protected final Access[] ref = new Access[6];
	private HeatConductor envCond;

	/**
	 * creates a SidedHeatReservoir with fixed heat capacity.<br>
	 * By default no side will perform heat transfer, use {@link #setR} to change that.
	 * @param C [J/K] heat capacity
	 */
	public SidedHeatReservoir(float C) {
		this.C = C;
	}

	/**
	 * sets the heat conductivity of a given side<br>
	 * Values of Infinity or NaN will completely disable heat transfer for that side.
	 * @param side the side to edit or null to set all
	 * @param R [K*t/J] heat conduction resistance
	 * @return this for construction convenience
	 */
	public SidedHeatReservoir setR(EnumFacing side, float R) {
		if (side != null) {
			Access acc = ref[side.ordinal()];
			if (Float.isFinite(R)) {
				R = Math.max(R, 1F / C);
				if (acc == null) {
					ref[side.ordinal()] = new Access(R);
					markUpdate();
				} else {
					acc.R = R;
					if (acc.link != null) acc.link.updateHeatCond();
				}
			} else if (acc != null) {
				if (acc.link != null) acc.link.disconnect();
				ref[side.ordinal()] = null;
				markUpdate();
			}
		} else for (EnumFacing s : EnumFacing.values())
			setR(s, R);
		return this;
	}

	@Override
	public boolean hasCapability(Capability<?> cap, EnumFacing side) {
		return tile != null && side != null && ref[side.ordinal()] != null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> cap, EnumFacing side) {
		if (tile == null || side == null) return null;
		return (T)ref[side.ordinal()];
	}

	@Override
	public boolean supportsCapability(Capability<?> cap) {
		return cap == IHeatAccess.CAPABILITY_HEAT_ACCESS;
	}

	/**
	 * marks this HeatReservoir to update its connections to neighboring blocks.<br>
	 * Call this when neighboring blocks change.
	 */
	public void markUpdate() {
		if (!check) {
			check = true;
			if (tile != null) TickRegistry.instance.updates.add(this);
		}
	}

	@Override
	public void process() {
		if (check) {
			float l = 0;
			BlockPos pos = tile.getPos();
			World world = tile.getWorld();
			for (EnumFacing s : EnumFacing.values()) {
				int i = s.ordinal();
				Access acc = ref[i];
				if (acc == null) continue;
				BlockPos pos1 = pos.offset(s);
				if (acc.link == null && world.isBlockLoaded(pos1)) {//unloaded chunks have a heat conductivity of zero
					TileEntity te = world.getTileEntity(pos1);
					IHeatAccess hr = te == null ? null : te.getCapability(IHeatAccess.CAPABILITY_HEAT_ACCESS, s.getOpposite());
					if (hr != null) new HeatConductor(acc, hr);
					else l += env.getCond(world.getBlockState(pos1), acc.R);
				}
			}
			if (l > C) l = C;
			Renv = 1F / l;
			if (l != 0) {
				if (envCond == null) new HeatConductor(this, new InfiniteReservoir(envTemp, 0));
			} else if (envCond != null) envCond.disconnect();
			check = false;
		}
	}

	@Override
	public void readNBT(NBTTagCompound nbt, String k, TileEntity tile) {
		this.T = nbt.getFloat(k + "T");
	}

	@Override
	public void writeNBT(NBTTagCompound nbt, String k) {
		nbt.setFloat(k + "T", T);
	}

	@Override
	public void initialize(TileEntity tile) {
		World world = tile.getWorld();
		if (world.isRemote) return;
		if (this.tile == null && check) TickRegistry.instance.updates.add(this);
		this.tile = tile;
		env = Environment.getEnvFor(world);
		envTemp = env.getTemp(world, tile.getPos());
		if (Float.isNaN(T)) T = envTemp;
	}

	@Override
	public void invalidate() {
		for (Access acc : ref)
			if (acc != null && acc.link != null)
				acc.link.disconnect();
		if (envCond != null) {
			envCond.disconnect();
			envCond = null;
		}
		check = false;
		tile = null;
	}

	private class Access implements IHeatAccess {

		private float R;
		HeatConductor link;

		Access(float R) {
			this.R = R;
		}

		@Override
		public float T() {
			return T;
		}

		@Override
		public float R() {
			return R;
		}

		@Override
		public void addHeat(float dQ) {
			T += dQ / C;
		}

		@Override
		public HeatConductor getLink() {
			return link;
		}

		@Override
		public void setLink(HeatConductor c) {
			if (link == c) return;
			if (link != null && c != null) link.disconnect();
			link = c;
			markUpdate();
		}

	}

	/**@return [K] environment temperature (for convenience since it's cached here) */
	public float envT() {
		return envTemp;
	}

	@Override
	public float T() {
		return T;
	}

	@Override
	public void addHeat(float dQ) {
		T += dQ / C;
	}

	@Override
	public HeatConductor getLink() {
		return envCond;
	}

	@Override
	public void setLink(HeatConductor c) {
		envCond = c;
	}

	@Override
	public float R() {
		return Renv;
	}

}
