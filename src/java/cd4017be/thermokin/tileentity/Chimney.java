package cd4017be.thermokin.tileentity;

import net.minecraft.block.Block;
import cd4017be.thermokin.block.BlockChimney;
import cd4017be.thermokin.physics.GasState;

public class Chimney extends GasVent {

	public static double gravity = 9.81;
	private int height = 0;

	@Override
	protected void ventGas(GasState gas) {
		if (height == 0) onNeighborBlockChange(blockType);
		GasState envG = env.getGas(worldObj, pos, height);
		double Pe = envG.nR * envG.type.m - gas.nR * gas.type.m / gas.V * (double)height;
		Pe = Math.max(0.5 * env.P, env.P - Pe * gravity);
		double Pa = gas.P();
		if (Pa > Pe)
			gas.extract(gas.V * (Math.sqrt(Pa / Pe) - 1.0));
		super.ventGas(gas);
	}

	@Override
	public void onNeighborBlockChange(Block b) {
		height = worldObj.getBlockState(pos).getValue(BlockChimney.prop) + 1;
	}

	@Override
	public byte getOrientation() {
		return 0;
	}

}
