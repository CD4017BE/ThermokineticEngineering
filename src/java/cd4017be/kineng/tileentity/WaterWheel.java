package cd4017be.kineng.tileentity;

import static cd4017be.kineng.physics.Ticking.dt;
import static java.lang.Math.*;
import cd4017be.kineng.block.BlockRotaryTool;
import cd4017be.kineng.physics.*;
import cd4017be.lib.block.AdvancedBlock.IInteractiveTile;
import cd4017be.math.cplx.CplxD;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fluids.FluidStack;

/** 
 * @author CD4017BE */
public class WaterWheel extends KineticMachine implements IWaterWheel, IInteractiveTile {

	double vsq;

	@Override
	public double passLiquid(double vsq, FluidStack liquid, EnumFacing dir) {
		if (con == null || dir.getAxis() == axis()) return vsq;
		Wheel wheel = getForce();
		int d = dir.ordinal();
		wheel.add(
			liquid.amount * liquid.getFluid().getDensity(liquid) * 0.001,
			copySign(vsq, ((d ^ d << 1) & 2) - 1)
		);
		return min(wheel.vsq, vsq);
	}

	@Override
	protected DynamicForce createForce(BlockRotaryTool block) {
		return new Wheel();
	}

	@Override
	public boolean onActivated(
		EntityPlayer player, EnumHand hand, ItemStack item, EnumFacing s, float X, float Y, float Z
	) {
		if (con == null || world.isRemote) return true;
		Wheel w = getForce();
		player.sendStatusMessage(new TextComponentString(
			String.format("%.0f mB/t @ %.1f m/s", w.p_m, sqrt(abs(w.p_Ekin / w.p_m)))
		), true);
		return true;
	}

	@Override
	public void onClicked(EntityPlayer player) {}

	static class Wheel extends DynamicForce {

		double Ekin, m;
		double p_Ekin, p_m, vsq;

		void add(double m, double vsq) {
			this.m += m;
			this.Ekin += vsq * m;
		}

		@Override
		public void work(double dE, double ds, double v) {
			vsq = v * v;
			p_Ekin = Ekin;
			p_m = m;
			Ekin = m = 0;
		}

		@Override
		public ForceCon getM(CplxD M, double av) {
			Fdv = -0.5 * m / dt;
			F = Ekin == 0 ? 0 :
				Ekin / ((abs(av * r) + sqrt(abs(Ekin / m))) * dt);
			return super.getM(M, av);
		}

	}

}
