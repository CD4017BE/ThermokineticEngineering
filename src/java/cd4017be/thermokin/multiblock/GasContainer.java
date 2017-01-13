package cd4017be.thermokin.multiblock;

import java.util.HashMap;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import cd4017be.thermokin.Objects;
import cd4017be.thermokin.physics.GasState;
import cd4017be.thermokin.recipe.Substances;
import cd4017be.lib.ModTileEntity;
import cd4017be.lib.templates.MultiblockComp;

public class GasContainer extends MultiblockComp<GasContainer, GasPhysics> implements IHeatReservoir {

	public double V;
	public float heatCond, refTemp;

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
	public float T() {
		return (float)network.gas.T;
	}

	@Override
	public float C() {
		return (float)network.gas.nR;
	}

	@Override
	public void addHeat(float dQ) {
		network.gas.T += dQ / network.gas.nR;
	}

	@Override
	public Capability<GasContainer> getCap() {
		return Objects.GAS_CAP;
	}

}
