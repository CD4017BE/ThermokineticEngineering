package cd4017be.thermokin.tileentity;

import net.minecraft.block.Block;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import cd4017be.lib.ModTileEntity;
import cd4017be.thermokin.Objects;
import cd4017be.thermokin.multiblock.GasContainer;
import cd4017be.thermokin.multiblock.GasPhysics.IGasCon;
import cd4017be.thermokin.physics.GasState;

public class GasValve extends ModTileEntity implements IGasCon, ITickable {

	private GasContainer input, output;
	public float control, slowFac;
	private float envP;
	public int mode;
	private boolean updateCon;

	@Override
	public void update() {
		if (updateCon || (input != null && input.invalid()) || output != null && output.invalid()) {
			updateCon = false;
			input = getNeighborCap(Objects.GAS_CAP, EnumFacing.VALUES[getOrientation()]);
			output = getNeighborCap(Objects.GAS_CAP, EnumFacing.VALUES[getOutSide()]);
		}
		if (input == null) return;
		GasState in = input.network.gas;
		double Pa = in.P();
		if (Pa <= control) return;
		if (output == null) {
			double Pref = control > envP ? control : envP;
			if (Pa > Pref)
				in.extract(in.V * (Math.sqrt(Pa / Pref) - 1.0) * slowFac);
		} else {
			GasState out = output.network.gas;
			double Pref = out.P();
			if (Pa > Pref && in.type == out.type) {
				double x = Math.sqrt(Pref / Pa);
				double dV = in.V * (1 - x) / (in.V / out.V + x) * slowFac;
				//double Pend = pa * (in.V / dV - dV / in.V);
				in.T *= in.V / (dV + in.V);
				out.T *= out.V / (out.V - dV);
				double dnR = in.nR * dV / in.V;
				in.nR -= dnR; out.T = (out.T * out.nR + dnR * in.T) / (out.nR += dnR);
			}
		}
	}

	@Override
	public boolean conGas(byte side) {
		return side == getOrientation() || side == getOutSide(); 
	}

	private int getOutSide() {
		return ((mode & 0xf) + getOrientation()) % 6;
	}

	@Override
	public void onNeighborBlockChange(Block b) {
		updateCon = true;
	}

	@Override
	public void onNeighborTileChange(BlockPos pos) {
		updateCon = true;
	}

}
