package cd4017be.thermokin.module;

import java.util.Arrays;

import cd4017be.api.IBlockModule;
import cd4017be.lib.capability.AbstractInventory;
import cd4017be.lib.util.ItemFluidUtil;
import cd4017be.lib.util.Utils;
import cd4017be.thermokin.tileentity.ModularMachine;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

/**
 * Modules:
 * - auto Transfer [speed, multi-target] run by tile
 * - direct Access [single-target] run by "pipe"
 * - buffer [size, multi-target] run by tile
 * 
 * @author CD4017BE
 *
 */
public class InventoryModule extends AbstractInventory implements IPartListener, IBlockModule {

	/** internal processing slots */
	public ItemStack[] items;
	/** capabilities for external access */
	public final IItemHandler[] caps = new IItemHandler[6];

	private final int size;
	private final byte[] modules;

	public InventoryModule(int ioMode, int size) {
		this.size = size;
		this.modules = new byte[size * 2];
		this.items = new ItemStack[size];
		Arrays.fill(items, ItemStack.EMPTY);
	}

	@Override
	public void readNBT(NBTTagCompound nbt, String k) {
		byte[] arr = nbt.getByteArray(k + "Cfg");
		System.arraycopy(arr, 0, modules, 0, Math.min(arr.length, modules.length));
		items = new ItemStack[size + (nbt.getByte(k + "L") & 0xff)];
		ItemFluidUtil.loadInventory(nbt.getTagList(k + "I", 0), items);
	}

	@Override
	public void writeNBT(NBTTagCompound nbt, String k) {
		nbt.setByteArray(k + "Cfg", modules);
		nbt.setByte(k + "L", (byte) (items.length - size));
		nbt.setTag(k + "S", ItemFluidUtil.saveInventory(items));
	}

	@Override
	public void initialize(TileEntity te) {
	}

	@Override
	public void invalidate() {
	}

	@Override
	public void onPartsLoad(ModularMachine m) {
		int n = size;
		for (int i = 0; i < caps.length; i++) {
			Part p = m.components[i + 6];
			IItemHandler acc = null;
			if (p instanceof PartItem) {
				PartItem pi = (PartItem)p;
				switch(pi.invType) {
				case ACCESS:
					acc = new Access(i, 1);//FIXME use correct index
					break;
				case AUTOMATIC:
					acc = null;
					break;
				case BUFFER:
					int l = pi.size;
					acc = new Access(n, l);
					n += l;
					break;
				}
			}
			caps[i] = acc;
		}
		int l = items.length;
		if (n != l) {//can only happen when NBT data corrupted
			items = Arrays.copyOf(items, n);
			if (n > l) Arrays.fill(items, l, n, ItemStack.EMPTY);
		}
	}

	@Override
	public void onPartChanged(ModularMachine m, int i) {
		
	}

	@Override
	public int getSlots() {
		return size;
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		items[slot] = stack;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return items[slot];
	}

	public IItemHandler getExtInventory(TileEntity te, int port) {
		byte cfg = modules[port];
		if (cfg < 0 || cfg >= 6) return null;
		IItemHandler acc = caps[cfg];
		if (acc != null) return acc;
		return Utils.neighborCapability(te, EnumFacing.VALUES[cfg], CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
	}

	private class Access extends AbstractInventory {

		final int s, l;

		public Access(int s, int l) {
			this.s = s;
			this.l = l;
		}

		@Override
		public void setStackInSlot(int slot, ItemStack stack) {
			items[slot + s] = stack;
		}

		@Override
		public int getSlots() {
			return l;
		}

		@Override
		public ItemStack getStackInSlot(int slot) {
			return items[slot + s];
		}

	}

}
