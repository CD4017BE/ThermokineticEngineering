package cd4017be.thermokin.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;
import cd4017be.thermokin.physics.GasState;
import cd4017be.thermokin.recipe.Substances;
import cd4017be.thermokin.recipe.Substances.Environment;
import cd4017be.thermokin.multiblock.GasContainer;
import cd4017be.thermokin.multiblock.GasPhysics;
import cd4017be.thermokin.multiblock.GasPhysics.IGasCon;
import cd4017be.lib.templates.MultiblockTile;

public class GasVent extends MultiblockTile<GasContainer, GasPhysics> implements IGasCon, ITickable {

	private Environment env;

	public GasVent() {
		comp = new GasContainer(this, 5F);
	}

	@Override
	public boolean conGas(byte side) {
		return side == this.getOrientation();
	}

	@Override
	public void update() {
		if (worldObj.isRemote) return;
		super.update();
		GasState gas = comp.network.gas;
		double Pa = gas.P();
		if (Pa > env.P) {
			gas.extract(gas.V * (Math.sqrt(Pa / env.P) - 1.0));
		} else {
			env.getGas(worldObj, pos, 100).exchange(gas);
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		comp.writeToNBT(nbt, "gas");
		return super.writeToNBT(nbt);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		comp = GasContainer.readFromNBT(this, nbt, "gas", 5F);
	}

	@Override
	public void setWorldObj(World world) {
		super.setWorldObj(world);
		env = Substances.getEnvFor(world);
	}

}
