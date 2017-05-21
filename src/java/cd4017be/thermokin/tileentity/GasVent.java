package cd4017be.thermokin.tileentity;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import cd4017be.lib.ModTileEntity;
import cd4017be.thermokin.Objects;
import cd4017be.thermokin.multiblock.GasContainer;
import cd4017be.thermokin.multiblock.GasPhysics.IGasCon;
import cd4017be.thermokin.physics.GasState;
import cd4017be.thermokin.recipe.Substances;
import cd4017be.thermokin.recipe.Substances.Environment;

public class GasVent extends ModTileEntity implements IGasCon, ITickable {

	protected Environment env;
	protected GasContainer gasRef;
	protected boolean updateCon = true;

	@Override
	public void update() {
		if (worldObj.isRemote) return;
		if (updateCon) {
			updateCon = false;
			gasRef = getNeighborCap(Objects.GAS_CAP, EnumFacing.VALUES[getOrientation()]);
		}
		if (gasRef == null) return;
		ventGas(gasRef.network.gas);
	}

	protected void ventGas(GasState gas) {
		double Pa = gas.P();
		if (Pa > env.P)
			gas.extract(gas.V * (Math.sqrt(Pa / env.P) - 1.0));
	}

	@Override
	public boolean conGas(byte side) {
		return side == this.getOrientation();
	}

	@Override
	public void onNeighborTileChange(BlockPos pos) {
		updateCon  = true;
	}

	@Override
	public void setWorldObj(World world) {
		super.setWorldObj(world);
		env = Substances.getEnvFor(world);
	}

}
