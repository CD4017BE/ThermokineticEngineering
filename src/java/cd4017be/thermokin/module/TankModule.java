package cd4017be.thermokin.module;

import java.util.Arrays;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;

import cd4017be.api.IBlockModule;
import cd4017be.lib.Gui.ITankContainer;
import cd4017be.lib.util.Utils;
import cd4017be.thermokin.module.PartIOModule.IOType;
import cd4017be.thermokin.tileentity.ModularMachine;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

public class TankModule implements ITankContainer, IPartListener, IBlockModule {

	/** internal processing tanks */
	public FluidStack[] fluids;
	/** tank capacities */
	public int[] capacities;
	/** capabilities for external access */
	public final Access[] caps = new Access[6];

	private final ModularMachine tile;
	private  Restriction<FluidStack> limiterIn;
	private  IntPredicate limiterOut;
	private  IntConsumer updater;

	private final int size;

	public TankModule(ModularMachine tile, int... caps) {
		this.tile = tile;
		this.size = caps.length;
		this.capacities = caps;
		this.fluids = new FluidStack[size];
	}

	@Override
	public void readNBT(NBTTagCompound nbt, String k, TileEntity te) {
		onPartsLoad();
		//TODO load fluids
	}

	@Override
	public void writeNBT(NBTTagCompound nbt, String k) {
		// TODO save fluids
	}

	@Override
	public void initialize(TileEntity te) {
	}

	@Override
	public void invalidate() {
	}

	@Override
	public void onPlaced(ModularMachine m, NBTTagCompound nbt) {
		onPartsLoad();
	}

	@Override
	public void addDrops(ModularMachine m, NBTTagCompound nbt, List<ItemStack> drops) {
	}

	private void onPartsLoad() {
		int n = size;
		byte[] refs = new byte[6];
		for (int i = 0; i < caps.length; i++) {
			Part p = tile.components[i + 6];
			Access acc = null;
			if (p instanceof PartIOModule) {
				PartIOModule pi = (PartIOModule)p;
				if (pi.hasFluid) {
					switch(pi.invType) {
					case EXT_ACC:
						int cfg = tile.getCfg(i);
						if (cfg < 0) break;
						if (cfg < 6)
							refs[i] = (byte)(cfg + 1);
						else if ((cfg -= 6) < size)
							acc = limiterIn == null ? new Access(cfg) : new RestrictedAccess(cfg);
						break;
					case BUFFER:
						int l = pi.size;
						acc = new Access(n);
						n++;
						break;
					default:
					}
				}
			}
			caps[i] = acc;
		}
		for (int i = 0; i < refs.length; i++) {
			byte b = refs[i];
			if (b != 0) caps[i] = caps[b - 1];
		}
		int l = fluids.length;
		if (n != l) {
			fluids = Arrays.copyOf(fluids, n);
			capacities = Arrays.copyOf(capacities, n);
		}
	}

	@Override
	public void onPartChanged(ModularMachine m, int i, Part old) {
		if (i < 6 || i >= 12) return;
		i -= 6;
		Access acc = caps[i];
		int s, l, nl = 0;
		byte refs = 0;
		if (acc != null && acc.s >= size && old instanceof PartIOModule && ((PartIOModule)old).invType == IOType.BUFFER) {
			l = 1;
			s = acc.s;
			for (int j = 0; j < caps.length; j++)
				if (caps[j] == acc) refs |= 1 << j;
		} else {
			l = 0;
			s = size;
			for (int j = 0; j < i; j++)
				if (caps[j] != null && caps[j].s == s)
					s++;
		}
		acc = null;
		Part p = m.components[i];
		if (p instanceof PartIOModule) {
			PartIOModule pi = (PartIOModule)p;
			if (pi.hasFluid) {
				switch(pi.invType) {
				case EXT_ACC:
					int cfg = m.getCfg(i);
					if (cfg < 0) break;
					if (cfg < 6)
						acc = caps[cfg + 1];
					else if ((cfg -= 6) < size)
						acc = limiterIn == null ? new Access(cfg) : new RestrictedAccess(cfg);
					break;
				case BUFFER:
					nl = 1; int c = pi.size;
					acc = new Access(s);
					break;
				default:
				}
			}
		}
		if (refs == 0) caps[i] = acc;
		else for (int j = 0; j < 6; j++)
			if ((refs >> i & 1) != 0)
				caps[j] = acc;
		int dl = nl - l;
		if (dl != 0) {
			FluidStack[] nFluids = new FluidStack[fluids.length + dl];
			System.arraycopy(fluids, 0, nFluids, 0, s);
			System.arraycopy(fluids, s + l, nFluids, s + nl, fluids.length - s - l);
			int[] nCaps = new int[capacities.length + dl];
			System.arraycopy(capacities, 0, nCaps, 0, s);
			System.arraycopy(capacities, s + l, nCaps, s + nl, capacities.length - s - l);
			for (int j = i + 1; j < caps.length; j++) {
				Access a = caps[j];
				if (a != null && a.s >= s && (p = m.components[j]) instanceof PartIOModule && ((PartIOModule)p).invType == IOType.BUFFER)
					a.s += dl;
			}
		}
	}

	@Override
	public void onCfgChange(ModularMachine m, int i, int cfg) {
		if (i < 6) {
			Part p = m.components[i + 6];
			if (!(p instanceof PartIOModule)) return;
			PartIOModule pi = (PartIOModule)p;
			if (!pi.hasFluid || pi.invType != IOType.EXT_ACC) return;
			if (cfg < 0)
				caps[i] = null;
			else if (cfg < 6)
				caps[i] = caps[cfg];
			else if ((cfg -= 6) < size)
				caps[i] = limiterIn == null ? new Access(cfg) : new RestrictedAccess(cfg);
		}
	}

	@Override
	public int getTanks() {
		return fluids.length;
	}

	@Override
	public FluidStack getTank(int i) {
		return fluids[i];
	}

	@Override
	public int getCapacity(int i) {
		return capacities[i];
	}

	@Override
	public void setTank(int i, FluidStack fluid) {
		fluids[i] = fluid;
	}

	/**
	 * @param port (>= 6) global resourceIO port id
	 * @return ItemHandler for port (either external inventory, internal buffer or null)
	 */
	public IFluidHandler getExtTank(int port) {
		int cfg = tile.getCfg(port);
		if (cfg < 0 || cfg >= 6) return null;
		IFluidHandler acc = caps[cfg];
		if (acc != null) return acc;
		return Utils.neighborCapability(tile, EnumFacing.VALUES[cfg], CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
	}

	@Override
	public boolean hasCapability(Capability<?> cap, EnumFacing side) {
		return side != null && caps[side.ordinal()] != null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> cap, EnumFacing side) {
		return side == null ? null : (T)caps[side.ordinal()];
	}

	@Override
	public boolean supportsCapability(Capability<?> cap) {
		return cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
	}

	private class Access implements IFluidHandler {

		int s;

		Access(int s) {
			this.s = s;
		}

		@Override
		public IFluidTankProperties[] getTankProperties() {
			return new IFluidTankProperties[] {new FluidTankProperties(fluids[s], capacities[s])};
		}

		@Override
		public int fill(FluidStack res, boolean doFill) {
			FluidStack fluid = fluids[s];
			int cap = capacities[s];
			if (fluid == null) {
				int m = Math.min(res.amount, cap);
				if (doFill) fluids[s] = new FluidStack(res, m);
				return m;
			} else if (fluid.isFluidEqual(res)) {
				int m = Math.min(res.amount, cap - fluid.amount);
				if (m > 0 && doFill) {
					fluid.amount += m;
					//set
				}
				return m;
			} else return 0;
		}

		@Override
		public FluidStack drain(FluidStack res, boolean doDrain) {
			FluidStack fluid = fluids[s];
			if (fluid == null || fluid.amount <= 0 || !fluid.isFluidEqual(res)) return null;
			int m = Math.min(res.amount, fluid.amount);
			if (doDrain && (fluid.amount -= m) <= 0) fluids[s] = null;//set
			return new FluidStack(fluid, m);
		}

		@Override
		public FluidStack drain(int m, boolean doDrain) {
			FluidStack fluid = fluids[s];
			if (fluid == null || fluid.amount <= 0) return null;
			if (fluid.amount < m) m = fluid.amount;
			if (doDrain && (fluid.amount -= m) <= 0) fluids[s] = null;//set
			return new FluidStack(fluid, m);
		}
		
	}

	private class RestrictedAccess extends Access {

		RestrictedAccess(int s) {
			super(s);
		}

		@Override
		public int fill(FluidStack res, boolean doFill) {
			int n = limiterIn.insertAmount(s, res);
			if (n <= 0) return 0;
			if (n < res.amount) res = new FluidStack(res, n);
			n = super.fill(res, doFill);
			if (doFill && n > 0 && updater != null)
				updater.accept(s);
			return n;
		}

		@Override
		public FluidStack drain(FluidStack res, boolean doDrain) {
			if (limiterOut.test(s)) {
				res = super.drain(res, doDrain);
				if (doDrain && res != null && updater != null)
					updater.accept(s);
				return res;
			}
			else return null;
		}

		@Override
		public FluidStack drain(int m, boolean doDrain) {
			if (limiterOut.test(s)) {
				FluidStack res = super.drain(m, doDrain);
				if (doDrain && res != null && updater != null) updater.accept(s);
				return res;
			}
			return null;
		}

	}

	@FunctionalInterface
	public static interface Restriction<O> {
		int insertAmount(int slot, O obj);
	}

}
