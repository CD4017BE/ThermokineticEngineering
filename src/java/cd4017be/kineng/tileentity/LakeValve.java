package cd4017be.kineng.tileentity;

import static net.minecraftforge.fluids.capability.CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
import cd4017be.lib.block.AdvancedBlock.ISelfAwareTile;
import cd4017be.lib.util.Utils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.*;


/** 
 * @author CD4017BE */
public class LakeValve extends LakeConnection implements IFluidHandler, ISelfAwareTile {

	public static int CAP;

	private FluidStack tank;

	@Override
	protected void tickLakeInteract() {
		FluidStack stack = tank == null ? lake.content : tank;
		if (stack != null) {
			int pressure = (int)((relLiquidLvl() - 0.5F) * 0.05F * (float)stack.getFluid().getDensity(tank));
			pressure = MathHelper.clamp(pressure + (CAP >> 1), 0, CAP) - amount();
			if (pressure < 0)
				drainTo(lake, -pressure);
			else if (pressure > 0)
				fillFrom(lake, pressure);
		}
		IFluidHandler acc = Utils.neighborCapability(this, getOrientation().front, FLUID_HANDLER_CAPABILITY);
		if (acc != null) {
			int pressure = amount() - (CAP >> 1);
			if (pressure > 0)
				drainTo(acc, pressure);
			else if (pressure < 0)
				fillFrom(acc, -pressure);
		}
	}

	private void fillFrom(IFluidHandler acc, int am) {
		fill(
			tank == null ? acc.drain(am, true)
			: acc.drain(new FluidStack(tank, am), true),
		true);
	}

	private void drainTo(IFluidHandler acc, int am) {
		tank.amount -= acc.fill(new FluidStack(tank, am), true);
	}

	private int amount() {
		return tank == null ? 0 : tank.amount;
	}

	@Override
	public IFluidTankProperties[] getTankProperties() {
		return new IFluidTankProperties[] {new FluidTankProperties(tank, CAP)};
	}

	@Override
	public int fill(FluidStack resource, boolean doFill) {
		if (resource == null) return 0;
		if (tank == null) {
			int n = Math.min(resource.amount, CAP);
			if (doFill) tank = new FluidStack(resource, n);
			return n;
		}
		if (!tank.isFluidEqual(resource)) return 0;
		int n = Math.min(resource.amount, CAP - tank.amount);
		if (doFill) tank.amount += n;
		return n;
	}

	@Override
	public FluidStack drain(FluidStack resource, boolean doDrain) {
		if (tank == null || !tank.isFluidEqual(resource)) return null;
		return drain(resource.amount, doDrain);
	}

	@Override
	public FluidStack drain(int maxDrain, boolean doDrain) {
		if (tank == null) return null;
		int n = Math.min(maxDrain, tank.amount);
		if (doDrain) tank.amount -= n;
		return new FluidStack(tank, n);
	}

	@Override
	protected void storeState(NBTTagCompound nbt, int mode) {
		super.storeState(nbt, mode);
		if (tank != null) tank.writeToNBT(nbt);
	}

	@Override
	protected void loadState(NBTTagCompound nbt, int mode) {
		super.loadState(nbt, mode);
		tank = FluidStack.loadFluidStackFromNBT(nbt);
	}

	@Override
	public void breakBlock() {
		if (tank != null && lake != null && !lake.invalid())
			tank.amount -= lake.fill(tank, true);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == FLUID_HANDLER_CAPABILITY;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		return capability == FLUID_HANDLER_CAPABILITY ? (T)this : null;
	}

}
