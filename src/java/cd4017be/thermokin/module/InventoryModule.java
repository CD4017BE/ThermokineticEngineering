package cd4017be.thermokin.module;

import java.util.Arrays;
import java.util.List;

import cd4017be.api.IBlockModule;
import cd4017be.lib.capability.AbstractInventory;
import cd4017be.lib.util.ItemFluidUtil;
import cd4017be.lib.util.Orientation;
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
	public final Access[] caps = new Access[6];

	private final ModularMachine tile;

	private final int size;
	/**modules[port] = sideIdx {0-5:BTNSWE, 6-7:none, 0x8: in/out, 0xf0: slot} */
	private final byte[] modules;

	public InventoryModule(ModularMachine tile, int size, byte... modules) {
		this.tile = tile;
		this.size = size;
		this.modules = modules;
		this.items = new ItemStack[size];
		Arrays.fill(items, ItemStack.EMPTY);
	}

	@Override
	public void readNBT(NBTTagCompound nbt, String k) {
		byte[] arr = nbt.getByteArray(k + "Cfg");
		for (int i = Math.min(arr.length, modules.length) - 1; i >= 0; i--) {
			modules[i] &= 0xf8;
			modules[i] |= arr[i] & 0x7;
		}
		onPartsLoad(tile);
		ItemFluidUtil.loadInventory(nbt.getTagList(k + "I", 0), items);
	}

	@Override
	public void writeNBT(NBTTagCompound nbt, String k) {
		nbt.setByteArray(k + "Cfg", modules);
		nbt.setTag(k + "S", ItemFluidUtil.saveInventory(items));
	}

	@Override
	public void initialize(TileEntity te) {
	}

	@Override
	public void invalidate() {
	}

	@Override
	public void onPlaced(ModularMachine m, NBTTagCompound nbt) {
		byte[] arr = nbt.getByteArray("iCfg");
		for (int i = Math.min(arr.length, modules.length) - 1; i >= 0; i--) {
			int cfg = arr[i] & 7;
			modules[i] &= 0xf8;
			modules[i] |= cfg < 6 ? m.orientation.rotate(EnumFacing.VALUES[cfg]).ordinal() : cfg;
		}
		onPartsLoad(m);
	}

	@Override
	public void addDrops(ModularMachine m, NBTTagCompound nbt, List<ItemStack> drops) {
		Orientation o = m.orientation.reverse();
		byte[] arr = new byte[modules.length];
		for (int i = 0; i < arr.length; i++) {
			byte val = modules[i];
			int cfg = val & 7;
			arr[i] = (byte)(val & 0xf8 | (cfg < 6 ? o.rotate(EnumFacing.VALUES[cfg]).ordinal() : cfg));
		}
		nbt.setByteArray("iCfg", arr);
		addToList(drops);
	}

	public void onPartsLoad(ModularMachine m) {
		int n = size;
		for (int i = 0; i < caps.length; i++) {
			Part p = m.components[i + 6];
			Access acc = null;
			if (p instanceof PartIOModule) {
				PartIOModule pi = (PartIOModule)p;
				switch(pi.invType) {
				case EXT_ACC:
					for (byte mod : modules)
						if ((mod & 7) == i) {
							acc = new Access(mod >> 4 & 15, 1);//TODO use special internal access
							break;
						}
					break;
				case INT_ACC:
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
		if (n != l) {
			items = Arrays.copyOf(items, n);
			if (n > l) Arrays.fill(items, l, n, ItemStack.EMPTY);
		}
	}

	@Override
	public void onPartChanged(ModularMachine m, int i) {
		if (i < 6 || i >= 12) return;
		Part p = m.components[i];
		i -= 6;
		Access acc = caps[i];
		Access nacc = null;
		int n = 0, l = acc != null && acc.s >= size ? acc.l : 0, nl = 0;
		for (int j = i - 1; j >= 0; j--)
			if (caps[j] != null) {
				n = caps[j].s + caps[j].l;
				if (n > size) break;
			}
		if (n < size) n = size;
		if (p instanceof PartIOModule) {
			PartIOModule pi = (PartIOModule)p;
			switch(pi.invType) {
			case EXT_ACC:
				for (byte mod : modules)
					if ((mod & 7) == i) {
						nacc = new Access(mod >> 4 & 15, 1);//TODO use special internal access
						break;
					}
				break;
			case INT_ACC: break;
			case BUFFER:
				nl = pi.size;
				nacc = new Access(n, nl);
				break;
			}
		}
		int dl = nl - l;
		if (dl != 0) {
			ItemStack[] nItems = new ItemStack[items.length + dl];
			System.arraycopy(items, 0, nItems, 0, n);
			for (int j = 0; j < l; j++) ItemFluidUtil.dropStack(getStackInSlot(j + n), m.getWorld(), m.getPos());
			Arrays.fill(nItems, n, n + nl, ItemStack.EMPTY);
			System.arraycopy(items, n + l, nItems, n + nl, items.length - n - l);
			for (int j = i + 1; j < caps.length; j++) {
				acc = caps[j];
				if (acc != null && acc.s >= n)
					caps[j] = new Access(acc.s + dl, acc.l);
			}
		}
		caps[i] = nacc;
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

	public IItemHandler getExtInventory(int port) {
		int cfg = modules[port] & 0x7;
		if (cfg >= 6) return null;
		IItemHandler acc = caps[cfg];
		if (acc != null) return acc;
		return Utils.neighborCapability(tile, EnumFacing.VALUES[cfg], CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
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
