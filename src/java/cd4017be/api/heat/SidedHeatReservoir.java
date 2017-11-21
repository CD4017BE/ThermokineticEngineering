package cd4017be.api.heat;

import cd4017be.api.IBlockModule;
import cd4017be.api.registry.Environment;
import cd4017be.api.CommutativeTickHandler;
import cd4017be.api.CommutativeTickHandler.ICommutativeTickable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * More advanced implementation of {@link IHeatReservoir} for use in TileEntities.<br>
 * This already handles heat exchange with surrounding blocks all on it's own using the CommutativeTick system. The hosting TileEntity only needs to expose it's {@link #getCapability heat capability} and {@link #markUpdate notify block changes}.
 * @author CD4017BE
 */
public class SidedHeatReservoir implements IHeatReservoir, ICommutativeTickable, IBlockModule {

	/** [J/K] heat capacity */
	public final float C;
	/** [K] temperature */
	public float T = Float.NaN;
	protected World world;
	protected BlockPos pos;
	public Environment env;
	protected float envTemp, envCond, dQ;
	protected boolean check;
	protected final Access[] ref = new Access[6];

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
					check = true;
				} else {
					acc.R = R;
					if (acc.link != null) acc.link.updateHeatCond();
				}
			} else if (acc != null) {
				if (acc.link != null) acc.link.disconnect();
				ref[side.ordinal()] = null;
				check = true;
			}
		} else for (EnumFacing s : EnumFacing.values())
			setR(s, R);
		return this;
	}

	/**
	 * @param side the side to access
	 * @return HeatAccess capability for the given side or null if not available
	 */
	public IHeatAccess getCapability(EnumFacing side) {
		if (world == null || world.isRemote || side == null) return null;
		return ref[side.ordinal()];
	}

	/**
	 * marks this HeatReservoir to update its connections to neighboring blocks.<br>
	 * Call this when neighboring blocks change.
	 */
	public void markUpdate() {
		check = true;
	}

	@Override
	public void prepareTick() {
		if (check) {
			envCond = 0;
			for (EnumFacing s : EnumFacing.values()) {
				int i = s.ordinal();
				Access acc = ref[i];
				if (acc == null) continue;
				BlockPos pos1 = pos.offset(s);
				if (acc.link == null && world.isBlockLoaded(pos1)) {//unloaded chunks have a heat conductivity of zero
					TileEntity te = world.getTileEntity(pos1);
					IHeatAccess hr = te == null ? null : te.getCapability(IHeatAccess.CAPABILITY_HEAT_ACCESS, s.getOpposite());
					if (hr != null) new HeatConductor(acc, hr);
					else envCond += env.getCond(world.getBlockState(pos1), acc.R);
				}
			}
			if (envCond > C) envCond = C;
			check = false;
		}
		dQ += (envTemp - T) * envCond;
	}

	@Override
	public void runTick() {
		T += dQ / C;
		dQ = 0;
	}

	@Override
	public void readNBT(NBTTagCompound nbt, String k) {
		this.T = nbt.getFloat(k + "T");
	}

	@Override
	public void writeNBT(NBTTagCompound nbt, String k) {
		nbt.setFloat(k + "T", T);
	}

	@Override
	public void initialize(World world, BlockPos pos) {
		if (this.world == null && !world.isRemote) CommutativeTickHandler.register(this);
		this.world = world;
		this.pos = pos;
		env = Environment.getEnvFor(world);
		envTemp = env.getTemp(world, pos);
		if (Float.isNaN(T)) T = envTemp;
		check = true;
	}

	@Override
	public void invalidate() {
		if (world == null || world.isRemote) return;
		CommutativeTickHandler.invalidate(this);
		for (Access acc : ref)
			if (acc != null && acc.link != null)
				acc.link.disconnect();
		check = false;
		world = null;
		pos = null;
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
			check = true;
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
		this.dQ += dQ;
	}

}
