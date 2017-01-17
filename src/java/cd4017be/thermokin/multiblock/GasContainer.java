package cd4017be.thermokin.multiblock;

import java.util.Arrays;
import java.util.HashMap;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import cd4017be.thermokin.Objects;
import cd4017be.thermokin.physics.GasState;
import cd4017be.thermokin.recipe.Substances;
import cd4017be.lib.ModTileEntity;
import cd4017be.lib.templates.MultiblockComp;

public class GasContainer extends MultiblockComp<GasContainer, GasPhysics> implements ICapabilityProvider {

	public double V;
	public float heatCond, refTemp;
	public final float[] R = new float[6];
	private final IHeatReservoir[] ref = new IHeatReservoir[6];

	public GasContainer(ModTileEntity tile, double V) {
		super(tile);
		this.V = V;
	}

	public void setUID(long uid) {
		super.setUID(uid);
		if (network == null) {
			World world = ((TileEntity)tile).getWorld();
			new GasPhysics(this, Substances.getEnvFor(world).getGas(world, ((TileEntity)tile).getPos(), V));
		}
	}

	public static GasContainer readFromNBT(ModTileEntity tile, NBTTagCompound nbt, String k, double V) {
		GasContainer pipe = new GasContainer(tile, V);
		new GasPhysics(pipe, GasState.readGasFromNBT(nbt, k, V));
		return pipe;
	}

	public void writeToNBT(NBTTagCompound nbt, String k) {
		if (network != null) network.gas.copy(V).writeGasToNBT(nbt, k);
	}

	public void remove() {
		if (network != null) network.remove(this);
	}

	public GasState evacuate() {
		HashMap<Long, GasContainer> map = new HashMap<Long, GasContainer>(1);
		map.put(uid, this);
		GasPhysics old = network;
		network = old.onSplit(map);
		old.components.remove(uid);
		GasState ng = network.gas;
		old.gas.nR += ng.nR; ng.nR = 0;
		updateCon = true;
		old.update = true;
		return ng;
	}

	@Override
	public Capability<GasContainer> getCap() {
		return Objects.GAS_CAP;
	}

	public void setResistance(float R) {
		Arrays.fill(this.R, R);
	}

	public class HeatWrapper implements IHeatReservoir {

		private final int side;

		public HeatWrapper(int side) {
			this.side = side;
		}

		@Override
		public float T() {
			return (float)network.gas.T;
		}

		@Override
		public float C() {
			return (float)network.gas.nR;
		}

		@Override
		public float R() {
			return R[side];
		}

		@Override
		public void addHeat(float dQ) {
			network.gas.T += dQ / network.gas.nR;
		}

		@Override
		public boolean invalid() {
			return GasContainer.this.invalid();
		}

		public GasContainer owner() {
			return GasContainer.this;
		}

	}

	@Override
	public boolean hasCapability(Capability<?> cap, EnumFacing s) {
		return cap == Objects.HEAT_CAP || cap == Objects.GAS_CAP;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> cap, EnumFacing s) {
		if (cap == Objects.GAS_CAP) return (T)this;
		if (cap == Objects.HEAT_CAP) {
			IHeatReservoir hr = ref[s.ordinal()];
			if (hr == null) ref[s.ordinal()] = hr = new HeatWrapper(s.ordinal());
			return (T)hr;
		}
		return null;
	}
}
