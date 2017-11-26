package cd4017be.thermokin.module;

import java.util.Arrays;

import cd4017be.api.IBlockModule;
import cd4017be.lib.capability.AbstractInventory;
import cd4017be.lib.util.ItemFluidUtil;
import cd4017be.thermokin.tileentity.ModularMachine;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.IItemHandler;

/**
 * Modules:
 * - auto Input [speed, multi-target] run by tile
 * - auto Output [speed?, multi-target] run by tile
 * - direct Access [single-target] run by "pipe"
 * - input buffer [size, multi-target] run by tile
 * - output buffer [size, multi-target] run by tile
 * 
 * @author CD4017BE
 *
 */
public class InventoryModule extends AbstractInventory implements IPartListener, IBlockModule {

	/** internal processing slots */
	public final ItemStack[] primary; //TODO unite both in one array
	/** buffer slots added by modules */
	protected ItemStack[] secondary;
	/** capabilities for external access */
	public final IItemHandler[] caps = new IItemHandler[6];
	/**0:no access, 1: input only, 2: output only, 3: both */
	public final byte ioMode;
	private byte modules = 0;//TODO more precise configuration

	public InventoryModule(int ioMode, int size) {
		this.ioMode = (byte)ioMode;
		this.primary = new ItemStack[size];
		Arrays.fill(primary, ItemStack.EMPTY);
	}

	@Override
	public void readNBT(NBTTagCompound nbt, String k) {
		modules = nbt.getByte(k + "Cfg");
		secondary = new ItemStack[nbt.getByte(k + "L") & 0xff];
		ItemFluidUtil.loadInventory(nbt.getTagList(k + "S", 0), secondary);
		ItemFluidUtil.loadInventory(nbt.getTagList(k + "P", 0), primary);
	}

	@Override
	public void writeNBT(NBTTagCompound nbt, String k) {
		nbt.setByte(k + "Cfg", modules);
		nbt.setByte(k + "L", (byte) secondary.length);
		nbt.setTag(k + "S", ItemFluidUtil.saveInventory(secondary));
		nbt.setTag(k + "P", ItemFluidUtil.saveInventory(primary));
	}

	@Override
	public void initialize(TileEntity te) {
		// TODO Auto-generated method stub
	}

	@Override
	public void invalidate() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPartsLoad(ModularMachine m) {
		int n = 0;
		for (int i = 0; i < caps.length; i++)
			if ((modules >> i & 1) != 0) {
				Part p = m.components[i + 6];
				if (p == null) {
					modules ^= 1 << i;
					caps[i] = null;
					continue;
				}
				int l = p.storCap;
				caps[i] = l <= 0 ? this : new Access(n, l);
				n += l;
			} else caps[i] = null;
		int l = secondary.length;
		if (n != l) {//can only happen when NBT data corrupted
			secondary = Arrays.copyOf(secondary, n);
			if (n > l) Arrays.fill(secondary, l, n, ItemStack.EMPTY);
		}
	}

	@Override
	public void onPartChanged(ModularMachine m, int i) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getSlots() {
		return primary.length;
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		primary[slot] = stack;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return primary[slot];
	}

	private class Access extends AbstractInventory {

		final int s, l;

		public Access(int s, int l) {
			this.s = s;
			this.l = l;
		}

		@Override
		public void setStackInSlot(int slot, ItemStack stack) {
			secondary[slot + s] = stack;
		}

		@Override
		public int getSlots() {
			return l;
		}

		@Override
		public ItemStack getStackInSlot(int slot) {
			return secondary[slot + s];
		}
	}

}
